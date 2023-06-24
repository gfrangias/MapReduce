<?php

include 'functions.php';
include 'zk.php';

if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
verifyToken();
if(!isset($_SESSION['username'])){
    header('Location: error.php?s=2');
}

// Get the username from the session
$username = $_SESSION['username'];

// Function to retrieve jobs for the user from ZooKeeper
function getJobs($username, $zk)
{
    $jobPath = '/jobs/' . $username ;
    $jobs = [];

    // Check if the job znode path exists for the user
    if ($zk->exists($jobPath)) {
        // Get the list of job znodes for the user
        $jobList = $zk->getChildren($jobPath);

        foreach ($jobList as $jobId) {
            $jobData = $zk->get($jobPath . '/' . $jobId);

            // Decode the JSON data
            $jobInfo = json_decode($jobData, true);

            // Add the job info to the jobs array
            $jobs[] = $jobInfo;
        }
    }

    return $jobs;
}

// Function to create a new job for the user in ZooKeeper
function createJob($username, $zk)
{

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

// Check if the form is submitted to create a new job
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['create_job'])) {
    createJob($username, $zk);
}

// Get the user's jobs from ZooKeeper
$jobs = getJobs($username, $zk);
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" crossorigin="anonymous" />
    <link rel="icon" type="image/x-icon" href="assets/brand/ico.png">
    <title>MapReduce UI - Job Manager</title>
</head>

<style>
      .logo {
        max-height: 50px;
        max-width: 100%;
    }
    .header {
        padding: 1rem;
        padding-left: 2rem; /* Add some padding to move the logo away from the left corner */
        padding-right: 2rem; /* Add some padding to move the user info away from the right corner */
    }
    .rounded-box {
      background-color: #f8f9fa;
      border-radius: 15px;
      padding: 1.5rem;
      margin-bottom: 2rem;
    }
    table {
        font-size: 0.93rem;
        background-color: none;
        border-collapse: separate;
        border-spacing: 0;
        width: 100%;
        margin-bottom: 0rem;
        border-radius: 5px;
        overflow: hidden;
    }

    /* Add these rules to apply rounded corners to the first and last rows in the table */
    table tr:first-child th:first-child {
        border-top-left-radius: 15px;
    }
    table tr:first-child th:last-child {
        border-top-right-radius: 15px;
    }
    table tr:last-child td:first-child {
        border-bottom-left-radius: 15px;
    }
    table tr:last-child td:last-child {
        border-bottom-right-radius: 15px;
    }
    th, td {
        padding: 0.75rem;
        text-align: center; /* Update this line to center the text */
        align-items: center;
        justify-content: center;
    }
    thead {
        background-color: #033894;
        color: white;
    }
    tbody tr:nth-child(odd) {
        background-color: #f2f2f2;
    }
    tbody tr:nth-child(even) {
        background-color: #ffffff;
    }
    thead th {
        font-weight: bold;
    }
    .table th.col-filename,
    .table td.col-filename {
      width: 50%;
    }

    .table th.col-size,
    .table td.col-size {
      width: 30%;
    }

    .table th.col-action,
    .table td.col-action {
      width: 20%;
    }
    .table td,
    .table th {
      border-top: 0;
      padding: 0.20rem;
      vertical-align: middle;
    }
    .table thead th {
      text-align: center;
    }
    td {
      text-align: center;
    }
    .sort-arrow {
        display: inline-block;
        width: 0;
        height: 0;
        margin-left: 5px;
        vertical-align: middle;
        border: 4px solid transparent;
    }

    .sort-arrow.arrow-up {
        border-bottom-color: #fff;
    }

    .sort-arrow.arrow-down {
        border-top-color: #fff;
    }
     th {
        cursor: pointer;
    }
    .manage-job-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 30px; /* Adjust the width as needed */
        height: 30px; /* Adjust the height as needed */
        padding: 0;
        border: none;
        background-color: transparent;
        cursor: pointer;
    }
    .table-container {
        position: relative;
    }

    .create-job-button {
        position: absolute;
        right: 10px; /* Adjust the right distance as needed */
        top: -60px;
    }
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
        .pink-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #bd7cbf;
            border-radius: 50%;
            margin-right: 5px;
        }
        .orange-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #de7f12;
            border-radius: 50%;
            margin-right: 5px;
        }
</style>    
<body>
    
<div class="header d-flex align-items-center justify-content-between">
    <a href="home.php">
        <img src="/assets/brand/logo.png" alt="Logo" class="logo">
    </a>
    <nav>
        <ul class="nav">
            <li class="nav-item">
                <a href="jobs.php" class="nav-link">Job Manager</a>
            </li>
            <?php if ($_SESSION['username'] == 'admin'): ?>
            <li class="nav-item">
                <a href="containers.php" class="nav-link">Container Manager</a>
            </li>
            <?php endif; ?>
            <li class="nav-item">
                <a href="files.php" class="nav-link">File Manager</a>
            </li>
            <?php if ($_SESSION['username'] == 'admin'): ?>
            <li class="nav-item">
                <a href="keycloak_user_management.php" class="nav-link">User Manager</a>
            </li>
            <?php endif; ?>
            
        </ul>
    </nav>
    <div class="user-info">
        <span>Welcome, <strong><?= htmlspecialchars($_SESSION['username']) ?></strong> | </span>
        <a href="logout.php" class="btn btn-secondary btn-sm">Logout</a>
    </div>
