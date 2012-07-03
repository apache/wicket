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

if (typeof(Wicket) === 'undefined') {
	window.Wicket = {};
}
if (typeof(Wicket.DateTimeInit) === 'undefined') {
	Wicket.DateTimeInit = {};
}

Wicket.DateTimeInit.CalendarInits = [];
Wicket.DateTimeInit.CalendarInitFinished = false;
Wicket.DateTimeInit.CalendarI18n = {};
Wicket.DateTimeInit.CalendarAdd = function(initFn) {
	if (Wicket.DateTimeInit.CalendarInitFinished) {
		// when a DatePicker is added via ajax, the loader is already finished, so
		// we call the init function directly.
		initFn();
	} else {
		// when page is rendered, all calendar components will be initialized after
		// the required js libraries have been loaded.
		Wicket.DateTimeInit.CalendarInits.push(initFn);
	}
};

Wicket.DateTimeInit.YuiLoader = new YAHOO.util.YUILoader({
	base: "${basePath}",
	${filter}
	allowRollup: ${allowRollup},
	require: ["wicket-date"],
	onSuccess: function() {
		Wicket.DateTimeInit.CalendarInitFinished = true;
		while (Wicket.DateTimeInit.CalendarInits.length > 0) {
			Wicket.DateTimeInit.CalendarInits.pop()();
		}
	}
});
Wicket.DateTimeInit.YuiLoader.addModule({
	name: "wicket-date",
	type: "js",
	requires: ["calendar"],
	fullpath: "${Wicket.DateTimeInit.DatePath}"
});
Wicket.DateTimeInit.YuiLoader.insert();