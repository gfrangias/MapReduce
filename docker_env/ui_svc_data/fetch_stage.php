<?php

include 'zk.php';
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}

header('Content-Type: application/json');


if(isset($_GET['jobpathid']) && !empty($_GET['jobpathid'])){
    $jobPath = '/jobs/'.$_SESSION['username'].'/'.$_GET['jobpathid'];
    if ($zk->exists($jobPath)) {
        // Get the job info from the znode
        $jobStage = $zk->get($jobPath);
        $jobStage = json_decode($jobStage,1);

        $jobStatistics = $zk->getChildren($jobPath.'/statistics');


        $workersArray = [];
        $timeArray = [];

        foreach ($jobStatistics as $stage) {
            $path = $jobPath . '/statistics/' . $stage;

            if ($zk->exists($path)) {
                $data = $zk->get($path);
                $znodeValue = json_decode($data, true);

                // Extract workers and time values
                $workers = $znodeValue['workers'];
                $time = $znodeValue['time'];

                // Add to workers array
                if (isset($workersArray[$stage])) {
                    $workersArray[$stage] += $workers;
                } else {
                    $workersArray[$stage] = $workers;
                }

                // Add to time array
                if (isset($timeArray[$stage])) {
                    $timeArray[$stage] += $time;
                } else {
                    $timeArray[$stage] = $time;
                }
            }
        }

        // Calculate total workers
        $workersTotal = array_sum($workersArray);
        $workersArray['total'] = $workersTotal;
        $workersArray['init'] = 0;
        $workersArray['completed'] = 0;


        // Calculate total time
        $timeTotal = array_sum($timeArray);
        $timeArray['total'] = $timeTotal;
        
        $data = ["stage" => $jobStage['status'], "workers"=>$workersArray, "times"=>$timeArray];

        echo json_encode($data);
    } else {
        // Job doesn't exist, return null or handle the error accordingly
        
        return null;
    }
}

