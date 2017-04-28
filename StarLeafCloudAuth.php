<?php
//settings
$username = "<your StarLeaf portal account email address>";
$password = "<your StarLeaf portal password>";
$api = "https://api.starleaf.com/v1/";
$cookiefile = dirname(__FILE__).'/cookie.txt';


//request challenge, salt and iterations
$challenge_url = $api."challenge?username=".urlencode($username);


$ch = curl_init();
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_URL,$challenge_url);
$result=curl_exec($ch);
curl_close($ch);
$challenge_values = json_decode($result, true);


//convert hex representations to binary
$bin_salt = hex2bin($challenge_values['salt']);
$bin_challenge = hex2bin($challenge_values['challenge']);


//generate key, 256 bit binary number
$key = hash_pbkdf2 ( "sha256"  ,$password, $bin_salt , $challenge_values['iterations'], 32, true );


//generate response
$response = hash_hmac( "sha256" , $bin_challenge, $key);


//put them into an array
$authenticate_data = array( "username" => $username, "response" => $response );
//turn it into JSON
$authenticate_data_string = json_encode( $authenticate_data );


//send authenticate POST request
$authenticate_url = $api."authenticate";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL,$authenticate_url);
curl_setopt($ch, CURLOPT_POST, 1);                                                                  
curl_setopt($ch, CURLOPT_POSTFIELDS, $authenticate_data_string);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookiefile);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookiefile);

$result=curl_exec($ch);
$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);


//gives NONE on success otherwise prints out failure message
var_dump(json_decode($result,true));
//gives 204 on success
echo("Got HTTP response code of :".$code."\n");


//list my account features
$features_url = $api."features";

$ch = curl_init();
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_URL, $features_url);
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookiefile);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookiefile);

$result=curl_exec($ch);
$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

var_dump(json_decode($result,true));
echo("Got HTTP response code of :".$code."\n");
?>