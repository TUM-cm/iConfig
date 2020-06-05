<?php

include 'userService.php';

session_start();

if (!isset($_SESSION['email'])) {
	header("Location: login.php");
}

if (isset($_SESSION['email']) and !empty($_SESSION['email'])) {
	$user = getValidUser($_SESSION['email']);
	if($user == null) {
		header("Location: login.php");
	}
} else {
	header("Location: login.php");
}

?>