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
 
YAHOO.namespace("wicket");

function init${widgetId}DpJs() {

	YAHOO.wicket.${widgetId}DpJs = new YAHOO.widget.Calendar("${widgetId}DpJs","${widgetId}Dp", { ${calendarInit} });
	YAHOO.wicket.${widgetId}DpJs.isVisible = function() { return YAHOO.wicket.${widgetId}DpJs.oDomContainer.style.display == 'block'; } 
 
	function showCalendar() {
		Wicket.DateTime.showCalendar(YAHOO.wicket.${widgetId}DpJs, YAHOO.util.Dom.get("${widgetId}").value, '${datePattern}');
		if (${alignWithIcon}) Wicket.DateTime.positionRelativeTo(YAHOO.wicket.${widgetId}DpJs.oDomContainer, "${widgetId}Icon");
	}

	YAHOO.util.Event.addListener("${widgetId}Icon", "click", showCalendar, YAHOO.wicket.${widgetId}DpJs, true);

	function selectHandler(type, args, cal) {
		YAHOO.util.Dom.get("${widgetId}").value = Wicket.DateTime.substituteDate('${datePattern}', args[0][0]);
		cal.hide();
		if (${fireChangeEvent} && YAHOO.wicket.${widgetId}DpJs.isVisible()) {
			var field = YAHOO.util.Dom.get("${widgetId}");
			if (typeof(field.onchange) != 'undefined') field.onchange();
		}
	}

	YAHOO.wicket.${widgetId}DpJs.selectEvent.subscribe(selectHandler, YAHOO.wicket.${widgetId}DpJs);
	YAHOO.wicket.${widgetId}DpJs.render();
}