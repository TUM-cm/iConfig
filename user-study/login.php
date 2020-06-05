<?php

include 'userService.php';
require_once 'database.php';

session_start();

function setUserSession($user) {
	$_SESSION['user_id'] = $user->getID();
	$_SESSION['email'] = $user->getEmail();
}

if(isset($_POST['loginSubmit']) && $_POST['loginSubmit'] == true) {
	$email = trim($_POST['email']);
	if (filter_var($email, FILTER_VALIDATE_EMAIL)) {
		$user = getValidUser($email);
		if($user != null) {
			$database = new Database();
			$query = "SELECT completed FROM user WHERE id='" . $user->getID() . "'";
			$completed = $database->execute_query($query)[0]['completed'];
			
			if ($completed == 1) {
				exit("You have finished the study. Thank you for your help.");
			} else {
				setUserSession($user);
				header('Location: html/explanation.html');
			}
		}
	} else {
		echo "The E-Mail address is not valid.";
	}
}

?>

<html>
	<head>
		<style type="text/css">
			body {
				font-family:arial;
				font-weight:bold;
				color:white;
			}
			html {
				background: url('html/background.jpg') no-repeat center center fixed;
				-webkit-background-size:cover;
				-moz-background-size:cover;
				-o-background-size:cover;
				background-size:cover;
			}
			input[type="submit"] {
				font-family:arial;
				font-weight:bold;
			}
		</style>
	</head>

	<body>
		<form action="login.php" method="post">
		<input type="hidden" name="loginSubmit" value="true" />
			<table style="width:100%; margin:auto">
				<tr>
					<td align="center">
						<p><h2>IoT Management for Bluetooth Low Energy Beacons</h2></p>
					</td>
				</tr>
				
				<tr>
					<td align="center">
						<h4>E-Mail: <input type="text" name="email" autofocus="autofocus" /> <input type="submit" name="submit" value="START" /></h4>
					</td>
				</tr>
				
				<tr>
					<td align="center">
						<p><b>Chair of Connected Mobility<br> Technical University of Munich</b></p></p>
					</td>
				</tr>
		</form>
	</body>
</html>