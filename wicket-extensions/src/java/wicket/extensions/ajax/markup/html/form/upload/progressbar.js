<script language="JavaScript">
var theRequest = false;
var total_upload_size = 1;


function goajax(page)
{
	theRequest = false;

	if(window.XMLHttpRequest)
	{
		theRequest = new XMLHttpRequest();
		if(theRequest.overrideMimeType)
		{
			theRequest.overrideMimeType('text/xml');
		}
	}
	else if(window.ActiveXObject)
	{
		try
		{
			theRequest = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try
			{
				theRequest = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {}
		}
	}
	if(!theRequest)
	{
		alert('Error: could not create XMLHTTP object.');
		return false;
	}

	theRequest.onreadystatechange = updateProgress;
	theRequest.open('GET', page, true);
	theRequest.send(null);
}

function updateProgress() {
    if (theRequest) {

        if (theRequest.readyState == 4) {

            if (theRequest.status == 200) {

                var update = new Array();
                update = theRequest.responseText.split('|');

                var completed_upload_size = update[1];
                total_upload_size = update[2];
                var progressPercent = update[0];
                var transferRate = update[3];
                var timeRemaining = update[4];

                if ((completed_upload_size != "") && (completed_upload_size != 0))
                {
                    $('UploadProgressBar1').firstChild.firstChild.style.width=progressPercent+'%';
                    $('UploadStatus1').innerHTML=progressPercent + '% finished, '
                            + completed_upload_size + ' of '
                            + total_upload_size + ' at '
                            + transferRate + 
                            + "; " + timeRemaining;
                }

                if (progressPercent == 100)
                {

                    $('UploadProgressBar1').firstChild.firstChild.style.width='100%';

                    $('status-win').style.display = 'none';

                    return null;
                }


                window.setTimeout("goajax('${statusUrl}')", 700);
            } else {
                alert('Error: got a not-OK status code...');
            }
        }
    }
}

function startupload()
{
    document.getElementById('${formMarkupId}').submit();
    $('UploadStatus1').innerHTML='Upload starting...';
    if($('UploadProgressBar1')){$('UploadProgressBar1').firstChild.firstChild.style.width='0%'};
    $('status-win').style.display = 'block';

    window.setTimeout("goajax('${statusUrl}', updateProgress)", 1200);
}

</script>