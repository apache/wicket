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
if (typeof wicketCalendarInits == 'undefined') {
	wicketCalendarInits = new Array();
	wicketCalendarInitFinished = false;
}

init${widgetId} = function() {
	Wicket.DateTime.init( {
		widgetId: "${widgetId}",
		componentId: "${componentId}",				
		calendarInit: { ${calendarInit} },
		datePattern: "${datePattern}",
		alignWithIcon: ${alignWithIcon},
		fireChangeEvent: ${fireChangeEvent},
		hideOnSelect: ${hideOnSelect}
	});
	${additionalJavascript}
};

if (wicketCalendarInitFinished) {
	// when a DatePicker is added via ajax, the loader is already finished, so
	// we call the init function directly.
	init${widgetId}();
} else {
	// when page is rendered, all calendar components will be initialized after
	// the required js libraries have been loaded.
	wicketCalendarInits.push(init${widgetId});
}

if (typeof wicketYuiLoader == 'undefined')	{
	wicketYuiLoader = new YAHOO.util.YUILoader({
		base: "${basePath}", 
		${filter}
		allowRollup: ${allowRollup},
		require: ["wicket-date"],		
		onSuccess: function() {
			wicketCalendarInitFinished = true;	
			while (wicketCalendarInits.length > 0) {
				wicketCalendarInits.pop()();
			}		
		}
	});
	
	wicketYuiLoader.addModule({
		name: "wicket-date",
		type: "js",
		requires: ["calendar"],
		fullpath: "${wicketDatePath}"		           
	});
	wicketYuiLoader.insert();
}

