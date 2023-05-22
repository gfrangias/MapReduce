<?php
// Start the session
session_start();

// Check if the user is logged in
if (!isset($_SESSION['username'])) {
    header('Location: index.php'); // Redirect to login page if not logged in
    exit();
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
        $zk->create('/jobs', null,  $acl);
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
    <title>Job Manager</title>
</head>

<body>
    <div class="container">
        <h1>Job Manager</h1>

        <h2>Welcome, <?php echo $username; ?></h2>

        <table class="table">
            <thead>
                <tr>
                    <th>Job ID</th>
                    <th>Creation Date</th>
                    <th>Dataset File</th>
                    <th>Executable File</th>
                    <th>Status</th>
                    <th>Result Path</th>
                </tr>
            </thead>
            <tbody>
            <?php if (count($jobs) > 0) : ?>
                <?php foreach ($jobs as $job) : ?>
                    <tr>
                        <td><?php echo $job['jobid']?></td>
                        <td><?php echo $job['creation_date']; ?></td>
                        <td><?php echo $job['dataset_file']; ?></td>
                        <td><?php echo $job['executable_file']; ?></td>
                        <td><?php echo $job['status']; ?></td>
                        <td><?php echo $job['result_path']; ?></td>
                    </tr>
                    <?php endforeach; ?>
                    <?php else : ?>
                        <tr>
                            <td colspan="6">No jobs submitted yet</td>
                        </tr>
                    <?php endif; ?>
            </tbody>

        <form method="POST" action="">
            <button type="submit" name="create_job" class="btn btn-primary">Create New Job</button>
        </form>
    </div>
</body>

</html>