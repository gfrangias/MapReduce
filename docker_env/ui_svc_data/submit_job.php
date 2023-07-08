<?php
include 'zk.php';
include 'containerScripts/deploy_monitor.php';

if (session_status() == PHP_SESSION_NONE) {
  session_start();
}

date_default_timezone_set('Europe/Athens');

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
            'reducers_num' => $postFields['reducersNum'],
            'stage' => "init"
        ]);                                                                                                                                                                                                                                                                                                                                                                                                 
    
        // Create the job znode in ZooKeeper
        $zk->create($jobPath . $jobId, $jobInfo,  $acl);

        // Create the tasks znode of the job in ZK
        $zk->create($jobPath . $jobId. '/tasks', null, $acl);

        // Create the statistics znode of the job in ZK
        $zk->create($jobPath . $jobId. '/statistics', null, $acl);
        $zk->create($jobPath . $jobId. '/statistics/chunking',null, $acl);
        $zk->create($jobPath . $jobId. '/statistics/shuffling',null, $acl);
        $zk->create($jobPath . $jobId. '/statistics/merging',null, $acl);
        $zk->create($jobPath . $jobId. '/statistics/mapping',null, $acl);
        $zk->create($jobPath . $jobId. '/statistics/reducing',null, $acl);

        return $jobId;


    }else{
        // Create the users jobs node
        try {
            $zk->create('/jobs', null,  $acl);
        } catch (Exception $e) {}        
        $zk->create('/jobs/'.$username, null,  $acl);

        createJob($username, $zk, $postFields);
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
      curl_setopt($curl, CURLOPT_URL, $arrayMonitorInfo['ipAddress'] . ":7000/api/job/assign/".$_SESSION['username'].'/' . $newJobId);
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
    sleep(10);  
    $newMonitorInfo = $zk->get("/monitors/".$newContainerName);    
    $newMonitorInfo = json_decode($newMonitorInfo,1);
      
    // Create a new cURL resource
    $curl = curl_init();

    // Set the cURL options
    $url = $newMonitorInfo['ipAddress'] . ":7000/api/job/assign/".$_SESSION['username'].'/'. $newJobId;
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_POST, true);
    // Add any additional options if needed, such as headers or data

    // Execute the cURL request
    $response = curl_exec($curl);
    echo curl_getinfo($curl, CURLINFO_HTTP_CODE);

    if(curl_getinfo($curl, CURLINFO_HTTP_CODE)==503){
      // The monitor was taken by another service during this request
      // I need to try to assign the job elsewhere
      // Close the cURL resource
      curl_close($curl);
      echo 'Failure';
      return;
    }

    // If we reach this point, the job is assigned to monitor
    $successfulAssignment = true;
    if($successfulAssignment){
      echo 'Success';
    }
    // Close the cURL resource
    curl_close($curl);
  }
}
?>

