<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header("Content-Type: application/json");

// Connection to MySQL
$conn = new mysqli("localhost", "root", "", "pathfinder_dashboard");

// Check connection
if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Query table
$result = $conn->query("SELECT * FROM skill");
if(!$result){
    die(json_encode(["error" => $conn->error]));
}

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
?>
