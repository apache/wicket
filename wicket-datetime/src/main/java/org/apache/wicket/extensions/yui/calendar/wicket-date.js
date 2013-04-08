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

/*globals YAHOO: true */

;(function (undefined) {
	'use strict';

	// Wicket Namespace

	if (typeof(Wicket) === "undefined") {
		window.Wicket = {};
	}

	Wicket.DateTime = {};

	/**
	 * Parses date from simple date pattern.
	 *
	 * Supports patterns built up from the following elements:
	 * yy OR yyyy for year
	 * M OR MM OR MMM OR MMMM for month
	 * d OR dd for day
	 * EEEE for weekday (optional)
	 */
	Wicket.DateTime.parseDate = function(cfg, value) {
		var numbers = value.match(/(\d+)/g);
		var pattern = cfg.datePattern;
		if (!numbers) {
			return NaN;
		}

		var day, month, year;
		var arrayPos = 0;
		for (var i = 0; i < pattern.length; i++) {
			var c = pattern.charAt(i);
			var len = 0;
			while ((pattern.charAt(i) === c) && (i < pattern.length)) {
				i++;
				len++;
			}
			if (c === 'y') {
				year = numbers[arrayPos++];
			} else if (c === 'M') {
				var nameArray;
				switch (len) {
				case 3:
					nameArray = cfg.calendarInit.MONTHS_SHORT;
					break;
				case 4:
					nameArray = cfg.calendarInit.MONTHS_LONG;
					break;
				default:
					nameArray = null;
				}
				if (nameArray) {
					for (var j = 0; j < nameArray.length; j++) {
						if (value.indexOf(nameArray[j]) >= 0) {
							month = j + 1;
							break;
						}
					}
				} else {
					month = numbers[arrayPos++];
				}
			} else if (c === 'd') {
				day = numbers[arrayPos++];
			}
			if (arrayPos > 2) {
				break;
			}
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
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setMilliseconds(0);
		date.setFullYear(year, (month - 1), day);

		return date;
	};

	/**
	 * Returns a string containing the value, with a leading zero if the value is < 10.
	 */
	Wicket.DateTime.padDateFragment = function(value) {
		return (value < 10 ? "0" : "") + value;
	};

	/**
	 * Gets the height of the displayed area of the window, as YAHOO.util.Dom.getViewportHeight()
	 * has issues with Firefox.
	 * See http://tech.groups.yahoo.com/group/ydn-javascript/message/5850
	 * Implementation taken from: http://www.quirksmode.org/viewport/compatibility.html#link2
	 */
	Wicket.DateTime.getViewportHeight = function() {
		var viewPortHeight;

		if (window.innerHeight) {// all browsers except IE
			viewPortHeight = window.innerHeight;
		} else if (document.documentElement && document.documentElement.clientHeight) {// IE 6 strict mode
			viewPortHeight = document.documentElement.height;
		} else if (document.body) {// other IEs
			viewPortHeight = document.body.clientHeight;
		}
		return viewPortHeight;
	};

	/**
	 * Position subject relative to target top-left by default.
	 * If there is too little space on the right side/bottom,
	 * the datepicker's position is corrected so that the right side/bottom
	 * is aligned with the display area's right side/bottom.
	 * @param subject the dom element to has to be positioned
	 * @param target id of the dom element to position relative to
	 */
	Wicket.DateTime.positionRelativeTo = function(subject, target) {

		var targetPos = YAHOO.util.Dom.getXY(target);
		var targetHeight = YAHOO.util.Dom.get(target).offsetHeight;
		var subjectHeight = YAHOO.util.Dom.get(subject).offsetHeight;
		var subjectWidth = YAHOO.util.Dom.get(subject).offsetWidth;

		var viewPortHeight = Wicket.DateTime.getViewportHeight();
		var viewPortWidth = YAHOO.util.Dom.getViewportWidth();

		// also take scroll position into account
		var scrollPos = [YAHOO.util.Dom.getDocumentScrollLeft(), YAHOO.util.Dom.getDocumentScrollTop()];

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
	};

	/**
	 * Return the result of interpolating the value (datetime) argument with the date pattern.
	 * The date has to be an array, where year is in the first, month in the second
	 * and date (day of month) in the third slot.
	 */
	Wicket.DateTime.substituteDate = function(cfg, datetime) {
		var day = datetime[2];
		var month = datetime[1];
		var year = datetime[0];

		var date = new Date();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setMilliseconds(0);
		date.setFullYear(year, (month - 1), day);

		var dayName = null;
		var datePattern = cfg.datePattern;

		// optionally do some padding to match the pattern
		if(datePattern.match(/dd+/)) {
			day = Wicket.DateTime.padDateFragment(day);
		}
		if (datePattern.match(/MMMM/)) {
			month = cfg.calendarInit.MONTHS_LONG[month - 1];
		}
		else if (datePattern.match(/MMM/)) {
			month = cfg.calendarInit.MONTHS_SHORT[month - 1];
		}
		else if(datePattern.match(/MM+/)) {
			month = Wicket.DateTime.padDateFragment(month);
		}
		if(datePattern.match(/yyy+/)) {
			year = Wicket.DateTime.padDateFragment(year);
		} else if(datePattern.match(/yy+/)) {
			year = Wicket.DateTime.padDateFragment(year % 100);
		}
		if (datePattern.match(/EEEE/)) {
			// figure out which weekday it is...
			var engDayName = date.toString().match(/(\S*)/)[0];
			var engDayNames = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
			for (var i = 0; i < engDayNames.length; i++) {
				if (engDayName === engDayNames[i]) {
					dayName = cfg.calendarInit.WEEKDAYS_LONG[i];
					break;
				}
			}
		}
		// replace pattern with real values
		var result = datePattern.replace(/d+/, day).replace(/y+/, year).replace(/M+/, month);

		if (dayName != null) {
			result = result.replace(/EEEE/, dayName);
		}

		return result;
	};

	/**
	 * Display the YUI calendar widget. If the date is not null (should be a string) then it is parsed
	 * using the provided date pattern, and set as the current date on the widget.
	 */
	Wicket.DateTime.showCalendar = function(widget, date, cfg) {
		if (date) {
			date = Wicket.DateTime.parseDate(cfg, date);
			if (!isNaN(date)) {
				widget.select(date);
				var firstDate = widget.getSelectedDates()[0];
				if (firstDate) {
					widget.cfg.setProperty("pagedate", (firstDate.getMonth() + 1) + "/" + firstDate.getFullYear());
					widget.render();
				}
			}
		}
		widget.show();
	};

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
		YAHOO.wicket[cfg.dpJs].isVisible = function() { return YAHOO.wicket[cfg.dpJs].oDomContainer.style.display === 'block'; };

		function showCalendar() {
			if (YAHOO.wicket[cfg.dpJs].oDomContainer.style.display !== 'block') {
				Wicket.DateTime.showCalendar(YAHOO.wicket[cfg.dpJs], YAHOO.util.Dom.get(cfg.componentId).value, cfg);
				if (cfg.alignWithIcon) {
					Wicket.DateTime.positionRelativeTo(YAHOO.wicket[cfg.dpJs].oDomContainer, cfg.icon);
				}
			}
		}

		YAHOO.util.Event.addListener(cfg.icon, "click", showCalendar, YAHOO.wicket[cfg.dpJs], true);

		if (cfg.showOnFieldClick) {
			YAHOO.util.Event.addListener(cfg.widgetId, "click", showCalendar, YAHOO.wicket[cfg.dpJs], true);
		}

		function selectHandler(type, args, cal) {
			YAHOO.util.Dom.get(cfg.componentId).value = Wicket.DateTime.substituteDate(cfg, args[0][0]);
			if (cal.isVisible()) {
				if (cfg.hideOnSelect) {
					cal.hide();
				}
				if (cfg.fireChangeEvent) {
					var field = YAHOO.util.Dom.get(cfg.componentId);
					if (field.onchangeoriginal) {
						field.onchangeoriginal();
					}
					if (field.onchange) {
						field.onchange();
					}
					Wicket.Event.fire(Wicket.$(cfg.componentId), 'change');
				}
			}
		}

		YAHOO.wicket[cfg.dpJs].selectEvent.subscribe(selectHandler, YAHOO.wicket[cfg.dpJs]);

		if(cfg.autoHide) {
			YAHOO.util.Event.on(document, "click", function(e) {

				var el = YAHOO.util.Event.getTarget(e);
				var dialogEl = document.getElementById(cfg.dp);
				var showBtn = document.getElementById(cfg.icon);
				var fieldEl = document.getElementById(cfg.componentId);

				if (YAHOO.wicket[cfg.dpJs] &&
					el !== dialogEl &&
					el !== fieldEl &&
					!YAHOO.util.Dom.isAncestor(dialogEl, el) &&
					el !== showBtn &&
					!YAHOO.util.Dom.isAncestor(showBtn, el))
				{
					YAHOO.wicket[cfg.dpJs].hide();
				}
	        });
	    }
	    YAHOO.wicket[cfg.dpJs].render();
	};

	/**
	 * Checks that `str` ends with `suffix`
	 * @param str The string to check
	 * @param suffix The suffix with which the `srt` must end
	 * @return {boolean} true if the `str` ends with `suffix`
	 */
	var endsWith = function(str, suffix) {
	    return str.indexOf(suffix, str.length - suffix.length) !== -1;
	};

	/**
	 * @param toDestroy An array of Wicket DateTime objects to destroy
	 */
	var destroyInternal = function (toDestroy) {
	
		// avoids creation of a function inside a loop (JSLint warning)
		function scheduleDestroy(toDestroy2) {
			window.setTimeout(function(){destroyInternal(toDestroy2);}, 5);
		}

		if (toDestroy && toDestroy.length > 1) {
			var i = 0;
			while (toDestroy.length > 0) {
				var name = toDestroy.pop();
				try {
					if (YAHOO.wicket[name]) {
						// this is expensive.
						YAHOO.wicket[name].destroy();
						delete YAHOO.wicket[name];
					}
				} catch (e) {
					if (Wicket.Log) {
						Wicket.Log.error(e);
					}
				}
				i++;
				if (i === 20) {
					scheduleDestroy(toDestroy);
					break;
				}
			}
		}
	};

	/**
	 * Schedules all YAHOO.wicket.** objects for destroy if their host HTML element
	 * is no more in the DOM document.
	 */
	var destroy = function() {
		if (!YAHOO.wicket) {
			return;
		}
		var deleted = 0;
		var available = 0;
		var toDestroy = [];
		for(var propertyName in YAHOO.wicket) {
			if (endsWith(propertyName, "DpJs")) {
				var id = propertyName.substring(0, propertyName.length - 4);
				var e = Wicket.$(id);
				available++;
				if (e === null) {
					try {
						deleted++;
						toDestroy.push(propertyName);
					} catch (ex) {
						if (Wicket.Log) {
							Wicket.Log.error(ex);
						}
					}
				}
			}
		}
		if (Wicket.Log) {
			Wicket.Log.info("Date pickers to delete="+deleted+", available="+available);
		}
		setTimeout(function(){destroyInternal(toDestroy);}, 5);
	};

	// init method variant that needs less character to invoke
	Wicket.DateTime.init2 = function(widgetId, componentId, calendarInit, datePattern,
			alignWithIcon, fireChangeEvent, hideOnSelect, showOnFieldClick, i18n, autoHide) {
		calendarInit.MONTHS_SHORT = i18n.MONTHS_SHORT;
		calendarInit.MONTHS_LONG = i18n.MONTHS_LONG;
		calendarInit.WEEKDAYS_MEDIUM = i18n.WEEKDAYS_MEDIUM;
		calendarInit.WEEKDAYS_LONG = i18n.WEEKDAYS_LONG;
		calendarInit.START_WEEKDAY = i18n.START_WEEKDAY;
		calendarInit.WEEKDAYS_1CHAR = i18n.WEEKDAYS_1CHAR;
		calendarInit.WEEKDAYS_SHORT = i18n.WEEKDAYS_SHORT;

		Wicket.DateTime.init({
			widgetId: widgetId,
			componentId: componentId,
			calendarInit: calendarInit,
			datePattern: datePattern,
			alignWithIcon: alignWithIcon,
			fireChangeEvent: fireChangeEvent,
			hideOnSelect: hideOnSelect,
			showOnFieldClick: showOnFieldClick,
			autoHide: autoHide
		});
	};

	YAHOO.register("wicket-date", Wicket.DateTime, {version: "6.7.0", build: "1"});

	// register a listener to clean up YAHOO.wicket cache.
	Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
		window.setTimeout(function(){destroy();}, 10);
	});
})();