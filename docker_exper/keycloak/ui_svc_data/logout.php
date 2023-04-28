<?php
    session_start();

    session_unset();
    session_destroy();
    
    echo '<script>window.location.replace("index.php");</script>';

?>