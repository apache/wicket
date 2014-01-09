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
 * Wicket Choice Ajax updates.
 *
 * @author svenmeier
 */

;(function (undefined) {
	"use strict";

	// introduce a namespace
	if (typeof (Wicket.Choice) === "undefined") {
		Wicket.Choice = {};

		/**
		 * Is a change accepted.
		 *
		 * @param name input name of choice
		 * @param attrs ajax attributes
		 */
		Wicket.Choice.acceptInput = function(name, attrs) {

			var srcElement = attrs.event.target;

			return (srcElement.name === name);
		};

		/**
		 * Get all checked input values.
		 *
		 * @param name input name of choice
		 * @param attrs ajax attributes
		 */
		Wicket.Choice.getInputValues = function(name, attrs) {
			var result = [], srcElement = attrs.event.target;

			var inputNodes = Wicket.$(attrs.c).getElementsByTagName("input");
			for (var i = 0 ; i < inputNodes.length ; i ++) {
				var inputNode = inputNodes[i];

				if (inputNode.name !== name) {
					continue;
				}
				if (!inputNode.checked) {
					continue;
				}

				var value = inputNode.value;
				result.push({ name: name, value: value });
			}

			return result;
		};
	}
})();
