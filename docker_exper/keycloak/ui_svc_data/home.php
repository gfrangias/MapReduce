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
    <title>Job Submission Interface</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 class="text-center my-4">Job Submission Interface</h1>
        <form action="upload.php" method="post" enctype="multipart/form-data">
            <div class="mb-3">
                <label for="mapReduceFile" class="form-label">MapReduce File:</label>
                <input type="file" class="form-control" id="mapReduceFile" name="mapReduceFile" required>
            </div>
            <div class="mb-3">
                <label for="inputFile" class="form-label">Dataset File:</label>
                <input type="file" class="form-control" id="inputFile" name="inputFile" required>
            </div>
            <button type="submit" class="btn btn-primary" name="submit">Submit Job</button>
        </form>
        <h2 class="my-4">Submitted Files</h2>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th scope="col">File Name</th>
                    <th scope="col">Size (MB)</th>
                    <th scope="col">Action</th>
                </tr>
            </thead>
            <tbody>
                <?php include 'get_files.php'; ?>
            </tbody>
        </table>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js"></script>
</body>
</html>

