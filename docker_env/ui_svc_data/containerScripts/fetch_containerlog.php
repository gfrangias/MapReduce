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
    // Split the logs into lines
    // Detect the encoding and convert to UTF-8 if necessary

    
    $lines = explode("\n", $response);

    $cleaned_lines = [];
    
    foreach ($lines as $line) {
        // Remove unwanted control characters except newline (\n) and carriage return (\r)
        $cleaned_line = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $line);
        
        // Echo the cleaned line
        echo $cleaned_line . "\n";
    }
}