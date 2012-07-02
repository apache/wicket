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
// introduce a namespace, just to be nice
if (typeof (Wicket.CheckboxSelector.Choice) === "undefined") {
	Wicket.CheckboxSelector.Choice = {};

	/**
	 * Returns a closure that finds all checkboxes associated with the given
	 * CheckboxMultipleChoice.
	 *
	 * @param parentChoiceId
	 *            The markup ID of the CheckboxMultipleChoise
	 */
	// adapted from AjaxFormChoiceComponentUpdatingBehavior
	Wicket.CheckboxSelector.Choice.findCheckboxesFunction = function(parentChoiceId) {
		"use strict";

		return function() {
			var result = [];
			var inputNodes = Wicket.$(parentChoiceId).getElementsByTagName(
					'input');
			for ( var i = 0; i < inputNodes.length; i++) {
				var inputNode = inputNodes[i];
				if (inputNode.id.indexOf(parentChoiceId + '-') >= 0) {
					result.push(inputNode);
				}
			}
			return result;
		};
	};
}
