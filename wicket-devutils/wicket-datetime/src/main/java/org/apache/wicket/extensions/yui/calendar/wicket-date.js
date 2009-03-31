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
	if (numbers == null) return Number.NaN;
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
	
	// also take scroll position into account
	scrollPos = [YAHOO.util.Dom.getDocumentScrollLeft(), YAHOO.util.Dom.getDocumentScrollTop()];
	
	// correct datepicker's position so that it isn't rendered off screen on the right side or bottom
	if (targetPos[0] + subjectWidth > scrollPos[0] + viewPortWidth) {
		// correct horizontal position
		YAHOO.util.Dom.setX(subject, Math.max(targetPos[0], viewPortWidth) - subjectWidth);
	} else {
		YAHOO.util.Dom.setX(subject, targetPos[0]);
	}
	if (targetPos[1] + targetHeight + 1 + subjectHeight > scrollPos[1] + viewPortHeight) {
		// correct vertical position
		YAHOO.util.Dom.setY(subject, Math.max(targetPos[1], viewPortHeight) - subjectHeight);
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
		if (!isNaN(date)) {
			widget.select(date);
			firstDate = widget.getSelectedDates()[0];
			widget.cfg.setProperty("pagedate", (firstDate.getMonth() + 1) + "/" + firstDate.getFullYear());
			widget.render();
		}
	}
	widget.show();
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
	
	function showCalendar() {
		Wicket.DateTime.showCalendar(YAHOO.wicket[cfg.dpJs], YAHOO.util.Dom.get(cfg.componentId).value, cfg.datePattern);
		if (cfg.alignWithIcon) Wicket.DateTime.positionRelativeTo(YAHOO.wicket[cfg.dpJs].oDomContainer, cfg.icon);
	}

	YAHOO.util.Event.addListener(cfg.icon, "click", showCalendar, YAHOO.wicket[cfg.dpJs], true);

	function selectHandler(type, args, cal) {
		YAHOO.util.Dom.get(cfg.componentId).value = Wicket.DateTime.substituteDate(cfg.datePattern, args[0][0]);
		if (cal.isVisible()) {
			if (cfg.hideOnSelect) cal.hide();
			if (cfg.fireChangeEvent) {
				var field = YAHOO.util.Dom.get(cfg.componentId);
				if (field.onchange != null && typeof(field.onchange) != 'undefined') field.onchange();
			}
		}
	}
 
	YAHOO.wicket[cfg.dpJs].selectEvent.subscribe(selectHandler,YAHOO.wicket[cfg.dpJs]);	 
	YAHOO.wicket[cfg.dpJs].render();
}

YAHOO.register("wicket-date", Wicket.DateTime, {version: "1.3.0", build: "rc1"});