<?php
  include 'functions.php';
  if (session_status() == PHP_SESSION_NONE) {
      session_start();
  }

  function retrieveResult($jobId) {
    $filePath = './uploads/results/'.$_SESSION['username'].'/'.$jobId.'/output.json';
    if (file_exists($filePath) && is_readable($filePath)) {
        // Read the file contents
        $fileContents = file_get_contents($filePath);
        // Output the file contents into the text box
        echo $fileContents;
    }
  }

  if(isset($_GET['jid']) && !empty($_GET['jid'])){
    echo retrieveResult($_GET['jid']);
  }
?>