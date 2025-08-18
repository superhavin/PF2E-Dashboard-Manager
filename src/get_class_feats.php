<?php
header("Content-Type: application/json");

// Connect to database
$conn = new mysqli("localhost", "root", "", "pathfinder_dashboard");

// Check connection
if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

// Query class feats
$sql = "SELECT feat_name, class_name FROM classfeat WHERE feat_level = 1";  // Optional: filter by level otherwise delete WHERE feat_level = 1
$result = $conn->query($sql);

$data = [];

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $class = $row["class_name"];
        $feat = $row["feat_name"];

        if (!isset($data[$class])) {
            $data[$class] = [];
        }

        $data[$class][] = $feat;
    }
}

echo json_encode($data);
$conn->close();
