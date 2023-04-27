<?php
	ob_start();
	session_start();

	$auth_error = $blank_error = false;

	if(isset($_SESSION['loggedIn']) && $_SESSION['loggedIn'] == TRUE){
		header("Location: /home.php");
	}

	if($_SERVER['REQUEST_METHOD'] == "POST"){

		$username = $_POST['username'];
		$password = $_POST['password'];

		if(empty($username) || empty($password)){

			$blank_error = TRUE;

		}else{

			$keycloakBaseUrl = 'http://172.16.0.3:8080';
			$realm = 'master';
			$client_id = 'ui_svc';
			$client_secret  = '4MSqVdyVoEpaB5mpkRIDAGw07gYeHOri';

			$tokenEndpoint = $keycloakBaseUrl . '/auth/realms/' . $realm . '/protocol/openid-connect/token';

			$ch = curl_init($tokenEndpoint);

			$postData = http_build_query([
				'grant_type' => 'password',
				'client_id' => $client_id,
				'client_secret' => $client_secret,
				'username' => $username,
				'password' => $password,
			]);

			curl_setopt($ch, CURLOPT_POST, true);
			curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

			$response = curl_exec($ch);

			if (!$response) {
				echo "Error: " . curl_error($ch);
			} else {
				$decodedResponse = json_decode($response, true);
				if (isset($decodedResponse['access_token'])) {
					$_SESSION['authToken'] = $decodedResponse['access_token'];
					$_SESSION['loggedIn'] = TRUE;
					header("Location: home.php");
				} else {
					$auth_error = TRUE;
				}
			}
			curl_close($ch);
		}
	}

?>



<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="description" content="">
	<meta name="generator" content="Hugo 0.104.2">
	<title>MadReduce UI - Index</title>

	<link rel="icon" type="image/x-icon" href="assets/brand/ico.png">
	

	

<link href="assets/dist/css/bootstrap.min.css" rel="stylesheet">

	<style>
	.bd-placeholder-img {
		font-size: 1.125rem;
		text-anchor: middle;
		-webkit-user-select: none;
		-moz-user-select: none;
		user-select: none;
	}

	@media (min-width: 768px) {
		.bd-placeholder-img-lg {
		font-size: 3.5rem;
		}
	}

	.b-example-divider {
		height: 3rem;
		background-color: rgba(0, 0, 0, .1);
		border: solid rgba(0, 0, 0, .15);
		border-width: 1px 0;
		box-shadow: inset 0 .5em 1.5em rgba(0, 0, 0, .1), inset 0 .125em .5em rgba(0, 0, 0, .15);
	}

	.b-example-vr {
		flex-shrink: 0;
		width: 1.5rem;
		height: 100vh;
	}

	.bi {
		vertical-align: -.125em;
		fill: currentColor;
	}

	.nav-scroller {
		position: relative;
		z-index: 2;
		height: 2.75rem;
		overflow-y: hidden;
	}

	.nav-scroller .nav {
		display: flex;
		flex-wrap: nowrap;
		padding-bottom: 1rem;
		margin-top: -1px;
		overflow-x: auto;
		text-align: center;
		white-space: nowrap;
		-webkit-overflow-scrolling: touch;
	}
	</style>

	
	<!-- Custom styles for this template -->
	<link href="css/signin.css" rel="stylesheet">
</head>
<body class="text-center">
	
<main class="form-signin w-100 m-auto">
	<form method="POST">
		<img class="mb-1" src="assets/brand/logo.png" alt="Logo" width="300">
		<h1 class="h5 mb-3 fw-normal">Please enter your credentials</h1>

		<div class="form-floating">
		<input type="text" class="form-control" id="floatingInput" placeholder="name" name = "username">
		<label for="floatingInput">Username</label>
		</div>
		<div class="form-floating">	
		<input type="password" class="form-control" id="floatingPassword" placeholder="Password" name = "password">
		<label for="floatingPassword">Password</label>
		</div>

		<button class="w-100 btn btn-primary" type="submit">Sign in</button>

		<?php
			if($blank_error){
				echo '<div class="mt-3 alert alert-primary" role="alert">Fill in all required fields!</div>';
			}

			if($auth_error){
				echo '<div class="mt-3 alert alert-danger" role="alert">Wrong email or password!</div>';
			}
		?>
		

		<p class="mt-5 mb-3 text-muted">&copy;<script>document.write(new Date().getFullYear())</script> No copyrighting LOL </p>
		<p class="mt-0 mb-3 text-muted">Version God bless us</p>
		
	</form>
</main>


	
</body>
</html>