<!DOCTYPE html>
<html>
<head>
  <title>MapReduce UI - Job Submission</title>
  <!-- Include Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <link rel="icon" type="image/x-icon" href="assets/brand/ico.png">
  <style>
    .loader {
      border: 8px solid #f3f3f3; /* Light grey */
      border-top: 8px solid #033894; /* Blue */
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 3s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
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
    .btn-primary{
      background-color: #033894;
      border: 1px solid #033894
    }
  </style>
</head>
<body>

<?php

?>

<div class="container">
<h1 class="mb-4 table-title"><strong>Job Submission</strong></h1>
  <form method="post" action="" enctype="multipart/form-data" id="job-form">
    <div class="mb-4 mt-4 font-weight-normal alert alert-dark">
      <strong>General Information about Job submissions</strong> 
      <ul>
        <li>Please provide below the information of the job you want to submit. Please follow this<a target="_blank" rel="noopener noreferrer" href='cdn/job_manual.pdf'> instructions.</a></li>
        <li>Keep in mind that the job submitter will try its best to handle your job request. If your job status won't change (init: 'queued') for a long period of time please consider archiving the current job and creating a new one. You can archive a job by accessing the settings of a job in the Job Manager.</li>
        <li>If a job is marked as failed then there was an error inside you executable that caused an exception during the distributed execution of the job.</li>
        <li>You can manage any submitted job from the Job Manager via its settings.</li>
        <li>The main computational resources needed for the execution are calculated automatically using a dedicated algorithm. You have the option to set the number of the reducers to be used.
      </ul>
    </div>
    <div class="form-group row">
      <label for="jobTitle" class="col-sm-4 col-form-label">Job Title:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="jobTitle" name="jobTitle" required>
      </div>
    </div>
    <div class="form-group row">
      <label for="execFile" class="col-sm-4 col-form-label">Executable python file (.py)</label>
      <div class="col-sm-8">
        <select class="form-control" id="execFile" name="execFile" required>
          <option value="" disabled selected>Select an executable file from your uploads</option>
          <?php
            // Get the list of files in the executable directory
            $executableFiles = scandir($executableDir);
            foreach ($executableFiles as $file) {
              $filename = basename($file);
              if($filename == '.' || $filename == '..' || trim(pathinfo($filename, PATHINFO_EXTENSION)) != 'jar'){
                continue;
              }
              echo '<option value="'.$file.'">'.$filename.'</option>';
            }
          ?>
        </select>
      </div>
    </div>
    <div class="form-group row">
      <label for="dataset" class="col-sm-4 col-form-label">Dataset file (.txt, .json):</label>
      <div class="col-sm-8">
        <select class="form-control" id="dataset" name="dataset" required>
          <option disabled selected>Select a dataset file from your uploads</option>
          <?php
            // Get the list of files in the dataset directory
            $datasetFiles = scandir($datasetDir);
            foreach ($datasetFiles as $file) {
              $filename = basename($file);
              if($filename == '.' || $filename == '..'){
                continue;
              }
              setlocale(LC_ALL,'en_US.UTF-8');
              if(trim(pathinfo($filename, PATHINFO_EXTENSION)) <> 'txt' && trim(pathinfo($filename, PATHINFO_EXTENSION)) <> 'json'){
                continue;
              }
              echo '<option value="'.$filename.'">'.$filename.'</option>';
            }
          ?>
        </select>
      </div>
    </div>
    <div class="form-group row">
      <label for="mapMethod" class="col-sm-4 col-form-label">Map Class:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="mapMethod" name="mapMethod" required>
      </div>
    </div>
    <div class="form-group row">
      <label for="reduceMethod" class="col-sm-4 col-form-label">Reduce Class:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="reduceMethod" name="reduceMethod" required>
      </div>
    </div>
    <div class="form-group row">
        <label for="reducersNum" class="col-sm-4 col-form-label">Number of Reducers: </label>
        <div class="col-sm-1">
          <select class="form-control" name="reducersNum" id="reducersNum" required>
            <!-- Use a loop to generate the options -->
            <?php for ($i = 1; $i <= 16; $i++) { ?>
              <option value="<?php echo $i; ?>"><?php echo $i; ?></option>
            <?php } ?>
          </select>
            </div>
      </div>
    <div class="col-sm-6">
        <button type="submit" class="btn btn-primary">Submit</button>
        <a href="jobs.php" class="btn btn-secondary">Back to Jobs</a>
        <div class="col-sm-20">
        
        </div>
      </div>
      <div class="container" style="display:none" id="alert-job">
            <div class="row alert alert-secondary">
              <div class="col-xs-6">
                <div class="loader col-sm-14"></div>
              </div>
              <div  class="pl-3 col-xs-6 d-flex align-items-center">
                  <di>Allow some time for the job to set up. Sit tight please</div>
              </div>
            </div>
    </div>
</div>
            </form>

<script>
    function startTimer() {
      var loadingDiv = document.getElementById('alert-job');

      loadingDiv.style.display = 'block'; // Show the loading div

      setTimeout(function() {
          loadingDiv.style.display = 'none'; // Hide the loading div after 6 seconds
      }, 10000);
    }

    function isFormValid() {
        var nameInput = document.getElementById("jobTitle");
        var execInput = document.getElementById("execFile");
        var dataInput = document.getElementById("dataset");
        var mapInput = document.getElementById("mapMethod");
        var reducInput = document.getElementById("reducersNum");
        var reduceMethodInput = document.getElementById("reduceMethod");

        var name = nameInput.value.trim();
        var exec = execInput.value.trim();
        var data = dataInput.value.trim();
        var map = mapInput.value.trim();
        var reducNum = reducInput.value.trim();
        var reduceMethod = reduceMethodInput.value.trim();

        // Perform validation based on your requirements
        if (name === "" || exec === "" || data ==="" || map ==="" || reducNum=== "" || reduceMethod ==="") {
            return false; // Form is not valid
        }
        return true; // Form is valid
    }

    // Function to handle form submission
    function handleFormSubmit() {
        if (isFormValid()) {
            startTimer();
        }
    }

    // Get the form element
    var form = document.getElementById("job-form");

    // Add an event listener for the form's submit event
    form.addEventListener("submit", handleFormSubmit);
</script>
</div>

<!-- Include Bootstrap JS -->
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>

</body>
</html>