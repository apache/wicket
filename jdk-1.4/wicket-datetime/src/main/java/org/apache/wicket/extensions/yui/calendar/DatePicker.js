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
 if (typeof wicketYuiLoader == 'undefined')	wicketYuiLoader = new YAHOO.util.YUILoader({base: "${basePath}", filter: "RAW"});

function checkWicketDate(name, loaderCallback) {
	if (typeof(Wicket) != 'undefined' && typeof(Wicket.DateTime) != 'undefined') {
		loaderCallback();
	} else {
		setTimeout(function() {
			checkWicketDate(name, loaderCallback);
		}, 50);
	}
};

wicketYuiLoader.addModule({
	name: "wicket-date",
	type: "js",
	fullpath: "${pathToWicketDate}",
	verifier: checkWicketDate,
	requires: ['calendar']
});	


function check${widgetId}Loader() {
	if (!wicketYuiLoader.initializing) {
		wicketYuiLoader.initializing = true;	
		wicketYuiLoader.require("wicket-date");
		wicketYuiLoader.insert(function() {
			wicketYuiLoader.initializing = false;
			init${widgetId}DpJs();
		});
	}  else {
		setTimeout(check${widgetId}Loader, 50);
	}
 }

check${widgetId}Loader();	
 
function init${widgetId}DpJs() {

	YAHOO.namespace("wicket");
	YAHOO.wicket.${widgetId}DpJs = new YAHOO.widget.Calendar("${widgetId}DpJs","${widgetId}Dp", { ${calendarInit} });
	YAHOO.wicket.${widgetId}DpJs.isVisible = function() { return YAHOO.wicket.${widgetId}DpJs.oDomContainer.style.display == 'block'; }
	if (${enableMonthYearSelection}) Wicket.DateTime.enableMonthYearSelection(YAHOO.wicket.${widgetId}DpJs); 
	
	function showCalendar() {
		Wicket.DateTime.showCalendar(YAHOO.wicket.${widgetId}DpJs, YAHOO.util.Dom.get("${componentId}").value, '${datePattern}');
		if (${alignWithIcon}) Wicket.DateTime.positionRelativeTo(YAHOO.wicket.${widgetId}DpJs.oDomContainer, "${widgetId}Icon");
		if (${enableMonthYearSelection}) Wicket.DateTime.enableMonthYearSelection(YAHOO.wicket.${widgetId}DpJs); 
	}

	YAHOO.util.Event.addListener("${widgetId}Icon", "click", showCalendar, YAHOO.wicket.${widgetId}DpJs, true);

	function selectHandler(type, args, cal) {
		YAHOO.util.Dom.get("${componentId}").value = Wicket.DateTime.substituteDate('${datePattern}', args[0][0]);
		var wasVisible = YAHOO.wicket.${widgetId}DpJs.isVisible();
		cal.hide();
		if (${fireChangeEvent} && wasVisible) {
			var field = YAHOO.util.Dom.get("${componentId}");
			if (typeof(field.onchange) != 'undefined') field.onchange();
		}
	}

	YAHOO.wicket.${widgetId}DpJs.selectEvent.subscribe(selectHandler,YAHOO.wicket.${widgetId}DpJs);
	YAHOO.wicket.${widgetId}DpJs.render();
}
