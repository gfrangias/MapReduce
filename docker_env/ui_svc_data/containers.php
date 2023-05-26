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
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MapReduce UI - Container Manager</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.4/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js"></script>
	<link rel="icon" type="image/x-icon" href="assets/brand/ico.png">


    <!-- Custom CSS -->
    <style>
        body {
            padding: 30px;
        }
        .small-text {
            font-size: 0.8rem; /* Adjust the value to your desired font size */
        }
        .tight-rows td {
            padding-top: 0.25rem;
            padding-bottom: 0.25rem;
        }
        .green-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: green;
            border-radius: 50%;
            margin-right: 5px;
        }
        .yellow-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #ffc107;
            border-radius: 50%;
            margin-right: 5px;
        }
        .blue-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #0D6EFD;
            border-radius: 50%;
            margin-right: 5px;
        }
        .red-bullet {
            display: inline-block;
            width: 8px;
            height: 8px;
            background-color: #DC3545;
            border-radius: 50%;
            margin-right: 5px;
        }
        .btn-act {
            margin-right: 5px;
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
    .btn-blk {
        background-color: black;
        border-color: black;
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
</head>
<body>
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
        <div class="container">
            <div class="upload-box rounded-box">
                <h1 class="mb-4 table-title"><strong>Container Management</strong></h1>
                <div class="table-toolbar mb-4 d-flex justify-content-end">
                    <button class='btn btn-success btn-sm mr-1 btn-act' onclick='createWorker()'>Deploy New Worker</button>
                    <button class='btn btn-success btn-sm mr-1 btn-act' onclick='createMonitor()'>Deploy New Monitor</button>
                </div>        


                 <div id="containerList" class="table-responsive"></div>
            </div>
        </div>
    </div>
    
    <!-- JavaScript -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <script>
        function fetchContainers() {
            $.ajax({
                type: 'GET',
                url: 'fetch_containers.php',
                success: function(response) {
                    $('#containerList').html(response);
                }
            });
        }

        $(document).ready(function() {
            fetchContainers();
            setInterval(fetchContainers, 3000); // Fetch every 3 seconds
        });
        function stopContainer(containerId) {
            $.ajax({
                type: 'POST',
                url: 'stop_container.php',
                data: {
                    container_id: containerId
                },
                success: function(response) {
                    if (response === 'success') {
                        fetchContainers();
                    } else {
                        alert('An error occurred while stopping the container.');
                    }
                }
            });
        }
    </script>
    <script>
        function stopContainer(containerId) {
            postRequest('containerScripts/stop_container.php', { container_id: containerId });
        }

        function pauseContainer(containerId) {
            postRequest('containerScripts/pause_container.php', { container_id: containerId });
        }

        function restartContainer(containerId) {
            postRequest('containerScripts/restart_container.php', { container_id: containerId });
        }

        function unpauseContainer(container_id) {
            postRequest('containerScripts/unpause_container.php', {container_id: container_id});
        }
        function createWorker(image_name) {
            postRequest('containerScripts/deploy_worker.php', {imageName: image_name});
        }
        function createMonitor(image_name) {
            postRequest('containerScripts/deploy_monitor.php', {imageName: image_name});
        }
        function deleteContainer(container_id) {
            postRequest('containerScripts/delete_container.php', {container_id: container_id});
        }

        function postRequest(url, data) {
            // Create a new FormData object
            const formData = new FormData();

            // Add the data properties to the FormData object
            for (const [key, value] of Object.entries(data)) {
                formData.append(key, value);
            }

            // Create a new XMLHttpRequest object to handle the POST request
            const xhr = new XMLHttpRequest();

            // Set up the POST request to the specified URL
            xhr.open('POST', url, true);

            // Set up the request headers
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

            // Handle the response
            xhr.onload = function () {
                if (xhr.status >= 200 && xhr.status < 400) {
                    // Success: Print the response to the console
                    console.log(xhr.responseText);
                    // Refresh the container list
                    fetchContainers();
                } else {
                    // Error: Print the error message to the console
                    console.error('Error in postRequest:', xhr.statusText);
                }
            };

            // Handle request errors
            xhr.onerror = function () {
                console.error('Request failed:', xhr.statusText);
            };

            // Send the POST request with the FormData object
            xhr.send(formData);
        }

        function showLog(id) {
            var win = window.open('', 'Container Log ' + id, 'width=500,height=500');
            win.document.title = "Log File for container " + id;

            // Check if this window has been opened before
            if(win.document.getElementsByTagName('textarea').length === 0) {
                var link = document.createElement('link');
                link.rel = 'stylesheet';
                link.href = 'https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css'; // Change this to your local bootstrap CSS file if needed
                win.document.head.appendChild(link);

                var container = win.document.createElement('div');
                container.className = 'container p-3';

                var textarea = win.document.createElement('textarea');
                textarea.className = 'form-control mb-3';
                textarea.readOnly = true;
                textarea.style.resize = 'none'; // Disable manual resize
                textarea.style.height = '80%'; // Set initial height
                textarea.style.overflow = 'auto'; // Enable scroll bar
                container.appendChild(textarea);

                var buttonContainer = win.document.createElement('div');
                buttonContainer.className = 'd-flex justify-content-end';
                container.appendChild(buttonContainer);

                var refreshButton = win.document.createElement('button');
                refreshButton.textContent = 'Refresh';
                refreshButton.className = 'btn btn-primary mr-2';
                refreshButton.onclick = function() {
                    fetchLogs(id, textarea);
                }
                buttonContainer.appendChild(refreshButton);

                var closeButton = win.document.createElement('button');
                closeButton.textContent = 'Close';
                closeButton.className = 'btn btn-secondary';
                closeButton.onclick = function() {
                    win.close();
                }
                buttonContainer.appendChild(closeButton);

                win.document.body.appendChild(container);

                fetchLogs(id, textarea);
            } else {
                // If the window is already opened, simply refresh the logs
                var textarea = win.document.getElementsByTagName('textarea')[0];
                fetchLogs(id, textarea);
            }
        }

        function fetchLogs(id, textarea) {
            fetch('containerScripts/fetch_containerlog.php?id=' + id)
            .then(response => response.text())
            .then(logText => {
                // Remove non-printable characters
                logText = logText.replace(/[^ -~\s]/g, '');

                // Update the textarea
                textarea.textContent = logText;

                // Adjust textarea height based on content
                textarea.style.height = '';
                textarea.style.height = textarea.scrollHeight + 'px';
            });
        }
</script>
</body>
</html>