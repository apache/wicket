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
if (typeof (Wicket.CheckboxSelector.Group) === "undefined") {
	Wicket.CheckboxSelector.Group = {};
	/**
	 * Returns a closure that finds all checkboxes associated with the given
	 * CheckGroup.
	 *
	 * @param formId
	 *            The markup ID of the containing form (needed because the
	 *            selector might be outside the form)
	 * @param groupName
	 *            The input name of the CheckGroup
	 */
	Wicket.CheckboxSelector.Group.findCheckboxesFunction = function(formId, groupName) {
		"use strict";

		return function() {
			var result = [];
			var parentForm = Wicket.$(formId);
			var parentGroup = parentForm[groupName];
			if (parentGroup.length) {
				for ( var i = 0; i < parentGroup.length; i++) {
					var checkbox = parentGroup[i];
					result.push(checkbox);
				}
			} else if (parentGroup) {
				result.push(parentGroup);
			}
			return result;
		};
	};
}
