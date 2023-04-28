function showTime(){
    var date = new Date();
    var h = date.getHours(); // 0 - 23
    var m = date.getMinutes(); // 0 - 59
    var s = date.getSeconds(); // 0 - 59
    
    var uh = date.getUTCHours();
    var um = date.getUTCMinutes();
    var us = date.getUTCSeconds();
    
    h = (h < 10) ? "0" + h : h;
    m = (m < 10) ? "0" + m : m;
    s = (s < 10) ? "0" + s : s;

    uh = (uh < 10) ? "0" + uh : uh;
    um = (um < 10) ? "0" + um : um;
    us = (us < 10) ? "0" + us : us;

    
    var time = h + ":" + m + ":" + s + " ";
    var utime = uh + ":" + um + ":" + us + " ";
    document.getElementById('time').innerHTML = time; 
    document.getElementById('utctime').innerHTML = utime;

    setTimeout(showTime, 1000);
    
}

showTime();