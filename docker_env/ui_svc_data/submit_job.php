<?php
if (session_status() == PHP_SESSION_NONE) {
  session_start();
}

 // Get the username (assuming it is available in the session or from some other source)
 $username = $_SESSION['username'];

 // Specify the server directories for executables and datasets
 $executableDir = './uploads/'.$username.'/executables';
 $datasetDir = './uploads/'.$username.'/datasets';


function createJob($username, $zk) {

    $acl = array(array("perms" => Zookeeper::PERM_ALL, "scheme" => "world", "id" => "anyone"));

    if($zk->exists('/jobs/'. $username)){
        $jobPath = '/jobs/' . $username . '/';

        // Generate a unique job ID
        $jobId = 'j' . uniqid();
    
        // Create the job info as a JSON string
        $jobInfo = json_encode([
            'jobid' => $jobId,
            'user' => $username,
            'title' => 'Word Count Example',
            'creation_date' => date('Y-m-d H:i:s'),
            'dataset_file' => 'dataset.txt',
            'executable_file' => 'executable.sh',
            'status' => 'queued',
            'result_path' => '/jobs/' . $username . '/result/' . $jobId . '.txt'
        ]);                                                                                                                                                                                                                                                                                                                                                                                                 
    
        // Create the job znode in ZooKeeper
        $zk->create($jobPath . $jobId, $jobInfo,  $acl);
    }else{
        // Create the users jobs node
        try {
            $zk->create('/jobs', null,  $acl);
        } catch (Exception $e) {}        
        $zk->create('/jobs/'.$username, null,  $acl);

        createJob($username, $zk);
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
  if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Process the submitted data here (e.g., save to a database, perform operations, etc.)

    // Display a success message
    echo '<div class="container">';
    echo '<h2>Job Submission Successful</h2>';
    echo '<p>Job Title: ' . $_POST['jobTitle'] . '</p>';
    echo '<p>Distributor Method: ' . $_POST['distributorMethod'] . '</p>';
    echo '<p>Map Method: ' . $_POST['mapMethod'] . '</p>';
    echo '<p>Reduce Method: ' . $_POST['reduceMethod'] . '</p>';
    echo '<p>Shuffle Method: ' . $_POST['shuffleMethod'] . '</p>';
    echo '<p>Merge Method: ' . $_POST['mergeMethod'] . '</p>';
    echo '<p>Executable file: ' . $_POST['execFile'] . '</p>';
    echo '<p>Dataset file: ' . $_POST['dataset'] . '</p>';
    echo '</div>';
  }
?>

<div class="container">
<h1 class="mb-4 table-title"><strong>Job Submission</strong></h1>
  <form method="post" action="" enctype="multipart/form-data">
    <div class="form-group row">
      <label for="jobTitle" class="col-sm-4 col-form-label">Job Title:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="jobTitle" name="jobTitle">
      </div>
    </div>
    <div class="form-group row">
      <label for="execFile" class="col-sm-4 col-form-label">Executable file (.jar):</label>
      <div class="col-sm-8">
        <select class="form-control" id="execFile" name="execFile">
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
        <select class="form-control" id="dataset" name="dataset">
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
        <input type="text" class="form-control" id="distributorMethod" name="distributorMethod">
      </div>
    </div>
    <div class="form-group row">
      <label for="mapMethod" class="col-sm-4 col-form-label">Map Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="mapMethod" name="mapMethod">
      </div>
    </div>
    <div class="form-group row">
      <label for="reduceMethod" class="col-sm-4 col-form-label">Reduce Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="reduceMethod" name="reduceMethod">
      </div>
    </div>
    <div class="form-group row">
      <label for="mergeMethod" class="col-sm-4 col-form-label">Merge Method:</label>
      <div class="col-sm-8">
        <input type="text" class="form-control" id="mergeMethod" name="mergeMethod">
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