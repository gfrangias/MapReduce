<?php
include 'functions.php';

if (session_status() == PHP_SESSION_NONE) {
    session_start();
}

// Get the username from the session
$username = $_SESSION['username'];

// Array of ZooKeeper addresses
$zookeeperAddresses = [
    '172.16.0.11:2181',
    '172.16.0.12:2181',
    '172.16.0.13:2181'
];

// Select a random ZooKeeper address
$randomAddress = $zookeeperAddresses[array_rand($zookeeperAddresses)];

// Connect to ZooKeeper
$zk = new Zookeeper($randomAddress);


    // Function to retrieve jobs for the user from ZooKeeper
    function getJobInfo($username, $jobId, $zk)
    {
        $jobPath = '/jobs/' . $username . '/' . $jobId;
    
        if ($zk->exists($jobPath)) {
            // Get the job info from the znode
            $jobInfo = $zk->get($jobPath);
    
            // Decode the JSON string into an associative array
            $jobInfoArray = json_decode($jobInfo, true);
            // Return the job info array
            return $jobInfoArray;
        } else {
            // Job doesn't exist, return null or handle the error accordingly
            return null;
        }
    }

?>



<!DOCTYPE html>
<html>
<head>
    <title>MapReduce UI - Job Details</title>
    <link rel="icon" type="image/x-icon" href="assets/brand/ico.png">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
</head>

<style>
     .green-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: green;
            border-radius: 50%;
            margin-right: 5px;
        }
        .yellow-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #ffc107;
            border-radius: 50%;
            margin-right: 5px;
        }
        .blue-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #0D6EFD;
            border-radius: 50%;
            margin-right: 5px;
        }
        .red-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #DC3545;
            border-radius: 50%;
            margin-right: 5px;
        }

        .green-bullet, .yellow-bullet, .blue-bullet, .red-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            margin-right: 5px;
        }

        .green-bullet { background-color: green; }
        .yellow-bullet { background-color: #ffc107; }
        .blue-bullet { background-color: #0D6EFD; }
        .red-bullet { background-color: #DC3545; }

        .rounded-container {
            border-bottom-left-radius: 1.25rem;
            border-bottom-right-radius: 1.25rem;
            overflow: hidden; /* This is important if the children don't also have rounded corners */
        }
        .rounded-top {
            background-color: #033894;
        }
        
</style>
<body>

    <!-- Navigation Bar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand">Job Details<?php if(isset($_GET['id'])){echo ' for <strong> '. $_GET['id'].'</strong>';}?> </a>
    </nav>

    <!-- Container -->
    <div class="container mt-4">

        <!-- Row -->
        <div class="row">

            <!-- Left Container: Job Info -->
            <div class="col-lg-6 rounded-container">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Job Info</h5>
                </div>
                <div class="rounded-container p-4 bg-light">
                    <?php
                    // Fetch job details from jobs.php using GET
                    $jobId = $_GET['id'];
                    $jobInfo = getJobInfo($username, $jobId, $zk); // Function to retrieve job info

                    // Display job details
                     echo "<p>Job Title: <strong> " . $jobInfo['title'] . "</strong></p>";
                     if (strpos($jobInfo['status'], 'running') !== false) {
                        echo "<p>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'queued') !== false){
                        echo "<p>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='yellow-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'exited') !== false){
                        echo "<p>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='green-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'error') !== false){
                        echo "<p>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='red-bullet'></span></p>";
                    }else {
                        echo "<p>Status: " . $jobInfo['status'] . "</p>";
                    }
                     echo "<p>Creation Date: <strong> " . $jobInfo['creation_date'] . "</strong></p>";
                     echo "<p>Executable: <strong> " . $jobInfo['executable_file'] . "</strong></p>";
                     echo "<p>Dataset: <strong> " . $jobInfo['dataset_file'] . "</strong></p>";                    
                    ?>
                </div>
            </div>

            <!-- Right Container: Execution Info -->
            <div class="col-lg-6 rounded-container">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Execution Info</h5>
                </div>
                <div class="rounded-container p-4 bg-light">
                    <?php
                    // Fetch execution details from jobs.php using GET
                    // $executionInfo = retrieveExecutionInfo($jobId); // Function to retrieve execution info

                    // Display execution details
                    // echo "<p>Monitor ID: " . $executionInfo['monitor_id'] . "</p>";
                    // echo "<p>Number of Workers: " . $executionInfo['num_workers'] . "</p>";
                    // Add more execution details as needed
                    ?>
                </div>
            </div>

        </div> <!-- End of row -->

        <!-- Bottom Container: Result -->
        <div class="row mt-4 rounded-container">
            <div class="col-lg-12">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Result</h5>
                </div>
                <div class="rounded-container p-4 bg-light">
                    <textarea class="form-control" rows="5" readonly><?php
                        // Fetch result from jobs.php using GET
                        // $result = retrieveResult($jobId); // Function to retrieve result

                        // Display result
                        // echo json_encode($result);
                    ?></textarea>
                </div>
            </div>
        </div>

      <div class="col-sm-12">
        <a href="jobs.php" class="btn btn-secondary">Back to Jobs</a>
      </div>

    </div> <!-- End of container -->

</body>
</html>
