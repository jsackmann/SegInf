<?php
	function get_db(){
		$dsn = 'mysql:dbname=tcbpg06;host=127.0.0.1';
		$user = "tcbpg06";
		$pass = "_holaseginf_";
		try{
			return new PDO($dsn,$user,$pass);
		}catch(PDOException $e){
			die("Connection with database failed: " . $e->getMessage());
		}
	}
?>
