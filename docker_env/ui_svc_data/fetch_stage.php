<?php

include 'zk.php';

header('Content-Type: application/json');

if(isset($_GET['jobpathid']) && !empty($_GET['jobpathid'])){
    if ($zk->exists($_GET['jobpathid'].'/stage') && $zk->exists($_GET['jobpathid'].'/statistics')) {
        // Get the job info from the znode
        $jobStage = $zk->get($_GET['jobpathid'].'/stage');
        $jobStatistics = $zk->getChildren($_GET['jobpathid'].'/statistics');

    } else {
        // Job doesn't exist, return null or handle the error accordingly
        return null;
    }
}

$data = [
    "stage" => "Planning Stage",
    "numOfWorkers" => [
        "Initial Stage" => 5, 
        "Planning Stage" => 10, 
        "Chunk Stage" => 15, 
        "Map Stage" => 12
    ]
];