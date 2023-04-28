<?php
    include 'functions.php';
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    verifyToken();

    if(isUserAdmin($_GET['id'], $_SESSION['authToken'])){
        header('Location: keycloak_user_management.php?msg_fail=Account+of+admin+is+not+subject+to+changes!');
        exit();
    }

function getUserById($userId) {
    // Keycloak API base URL
    $keycloakApiBaseUrl = "http://172.16.0.3:8080/auth/admin/realms/master/users/";

    // Replace with your access token
    $accessToken = $_SESSION['authToken'];

    $url = $keycloakApiBaseUrl . $userId;
    $ch = curl_init($url);

    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Authorization: Bearer ' . $accessToken,
    ));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    curl_close($ch);

    if ($httpCode >= 200 && $httpCode < 300) {
        return json_decode($response, true);
    } else {
        return null;
    }
}


$user = null;
if (isset($_GET['id'])) {
 
    $userId = $_GET['id'];
    $user = getUserById($userId);
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit User</title>

    <!-- Bootstrap CSS -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 class="my-4">Edit User</h1>

        <?php if ($user): ?>
            <form action="update_user.php" method="post">
                <input type="hidden" name="id" value="<?= htmlspecialchars($user['id']) ?>">

                <div class="form-group">
                    <label for="username">Username:</label>
                    <input disabled type="text" class="form-control" id="username" name="username" value="<?= htmlspecialchars($user['username']) ?>" required>
                </div>

                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" class="form-control" id="email" name="email" value="<?= htmlspecialchars($user['email']) ?>" required>
                </div>

                <div class="form-group">
                    <label for="firstName">First Name:</label>
                    <input type="text" class="form-control" id="firstName" name="firstName" value="<?= htmlspecialchars($user['firstName']) ?>">
                </div>

                <div class="form-group">
                    <label for="lastName">Last Name:</label>
                    <input type="text" class="form-control" id="lastName" name="lastName" value="<?= htmlspecialchars($user['lastName']) ?>">
                </div>

                <button type="submit" class="btn btn-primary">Update User</button>
            </form>
        <?php else: ?>
            <p>Invalid user ID.</p>
        <?php endif; ?>
    </div>
</body>
</html>