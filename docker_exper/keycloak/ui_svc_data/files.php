<?php
session_start();
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Files</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/axios/0.21.1/axios.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
  <script src="https://cdn.datatables.net/1.10.25/js/jquery.dataTables.min.js"></script>


  <style>
  </style>
  <style>
  .table td,
  .table th {
    border-top: 0;
    padding: 0.15rem;
  }
</style>
<style>
    .table td,
    .table th {
      border-top: 0;
      padding: 0.10rem;
      vertical-align: middle;
    }
    .table thead th {
      text-align: center;
    }
    td {
      text-align: center;
    }

    .action-cell {
      display: flex;
      justify-content: center;
      align-items: center;
    }
    body {
          background-color: #f8f9fa;
      }

    .delete-wrapper {
      text-align: center;
    }
    .progress {
      margin-top: 1.5rem;
      margin-bottom: 1.5rem;
      background-color: #f8f9fa;
    }
    .progress.custom {
      background-color: #033894;
    }
    body {
        background-color: #ffffff;
    }
    .centered {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 30vh;
    }
    table {
        font-size: 0.93rem;
        background-color: none;
        border-collapse: separate;
        border-spacing: 0;
        width: 100%;
        margin-bottom: 0rem;
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
        align-items: center;
        justify-content: center;
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
    .table th.col-filename,
    .table td.col-filename {
      width: 50%;
    }

    .table th.col-size,
    .table td.col-size {
      width: 30%;
    }

    .table th.col-action,
    .table td.col-action {
      width: 20%;
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
        margin-top: -5rem; /* Adjust the margin value to move the table up or down */
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
    .rounded-box {
      background-color: #f8f9fa;
      border-radius: 15px;
      padding: 1.5rem;
      margin-bottom: 2rem;
    }

    .distance{
      margin-top: 4.5rem;
    }
   
    .table-wrapper {
      margin-bottom: 2rem;
      max-width: 100%;
      overflow-x: auto;
    }

    .vspace {
      margin-top: 1rem;
      margin-bottom: 1rem;
    }
    .vspace2 {
      margin-top: 3rem;
      margin-bottom: 3rem;
    }
    .button-container {
      display: flex;
      align-items: center;
    }

    .progress {
      margin-left: 1rem;
      background-color: #ffffff;
    }
    .btn-short {
      padding-top: 0.25rem;
      padding-bottom: 0.25rem;
      font-size: 0.875rem;
      line-height: 1.5;
    }
    .btn-upl {
      background-color: #033894;
      border-color: #033894;
    }

    .progress-bar {
      background-image: linear-gradient(
        45deg,
        #007bff 25%,
        transparent 25%,
        transparent 50%,
        #007bff 50%,
        #007bff 75%,
        transparent 75%,
        transparent
      );
      background-size: 200% 100%;
      animation: progressBarAnimation 2s linear infinite;
    }
    </style>
  
</style>
</head>
<div class="header d-flex align-items-center justify-content-between">
    <a href="home.php">
        <img src="/assets/brand/logo.png" alt="Logo" class="logo">
    </a>
    <nav>
         <ul class="nav">
            <?php if ($_SESSION['username'] == 'admin'): ?>
            <li class="nav-item">
                <a href="containers.php" class="nav-link">Container Manager</a>
            </li>
            <?php endif; ?>
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
  <div class="container">
  <div class="upload-box rounded-box">
  <h1 class="mb-4 table-title"><strong>File Management</strong></h1>
  <form>
    <div class="mb-3">
      <label for="mapreduce-file" class="form-label">MapReduce File</label>
      <input type="file" class="form-control" id="mapreduce-file" name="mapreduce-file" required>
    </div>
    <div class="mb-3">
      <label for="dataset-file" class="form-label">Dataset File</label>
      <input type="file" class="form-control" id="dataset-file" name="dataset-file" required>
    </div>
    <div class="button-container">
      <button id="upload-btn" class="btn btn-primary btn-upl" disabled='true' name="submit">Upload files</button>
      <div class="progress" style="width: 200px;">
        <div class="progress-bar" role="progressbar" id="prbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
      </div>
    </div>
  </form>
  <span id="username" class="invisible"><?php echo $_SESSION['username'] ?></span>
    <div class="vspace2"></div>

    
    <div class="container custom-container">
  <div class="table-wrapper">
    <h3 class="font-weight-bold text-center">MapReduce Files</h3>
    <table class="table table-striped" id="mapreduce-table">
          <thead>
          <tr>
            <th class="col-filename">Filename</th>
            <th class="col-size">Size</th>
            <th class="col-action">Action</th>
          </tr>
        </thead>
      <tbody>
        <!-- MapReduce file rows will be added here dynamically -->
      </tbody>
    </table>
  </div>

  <div class="vspace"></div>

  <div class="table-wrapper">
    <h3 class="font-weight-bold text-center">Dataset Files</h3>
    <table class="table table-striped" id="dataset-table">
    <thead>
    <tr>
      <th class="col-filename">Filename</th>
      <th class="col-size">Size</th>
      <th class="col-action">Action</th>
    </tr>
  </thead>
      <tbody>
        <!-- Dataset file rows will be added here dynamically -->
      </tbody>
    </table>
  </div>
</div>
  </div>
  
  
</div>
</body>
<script src="js/files.js"></script>
</body>
</html>