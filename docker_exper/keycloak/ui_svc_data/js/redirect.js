window.onload = function(){
    function pageRedirect(){
        var delay = 5000; // time in ms
               
        setTimeout(function(){
         window.location = "../index.php";
        },delay);
        
    }
    pageRedirect();
}

$(document).ready(function (param) {  

    $('#redmsg').delay(1000).fadeOut(0, function(){
                                $('#redmsg').text('You will be redirected in 2 seconds...').show().delay(1000)
                                .fadeOut(0, function(){
                                    $('#redmsg').text('You will be redirected in 1 second...').show().delay(1500)
                                        .fadeOut(0, function(){
                                            $('#redmsg').text('Redirecting you...').show()
                                        })
                                            })
                            })    
})