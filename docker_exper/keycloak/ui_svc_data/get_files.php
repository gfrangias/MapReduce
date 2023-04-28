<?php
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}
if(!isset($_SESSION['authToken']) || !isset($_SESSION['loggedIn'])){
    header("Location: error.php");
}

verifyToken();

$uploadDirectory = 'uploads/'.$_SESSION['username'].'/';
if (!file_exists($uploadDirectory)) {
    mkdir($uploadDirectory, 0755, true);
}
$files = scandir($uploadDirectory);

foreach ($files as $file) {
    if ($file !== '.' && $file !== '..') {
        $filePath = $uploadDirectory . $file;
        $fileSize = filesize($filePath) / (1024 * 1024); // File size in MB
        echo "<tr>";
        echo "<td>{$file}</td>";
        echo "<td>" . number_format($fileSize, 2) . " MB</td>";
        echo "<td><form action='delete.php' method='post'><input type='hidden' name='fileToDelete' value='{$file}'><button type='submit' class='btn btn-danger btn-sm' name='delete'>Delete</button></form></td>";
        echo "</tr>";
    }
}
?>