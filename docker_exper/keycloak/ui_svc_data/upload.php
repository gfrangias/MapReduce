<?php
include 'functions.php';
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
if(!isset($_SESSION['authToken']) || !isset($_SESSION['loggedIn'])){
    header("Location: error.php");
}

verifyToken();

if (isset($_POST['submit'])) {
    $mapReduceFile = $_FILES['mapReduceFile'];
    $inputFile = $_FILES['inputFile'];

    $uploadDirectory = 'uploads/'.$_SESSION['username'].'/';
    if (!file_exists($uploadDirectory)) {
        mkdir($uploadDirectory, 0755, true);
    }
    $mapReduceFilePath = $uploadDirectory . basename($mapReduceFile['name']);
    $inputFilePath = $uploadDirectory . basename($inputFile['name']);

    echo $mapReduceFilePath;
    

    if (move_uploaded_file($mapReduceFile['tmp_name'], $mapReduceFilePath) &&
        move_uploaded_file($inputFile['tmp_name'], $inputFilePath)) {
        echo "Job files uploaded successfully!";
    } else {
        echo "An error occurred while uploading the job files. Please try again.";
    }
} else {
    header("Location: index.php");
}
?>
