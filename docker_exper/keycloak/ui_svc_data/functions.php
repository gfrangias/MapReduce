<?php


    function verifyToken(){
        if(!isset($_SESSION['authToken']) || !isset($_SESSION['refreshToken'])){
            session_destroy();
            session_unset();
            echo '<script>window.location.replace("error.php?s=1");</script>';
        }
        $url = 'http://172.16.0.3:8080/auth/realms/master/protocol/openid-connect/token';
        $client_id = 'ui_svc';
        $client_secret = 'kw3rfyIlfTU0hVuUVyGt5DSxx4s3MZbC';
        $refresh_token = $_SESSION['refreshToken'];

        $data = array(
            'client_id' => $client_id,
            'client_secret' => $client_secret,
            'refresh_token' => $refresh_token,
            'grant_type' => 'refresh_token'
        );

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

        $_SESSION['authToken'] = $access_token;
        
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
    
?>