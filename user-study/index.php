<?php

include 'userAuthentication.php';
require_once 'database.php';

session_start();

if (isset($_SESSION['email']) and !empty($_SESSION['email'])) {
	$database = new Database();
	$query = "SELECT completed FROM user WHERE id='" . $_SESSION['user_id'] . "'";
	$completed = $database->execute_query($query)[0]['completed'];
	if ($completed == 1) {
		exit("You have finished the study. Thank you for your help.");
	} else {
		header('Location: html/explanation.html');
	}
	$database->close();
} else {
	header("Location: login.php");
}

?>