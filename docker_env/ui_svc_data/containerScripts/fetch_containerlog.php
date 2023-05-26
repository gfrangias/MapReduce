<?php

if($_SERVER['REQUEST_METHOD'] == "GET" && isset($_GET['id'])){
    $url = "http://172.17.0.1:2375/containers/{$_GET['id']}/logs?stdout=1"; // Replace 'localhost' with the appropriate Docker host address

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["Content-Type: application/json"]);

    $response = curl_exec($ch);

    if ($response === false) {
        // Error occurred
        $errorMessage = curl_error($ch);
        curl_close($ch);
        return $errorMessage;
    }

    curl_close($ch);

    echo $response;

    return $response;
}