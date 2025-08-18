<?php
header("Content-Type: application/json");
error_reporting(0); // suppress warnings/notices

$conn = new mysqli("localhost", "root", "", "pathfinder_dashboard");
if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

if (!isset($_GET['ancestry']) || empty($_GET['ancestry'])) {
    echo json_encode([]);
    exit;
}

$ancestry = $_GET['ancestry'];
$stmt = $conn->prepare("SELECT feat_name, feat_level FROM ancestryfeat WHERE ancestry_name = ?");
$stmt->bind_param("s", $ancestry);
$stmt->execute();
$result = $stmt->get_result();

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
$stmt->close();
$conn->close();
?>
