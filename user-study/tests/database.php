<?php 

$host = "vmott11.informatik.tu-muenchen.de";
$username = "beacon_study";
$password = "o4MNmlrbFwo1wgGo";
$dbname = "beacon_study";

mysqli_connect($host, $username, $password, $dbname);

// Check connection
if (mysqli_connect_errno()) {
	echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

?>