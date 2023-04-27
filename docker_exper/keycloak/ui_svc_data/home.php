<?php
    session_start();

    if(!isset($_SESSION['authToken']) || !isset($_SESSION['loggedIn'])){
        header("Location: error.php");
    }
    

    echo 'Ouzo page 123344444';
?>