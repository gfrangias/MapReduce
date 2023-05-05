<?php
$api_url = 'http://172.17.0.1:2375/containers/json?all=1';

// Create a stream context for making the HTTP request
$context = stream_context_create([
    'http' => [
        'method' => 'GET',
        'header' => "Content-Type: application/json\r\n"
    ]
]);

// Fetch the containers using the Docker API
$response = file_get_contents($api_url, false, $context);

if ($response === false) {
    echo "Error fetching container information.";
} else {
    // Decode the JSON response
    $containers = json_decode($response, true);
    function sortByName($a, $b) {
        return strcmp($a['Names'][0], $b['Names'][0]);
    }
    
    usort($containers, 'sortByName');

    // Build the table with the container information
    echo "<table class='table table-striped small-text tight-rows'>";
    echo "<thead>";
    echo "<tr><th>ID</th><th>Name</th><th>Image</th><th>Created</th><th>IPv4</th><th>State</th><th>Status</th><th>Actions</th></tr>";
    echo "</thead>";
    echo "<tbody>";

    foreach ($containers as $container) {
        echo "<tr class='align-middle'>";
        echo "<td>" . substr($container['Id'], 0, 12) . "</td>";
        echo "<td>" . $container['Names'][0] . "</td>";

        echo "<td>" . $container['Image'] . "</td>";
        echo "<td>" . date('Y-m-d H:i:s', $container['Created']) . "</td>";
        echo "<td>" . array_values($container['NetworkSettings']['Networks'])[0]['IPAddress']. "</td>";
        if (strpos($container['State'], 'running') !== false) {
            echo "<td>" . "<span class='green-bullet'></span>". $container['State'] ."</td>";

        } elseif (strpos($container['State'], 'paused') !== false){
            echo "<td>" . "<span class='yellow-bullet'></span>". $container['State'] ."</td>";
        } elseif (strpos($container['State'], 'restarting') !== false){
            echo "<td>" . "<span class='blue-bullet'></span>". $container['State'] ."</td>";
        } elseif (strpos($container['State'], 'exited') !== false){
            echo "<td>" . "<span class='red-bullet'></span>". $container['State'] ."</td>";
        }else {
            echo "<td>" . $container['State'] . "</td>";
        }
        echo "<td>" . $container['Status'] . "</td>";
        echo "<td>";
        if (strpos($container['Status'], 'Exited') !== false) {
            echo "<button title='Bring Up' class='btn btn-success btn-sm btn-act' onclick='restartContainer(\"" . $container['Id'] . "\")'><i class='fas fa-arrow-up' style='color: white;'></i></button>";
        } else {
            echo "<button title='Stop' class='btn btn-danger btn-sm mr-1 btn-act' onclick='stopContainer(\"" . $container['Id'] . "\")'><i class='fas fa-stop'></i></button>";
        }
        if (strpos($container['Status'], 'Paused') !== false) {
            echo "<button class='btn btn-success btn-sm mr-1 btn-act' onclick='unpauseContainer(\"" . $container['Id'] . "\")' title='Unpause container'><i class='fas fa-play' style='color: white;'></i></button>";
        } else {
            echo "<button class='btn btn-warning btn-sm mr-1 btn-act' onclick='pauseContainer(\"" . $container['Id'] . "\")' title='Pause container'><i class='fas fa-pause' style='color: white;'></i></button>";
        }
        echo "<button title='Restart' class='btn btn-primary btn-sm btn-act' onclick='restartContainer(\"" . $container['Id'] . "\")'><i class='fas fa-redo'></i></button>";
        echo "<button title='Purge' class='btn btn-primary btn-blk btn-sm' onclick='deleteContainer(\"" . $container['Id'] . "\")'><i class='fas fa-skull-crossbones'></i></button>";
        echo "</td>";
        echo "</tr>";
    }

    echo "</tbody>";
    echo "</table>";
}
?>