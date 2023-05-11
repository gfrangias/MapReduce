<?php
if (isset($_POST['delete'])) {
    $fileToDelete = $_POST['fileToDelete'];
    $uploadDirectory = 'uploads/';
    $filePath = $uploadDirectory . $fileToDelete;

    if (file_exists($filePath)) {
        if (unlink($filePath)) {
            echo "The file '{$fileToDelete}' has been deleted.";
        } else {
            echo "An error occurred while deleting the file. Please try again.";
        }
    } else {
        echo "The file '{$fileToDelete}' does not exist.";
    }
} else {
    header("Location: index.php");
}
?>