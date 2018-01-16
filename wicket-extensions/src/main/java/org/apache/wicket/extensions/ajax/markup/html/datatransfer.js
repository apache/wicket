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

;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = {};
	}

	if (Wicket.DataTransfer) {
		return;
	}

	Wicket.DataTransfer = {
		getFilesAsParamArray : function(ev, name) {
			var files = [];
				
			function pushFile(file) {
				files.push({'name' : name, 'value' : file}); 
			};

			var dataTransfer = ev.dataTransfer; 
			var i;
			if (dataTransfer.items) { 
			  for (i = 0; i < dataTransfer.items.length; i++) { 
			    if (dataTransfer.items[i].kind == 'file') { 
			      pushFile(dataTransfer.items[i].getAsFile()); 
			    } 
			  } 
			} else { 
			  for (i = 0; i < dataTransfer.files.length; i++) { 
			    pushFile(dataTransfer.files[i]); 
			  } 
			}
			
			return files;
		}
	};
})();
