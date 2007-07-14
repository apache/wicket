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

// init the date picker
function init${widgetId}DpJs() {

 // create date picker instance
 YAHOO.wicket.${widgetId}DpJs = new YAHOO.widget.Calendar("${widgetId}DpJs","${widgetId}Dp", { ${calendarInit} });
 if (${alignWithIcon}) {
	 iconPos = YAHOO.util.Dom.getXY("${widgetId}Icon");
	 iconHeight = parseInt(YAHOO.util.Dom.getStyle("${widgetId}Icon", "height"));
	 YAHOO.wicket.${widgetId}DpJs.oDomContainer.style.top = iconPos[1] + iconHeight + 1 + "px";
	 YAHOO.wicket.${widgetId}DpJs.oDomContainer.style.left = iconPos[0] + "px";
}

 YAHOO.wicket.${widgetId}DpJs.isVisible = function() { return YAHOO.wicket.${widgetId}DpJs.oDomContainer.style.display == 'block'; } 
 
 // inner function to show the calendar
 function showCalendar() {
    var dateValue = YAHOO.util.Dom.get("${widgetId}").value;
    if (dateValue) {
      dateValue = Wicket.DateTime.parseDate('${datePattern}', dateValue);
      YAHOO.wicket.${widgetId}DpJs.select(dateValue);
      var firstDate = YAHOO.wicket.${widgetId}DpJs.getSelectedDates()[0];
      YAHOO.wicket.${widgetId}DpJs.cfg.setProperty("pagedate", (firstDate.getMonth() + 1) + "/" + firstDate.getFullYear());
      YAHOO.wicket.${widgetId}DpJs.render();
    }
    YAHOO.wicket.${widgetId}DpJs.show();
  }

  // trigger popping up the date picker when the icon is clicked 
  YAHOO.util.Event.addListener("${widgetId}Icon", "click", showCalendar, YAHOO.wicket.${widgetId}DpJs, true);

  // inner function for handling calendar selects  
  function selectHandler(type, args, cal) {
    var selDateArray = args[0][0];
    var yr = selDateArray[0];
    var month = selDateArray[1];
    var dt = selDateArray[2];
    // optionally do some padding to match the pattern
    if('${datePattern}'.match(/\bdd\b/)) dt = Wicket.DateTime.padDateFragment(dt);
    if('${datePattern}'.match(/\bMM\b/)) month = Wicket.DateTime.padDateFragment(month);
    if('${datePattern}'.match(/\byy\b/)) yr = Wicket.DateTime.padDateFragment(yr % 100);
    // replace pattern with real values
    var val = '${datePattern}'.replace(/d+/, dt).replace(/M+/, month).replace(/y+/, yr);   
    var wasVisible = YAHOO.wicket.${widgetId}DpJs.isVisible();
    YAHOO.util.Dom.get("${widgetId}").value = val;
    // hide picker
    cal.hide();
    // fire onchange notification 
    if (wasVisible && ${fireChangeEvent}) {
    	var field = YAHOO.util.Dom.get("${widgetId}");
    	if (typeof(field.onchange) != 'undefined') {
    		field.onchange();
    	}
    }
  }

  // register the select handler function
  YAHOO.wicket.${widgetId}DpJs.selectEvent.subscribe(selectHandler, YAHOO.wicket.${widgetId}DpJs);
  
  // now that everything is set up, render the date picker
  YAHOO.wicket.${widgetId}DpJs.render();
}