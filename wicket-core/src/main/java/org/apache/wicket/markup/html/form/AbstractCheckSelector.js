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

// introduce a namespace
if (typeof (Wicket.CheckboxSelector) === "undefined") {
	Wicket.CheckboxSelector = {};

	/**
	 * Called in the onclick handler of the select-all-checkbox. Updates all
	 * associated checkboxes by simulating a click on those that need to have
	 * their state changed.
	 *
	 * @param newCheckedState
	 *            the state to which all checkboxes should be set
	 * @param findCheckboxes
	 *            a function that returns an array containing the IDs of all
	 *            associated checkboxes
	 */
	// adapted from AjaxFormChoiceComponentUpdatingBehavior
	Wicket.CheckboxSelector.updateAllCheckboxes = function(newCheckedState, findCheckboxes) {
		"use strict";

		var i,
			checkboxes = findCheckboxes();
		for (i in checkboxes) {
			var checkbox = checkboxes[i];
			if (checkbox.checked !== newCheckedState) {
				checkbox.click();
			}
		}
	};

	/**
	 * Called in the onclick handlers of the associated checkboxes if the auto
	 * update feature is active. Checks the state of all checkboxes - if all are
	 * checked, the selector is checked too. Otherwise the selector is
	 * unchecked.
	 *
	 * @param selectorId
	 *            the ID of the selector checkbox
	 * @param findCheckboxes
	 *            a function that returns an array containing the IDs of all
	 *            associated checkboxes
	 */
	Wicket.CheckboxSelector.updateSelectorState = function(selectorId, findCheckboxes) {
		"use strict";

		var i,
			checkboxes = findCheckboxes(),
			allChecked = true;

		for (i in checkboxes) {
			if (!(checkboxes[i].checked)) {
				allChecked = false;
				break;
			}
		}
		var selector = Wicket.$(selectorId);
		selector.checked = allChecked;
	};

	/**
	 * Called in the onLoad event if the auto update feature is active. Attaches
	 * an onclick handler to all associated checkboxes.
	 *
	 * @param selectorId
	 *            the ID of the selector checkbox
	 * @param findCheckboxes
	 *            a function that returns an array containing the IDs of all
	 *            associated checkboxes
	 */
	Wicket.CheckboxSelector.attachUpdateHandlers = function(selectorId, findCheckboxes) {
		"use strict";

		var i,
			checkboxes = findCheckboxes(),
			clickHandler = function() {
				Wicket.CheckboxSelector.updateSelectorState(selectorId, findCheckboxes);
			};

		for (i in checkboxes) {
			Wicket.Event.add(checkboxes[i], 'click', clickHandler);
		}
		// update selector state once to get the right initial state
		Wicket.CheckboxSelector.updateSelectorState(selectorId, findCheckboxes);
	};

	/**
	 * Called in the onLoad event to initialize the selector checkbox.
	 * @param selectorId
	 *            the ID of the selector checkbox
	 * @param findCheckboxes
	 *            a function that returns an array containing the IDs of all
	 *            associated checkboxes
	 */
	Wicket.CheckboxSelector.initializeSelector = function(selectorId, findCheckboxes) {
		"use strict";

		var selector = Wicket.$(selectorId);
		Wicket.Event.add(selector, 'click', function() {
			Wicket.CheckboxSelector.updateAllCheckboxes(selector.checked, findCheckboxes);
		});
	};
}
