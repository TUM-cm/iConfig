<?php

require_once 'database.php';
$database = new Database();

session_start();

if (isset($_SESSION['user_id']) and isset($_POST['submit'])) {
	
	// Forward to questions
	if (isset($_POST['next_html']) and $_POST['next_html'] == "part1.html") {
		header('Location: html/' . $_POST['next_html']);
	}
	
	$query = "SELECT COUNT(*) FROM result WHERE user_id='" . $_SESSION['user_id'] . "'";
	$exists = $database->if_exists($query);
	
	if ($exists) {
		// Update
		$next = false;
		$values = "";
		foreach ($_POST as $field => $value) {
			if ($field != "submit" and $field != 'next_html') {
				if ($next) {
					$values .= ",";
				}
				$values .= $field . "='" . $value . "'";
				$next = true;
			}
		}
		$query = "UPDATE result SET " . $values . " WHERE user_id='" . $_SESSION['user_id'] . "'";		
		$database->update($query);
	} else {
		// Insert
		$fields = "user_id";
		$values = "'" . $_SESSION['user_id'] . "'";
		foreach ($_POST as $field => $value) {			
			if ($field != "submit" and $field != 'next_html') {				
				$fields .= "," . $field;
				$values .= ",'". $value . "'";
			}
		}
		$query = "INSERT INTO result (" . $fields . ") VALUES (" . $values . ")";		
		$database->insert($query);
	}
	
	$query = "SELECT * FROM result where user_id='" . $_SESSION['user_id'] . "'";	
	$result = $database->execute_query($query)[0];
	
	/*print_r($result);
	echo "<br>";*/
	
	$completed = true;
	$ignore_fields = array("start_manual_config_1", "end_manual_config_1", "success_rate_manual_config_1", "error_rate_manual_config_1", "error_fields_manual_config_1",
			"start_manual_config_2", "end_manual_config_2", "success_rate_manual_config_2", "error_rate_manual_config_2", "error_fields_manual_config_2",
			"start_manual_config_3", "end_manual_config_3", "success_rate_manual_config_3", "error_rate_manual_config_3", "error_fields_manual_config_3",
			"start_automatic_config_1", "end_automatic_config_1", "success_rate_automatic_config_1", "error_rate_automatic_config_1", "error_fields_automatic_config_1",
			"start_automatic_config_2", "end_automatic_config_2", "success_rate_automatic_config_2", "error_rate_automatic_config_2", "error_fields_automatic_config_2",
			"start_automatic_config_3", "end_automatic_config_3", "success_rate_automatic_config_3", "error_rate_automatic_config_3", "error_fields_automatic_config_3");
	
	foreach ($result as $key => $value) {
		
		/*echo "key: ";
		echo $key;
		echo "<br>";
		
		echo "value: ";
		echo $value;
		echo "<br>";*/
		
		$check = true;
		foreach ($ignore_fields as $ignore_field) {
			if ($key == $ignore_field) {
				$check = false;
			}
		}
		
		/*echo "check: ";
		echo (int)$check;
		echo "<br>";*/
		
		if ($check) {
			if (!isset($value) || trim($value)==='') {	
				$completed = false;
				break;
			} else if (is_numeric($value) and ($value < 1 or $value > 5)) {
				$completed = false;
				break;
			}
		}
		
		/*echo "completed: ";
		echo (int)$completed;
		echo "<br>";*/
	}
	
	if (!$completed) {
		header('Location: html/' . $_POST['next_html']);
	} else {
		$query = "UPDATE user SET completed='1' WHERE id='" . $_SESSION['user_id'] . "'";		
		$database->update($query);
		$database->close();
		$_SESSION['user_id'] = '';
		$_SESSION['email'] = '';
		session_destroy();		
		exit("You have finished the study. Thank you for your help.");
	}

}

?>