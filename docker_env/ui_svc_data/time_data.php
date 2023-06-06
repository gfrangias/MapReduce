<?php

include 'zk.php';

// Retrieve ZK data for each task
$mapTime = $zk->get('/jobs/job1/maptime');
$reduceTime = $zk->get('/jobs/job1/reducetime');
$preprocessingTime = $zk->get('/jobs/job1/preprocessingtime');

// Prepare the data for the pie chart
$data = [];

if ($mapTime !== null) {
    $data[] = (float) $mapTime;
}

if ($reduceTime !== null) {
    $data[] = (float) $reduceTime;
}

if ($preprocessingTime !== null) {
    $data[] = (float) $preprocessingTime;
}

if ($preprocessingTime !== null) {
    $data[] = (float) $preprocessingTime;
}

// Return the data as JSON
header('Content-Type: application/json');
echo json_encode($data);