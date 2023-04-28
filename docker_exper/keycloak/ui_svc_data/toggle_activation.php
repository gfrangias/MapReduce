<?php
include 'functions.php';
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
verifyToken();

if(isUserAdmin($_POST['user_id'], $_SESSION['authToken'])){
    header('Location: keycloak_user_management.php?msg_fail=Account+of+admin+is+not+subject+to+activation+status+changes!');
    exit();
}

$user_id = $_POST['user_id'];
$current_status = $_POST['current_status'];


$ch = curl_init();

$access_token = $_SESSION['authToken'];

    $url = "http://172.16.0.3:8080/auth/admin/realms/master/users/". $user_id;

$headers = [
    'Content-Type: application/json',
    'Authorization: Bearer ' . $access_token
];

$data = json_encode([
    'enabled' => !$current_status
]);

curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);

curl_close($ch);

if ($http_code == 204) {
    header('Location: keycloak_user_management.php?msg_success=User activation status updated successfully.');
} else {
    header('Location: keycloak_user_management.php?msg_fail=Error updating user activation status.');
}
?>





