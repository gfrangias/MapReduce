<?php


    function verifyToken(bool $isAdminTokenToRefresh = false){


        if(!isset($_SESSION['authToken']) || !isset($_SESSION['refreshToken'])){
            session_destroy();
            session_unset();
            echo '<script>window.location.replace("error.php?s=1");</script>';
        }
        $url = 'http://172.16.0.3:8080/auth/realms/master/protocol/openid-connect/token';
        $client_id = 'ui_svc';
        $client_secret = 'eVjxA3mr9Uyet48lgEWmnZddkEzmwu5K';
        $refresh_token = $_SESSION['refreshToken'];


        $data = array(
            'client_id' => $client_id,
            'client_secret' => $client_secret,
            'refresh_token' => $refresh_token,
            'grant_type' => 'refresh_token'
        );

        if($isAdminTokenToRefresh){
            $data = array(
                'client_id' => 'admin-cli',
                'refresh_token' => $_SESSION['adminRefreshToken'],
                'grant_type' => 'refresh_token'
            );
        }

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($data));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        curl_close($ch);

        if(curl_errno($ch)) {
            $error_msg = curl_error($ch);
        }
        
        curl_close($ch);
        
        $token = json_decode($response);
        
        if(!$token || !isset($token->access_token) || !isset($token->refresh_token)) {
            session_destroy();
            session_unset();
            echo '<script>window.location.replace("error.php?s=1");</script>';
        }
        
        $access_token = $token->access_token;

        if($isAdminTokenToRefresh){
            $_SESSION['adminToken'] = $access_token;
            return;
        }        


        $_SESSION['authToken'] = $access_token;
        
    }

    function getTokenInfo($accessToken) {
        $ch = curl_init();
    
        $url = 'http://172.16.0.3:8080/auth/realms/master/protocol/openid-connect/userinfo';
    
        $headers = array(
            'Authorization: Bearer ' . $accessToken,
        );
    
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
        $result = curl_exec($ch);
    
        if (curl_errno($ch)) {
            echo 'Error:' . curl_error($ch);
        } else {
            $response = json_decode($result);
            $userId = $response->sub;
            return $userId;
        }
    }

    function isUserAdmin($userId, $token) {
        // Set Keycloak admin credentials
        $keycloakUrl = "http://172.16.0.3:8080/auth";
        $realm = "master";
    
        // Get user roles
        $userUrl = $keycloakUrl . "/admin/realms/" . $realm . "/users/" . $userId . "/role-mappings/realm";
        $ch = curl_init($userUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            "Authorization: Bearer " . $token
        ]);
        $userResult = curl_exec($ch);
        curl_close($ch);
        $userRoles = json_decode($userResult, true);
    
        // Check if user is admin
        $isAdmin = false;
        foreach ($userRoles as $role) {
            if ($role["name"] == "admin") {
                $isAdmin = true;
                break;
            }
        }
    
        return $isAdmin;
    }


    function acquireToken(){
    
        $curl = curl_init();

        curl_setopt_array($curl, array(
          CURLOPT_URL => 'http://172.16.0.3:8080/auth/realms/master/protocol/openid-connect/token',
          CURLOPT_RETURNTRANSFER => true,
          CURLOPT_FOLLOWLOCATION => true,
          CURLOPT_POST => true,
          CURLOPT_POSTFIELDS => http_build_query(array(
            'client_id' => 'ui_svc',
            'client_secret' => 'vaC19724yiUpgcWYjfGAc6i9xvgwkdhf',
            'grant_type' => 'client_credentials',
          )),
          CURLOPT_HTTPHEADER => array(
            'Content-Type: application/x-www-form-urlencoded',
          ),
        ));
        
        $response = curl_exec($curl);
        
        if (curl_errno($curl)) {
          $error_msg = curl_error($curl);
          exit();
        }
        
        curl_close($curl);

        return json_decode($response,true);
    }
    
?>