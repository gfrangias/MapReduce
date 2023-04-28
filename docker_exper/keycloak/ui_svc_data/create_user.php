<?php
include 'functions.php';
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
verifyToken();



if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get user data from form
    $username = $_POST['username'];
    $email = $_POST['email'];
    $firstName = $_POST['firstName'];
    $lastName = $_POST['lastName'];
    $password = $_POST['password'];
    $passwordRepeat = $_POST['passwordRepeat'];

    // Validate form data
    $errors = array();
    if (empty($username)) {
        $errors[] = 'Username is required';
    }
    if (empty($email)) {
        $errors[] = 'Email is required';
    }
    if (empty($password)) {
        $errors[] = 'Password is required';
    }
    if ($password !== $passwordRepeat) {
        $errors[] = 'Passwords do not match';
    }

    // Create user in Keycloak
    if (empty($errors)) {
        $curl = curl_init();

        curl_setopt_array($curl, array(
        CURLOPT_URL => 'http://172.16.0.3:8080/auth/admin/realms/master/users',
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => '',
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 0,
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => 'POST',
        CURLOPT_POSTFIELDS =>'{
            "username":"'.$username.'",
            "email": "'.$email.'",
            "firstName": "'.$firstName.'",
            "lastName": "'.$lastName.'",
            "enabled": true,
            "credentials": [
                {
                    "type": "password",
                    "value": "'.$password.'",
                    "temporary": false
                }
            ]
        }',
        CURLOPT_HTTPHEADER => array(
            'Content-Type: application/json',
            'Authorization: Bearer '. acquireToken()['access_token']
        )));

        $response = curl_exec($curl);

        curl_close($curl);

        $httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

        if ($httpCode === 201) {
            $message = 'User created successfully';
            $messageClass = 'success';
        } else if($httpCode === 409){
            $message = 'User already exists';
            $messageClass = 'info';
        } else {
            $message = 'Failed to create user '.$httpCode;
            $messageClass = 'danger';
        }
        
    } else {
        $message = implode('<br>', $errors);
        $messageClass = 'danger';
    }
}

// HTML form for creating a new user
?>
<!DOCTYPE html>
<html>
<head>
    <title>MapReduce UI - Create User</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.1.3/css/bootstrap.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.1.3/js/bootstrap.bundle.min.js"></script>
</head>
<body>
    <div class="container py-5">
        <h1 class="mb-4">Create User</h1>
        <?php if (isset($message)): ?>
    <div class="alert alert-<?= $messageClass ?> alert-dismissible fade show" role="alert">
        <?= $message ?>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <?php endif; ?>

    <form method="POST">
        <div class="mb-3">
            <label for="username" class="form-label">Username</label>
            <input type="text" class="form-control" id="username" name="username" required>
        </div>
        <div class="mb-3">
            <label for="email" class="form-label">Email</label>
            <input type="email" class="form-control" id="email" name="email" required>
        </div>
        <div class="mb-3">
            <label for="firstName" class="form-label">First Name</label>
            <input type="text" class="form-control" id="firstName" name="firstName">
        </div>
        <div class="mb-3">
            <label for="lastName" class="form-label">Last Name</label>
            <input type="text" class="form-control" id="lastName" name="lastName">
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Password</label>
            <input type="password" class="form-control" id="password" name="password" required>
        </div>
        <div class="mb-3">
            <label for="passwordRepeat" class="form-label">Repeat Password</label>
            <input type="password" class="form-control" id="passwordRepeat" name="passwordRepeat" required>
        </div>
        <div class="mb-3">
            <button type="submit" class="btn btn-primary">Create</button>
            <a href="keycloak_user_management.php" class="btn btn-secondary">Cancel</a>
        </div>
    </form>
</div>