function registerAutoassist(id, callbackUrl) {
    var foo = function() {
        var tt = new AutoAssist(id, {setRequestOptions: function() {
                var pars = "val=" + this.txtBox.value;
                return { url: callbackUrl, parameters: pars };
            }});
    }
    Event.observe(window, "load", foo);
}