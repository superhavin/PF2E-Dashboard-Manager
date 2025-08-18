<?php
header("Content-Type: application/json");

$pdo = new PDO("mysql:host=localhost;dbname=pathfinder_dashboard", "root", "");

$stmt = $pdo->query("SELECT ability_name FROM ability");

echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
?>
