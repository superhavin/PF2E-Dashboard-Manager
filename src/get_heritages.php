<?php
// get_heritages.php
header("Content-Type: application/json");

// DB connection (same as get_ancestries.php)
$conn = new mysqli("localhost", "root", "", "pathfinder_dashboard");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// make sure ancestry was passed
if (!isset($_GET['ancestry']) || empty($_GET['ancestry'])) {
    echo json_encode([]);
    exit;
}

$ancestry = $_GET['ancestry'];

// query only heritages that belong to this ancestry
$stmt = $conn->prepare("SELECT heritage_name FROM ancestryheritage WHERE ancestry_name = ?");
$stmt->bind_param("s", $ancestry);
$stmt->execute();
$result = $stmt->get_result();

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row; // each row is {"heritage_name": "Stone Dwarf"}
}

echo json_encode($data);

$stmt->close();
$conn->close();
?>
