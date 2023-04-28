<?php
    include 'functions.php';
    if (session_status() == PHP_SESSION_NONE) {
        session_start();
    }
    verifyToken();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MapReduce UI - User Management</title>

    <!-- Bootstrap CSS -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Add this line inside the <head> tag -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.3/dist/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

    <!-- Custom CSS -->
    <style>
    body {
        background-color: #f8f9fa;
    }
    .centered {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
    }
     .user-table {
        width: 100%;
        background-color: #e9ecef;
        border-radius: 15px;
        padding: 30px;
        overflow: hidden; /* Add this line to make the rounded corners visible */
    }
    table {
        background-color: none;
        border-collapse: separate;
        border-spacing: 0;
        width: 100%;
        margin-bottom: 1rem;
        border-radius: 5px;
        overflow: hidden;
    }

    /* Add these rules to apply rounded corners to the first and last rows in the table */
    table tr:first-child th:first-child {
        border-top-left-radius: 15px;
    }
    table tr:first-child th:last-child {
        border-top-right-radius: 15px;
    }
    table tr:last-child td:first-child {
        border-bottom-left-radius: 15px;
    }
    table tr:last-child td:last-child {
        border-bottom-right-radius: 15px;
    }
    th, td {
        padding: 0.75rem;
        text-align: center; /* Update this line to center the text */
    }
    thead {
        background-color: #033894;
        color: white;
    }
    tbody tr:nth-child(odd) {
        background-color: #f2f2f2;
    }
    tbody tr:nth-child(even) {
        background-color: #ffffff;
    }
    thead th {
        font-weight: bold;
    }

    .alert {
        padding: 1rem;
        border-radius: 5px;
    }
    .alert-info {
        background-color: #d1ecf1;
        border-color: #bee5eb;
        color: #0c5460;
    }
    .header {
        padding: 1rem;
        padding-left: 2rem; /* Add some padding to move the logo away from the left corner */
        padding-right: 2rem; /* Add some padding to move the user info away from the right corner */
    }
    .logo {
        max-height: 50px;
        max-width: 100%;
    }
    .user-info {
        display: flex;
        align-items: center;
        margin-right: 1rem; /* Add some margin to move the user info away from the right corner */
    }
    .user-info span {
        margin-right: 0.5rem;
    }
    .custom-container {
        margin-top: -10rem; /* Adjust the margin value to move the table up or down */
    }

    .activation-text {
        color: green;
    }
    .inactive-text {
        color: red;
    }
    .alert-container {
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 1000;
    }   
   
    </style>
</head>
<body>
    <div class="header d-flex align-items-center justify-content-between">
        <img src="/assets/brand/logo.png" alt="Logo" class="logo">
        <div class="user-info">
            <span>Welcome, <strong><?= htmlspecialchars($_SESSION['username']) ?></strong> | </span>
            <a href="logout.php" class="btn btn-secondary btn-sm">Logout</a>
        </div>
    </div>
    
    <div class="container centered">
        <div class="container custom-container">    
            <div class="user-table">
            <h1 class="mb-4 table-title"><strong>User Management</strong></h1>
            <div class="table-toolbar mb-4 d-flex justify-content-end">
            <a href="create_user.php" class="btn btn-success">Create user<i class="bi bi-plus-lg"></i></a>
            </div>
            <?php include 'get_all_users.php'; ?>

                <?php if (isset($_GET['msg_success'])): ?>
                    <div class="alert alert-success">
                        <button type="button" class="close" data-dismiss="alert">&times;</button>
                        <?= htmlspecialchars($_GET['msg_success']) ?>
                </div>
            <?php endif; ?>
            <?php if (isset($_GET['msg_fail'])): ?>
                    <div class="alert alert-danger">
                        <button type="button" class="close" data-dismiss="alert">&times;</button>
                        <?= htmlspecialchars($_GET['msg_fail']) ?>
                </div>
            <?php endif; ?>
            </div>
        </div>
    </div>

    <!-- Add this JavaScript code just before the closing </body> tag -->
    <script>
        const messageContainer = document.querySelector('.alert');
        if (messageContainer) {
            setTimeout(() => {
                messageContainer.style.display = 'none';
            }, 5000); // Hide the message container after 5 seconds
        }
    </script>
</body>
</html>