<?php
   include 'functions.php';
   if (session_status() == PHP_SESSION_NONE) {
       session_start();
   }
   verifyToken();



function updateUser($userId, $updatedUser) {
    // Keycloak API base URL
    $keycloakApiBaseUrl = "http://172.16.0.3:8080/auth/admin/realms/master/users/";

    // Replace with your access token
    $accessToken = $_SESSION['authToken'];

    $url = $keycloakApiBaseUrl . $userId;
    $ch = curl_init($url);

    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "PUT");
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($updatedUser));
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Authorization: Bearer ' . $accessToken,
        'Content-Type: application/json'
    ));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    curl_close($ch);

    return $httpCode >= 200 && $httpCode < 300;

}



if (isset($_POST['id']) && isset($_POST['username']) && isset($_POST['email'])) {
    $userId = $_POST['id'];
    if(isUserAdmin($_POST['id'], $_SESSION['authToken'])){
        header('Location: keycloak_user_management.php?msg_fail=Account+of+admin+is+not+subject+to+changes!');
        exit();
    }

    $updatedUser = array(
        'id' => $userId,
        'username' => $_POST['username'],
        'email' => $_POST['email'],
        'firstName' => isset($_POST['firstName']) ? $_POST['firstName'] : '',
        'lastName' => isset($_POST['lastName']) ? $_POST['lastName'] : ''
    );

    if (updateUser($userId, $updatedUser)) {
        header('Location: keycloak_user_management.php?msg_success=User+updated+successfully');
    } else {
        header('Location: keycloak_user_management.php?msg_fail=Failed+to+update+user');
    }
} else {
    header('Location: keycloak_user_management.php?msg=Invalid+user+information');
}