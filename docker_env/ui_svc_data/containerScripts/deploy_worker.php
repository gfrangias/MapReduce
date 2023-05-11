<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $imageName = 'mysql:5.7';
   
    $networkConfig = [
        "NetworkMode" => "map_reduce_net"
    ];

    
    $postData = json_encode([
        'Image' => $imageName,
        'HostConfig' => $networkConfig,
        'Env' => ['MYSQL_ROOT_PASSWORD=root'],
    ]);
    $containerName = 'w'.uniqid();
    $url = "http://172.17.0.1:2375/containers/create";

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Content-Length: ' . strlen($postData),
    ]);

    $response = curl_exec($ch);
    $http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($http_status >= 200 && $http_status < 300) {
        $response_data = json_decode($response, true);
        $container_id = $response_data['Id'];

        //Rename the container
        $url = "http://172.17.0.1:2375/containers/".$container_id."/rename?name=".$containerName;
        $postData = json_encode(['name'=>$containerName]);
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        $http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);


        // Start the container
        $start_url = "http://172.17.0.1:2375/containers/{$container_id}/start";
        $start_ch = curl_init($start_url);
        curl_setopt($start_ch, CURLOPT_CUSTOMREQUEST, "POST");
        curl_setopt($start_ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($start_ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
        ]);

        $start_response = curl_exec($start_ch);
        $start_http_status = curl_getinfo($start_ch, CURLINFO_HTTP_CODE);
        curl_close($start_ch);

        if ($start_http_status >= 200 && $start_http_status < 300) {
            echo "Container created and started";
        } else {
            echo "Error starting container: " . $start_response;
        }

    } else {
        echo "Error creating container: " . $response;
    }

    curl_close($ch);
} else {
    echo "Invalid request";
}
?>