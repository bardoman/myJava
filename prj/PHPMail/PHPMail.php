
<?php
// The message
$message.= "This is a Cron message ";

//date_default_timezone_set('UTC');

date_default_timezone_set('America/Denver');


$dt = date(DATE_RFC2822);

$message.= $dt;

mail('bruceeifler@gmail.com', 'message', $message);
?>

