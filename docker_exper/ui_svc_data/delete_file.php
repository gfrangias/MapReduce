<?php
session_start();

function deleteFile($dir, $fileName) {
  $filePath = $dir . '/' . $fileName;
  if (file_exists($filePath)) {
    unlink($filePath);
  }
}

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $data = json_decode(file_get_contents('php://input'), true);
  $username = $_SESSION['username'];
  $fileType = $data['fileType'];
  $fileName = $data['fileName'];

  if ($fileType === 'mapreduce-table') {
    $dir = "uploads/{$username}/mapreduce";
  } elseif ($fileType === 'dataset-table') {
    $dir = "uploads/{$username}/mareduce";
  } else {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid fileType']);
    exit;
  }

  deleteFile($dir, $fileName);

  echo json_encode(['success' => true]);
} else {
  http_response_code(405);
  echo json_encode(['error' => 'Method Not Allowed']);
}