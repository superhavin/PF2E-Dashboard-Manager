<?php
header("Content-Type: application/json");

//connection to MySQL
$conn = new mysqli("localhost", "root", "", "pathfinder_dashboard");

//check connection
if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Query table
$result = $conn->query("SELECT * FROM background");
$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
?>
