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

;(function($) {
	'use strict';

	$.fn.wicketAtmosphere = function(params) {
		var callbackAdded = false;

		// jquery.atmosphere.response
		function callback(response) {

			if (response.transport !== 'polling' && response.state === 'messageReceived') {
				$.atmosphere.log('info', [ "response.responseBody: " + response.responseBody ]);
				if (response.status === 200) {
					Wicket.Ajax.process(response.responseBody);
				}
			} else if (response.state === "opening") {
			}
		}

		var connectedEndpoint = $.atmosphere.subscribe(params.url,
				!callbackAdded ? callback : null, $.atmosphere.request = params);
		callbackAdded = true;

		$(window).bind("beforeunload", function() {
			callbackAdded = false;
			$.atmosphere.unsubscribe();
		});
	};
}(jQuery));
