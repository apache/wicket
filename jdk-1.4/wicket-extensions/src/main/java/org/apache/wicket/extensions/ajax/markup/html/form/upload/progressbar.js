/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var wupb= {

    Def : function(formid, statusid, barid, url) {
        this.formid=formid;
        this.statusid=statusid;
        this.barid=barid;
        this.url=url;
    },
    
 	get : function(id) {
		return document.getElementById(id);
	},
	
	start : function(def) {
		wupb.get(def.formid).submit();
		wupb.get(def.statusid).innerHTML='Upload starting...';
	    wupb.get(def.barid).firstChild.firstChild.style.width='0%';
	    
		wupb.get(def.statusid).style.display='block';
	    wupb.get(def.barid).style.display='block';
	    
	    window.setTimeout(function() { wupb.ajax(def); }, 1000);
	},
	
	ajax : function(def) {
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
	
		transport.onreadystatechange = function() { wupb.update(transport, def); };
		transport.open('GET', def.url+'?anticache='+Math.random(), true);
		transport.send(null);
	},
	
	update: function(transport, def) {
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
                    wupb.get(def.barid).firstChild.firstChild.style.width=progressPercent+'%';
                    wupb.get(def.statusid).innerHTML=progressPercent + '% finished, '
                            + completed_upload_size + ' of '
                            + total_upload_size + ' at '
                            + transferRate  
                            + "; " + timeRemaining;
                }

                if (progressPercent == 100)
                {

                    wupb.get(def.barid).firstChild.firstChild.style.width='100%';
                    
					wupb.get(def.statusid).style.display='none';
				    wupb.get(def.barid).style.display='none';

                    return null;
                }


                window.setTimeout(function() { wupb.ajax(def); }, 1000);
            } else {
                alert('Error: got a not-OK status code...');
            }
        }
	}
}
