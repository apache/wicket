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
	date = new Date();
	date.setFullYear(year, (month - 1), day);
	return date;
}

/** 
 * Returns a string containing the value, with a leading zero if the value is < 10.
 */
Wicket.DateTime.padDateFragment = function(value) {
	return (value < 10 ? "0" : "") + value;
}
