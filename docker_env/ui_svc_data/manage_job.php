<?php
include 'functions.php';
$stages = array('init', 'chunking', 'mapping', 'shuffling', 'reducing', 'merging', 'completed');

if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
verifyToken();


if(!isset($_GET['id']) || empty($_GET['id'])){
    header('Location: jobs.php');
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
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/apexcharts@3.28.3/dist/apexcharts.min.js"></script>
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
        .rounded-container {
            border-bottom-left-radius: 1.25rem;
            border-bottom-right-radius: 1.25rem;
            overflow: hidden; /* This is important if the children don't also have rounded corners */
            
        }
        .rounded-top {
            background-color: #033894;
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
        .chart-container {
            height: 100%;
        }
        .box{
            border-bottom-left-radius:  1.25rem;
            border-bottom-right-radius:  1.25rem;
        }
        .stage {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background-color: #ddd;
            color: black;
            line-height: 50px;
            text-align: center;
            display: inline-block;
            position: relative;
            margin-right: 60px;
        }

        .stage.past {
            background-color: #28a745;
            color: white;
        }

        .stage.current {
            background-color: #007bff;
            color: white;
        }

        .stage:hover {
            cursor: pointer;
        }

        .stage:not(:last-child)::after {
            content: "";
            position: absolute;
            z-index: -1;
            right: -60px;
            top: 25px;
            height: 1px;
            width: 60px;
            background: #ddd;
        }

        .stage.past:not(:last-child)::after {
            background: #28a745;
        }

        .stage.current:not(:last-child)::after,
        .stage.current ~ .stage:not(:last-child)::after {
            border: 1px dashed #ddd;
        }

        .tooltip-inner {
            border-radius: 5px;
            max-width: 200px; /* If you want to change the width */
            font-size: 15px; /* Change this to increase/decrease the font size */
            padding: 10px; /* Change this to increase/decrease the padding */
        }
</style>
<body>

    <!-- Navigation Bar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand">Job Details<?php if(isset($_GET['id'])){echo ' for <strong> '. $_GET['id'].'</strong>';}?> </a>
        <div hidden disabled id = "jid"><?echo $_GET['id']?></div>
    </nav>

    <!-- Container -->
    <div class="container mt-4">

        <!-- Row -->
        <div class="row">

            <!-- Left Container: Job Info -->
            <div class="col-lg-4 rounded-container">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Job Info</h5>
                </div>
                <div class="rounded-container p-4 bg-light">
                    <?php
                    $jobId = $_GET['id'];
                    $jobInfo = getJobInfo($username, $jobId, $zk); // Function to retrieve job info
                    // Display job details
                    echo "<p>Job Title: <strong> " . $jobInfo['title'] . "</strong></p>";
                    if (strpos($jobInfo['status'], 'running') !== false) {
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'queued') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='yellow-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'completed') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='green-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'failed') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='red-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'planning') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='pink-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'mapping') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'reducing') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'shuffling') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'chunking') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    } elseif (strpos($jobInfo['status'], 'merging') !== false){
                        echo "<p id='job-status'>Status: " . "<strong>". $jobInfo['status'] ."</strong> <span class='blue-bullet'></span></p>";
                    }else {
                        echo "<p id='job-status'>Status: " . $jobInfo['status'] . "</p>";
                    }
                     echo "<p>Creation Date: <strong> " . $jobInfo['creation_date'] . "</strong></p>";
                     echo "<p>Executable: <strong> " . $jobInfo['executable_file'] . "</strong></p>";
                     echo "<p>Dataset: <strong> " . $jobInfo['dataset_file'] . "</strong></p>";       
                     echo "<p>Map Class: <strong> " . $jobInfo['map_method'] . "</strong></p>";  
                     echo "<p>Reduce Class: <strong> " . $jobInfo['reduce_method'] . "</strong></p>";    
                    ?>
                </div>
            </div>

            <!-- Right Container: Execution Info -->
            <div class="col-lg-8 rounded-container">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Execution Time Info</h5>
                </div>
                <div class="container-fluid h-100  flex-column">
                    <div class="row flex-grow-1 bg-light box">
                        <div class="pt-3 col-md-3">
                        
                        </div>
                        <div class="pt-5 col-md-6 pb-4 d-flex flex-column">
                            <div id="chart" class="chart-container"></div>
                            <p id="message"></p>
                        </div>
                    </div>
                </div>
            </div>
        <div class="container mt-4">
            <div class="timeline text-center">
                <?php foreach($stages as $stage): ?>
                    <div class="stage" id="<?php echo str_replace(' ', '_', $stage); ?>" data-toggle="tooltip" data-placement="bottom" title="">
                        <p class="my-auto"><?php echo strtoupper(substr($stage, 0, 1)); ?></p>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>
        </div> <!-- End of row -->

        <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
        <script>
       $(document).ready(function(){
            function fetchStage() {
                var jid = $('#jid').text();
                $.ajax({
                    url: 'fetch_stage.php',
                    type: 'get',
                    data: { 'jobpathid': jid }, 
                    dataType: 'json',
                    success: function(response) {
                        $('.stage').removeClass('current past');
                        $('#'+response.stage.replace(' ', '_')).addClass('current').prevAll().addClass('past');
                        $.each(response.workers, function(stage, num) {
                            var tooltipTitle = "<strong>" + stage.charAt(0).toUpperCase() + stage.slice(1) + "</strong>";
                            var tooltipContent = 'Num Of Workers: ' + num;
                            if(stage=='init' || stage=='completed'){
                                tooltipContent = '';
                            }
                            $('#'+stage.replace(' ', '_')).attr('data-original-title', tooltipTitle + '<br/>' + tooltipContent);

                        });
                    },
                    complete: function() {
                        setTimeout(fetchStage, 5000);
                    }
                });
            }
            fetchStage();
            $('[data-toggle="tooltip"]').tooltip({html: true});
        });
        </script>
        <!-- Bottom Container: Result -->
        <div class="row mt-4 rounded-container">
            <div class="col-lg-12">
                <div class="rounded-top p-2 text-white">
                    <h5 class="mb-0">Result</h5>
                </div>
                <div class="rounded-container p-4 bg-light">
                    <textarea class="form-control" rows="5" readonly id="result-box"><?php
                    ?></textarea>
                </div>
            </div>
        </div>

      <div class="col-sm-12">
        <a href="jobs.php" class="btn btn-secondary">Back to Jobs</a>
      </div>

    </div> <!-- End of container -->

    <script>
        var chart; // move this declaration outside of the function
        function updateChart() {
            var jid = $('#jid').text();
            $.ajax({
                url: 'fetch_stage.php',
                type: 'GET',
                data: { 'jobpathid': jid },
                dataType: 'json',
                success: function(data) {
                    if (data.length === 0) {
                        $("#chart").empty();
                        $("#message").text("No stats available yet.");
                    } else {
                        if (chart) {
                            chart.destroy();
                        }
                        var totalTime = data.times.total;
                        delete data.times.total; // Remove the "total" property from the data object

                        var labels = [];
                        var series = [];
                        for (var label in data.times) {
                            if (data.times.hasOwnProperty(label) && data.times[label] !== null) {
                                labels.push(label.charAt(0).toUpperCase() + label.slice(1));
                                series.push(data.times[label]);
                            }
                        }

                        var chartOptions = {
                            series: series,
                            chart: {
                                type: 'pie',
                                width: '105%',
                            },
                            labels: labels,
                            colors: ['#007BFF', '#8f8f8f', '#343A40', '#4d68b0', '#033894'],
                            dataLabels: {
                                formatter: function(val, opts) {
                                    return opts.w.config.series[opts.seriesIndex] + " seconds";
                                },
                            },
                            responsive: [{
                                breakpoint: 500,
                                options: {
                                    chart: {
                                        width: 100
                                    },
                                    legend: {
                                        position: 'bottom'
                                    }
                                }
                            }]
                        };

                        chart = new ApexCharts(document.querySelector("#chart"), chartOptions);
                        chart.render();
                        $("#message").empty();
                    }
                },
                error: function (xhr, status, error) {
                    console.error(xhr); // Log the XHR object for debugging purposes
                    console.error(status); // Log the status for debugging purposes
                    console.error(error); // Log the error for debugging purposes

                    $("#chart").empty();
                    $("#message").text("Error occurred while retrieving data.");
                }
            });
        }

        function updateJobStatus() {
            var jid = $('#jid').text();
            $.ajax({
                url: 'fetch_stage.php',
                type: 'GET',
                data: { 'jobpathid': jid },
                dataType: 'json',
                success: function(response) {
                    var status = response.stage;
                    var bulletClass = '';

                    if (status.includes('queued')) {
                        bulletClass = 'yellow-bullet';
                    } else if (status.includes('completed')) {
                        bulletClass = 'green-bullet';
                         // Job is completed, clear both intervals
                        clearInterval(jobStatusInterval);
                        clearInterval(chartInterval);
                         // Job is completed, fetch the job result
                        $.ajax({
                            url: 'fetch_jobresult.php',
                            type: 'GET',
                            data: { 'jid': jid },
                            success: function(result) {
                                $('#result-box').text(result);
                            },
                            error: function(xhr, status, error) {
                                console.error(xhr); // Log the XHR object for debugging purposes
                                console.error(status); // Log the status for debugging purposes
                                console.error(error); // Log the error for debugging purposes
                                $('#result-box').text("Error occurred while fetching job result.");
                            }
                        });
                    } else if (status.includes('failed')) {
                        bulletClass = 'red-bullet';
                    } else {
                        bulletClass = 'blue-bullet';
                    }

                    var statusHtml = '<p>Status: <strong>' + status + '</strong> <span class="' + bulletClass + '"></span></p>';
                    $('#job-status').html(statusHtml);
                },
                error: function(xhr, status, error) {
                    console.error(xhr); // Log the XHR object for debugging purposes
                    console.error(status); // Log the status for debugging purposes
                    console.error(error); // Log the error for debugging purposes
                    $('#job-status').text("Error occurred while retrieving job status.");
                }
            });
        }

        function fetchJobResult() {

        }

        $(document).ready(function () {
            updateChart();
            updateJobStatus();
            // Start the intervals and store the interval IDs
            jobStatusInterval = setInterval(updateJobStatus, 5000);
            chartInterval = setInterval(updateChart, 8000);
        });
    </script>

</body>
</html>
