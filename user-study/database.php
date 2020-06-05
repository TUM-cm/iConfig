<?php

class Database {
	
	private $con;
	
	public function __construct() {
		$host = "vmott11.informatik.tu-muenchen.de";
		$username = "beacon_study";
		$password = "o4MNmlrbFwo1wgGo";
		$dbname = "beacon_study";
		if ($this->con == null) {
			$this->con = mysqli_connect($host, $username, $password, $dbname);
		}
		if (mysqli_connect_errno()) {
			echo "Failed to connect to MySQL: " . mysqli_connect_error();
		}
	}
	
	public function execute_query($query) {		
		$result = mysqli_query($this->con, $query);
		$rows = array();
		while($row = mysqli_fetch_array($result, MYSQL_ASSOC)) {
			$rows[] = $row;
		}
		return $rows;
	}
	
	public function update($query) {
		$result = mysqli_query($this->con, $query);
	}
	
	public function delete($query) {
		$result = mysqli_query($this->con, $query);
	}
	
	public function insert($query) {
		$result = mysqli_query($this->con, $query);
	}
	
	public function if_exists($query) {
		$result = mysqli_query($this->con, $query);
		$res = mysqli_fetch_array($result);
		if ($res[0] > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public function close() {
		mysqli_close($this->con);
	}
	
}

?>