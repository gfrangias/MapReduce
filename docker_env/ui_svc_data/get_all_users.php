<?php
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    verifyToken();

function getAllUsers() {
    // Keycloak API base URL
    $keycloakApiBaseUrl = "http://172.16.0.3:8080/auth/admin/realms/master/users/";



    $accessToken = $_SESSION['authToken'];
    $url = $keycloakApiBaseUrl;
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

$users = getAllUsers();
?>
<?php if ($users): ?>
    <table>
        <thead>
            <tr>
                <th>Username</th>
                <th>Email</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Status</th> <!-- Add this line -->
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($users as $user): 
                    $enabled = $user['enabled'];?>
                <tr>
                    <td><?= htmlspecialchars($user['username']) ?></td>
                    <td><?= isset($user['email']) ? htmlspecialchars($user['email']) : 'Undefined' ?></td>
                    <td><?= isset($user['firstName']) ? htmlspecialchars($user['firstName']) : 'Undefined' ?></td>
                    <td><?= isset($user['lastName']) ? htmlspecialchars($user['lastName']) : 'Undefined' ?></td>
                    <td class="<?= $enabled ? 'activation-text' : 'inactive-text' ?>"><?= $enabled ? 'Active' : 'Inactive' ?></td>
                    <td>
                        <form action="toggle_activation.php" method="post" class="d-inline">
                            <input type="hidden" name="user_id" value="<?= htmlspecialchars($user['id']) ?>">
                            <input type="hidden" name="current_status" value="<?= $enabled ?>">
                            <button type="submit" class="btn btn-sm <?= $enabled ? 'btn-success' : 'btn-danger' ?>">
                                <i class="fas <?= $enabled ? 'fa-toggle-on' : 'fa-toggle-off' ?>"></i>
                            </button>
                        </form>
                        <a href="edit_user.php?id=<?= urlencode($user['id']) ?>" class="btn btn-sm btn-warning">
                            <i class="fas fa-edit"></i>
                        </a>
                        <a href="delete_user.php?id=<?= urlencode($user['id']) ?>" class="btn btn-sm btn-danger" onclick="return confirm('Are you sure you want to delete this user?');">
                            <i class="fas fa-trash-alt"></i>
                        </a>
                       
                    </td>
                    
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
<?php else: ?>
    <p>No users found.</p>
<?php endif; ?>

