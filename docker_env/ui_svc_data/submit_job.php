<?php
include 'zk.php';
include 'containerScripts/deploy_monitor.php';

if (session_status() == PHP_SESSION_NONE) {
  session_start();
}

 // Get the username (assuming it is available in the session or from some other source)
 $username = $_SESSION['username'];

 // Specify the server directories for executables and datasets
 $executableDir = './uploads/'.$username.'/executables';
 $datasetDir = './uploads/'.$username.'/datasets';


 //It creates the znode for the job in ZK
function createJob($username, $zk, $postFields) {

    $acl = array(array("perms" => Zookeeper::PERM_ALL, "scheme" => "world", "id" => "anyone"));

    if($zk->exists('/jobs/'. $username)){
        $jobPath = '/jobs/' . $username . '/';

        // Generate a unique job ID
        $jobId = 'j' . uniqid();
    
        // Create the job info as a JSON string
        $jobInfo = json_encode([
            'jobid' => $jobId,
            'user' => $username,
            'title' => $postFields['jobTitle'],
            'creation_date' => date('Y-m-d H:i:s'),
            'dataset_file' => $postFields['dataset'],
            'executable_file' => $postFields['execFile'],
            'status' => 'queued',
            'map_method' => $postFields['mapMethod'],
            'reduce_method' => $postFields['reduceMethod'],
            'shuffle_method' => $postFields['shuffleMethod'],
            'merge_method' => $postFields['mergeMethod']
        ]);                                                                                                                                                                                                                                                                                                                                                                                                 
    
        // Create the job znode in ZooKeeper
        $zk->create($jobPath . $jobId, $jobInfo,  $acl);
        return $jobId;
    }else{
        // Create the users jobs node
        try {
            $zk->create('/jobs', null,  $acl);
        } catch (Exception $e) {}        
        $zk->create('/jobs/'.$username, null,  $acl);

        createJob($username, $zk);
    }   
  
}


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  
  $currMonitors = $zk->getChildren('/monitors');
  $numOfMonitorsCurrent = count($currMonitors);
  $newJobId = createJob($username, $zk, $_POST);
  $successfulAssignment = false;

  foreach($currMonitors as $item){
    //Dont want to include the non-functional /monitors/leader node
    if($item == 'leader'){
      continue;
    }

    $monitorInfo = $zk->get('/monitors/'.$item);
    $arrayMonitorInfo = json_decode($monitorInfo,1);

    //If the monitor of the iteration is occupied no need to bother
    if($arrayMonitorInfo['occupied']){
      continue;
    }else{
      //Assign job to the monitor 
      //Tell him to handle the job via his API
      
      // Create a new cURL resource
      $curl = curl_init();

      // Set the cURL options
      curl_setopt($curl, CURLOPT_URL, $arrayMonitorInfo['ipAddress'] . ":7000/api/job/assign/" . $newJobId);
      curl_setopt($curl, CURLOPT_POST, true);
      // Add any additional options if needed, such as headers or data

      // Execute the cURL request
      $response = curl_exec($curl);

      if(curl_getinfo($curl, CURLINFO_HTTP_CODE)==503){
        // The monitor was taken by another service during this request
        // I need to try to assign the job elsewhere
        // Close the cURL resource
        curl_close($curl);
        continue;
      }

      // If we reach this point, the job is assigned to monitor
      $successfulAssignment = true;
      break;

      // Close the cURL resource
      curl_close($curl);
    }
  }

  //If the iteration over the monitors available in /monitors didnt succeed at assigning the job, then 
  //we need to deploy new monitor
  if(!$successfulAssignment){
    $newContainerName = deployMonitor();
    try{
        $zk->get("/monitors/".$newContainerName);
        $newMonitorInfo();
    }catch(Exception $e){
      
    }
  }

}
?>

<!DOCTYPE html>
<html>
<head>
  <title>MapReduce UI - Job Submission</title>
  <!-- Include Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
  <link rel="icon" type="image/x-icon" href="assets/brand/ico.png">
  <style>
    .container {
      background-color: #f8f9fa;
      border-radius: 10px;
      padding: 20px;
      margin-top: 50px;
    }
    .btn-container {
      display: flex;
      justify-content: flex-end;
    }
  </style>
</head>
<body>

<?php

?>

<div class="container">
<h1 class="mb-4 table-title"><strong>Job Submission</strong></h1>
  <form method="post" action="" enctype="multipart/form-data">
    <div class="form-group row">
      <label for="jobTitle" class="col-sm-4 col-form-label">Job Title:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="jobTitle" name="jobTitle" required>
      </div>
    </div>
    <div class="form-group row">
      <label for="execFile" class="col-sm-4 col-form-label">Executable file (.jar):</label>
      <div class="col-sm-8">
        <select class="form-control" id="execFile" name="execFile" required>
          <option value="" disabled selected>Select an executable file</option>
          <?php
            // Get the list of files in the executable directory
            $executableFiles = scandir($executableDir);
            foreach ($executableFiles as $file) {
              $filename = basename($file);
              if($filename == '.' || $filename == '..'){
                continue;
              }
              echo '<option value="'.$file.'">'.$filename.'</option>';
            }
          ?>
        </select>
      </div>
    </div>
    <div class="form-group row">
      <label for="dataset" class="col-sm-4 col-form-label">Dataset file:</label>
      <div class="col-sm-8">
        <select class="form-control" id="dataset" name="dataset" required>
          <option disabled selected>Select a dataset file</option>
          <?php
            // Get the list of files in the dataset directory
            $datasetFiles = scandir($datasetDir);
            foreach ($datasetFiles as $file) {
              $filename = basename($file);
              if($filename == '.' || $filename == '..'){
                continue;
              }
              echo '<option value="'.$filename.'">'.$filename.'</option>';
            }
          ?>
        </select>
      </div>
    </div>
    <div class="form-group row">
      <label for="distributorMethod" class="col-sm-4 col-form-label">Distributor Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="distributorMethod" name="distributorMethod" required>
      </div>
    </div>
    <div class="form-group row">
      <label for="mapMethod" class="col-sm-4 col-form-label">Map Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="mapMethod" name="mapMethod" required>
      </div>
    </div>
    <div class="form-group row">
      <label for="reduceMethod" class="col-sm-4 col-form-label">Reduce Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="reduceMethod" name="reduceMethod" required>
      </div>
    </div>
    <div class="form-group row">
        <label for="reducersNum" class="col-sm-4 col-form-label">Number of Reducers: </label>
        <div class="col-sm-1">
          <select class="form-control" name="reducersNum" required>
            <!-- Use a loop to generate the options -->
            <?php for ($i = 1; $i <= 20; $i++) { ?>
              <option value="<?php echo $i; ?>"><?php echo $i; ?></option>
            <?php } ?>
          </select>
            </div>
      </div>
    <div class="form-group row">
      <label for="mergeMethod" class="col-sm-4 col-form-label">Merge Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="mergeMethod" name="mergeMethod" required>
      </div>
    </div>
    <div class="col-sm-6">
        <button type="submit" class="btn btn-primary">Submit</button>
        <a href="jobs.php" class="btn btn-secondary">Back to Jobs</a>
      </div>
  </form>
</div>

<!-- Include Bootstrap JS -->
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>

</body>
</html>