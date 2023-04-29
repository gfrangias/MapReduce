<?php
    include 'functions.php';
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }

    if(!isset($_SESSION['authToken']) || !isset($_SESSION['loggedIn'])){
        header("Location: error.php");
    }
    
    verifyToken();
?>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MapReduce UI - Home</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/axios/0.21.1/axios.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
<style>
      .logo {
        max-height: 50px;
        max-width: 100%;
    }
    .header {
        padding: 1rem;
        padding-left: 2rem; /* Add some padding to move the logo away from the left corner */
        padding-right: 2rem; /* Add some padding to move the user info away from the right corner */
    }
</style>
</head>
<body>
<div class="header d-flex align-items-center justify-content-between">
    <a href="home.php">
        <img src="/assets/brand/logo.png" alt="Logo" class="logo">
    </a>
    <nav>
        <ul class="nav">
            <li class="nav-item">
                <a href="files.php" class="nav-link">File Manager</a>
            </li>
            <?php if ($_SESSION['username'] == 'admin'): ?>
            <li class="nav-item">
                <a href="keycloak_user_management.php" class="nav-link">User Manager</a>
            </li>
            <?php endif; ?>
        </ul>
    </nav>
    <div class="user-info">
        <span>Welcome, <strong><?= htmlspecialchars($_SESSION['username']) ?></strong> | </span>
        <a href="logout.php" class="btn btn-secondary btn-sm">Logout</a>
    </div>
</div>
</body>
</html>

