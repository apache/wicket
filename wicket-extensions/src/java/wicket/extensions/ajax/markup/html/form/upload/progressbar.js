var wupb= {
	get : function(id) {
		return document.getElementById(id);
	},
	
	start : function() {
		wupb.get('${formMarkupId}').submit();
		wupb.get('${status-id}').innerHTML='Upload starting...';
	    wupb.get('${bar-id}').firstChild.firstChild.style.width='0%';
	    
		wupb.get('${status-id}').style.display='block';
	    wupb.get('${bar-id}').style.display='block';
	    
	    window.setTimeout(function() { wupb.ajax('${statusUrl}'); }, 1000);
	},
	
	ajax : function(url) {
		transport = false;

		if(window.XMLHttpRequest)
		{
			transport = new XMLHttpRequest();
			if(transport.overrideMimeType)
			{
				transport.overrideMimeType('text/xml');
			}
		}
		else if(window.ActiveXObject)
		{
			try
			{
				transport = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try
				{
					transport = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {}
			}
		}
		if(!transport)
		{
			alert('Error: could not create XMLHTTP object.');
			return false;
		}
	
		transport.onreadystatechange = function() { wupb.update(transport, url); };
		transport.open('GET', url, true);
		transport.send(null);
	},
	
	update: function(transport, url) {
		if (transport.readyState == 4) {

            if (transport.status == 200) {

                var update = transport.responseText.split('|');


                var completed_upload_size = update[2];
                var total_upload_size = update[3];
                var progressPercent = update[1];
                var transferRate = update[4];
                var timeRemaining = update[5];
                


                if ((completed_upload_size != "") && (completed_upload_size != 0))
                {
                    wupb.get('${bar-id}').firstChild.firstChild.style.width=progressPercent+'%';
                    wupb.get('${status-id}').innerHTML=progressPercent + '% finished, '
                            + completed_upload_size + ' of '
                            + total_upload_size + ' at '
                            + transferRate  
                            + "; " + timeRemaining;
                }

                if (progressPercent == 100)
                {

                    wupb.get('${bar-id}').firstChild.firstChild.style.width='100%';
                    
					wupb.get('${status-id}').style.display='none';
				    wupb.get('${bar-id}').style.display='none';

                    return null;
                }


                window.setTimeout(function() { wupb.ajax(url); }, 1000);
            } else {
                alert('Error: got a not-OK status code...');
            }
        }
	}
}
