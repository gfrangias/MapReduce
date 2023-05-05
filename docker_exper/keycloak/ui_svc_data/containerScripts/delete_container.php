<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['container_id'])) {
    $containerId = $_POST['container_id'];

    // Stop the container
    $stop_url = "http://172.17.0.1:2375/containers/{$containerId}/stop";
    $ch = curl_init($stop_url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    $http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($http_status >= 200 && $http_status < 300 || $http_status == 304){
        // Delete the container
        $delete_url = "http://172.17.0.1:2375/containers/{$containerId}";
        $delete_ch = curl_init($delete_url);
        curl_setopt($delete_ch, CURLOPT_CUSTOMREQUEST, "DELETE");
        curl_setopt($delete_ch, CURLOPT_RETURNTRANSFER, true);

        $delete_response = curl_exec($delete_ch);
        $delete_http_status = curl_getinfo($delete_ch, CURLINFO_HTTP_CODE);

        if ($delete_http_status >= 200 && $delete_http_status < 300) {
            echo "Container stopped and deleted";
        } else {
            echo "Error deleting container: " . $delete_response;
        }

        curl_close($delete_ch);
    } else {
        echo "Error stopping container: " . $response;
    }

    curl_close($ch);
} else {
    echo "Invalid request";
}
?>