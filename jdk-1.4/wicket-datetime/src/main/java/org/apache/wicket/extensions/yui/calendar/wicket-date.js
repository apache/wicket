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
 
/*
 * Wicket Date parse function for the Calendar/ Date picker component.
 */

// Wicket Namespace

if (typeof(Wicket) == "undefined")
	Wicket = { };
	
Wicket.DateTime = { }

/**
 * Parses date from simple date pattern. Only parses dates with yy, MM and dd like patterns, though 
 * it is safe to have time as long as it comes after the pattern (which should be the case 
 * anyway 99.9% of the time).
 */
Wicket.DateTime.parseDate = function(pattern, value) {
	numbers = value.match(/(\d+)/g);
	var day, month, year;
	arrayPos = 0;
	for (i = 0; i < pattern.length; i++) {
		c = pattern.charAt(i);
		while ((pattern.charAt(i) == c) && (i < pattern.length)) {
			i++;
		}
		if (c == 'y') {
			year = numbers[arrayPos++];
		} else if (c == 'M') {
			month = numbers[arrayPos++];
		} else if (c == 'd') {
			day = numbers[arrayPos++];
		}
		if (arrayPos > 2) break;
	}
	// TODO this is a bit crude. Make nicer some time.
	if (year < 100) {
		if (year < 70) {
			year = year * 1 + 2000;
		} else {
			year = year * 1 + 1900;
		}
	}
	var date = new Date();
	date.setFullYear(year, (month - 1), day);
	return date;
}

/** 
 * Returns a string containing the value, with a leading zero if the value is < 10.
 */
Wicket.DateTime.padDateFragment = function(value) {
	return (value < 10 ? "0" : "") + value;
}

/**
 * Gets the height of the displayed area of the window, as YAHOO.util.Dom.getViewportHeight()
 * has issues with Firefox. 
 * See http://tech.groups.yahoo.com/group/ydn-javascript/message/5850
 * Implementation taken from: http://www.quirksmode.org/viewport/compatibility.html#link2
 */
Wicket.DateTime.getViewportHeight = function() {	  
	if (window.innerHeight) // all browsers except IE
		viewPortHeight = window.innerHeight;
	else if (document.documentElement && document.documentElement.clientHeight) // IE 6 strict mode
		viewPortHeight = document.documentElement.height;		
	else if (document.body) // other IEs
		viewPortHeight = document.body.clientHeight;
	return viewPortHeight;
}

/** 
 * Position subject relative to target top-left by default.
 * If there is too little space on the right side/bottom,
 * the datepicker's position is corrected so that the right side/bottom
 * is aligned with the display area's right side/bottom.
 * @param subject the dom element to has to be positioned
 * @param target id of the dom element to position relative to
 */
Wicket.DateTime.positionRelativeTo = function(subject, target) {
	
	targetPos = YAHOO.util.Dom.getXY(target);
	targetHeight = YAHOO.util.Dom.get(target).offsetHeight;
	subjectHeight = YAHOO.util.Dom.get(subject).offsetHeight;
	subjectWidth = YAHOO.util.Dom.get(subject).offsetWidth;		
	
	viewPortHeight = Wicket.DateTime.getViewportHeight();	
	viewPortWidth = YAHOO.util.Dom.getViewportWidth();
	
	// correct datepicker's position so that it isn't rendered off screen on the right side or bottom
	if (targetPos[0] + subjectWidth > viewPortWidth) {
		// correct horizontal position
		YAHOO.util.Dom.setX(subject, viewPortWidth - subjectWidth);
	} else {
		YAHOO.util.Dom.setX(subject, targetPos[0]);
	}
	if (targetPos[1] + targetHeight + 1 + subjectHeight > viewPortHeight) {
		// correct vertical position
		YAHOO.util.Dom.setY(subject, viewPortHeight - subjectHeight);
	} else {
		YAHOO.util.Dom.setY(subject, targetPos[1] + targetHeight + 1);
	}
}

/**
 * Return the result of interpolating the value (date) argument with the date pattern.
 * The dateValue has to be an array, where year is in the first, month in the second
 * and date (day of month) in the third slot.
 */
Wicket.DateTime.substituteDate = function(datePattern, date) {
	day = date[2];
	month = date[1];
	year = date[0];
	// optionally do some padding to match the pattern
	if(datePattern.match(/\bdd\b/)) day = Wicket.DateTime.padDateFragment(day);
	if(datePattern.match(/\bMM\b/)) month = Wicket.DateTime.padDateFragment(month);
	if(datePattern.match(/\byy\b/)) year = Wicket.DateTime.padDateFragment(year % 100);
	// replace pattern with real values
	return datePattern.replace(/d+/, day).replace(/M+/, month).replace(/y+/, year);
}

/**
 * Display the YUI calendar widget. If the date is not null (should be a string) then it is parsed
 * using the provided date pattern, and set as the current date on the widget.
 */
Wicket.DateTime.showCalendar = function(widget, date, datePattern) {
	if (date) {
		date = Wicket.DateTime.parseDate(datePattern, date);
		if (!isNaN(date.getTime())) { 		
			widget.select(date);
			firstDate = widget.getSelectedDates()[0];
			widget.cfg.setProperty("pagedate", (firstDate.getMonth() + 1) + "/" + firstDate.getFullYear());
			widget.render();
		}
	}
	widget.show();
}

/**
 * Renders the Month-Year-label as two select boxes.
 * The year-select uses the following pattern for the widget.
 */
