<?php
session_start();

function saveFile($file, $targetDir) {
  $filename = basename($file['name']);
  $targetFile = $targetDir . '/' . $filename;
  $i = 1;

  while (file_exists($targetFile)) {
    $filenameWithoutExt = pathinfo($filename, PATHINFO_FILENAME);
    $extension = pathinfo($filename, PATHINFO_EXTENSION);
    $filename = $filenameWithoutExt . '-' . $i . '.' . $extension;
    $targetFile = $targetDir . '/' . $filename;
    $i++;
  }

  move_uploaded_file($file['tmp_name'], $targetFile);
  return $filename;
}

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $username = $_SESSION['username'];
  $mapreduceDir = "uploads/{$username}/executables";
  $datasetDir = "uploads/{$username}/datasets";

  if (!file_exists($mapreduceDir)) {
    mkdir($mapreduceDir, 0777, true);
  }
  if (!file_exists($datasetDir)) {
    mkdir($datasetDir, 0777, true);
  }

  $mapreduceFilename = saveFile($_FILES['mapreduce-file'], $mapreduceDir);
  $datasetFilename = saveFile($_FILES['dataset-file'], $datasetDir);

  echo json_encode([
    'mapreduceFilename' => $mapreduceFilename,
    'datasetFilename' => $datasetFilename,
  ]);
} else {
  http_response_code(405);
  echo json_encode(['error' => 'Method Not Allowed']);
}