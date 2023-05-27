<?php
session_start();

function listFiles($dir) {
  $files = array();
  if (is_dir($dir)) {
    if ($dh = opendir($dir)) {
      while (($file = readdir($dh)) !== false) {
        if ($file != '.' && $file != '..') {
          $filePath = $dir . '/' . $file;
          $files[] = array(
            'name' => $file,
            'size' => filesize($filePath),
            'path' => $dir.'/'.$file,
            'path' => $dir.'/'.$file
          );
        }
      }
      closedir($dh);
    }
  }
  return $files;
}

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
  $username = $_SESSION['username'];
  $mapreduceDir = "uploads/{$username}/executables";
  $datasetDir = "uploads/{$username}/datasets";

  echo json_encode([
    'mapreduceFiles' => listFiles($mapreduceDir),
    'datasetFiles' => listFiles($datasetDir),
  ]);
} else {
  http_response_code(405);
  echo json_encode(['error' => 'Method Not Allowed']);
}