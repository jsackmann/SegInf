<?php
$path = "/var/www/downloads/";
if($_POST['digest']){
	$digest = $_POST['digest'];
	$found = false;
	foreach(scandir("$path") as $file){
		$fullPath = "$path/$file";
		if(is_file($fullPath)){
			if(sha1_file($fullPath)	=== $digest){
				$found = true;
				break;
			}
		}	
	}
	echo $found ? "OK" : "FOUND";
}else{
	$file = $_FILES["imageName"];

	$filename = $path . basename($file["name"]);

	if(move_uploaded_file($file["tmp_name"],$filename)){
		echo "Estas re duro amigo: $filename";
	}else{
		echo "Estas mas que rererere duro amigo: $filename";
	}
}

?>