</div>


    <div class="rounded-box container">
        <h1 class="mb-4 table-title"><strong>Job Management</strong></h1>
        
        <div class='table-container'>

        <a href="submit_job.php" class="create-job-button btn btn-success">Create new job</a>
        
            <table class="table table-wrapper">
                <thead>
                <tr>
                    <th onclick="sortTable(0)">Job Title <span class="sort-arrow"></span></th>
                    <th onclick="sortTable(1)">Job ID <span class="sort-arrow"></span></th>
                    <th onclick="sortTable(2)">Creation Date <span class="sort-arrow"></span></th>
                    <th onclick="sortTable(3)">Dataset File <span class="sort-arrow"></span></th>
                    <th onclick="sortTable(4)">Executable File <span class="sort-arrow"></span></th>
                    <th onclick="sortTable(5)">Status <span class="sort-arrow"></span></th>
                    <th>Actions</th>
                    </tr>
                </thead>    
                <tbody>
                <?php if (count($jobs) > 0) : ?>
                    <?php foreach ($jobs as $job) : ?>
                        <tr>
                            <td><strong><?php echo $job['title']?></strong></td>
                            <td><?php echo $job['jobid']?></td>
                            <td><?php echo $job['creation_date']; ?></td>
                            <td><?php echo $job['dataset_file']; ?></td>
                            <td><?php echo $job['executable_file']; ?></td>
                            <td><?php
                            if (strpos($job['status'], 'running') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'chunking') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'shuffling') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'mapping') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'reducing') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'merging') !== false) {
                                echo $job['status'] ." <span class='blue-bullet'></span>";
                            } elseif (strpos($job['status'], 'queued') !== false){
                                echo $job['status'] ." <span class='yellow-bullet'></span>";
                            } elseif (strpos($job['status'], 'completed') !== false){
                                echo $job['status'] ." <span class='green-bullet'></span>";
                            } elseif (strpos($job['status'], 'failed') !== false){
                                echo $job['status'] ." <span class='red-bullet'></span>";
                            } elseif (strpos($job['status'], 'resourcing') !== false){
                                echo $job['status'] ." <span class='orange-bullet'></span>";
                            } elseif (strpos($job['status'], 'planning') !== false){
                                echo $job['status'] ." <span class='pink-bullet'></span>";
                            }else {
                                echo $job['status'] . "</p>";
                            }
                            ?></td>
                            <td>
                                <form method="GET" action="manage_job.php">
                                    <input type="hidden" name="id" value="<?php echo $job['jobid']; ?>">
                                    <button type="submit" class="manage-job-button">
                                        <i class="fas fa-cog"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                        <?php else : ?>
                            <tr>
                                <td colspan="8">No jobs submitted yet for user <strong><?php echo $_SESSION['username']; ?></strong></td>
                            </tr>
                        <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</body>
<script>
    function sortTable(columnIndex) {
        var table, rows, switching, i, x, y, shouldSwitch;
        table = document.querySelector("table");
        switching = true;

        // Set the sorting direction for the column
        var sortDirection = table.getAttribute("data-sort-direction") === "asc" ? "desc" : "asc";

        // Remove the sort-arrow class from all columns
        var sortArrow = table.querySelectorAll("th .sort-arrow");
        sortArrow.forEach(function (arrow) {
            arrow.classList.remove("arrow-up");
            arrow.classList.remove("arrow-down");
        });

        // Determine the sorting direction and update the arrow class for the clicked column
        if (sortDirection === "asc") {
            sortArrow[columnIndex].classList.add("arrow-up");
        } else {
            sortArrow[columnIndex].classList.add("arrow-down");
        }

        table.setAttribute("data-sort-direction", sortDirection);

        while (switching) {
            switching = false;
            rows = table.getElementsByTagName("tr");

            // Loop through all table rows except the header
            for (i = 1; i < rows.length - 1; i++) {
                shouldSwitch = false;
                x = rows[i].getElementsByTagName("td")[columnIndex];
                y = rows[i + 1].getElementsByTagName("td")[columnIndex];

                // Check if the two rows should switch places based on the sorting direction
                if (sortDirection === "asc") {
                    if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                        shouldSwitch = true;
                        break;
                    }
                } else if (sortDirection === "desc") {
                    if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
                        shouldSwitch = true;
                        break;
                    }
                }
            }

            if (shouldSwitch) {
                // Swap the rows and mark the switching flag as true
                rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
                switching = true;
            }
        }
    }
</script>
</html>