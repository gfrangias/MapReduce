<?php
    session_start();
    session_unset();
    session_destroy();
?>


<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="generator" content="Hugo 0.104.2">
    <title>MapReduce UI · Authentication Error</title>

    <link rel="icon" type="image/x-icon" href="../assets/brand/ico.png">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://code.jquery.com/ui/1.13.0/jquery-ui.js"></script>
    <script src="js/redirect.js"></script>
    

<link href="../assets/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
    .bd-placeholder-img {
        font-size: 1.125rem;
        text-anchor: middle;
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
    }

    @media (min-width: 768px) {
        .bd-placeholder-img-lg {
        font-size: 3.5rem;
        }
    }

    .b-example-divider {
        height: 3rem;
        background-color: rgba(0, 0, 0, .1);
        border: solid rgba(0, 0, 0, .15);
        border-width: 1px 0;
        box-shadow: inset 0 .5em 1.5em rgba(0, 0, 0, .1), inset 0 .125em .5em rgba(0, 0, 0, .15);
    }

    .b-example-vr {
        flex-shrink: 0;
        width: 1.5rem;
        height: 100vh;
    }

    .bi {
        vertical-align: -.125em;
        fill: currentColor;
    }

    .nav-scroller {
        position: relative;
        z-index: 2;
        height: 2.75rem;
        overflow-y: hidden;
    }

    .nav-scroller .nav {
        display: flex;
        flex-wrap: nowrap;
        padding-bottom: 1rem;
        margin-top: -1px;
        overflow-x: auto;
        text-align: center;
        white-space: nowrap;
        -webkit-overflow-scrolling: touch;
    }
    </style>

    
    <!-- Custom styles for this template -->
    <link href="../css/signin.css" rel="stylesheet">
</head>
<body class="text-center">
    
<main class="form-signin w-100 m-auto">
    <form>
        <img class="mb-1" src="../assets/brand/logo.png" alt="Logo" width="300">
        
        <div class="form-floating">
            <div class="alert alert-danger mt-3"> <?php if(!isset($_GET['s'])){ 
                                                            echo "<strong>You need to sign in first in order to access this page! </strong> <br> Error: 0x0010";
                                                        }else{
                                                            echo "<strong> Your session expired! Keep in mind each session is active for 4 hours </strong> <br> Error: 0x0011";
                                                        }
                                                  ?>
            </div>
        </div>

        <div id="redmsg" class="text-muted">You will be redirected in 3 seconds...</div>
        <p class="mt-5 mb-3 text-muted">&copy;<script>document.write(new Date().getFullYear())</script>  No copyrighting LOL </p>
        <p class="mt-0 mb-3 text-muted">Version God bless us</p>
        
    </form>
</main>


    
</body>
</html>


