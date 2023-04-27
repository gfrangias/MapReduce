<?php

    function verifyToken(){
        $url = 'http://172.16.0.3/auth/realms/master/protocol/openid-connect/token';
        $client_id = 'ui_svc';
        $client_secret = '4MSqVdyVoEpaB5mpkRIDAGw07gYeHOri';
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
        }
        
        $access_token = $token->access_token;

        $_SESSION['authToken'] = $access_token;
        
    }

?>