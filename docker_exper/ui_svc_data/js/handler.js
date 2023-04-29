function signout(){

    $.ajax({
        type: "post",
        url: "auth/signout.php",
        data: {
        },
        success: function (response) {
           console.log("Signing out...");
        }
    });
}