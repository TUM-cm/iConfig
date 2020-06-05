<?php

require_once 'database.php';

class User {
	
	private $id;
	private $email;
	
	function __construct($id, $email) {
		$this->id = $id;
		$this->email = $email;
	}
		
	function getID() {
		return $this->id;
	}
		
	function getEmail() {
		return $this->email;
	}
}

// Returns all users in the structure of the class user
function getUsers($database) {
	$users = array();
	$query = "SELECT * FROM user";
	$result_users = $database->execute_query($query);
	for($i = 0; $i < sizeof($result_users); $i++) {
		$users[] = new User($result_users[$i]['id'], $result_users[$i]['email']);
	}		
	return $users;
}
	
/* Returns the user, if it exists in the table user.	
	Otherwise the user is automatically created. */
function getValidUser($email) {
	$database = new Database();
	$users = getUsers($database);
	// Return existing user via e-mail
	foreach($users as $user) {
		if($email == $user->getEmail()) {
			return $user;
		}
	}
	// Create new user automatically and get id
	$completed = 0;
	$query = "INSERT INTO user (email, completed) VALUES ('" . $email . "', " . $completed . ")";
	$database->insert($query);
	$query = "SELECT * FROM user WHERE email='" . $email . "'";
	$result_user = $database->execute_query($query);
	$user = new User($result_user[0]['id'], $result_user[0]['email']);
	return $user;
}

?>