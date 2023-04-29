<?php

    include 'functions.php';
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    verifyToken();

function deleteUser($userId) {
    // Keycloak API base URL
    $keycloakApiBaseUrl = "http://172.16.0.3:8080/auth/admin/realms/master/users/";

    // Replace with your access token
    $accessToken = $_SESSION['authToken'];

    $url = $keycloakApiBaseUrl . $userId;
    $ch = curl_init($url);

    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Authorization: Bearer ' . $accessToken,
    ));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    curl_close($ch);

    return $httpCode >= 200 && $httpCode < 300;
}


if (isset($_GET['id'])) {
    $userId = $_GET['id'];
    
    if(isUserAdmin($_GET['id'], $_SESSION['authToken'])){
        header('Location: keycloak_user_management.php?msg_fail=Account+of+admin+is+not+subject+to+deletion!');
        exit();
    }
    
    if (deleteUser($userId)) {
        header('Location: keycloak_user_management.php?msg_success=User+deleted+successfully');
    } else {
        header('Location: keycloak_user_management.php?msg_fail=Failed+to+delete+user');
    }

} else {
    header('Location: keycloak_user_management.php?msg=Invalid+user+ID');
}