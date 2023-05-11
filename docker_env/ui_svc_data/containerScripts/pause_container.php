<?php
if (isset($_POST['container_id'])) {
    $container_id = $_POST['container_id'];
    $api_url = 'http://172.17.0.1:2375/containers/' . $container_id . '/pause';

    $context = stream_context_create([
        'http' => [
            'method' => 'POST',
            'header' => "Content-Type: application/json\r\n",
            'timeout' => 5
        ]
    ]);

    $response = @file_get_contents($api_url, false, $context);

    if ($response === false) {
        $error = error_get_last();
        echo "Error pausing container: " . $error['message'];
    } else {
        echo "Container paused successfully.";
    }
} else {
    echo "Container ID is not set.";
}
?>