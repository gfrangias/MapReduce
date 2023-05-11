<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['container_id'])) {
    $container_id = $_POST['container_id'];
    $url = "http://172.17.0.1:2375/containers/" . $container_id . "/unpause";
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
    $response = curl_exec($ch);
    $http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    
    curl_close($ch);
    
    if ($http_status >= 200 && $http_status < 300) {
        echo "Container unpaused";
    } else {
        echo "Error unpausing container: " . $response;
    }
} else {
    echo "Invalid request";
}
?>