Wicket.DateTime.enableMonthYearSelection = function(widget) {
	var monthSelectId = widget.id + "MonthSelect";	
	var yearInputId = widget.id + "YearInput";
	var yearUpId = widget.id + "YearUp";
	var yearDownId = widget.id + "YearDown";
	
	// sets the select boxes to the proper values after navigating the datepicker with the arrows
	var sync = function(type) {
		var month = parseInt(widget.cfg.getProperty(YAHOO.widget.Calendar._DEFAULT_CONFIG.PAGEDATE.key).getMonth());
		var year = parseInt(widget.cfg.getProperty(YAHOO.widget.Calendar._DEFAULT_CONFIG.PAGEDATE.key).getFullYear());
		YAHOO.util.Dom.get(monthSelectId).selectedIndex = month;
		YAHOO.util.Dom.get(yearInputId).value = year;
	}
	
	widget.renderEvent.subscribe(sync);		
	
	// override the default applyListeners method to register onChange-listeners for the select boxes 
	if (typeof(widget.YUIApplyListeners) == 'undefined') {
		widget.YUIApplyListeners = widget.applyListeners;
	}
	widget.applyListeners = function () {
		widget.YUIApplyListeners();
		var E = YAHOO.util.Event;
		E.on(monthSelectId, "change", function() {
			widget.setMonth(YAHOO.util.Dom.get(monthSelectId).value);
			widget.render();
		});
		
		widget.yearChanged = new YAHOO.util.CustomEvent("yearChanged", widget);
		widget.yearChanged.subscribe(function() {			
			widget.setYear(YAHOO.util.Dom.get(yearInputId).value);
			widget.render();
		});				
		
		E.on(yearInputId, "blur", function() { processNumber(0); }, this);		
		
		var processNumber = function(offset) {
			var field = YAHOO.util.Dom.get(yearInputId);
			field.value = field.value.replace(/\D*/, "");
			field.value = parseInt(field.value, 10) + offset; 
			if (/\d+/.test(field.value)) {
				widget.yearChanged.fire();				
			}   
		};
		
		E.on(yearUpId, "click", function() {
			processNumber(1);
		});
		
		E.on(yearDownId, "click", function() {
			processNumber(-1);
		});
	}
		
	// override the function which is used to generate the month label and render two select boxes instead
  	widget.buildMonthLabel = function () {
		var pageDate = widget.cfg.getProperty(YAHOO.widget.Calendar._DEFAULT_CONFIG.PAGEDATE.key);

		// generate month select box using localized strings
		var selectHtml = "<select id=\"" + monthSelectId + "\">";
		var i;
		for (i = 0; i < 12; i++) {
			selectHtml += "<option value=\"" + i + "\"";
			if (i == pageDate.getMonth()) {
				selectHtml += " selected=\"selected\"";
			} 	
			selectHtml += ">" + widget.Locale.LOCALE_MONTHS[i] + "</option>";
		}
		selectHtml += "</select>";

		// generate year input and spinner buttons	
		selectHtml += "<table>";	
		selectHtml += "<tr><th><a class='yearDown' id='" + yearDownId + "'/></th>";
		selectHtml += "<th><input type='text' size='4' id='" + yearInputId + "'/></th>";
		selectHtml += "<th><a class='yearUp' id='" + yearUpId + "'/></th>";			
		selectHtml += "</tr></table>";
		return selectHtml;  
	}
}

// configures a datepicker using the cfg object
Wicket.DateTime.init = function(cfg) {
	cfg.dpJs = cfg.widgetId + "DpJs";
	cfg.dp = cfg.widgetId + "Dp";
	cfg.icon = cfg.widgetId +"Icon";
	YAHOO.namespace("wicket");
	if (cfg.calendarInit.pages && cfg.calendarInit.pages > 1) {
		YAHOO.wicket[cfg.dpJs] = new YAHOO.widget.CalendarGroup(cfg.dpJs,cfg.dp, cfg.calendarInit);
	} else {
		YAHOO.wicket[cfg.dpJs] = new YAHOO.widget.Calendar(cfg.dpJs,cfg.dp, cfg.calendarInit);
	}	
	YAHOO.wicket[cfg.dpJs].isVisible = function() { return YAHOO.wicket[cfg.dpJs].oDomContainer.style.display == 'block'; }
	if (cfg.enableMonthYearSelection) Wicket.DateTime.enableMonthYearSelection(YAHOO.wicket[cfg.dpJs]); 
	
	function showCalendar() {
		Wicket.DateTime.showCalendar(YAHOO.wicket[cfg.dpJs], YAHOO.util.Dom.get(cfg.componentId).value, cfg.datePattern);
		if (cfg.alignWithIcon) Wicket.DateTime.positionRelativeTo(YAHOO.wicket[cfg.dpJs].oDomContainer, cfg.icon);
		if (cfg.enableMonthYearSelection) Wicket.DateTime.enableMonthYearSelection(YAHOO.wicket[cfg.dpJs]); 
	}

	YAHOO.util.Event.addListener(cfg.icon, "click", showCalendar, YAHOO.wicket[cfg.dpJs], true);

	function selectHandler(type, args, cal) {
		YAHOO.util.Dom.get(cfg.componentId).value = Wicket.DateTime.substituteDate(cfg.datePattern, args[0][0]);
		var wasVisible = YAHOO.wicket[cfg.dpJs].isVisible();
		if (cfg.hideOnSelect) { cal.hide(); }
		if (cfg.fireChangeEvent && wasVisible) {
			var field = YAHOO.util.Dom.get(cfg.componentId);
			if (field.onchange != null && typeof(field.onchange) != 'undefined') field.onchange();
		}
	}

	YAHOO.wicket[cfg.dpJs].selectEvent.subscribe(selectHandler,YAHOO.wicket[cfg.dpJs]);
	YAHOO.wicket[cfg.dpJs].render();
}
