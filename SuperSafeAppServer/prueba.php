<?php
$stuff = <<<EOF
{"sha1":"75344f5fe11a8f6615dc64cdec07e5541d6f4c82","pwd":"14xkljvUHcXqjN7M2szEROfjWQt3Ux7skA9vdE9i9HY=","filename":"dos.jpg"}
EOF;

echo extract(json_decode($stuff,true));
echo $sha1 . "\n" . $pwd . "\n" . $filename;


?>
