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
 * Wicket Ajax Autocomplete
 *
 * @author Janne Hietam&auml;ki
 */
/* jshint latedef: false */
;(function (undefined) {
	'use strict';

	if (!window.Wicket) {
		window.Wicket = {};
	}

	if (Wicket.AutoComplete) {
		return;
	}

	Wicket.AutoCompleteSettings = {
		enterHidesWithNoSelection : false
	};

	Wicket.AutoComplete=function(ajaxAttributes, cfg){
		const KEY_TAB=9;
		const KEY_ENTER=13;
		const KEY_ESC=27;
		const KEY_LEFT=37;
		const KEY_UP=38;
		const KEY_RIGHT=39;
		const KEY_DOWN=40;
		const KEY_SHIFT=16;
		const KEY_CTRL=17;
		const KEY_ALT=18;

		let selected=-1;	// index of the currently selected item
		let elementCount=0; // number of items on the auto complete list
		let visible=0;		// is the list visible

		let ignoreKeyEnter = false;		// ignore key ENTER because is already hid the autocomplete list
		let ignoreOneFocusGain = false; // on FF, clicking an option in the pop-up would make field loose focus; focus() call only has effect in FF after popup is hidden, so the re-focusing must not show popup again in this case
		let triggerChangeOnHide = false;		// should a change be triggered on hiding of the popup

		let initialElement;

		// holds the eventual margins, padding, etc. of the menu container.
		// it is computed when the menu is first rendered, and then reused.
		let initialDelta = -1;
		// remember popup container border size so we can use style.width/height = ... correctly; array [horizontal, vertical]
		let usefulDimensionsInitialized = false;
		const containerBorderWidths = [0, 0];
		let scrollbarSize = 0;
		let selChSinceLastRender = false;

		// holds a throttler, for not sending many requests if the user types
		// too quickly.
		const localThrottler = new Wicket.Throttler(true);
		const throttleDelay = cfg.throttleDelay;

		//this is the minimum input length required to display the autocomplete list
		const minInputLength = cfg.showListOnEmptyInput === true ? 0 : cfg.minInputLength || 1;

		// timeout handler that cancels the hiding of the menu if the focus is still on menu items
		let hideAutoCompleteTimer;

		// A flag indicating whether the 'change' event has been triggered manually after selection
		// from the menu.
		// In this case we don't want to render the menu.
		// It is usually rendered on successful Ajax response
		let isTriggeredChange = false;

		function initialize(){
			let isShowing = false;
			// Remove the autocompletion menu if still present from
			// a previous call. This is required to properly register
			// the mouse event handler again 
			const choiceDiv = document.getElementById(getMenuId());
			if (choiceDiv !== null) {
				isShowing = choiceDiv.showingAutocomplete;
				choiceDiv.parentNode.parentNode.removeChild(choiceDiv.parentNode);
			}

			const obj = Wicket.$(ajaxAttributes.c);
			initialElement = obj;

			Wicket.Event.add(obj, 'blur', function (jqEvent) {
				const menuId=getMenuId();
				//workaround for IE. Clicks on scrollbar trigger
				//'blur' event on input field. (See https://issues.apache.org/jira/browse/WICKET-5882)
				if (menuId !== document.activeElement.id && (menuId + "-container") !== document.activeElement.id) {
					hideAutoCompleteTimer = window.setTimeout(function() {
							hideAutoComplete();
							isTriggeredChange = false;
							triggerChangeOnHide = false;
					}, 500);
				} else {
					this.focus();
				}
			});

			Wicket.Event.add(obj, 'focus', function (jqEvent) {
				const input = jqEvent.target;
				if (!ignoreOneFocusGain && (cfg.showListOnFocusGain || (cfg.showListOnEmptyInput && (!input.value))) && visible === 0) {
					getAutocompleteMenu().showingAutocomplete = true;
					if (cfg.showCompleteListOnFocusGain) {
						updateChoices(true);
					} else {
						updateChoices();
					}
				}
				ignoreOneFocusGain = false;
			});

			Wicket.Event.add(obj, 'keydown', function (jqEvent) {
				const keyCode = Wicket.Event.keyCode(jqEvent);
				switch (keyCode) {
					case KEY_UP:
						if (elementCount > 0) {
							if (selected>-1) {
								setSelected(selected-1);
							}

							const searchTerm = Wicket.$(ajaxAttributes.c).value;
							if(selected === -1 && searchTerm) {
								// select the last element
								setSelected(elementCount-1);
								showAutoComplete();
							}
							render(true, false);
							jqEvent.preventDefault();
						}

						break;
					case KEY_DOWN:
						if (elementCount > 0) {
							if (selected < elementCount-1) {
								setSelected(selected+1);
							} else if (selected === elementCount-1) {
								// select the first element
								setSelected(0);
							}
							if (visible === 0) {
								updateChoices();
							} else {
								render(true, false);
								showAutoComplete();
							}
							jqEvent.preventDefault();
						}

						break;
					case KEY_ESC:
						if (visible === 1) {
							hideAutoComplete();
							return jqEvent.stopPropagation();
						}
						break;
					case KEY_TAB:
					case KEY_ENTER:
						ignoreKeyEnter = false;

						if (selected > -1) {
							let value = getSelectedValue();
							value = handleSelection(value);

							if (value) {
								obj.value = value;
								triggerChangeOnHide = true;
							}

							hideAutoComplete();

							if (cfg.keyTabBehavior === 'selectFocusAutocompleteInput' && keyCode === KEY_TAB) {
								// prevent moving focus to the next component if an item in the dropdown is selected
								// using the Tab key
								jqEvent.preventDefault();
							}

							ignoreKeyEnter = true;
						} else if (Wicket.AutoCompleteSettings.enterHidesWithNoSelection) {
							hideAutoComplete();

							ignoreKeyEnter = true;
						}

						return true;

					default:
				}
			});

			Wicket.Event.add(obj, 'change', function (jqEvent) {
				if (visible === 1) {
					// don't let any other change handler get this
					jqEvent.stopImmediatePropagation();

					triggerChangeOnHide = true;
				}
			});

			Wicket.Event.add(obj, 'input change', function (jqEvent) {
				const kc = Wicket.Event.keyCode(jqEvent);
				switch(kc) {
					case KEY_TAB:
					case KEY_ENTER:
						return jqEvent.stopImmediatePropagation();
					case KEY_UP:
					case KEY_DOWN:
					case KEY_ESC:
					case KEY_RIGHT:
					case KEY_LEFT:
					case KEY_SHIFT:
					case KEY_ALT:
					case KEY_CTRL:
						break;
					default:
						updateChoices();
				}
			});

			Wicket.Event.add(obj, 'keypress', function (jqEvent) {
				if(Wicket.Event.keyCode(jqEvent) === KEY_ENTER && ignoreKeyEnter){
					jqEvent.stopImmediatePropagation();
					return false;
				}
			});

			if (Wicket.Focus.getFocusedElement() === obj && isShowing === true)
			{
				// element already has focus, we should show list
				if (cfg.showListOnFocusGain) {
					if (cfg.showCompleteListOnFocusGain) {
						updateChoices(true);
					} else {
						updateChoices();
					}
				}
			}
		}

		function clearMenu()
		{
			// Remove the autocompletion menu if still present from
			// a previous call. This is required to properly register
			// the mouse event handler again
			const choiceDiv=document.getElementById(getMenuId());
			if (choiceDiv !== null) {
				choiceDiv.parentNode.parentNode.removeChild(choiceDiv.parentNode);
			}
		}

		function setSelected(newSelected) {
			if (newSelected !== selected) {
				selected = newSelected;
				selChSinceLastRender = true;
			}
		}

		function handleSelection(input) {
			const attr = getSelectableElement(selected).attributes.onselect;
			return attr ? eval(attr.value) : input;
		}

		function getSelectableElements() {
			const menu = getAutocompleteMenu();
			const firstChild = menu.firstChild;
			const selectableElements = [];
			if (firstChild.tagName.toLowerCase() === 'table') {
				let selectableInd=0;
				for (let i = 0; i < firstChild.childNodes.length; i++) {
					const tbody = firstChild.childNodes[i];
					for (let j = 0; j < tbody.childNodes.length; j++) {
						selectableElements[selectableInd++]=tbody.childNodes[j];
					}
				}
				return selectableElements;
			} else {
				return firstChild.childNodes;
			}
		}
		function getSelectableElement(selected) {
			const menu = getAutocompleteMenu();
			const firstChild = menu.firstChild;
			if (firstChild.tagName.toLowerCase() === 'table') {
				let selectableInd=0;
				for (let i = 0; i < firstChild.childNodes.length; i++) {
					const tbody = firstChild.childNodes[i];
					for (let j = 0; j < tbody.childNodes.length; j++) {
						if (selectableInd === selected) {
							return tbody.childNodes[j];
						}
						selectableInd++;
					}
				}
			}

			return firstChild.childNodes[selected];
		}

		function getMenuId() {
			return ajaxAttributes.c+"-autocomplete";
		}

		function getAutocompleteMenu() {
			let choiceDiv=document.getElementById(getMenuId());
			if (choiceDiv === null) {
				const container = document.createElement("div");
				container.className ="wicket-aa-container";
				if(cfg.className) {
				  container.className += ' ' + cfg.className;
				}
				document.body.appendChild(container);
				container.style.display="none";
				container.style.overflow="auto";
				container.style.position="absolute";
				container.style.margin="0px"; // this needs to be 0 or size/location calculations would not be exact
				container.style.padding="0px"; // this needs to be 0 or size/location calculations would not be exact
				container.id=getMenuId()+"-container";

				container.show = function() { Wicket.DOM.show(this.id); };
				container.hide = function() { Wicket.DOM.hide(this.id); };

				choiceDiv=document.createElement("div");
				container.appendChild(choiceDiv);
				choiceDiv.id=getMenuId();
				choiceDiv.className = "wicket-aa";
			}


			return choiceDiv;
		}

		function getAutocompleteContainer() {
			return getAutocompleteMenu().parentNode;
		}

		function updateChoices(showAll){
			setSelected(-1);
			if (showAll) {
				localThrottler.throttle(getMenuId(), throttleDelay, actualUpdateChoicesShowAll);
			} else {
				localThrottler.throttle(getMenuId(), throttleDelay, actualUpdateChoices);
			}
		}

		function actualUpdateChoicesShowAll() {
			prepareAndExecuteAjaxUpdate(doUpdateAllChoices, '');
		}

		function actualUpdateChoices() {
			prepareAndExecuteAjaxUpdate(doUpdateChoices, Wicket.$(ajaxAttributes.c).value);
		}

		function prepareAndExecuteAjaxUpdate(successHandler, currentInput){
			showIndicator();

			const attrs = Object.assign({}, ajaxAttributes);

			attrs.c = undefined;

			attrs.pre = attrs.pre || [];
			attrs.pre.push(function (attributes) {
				const input = Wicket.$(ajaxAttributes.c);
				if (!input) {
					// WICKET-6366 input might no longer be on page
					return false;
				}

				const activeIsInitial = (document.activeElement === initialElement);
				const hasMinimumLength = input.value.length >= minInputLength;

				const result = hasMinimumLength && activeIsInitial;

				if (!result) {
					hideAutoComplete();
				}

				return result;
			});

			attrs.sh = attrs.sh || [];
			attrs.sh.push(successHandler);

			attrs.ep = attrs.ep || [];
			attrs.ep.push({'name' : cfg.parameterName, 'value' : currentInput});

			Wicket.Ajax.ajax(attrs);
		}

		function showIndicator() {
			Wicket.DOM.show(ajaxAttributes.i);
		}

		function hideIndicator() {
			Wicket.DOM.hide(ajaxAttributes.i);
		}

		function showAutoComplete() {
			const input = Wicket.$(ajaxAttributes.c);
			const container = getAutocompleteContainer();
			const index=getOffsetParentZIndex(ajaxAttributes.c);
			container.show();

			// Accessibility
			const liElements = container.querySelectorAll("li");
			const size = liElements.length;

			liElements.forEach(function (el, index) {
				el.setAttribute("aria-posinset", index + 1);
				el.setAttribute("aria-setsize", size);
				el.setAttribute("tabindex", -1);
				el.setAttribute("role", "option");
			});

			container.querySelectorAll("ul").forEach(function (el) {
				el.setAttribute("id", "wicket-autocomplete-listbox-" + ajaxAttributes.c);
				el.setAttribute("role", "listbox");
			});
			input.setAttribute("aria-expanded", "true");


			if (!isNaN(Number(index))) {
				container.style.zIndex=(Number(index)+1);
			}
			if (!usefulDimensionsInitialized)
			{
				initializeUsefulDimensions(input, container);
			}
			if (cfg.adjustInputWidth) {
				const newW = input.offsetWidth-containerBorderWidths[0];
				container.style.width = (newW >= 0 ? newW : input.offsetWidth)+'px';
			}

			calculateAndSetPopupBounds(input, container);

			visible = 1;
			triggerChangeOnHide = false;
		}

		function initializeUsefulDimensions(input, container) {
			usefulDimensionsInitialized = true;
			// a few checks to increase the odds that we can count on clientWidth/Height
			if (typeof (container.clientWidth) !== "undefined" && typeof (container.clientHeight) !== "undefined" && container.clientWidth > 0 && container.clientHeight > 0) {
				const tmp = container.style.overflow; // clientWidth & clientHeight exclude border and scollbars
				container.style.overflow = "visible";
				containerBorderWidths[0] = container.offsetWidth - container.clientWidth;
				containerBorderWidths[1] = container.offsetHeight - container.clientHeight;

				if (cfg.useSmartPositioning) {
					container.style.overflow = "scroll";
					scrollbarSize = container.offsetWidth - container.clientWidth - containerBorderWidths[0];
				}
				container.style.overflow = tmp;
			}
		}

		function hideAutoComplete(){
			hideAutoCompleteTimer = undefined;

			const input = Wicket.$(ajaxAttributes.c);
			if (input) {
				input.setAttribute("aria-expanded", "false");
				input.removeAttribute("aria-activedescendant");
			}

			visible = 0;
			setSelected(-1);

			//WICKET-5382
			hideIndicator();

			const container = getAutocompleteContainer();
			if (container)
			{
				container.hide();
				if (!cfg.adjustInputWidth && container.style.width !== "auto") {
					container.style.width = "auto"; // let browser auto-set width again next time it is shown
				}
			}

			if (triggerChangeOnHide) {
				triggerChangeOnHide = false;
				isTriggeredChange = true;
				Wicket.Event.fire(input, 'change');
			}
		}

		function getWindowWidthAndHeigth() {
			return [ window.innerWidth, window.innerHeight ];
		}

		function getWindowScrollXY() {
			return [ window.scrollX, window.scrollY ];
		}

		function calculateAndSetPopupBounds(input, popup)
		{
			let leftPosition=0;
			let topPosition=0;
			const inputPosition=getPosition(input);
			if (cfg.useSmartPositioning) {
				// there are 4 possible positions for the popup: top-left, top-right, buttom-left, bottom-right
				// relative to the field; we will try to use the position that does not get out of the visible page
				if (popup.style.width === "auto") {
					popup.style.left = "0px"; // allow browser to stretch div as much as needed to see where the popup should be put
					popup.style.top = "0px";
				}
				const windowScrollXY = getWindowScrollXY();
				const windowWH = getWindowWidthAndHeigth();
				const windowScrollX = windowScrollXY[0];
				const windowScrollY = windowScrollXY[1];
				const windowWidth = windowWH[0];
				const windowHeight = windowWH[1];

				let dx1 = windowScrollX + windowWidth - inputPosition[0] - popup.offsetWidth;
				let dx2 = inputPosition[0] + input.offsetWidth - popup.offsetWidth - windowScrollX;
				if (popup.style.width === "auto" && dx1 < 0 && dx2 < 0) {
					// browser determined popup width; if it does not fit either right or left aligned with the input, calculate and set fixed width
					// so that after initial position calculation after popup opens, bounds do not change every time a mouse over or other event happens.
					// The browser can change the width/height when div if repositioned - if they were not already restricted because of maxHeight and field width (and that can result in a relocation of the div and so on).
					const newW = popup.offsetWidth + Math.max(dx1, dx2) - containerBorderWidths[0];
					popup.style.width = (newW >= 0 ? newW : popup.offsetWidth + Math.max(dx1, dx2))+'px';
					dx1 = windowScrollX + windowWidth - inputPosition[0] - popup.offsetWidth;
					dx2 = inputPosition[0] + input.offsetWidth - popup.offsetWidth - windowScrollX;
				}

				let dy1 = windowScrollY + windowHeight - inputPosition[1] - input.offsetHeight - popup.offsetHeight;
				let dy2 = inputPosition[1] - popup.offsetHeight - windowScrollY;
				if (dy1 < 0 && dy2 < 0) {
					// limit height if it gets outside the screen
					const newH = popup.offsetHeight + Math.max(dy1, dy2) - containerBorderWidths[1];
					popup.style.height = (newH >= 0 ? newH : popup.offsetHeight + Math.max(dy1, dy2))+'px';
					dy1 = windowScrollY + windowHeight - inputPosition[1] - input.offsetHeight - popup.offsetHeight;
					dy2 = inputPosition[1] - popup.offsetHeight - windowScrollY;
				}

				// choose the location that shows the most surface of the popup, with preference for bottom right
				if (dx1 < 0 && dx1 < dx2) {
					if (dy1 < 0 && dy1 < dy2) {
						// choice 4 : top left
						leftPosition = inputPosition[0] + input.offsetWidth - popup.offsetWidth;
						topPosition = inputPosition[1] - popup.offsetHeight;
					} else {
						// choice 3 : bottom left
						leftPosition = inputPosition[0] + input.offsetWidth - popup.offsetWidth;
						topPosition = inputPosition[1] + input.offsetHeight;
					}
				} else {
					if (dy1 < 0 && dy1 < dy2) {
						// choice 2 : top right
						leftPosition = inputPosition[0];
						topPosition = inputPosition[1] - popup.offsetHeight;
					} else {
						// choice 1 : bottom right
						leftPosition = inputPosition[0];
						topPosition = inputPosition[1] + input.offsetHeight;
					}
				}
				if (popup.style.width === "auto") {
					const newWidth = popup.offsetWidth - containerBorderWidths[0];
					popup.style.width = (newWidth >= 0 ? (newWidth + (popup.scrollWidth-popup.clientWidth)) : popup.offsetWidth)+'px';
				}
			} else {
				leftPosition = inputPosition[0];
				topPosition = inputPosition[1] + input.offsetHeight;
			}
			popup.style.left=leftPosition+'px';
			popup.style.top=topPosition+'px';
		}

		function getPosition(obj) {
			const rectangle = obj.getBoundingClientRect();

			let leftPosition = (rectangle.left + window.scrollX) || 0;
			let topPosition = (rectangle.top + window.scrollY) || 0;
			if (!cfg.ignoreBordersWhenPositioning) {
				topPosition += obj.clientTop || 0;
				leftPosition += obj.clientLeft || 0;
			}

			return [leftPosition,topPosition];
		}

		function doUpdateAllChoices(attributes, jqXHR, resp, textStatus) {
			doUpdateChoices(attributes, jqXHR, resp, textStatus, -1);
		}
		function doUpdateChoices(attributes, jqXHR, resp, textStatus, defaultSelection) {
			getAutocompleteMenu().showingAutocomplete = false;

			// check if the input hasn't been cleared in the meanwhile or has been replaced by ajax
			const input=Wicket.$(ajaxAttributes.c);
			if ((input !== initialElement) || (document.activeElement !== input) || !cfg.showListOnEmptyInput && (input.value === null || input.value === "")) {
				hideAutoComplete();
				hideIndicator();
				if (input !== initialElement)
				{
					clearMenu();
				}
				return;
			}

			const element = getAutocompleteMenu();
			if (!cfg.adjustInputWidth && element.parentNode && element.parentNode.style.width !== "auto") {
				element.parentNode.style.width = "auto"; // let browser auto-set width again as displayed elements may change
				selChSinceLastRender = true; // selected item will not have selected style until rendrered
			}
			element.innerHTML=resp;
			element.firstChild.role = "listbox";
			const selectableElements = getSelectableElements();
			if (selectableElements) {
				elementCount=selectableElements.length;

				const clickFunc = function(event) {
					// mouseOver might not be called, so select here at least
					setSelected(getElementIndex(this));

					let value = getSelectedValue();
					value = handleSelection(value);

					const input = Wicket.$(ajaxAttributes.c);
					if (value) {
						input.value = value;
						triggerChangeOnHide = true;
					}

					hideAutoComplete();

					if (document.activeElement !== input) {
						ignoreOneFocusGain = true;
						input.focus();
					}
					return true;
				};

				const mouseOverFunc = function(event) {
					setSelected(getElementIndex(this));
					render(false, false); // don't scroll - breaks mouse wheel scrolling
					showAutoComplete();
				};

				const mouseDownFunc = function(event) {
					// Give a chance the menu's blur event handler to be executed and eventually set
					// 'hideAutoCompleteTimer'
					// And then cancel the hiding of the menu
					window.setTimeout(function() {
						if (hideAutoCompleteTimer) {
							window.clearTimeout(hideAutoCompleteTimer);
						}
					}, 50);
				};
				for(let i = 0;i < elementCount; i++) {
					const node = selectableElements[i];
					node.onclick = clickFunc;
					node.onmouseover = mouseOverFunc;
					node.onmousedown = mouseDownFunc;
					node.role = "option";
					node.id = getMenuId() + '-item-' + i;
					node.setAttribute("tabindex", -1);
					node.setAttribute("aria-posinset", i + 1);
					node.setAttribute("aria-setsize", elementCount);
				}
			} else {
				elementCount=0;
			}

			if(elementCount>0){
				if(cfg.preselect === true){
					let selectedIndex = defaultSelection?defaultSelection:0;
					for(let ec = 0; ec < elementCount; ec++) {
						const selectableElement = selectableElements[ec];
						const attr = selectableElement.attributes.textvalue;
						let value;
						if (attr === undefined) {
							value = selectableElement.innerHTML;
						} else {
							value=attr.value;
						}
						if (value === input.value)
						{
							selectedIndex = ec;
							break;
						}
					}
					setSelected(selectedIndex);
				}

				if (isTriggeredChange) {
					isTriggeredChange = false;
				} else {
					showAutoComplete();
				}
			} else {
				hideAutoComplete();
			}
			render(false, true);

			scheduleEmptyCheck();

			Wicket.Log.info("Response processed successfully.");
			hideIndicator();
		}

		function scheduleEmptyCheck() {
			window.setTimeout(function() {
				const input=Wicket.$(ajaxAttributes.c);

				// WICKET-6366 input might no longer be on page
				if (input) {
					if (!cfg.showListOnEmptyInput && (input.value === null || input.value === "")) {
						hideAutoComplete();
					}
				}
			}, 100);
		}

		function getSelectedValue(){
			getAutocompleteMenu();
			const selectableElement = getSelectableElement(selected);
			const attr=selectableElement.attributes.textvalue;
			let value;
			if (!attr) {
				value=selectableElement.innerHTML;
			} else {
				value=attr.value;
			}
			return value;
		}

		function getElementIndex(element) {
			const selectableElements = getSelectableElements();
			for(let i=0;i<selectableElements.length;i++){
				const node=selectableElements[i];
				if(node === element) {
					return i;
				}
			}
			return -1;
		}

		function adjustScrollOffset(menu, item) { // this should consider margins/paddings; now it is not exact
			if (item.offsetTop + item.offsetHeight > menu.scrollTop + menu.offsetHeight) {
				menu.scrollTop = item.offsetTop + item.offsetHeight - menu.offsetHeight;
			} else
			// adjust to the top
			if (item.offsetTop < menu.scrollTop) {
				menu.scrollTop = item.offsetTop;
			}
		}

		function render(adjustScroll, adjustHeight) {
			const menu=getAutocompleteMenu();
			let height=0;
			let node=getSelectableElement(0);
			const re = /\bselected\b/gi;
			let sizeAffected = false;
			const input=Wicket.$(ajaxAttributes.c);

			for(let i=0;i<elementCount;i++)
			{
				const origClassNames = node.className;
				let classNames = origClassNames.replace(re, "");

				if(selected===i){
					classNames += " selected";

					if (node && node instanceof HTMLElement && node.attributes) {
						node.setAttribute("aria-selected", "true");
						input.setAttribute("aria-activedescendant", node.id);
					}

					if (adjustScroll) {
						adjustScrollOffset(menu.parentNode, node);
					}
				}
				else {
					if (node && node instanceof HTMLElement && node.attributes) {
						node.setAttribute("aria-selected", "false");
					}
				}

				if (classNames !== origClassNames) {
					node.className = classNames;
				}

				if (cfg.maxHeight > -1) {
					height+=node.offsetHeight;
				}
				node = node.nextSibling;
			}
			if (cfg.maxHeight > -1) {
				if (initialDelta === -1)
				{
					// remember size occupied by parent border, padding, and menu+ul margins, border, padding
					initialDelta = menu.parentNode.offsetHeight - height;
				}
				if (height + initialDelta > cfg.maxHeight) {
					const newH = cfg.maxHeight - containerBorderWidths[1];
					menu.parentNode.style.height = (newH >= 0 ? newH : cfg.maxHeight) + "px";
					sizeAffected = true;
				} else if (menu.parentNode.style.height !== "auto") { // if height is limited
					// this also changes the scroll, in some cases we don't want that
					if (adjustHeight)
					{
						menu.parentNode.style.height = "auto"; // no limiting, let popup determine it's own height
					}
					sizeAffected = true;
				}
			}
			if (cfg.useSmartPositioning && !cfg.adjustInputWidth && menu.parentNode.style.width !== "auto" && selChSinceLastRender) {
				// selected item has different padding - so the preferred width of the popup might want to change so as not to wrap it
				selChSinceLastRender = false;
				menu.parentNode.style.width = "auto";
				sizeAffected = true;
			}
			if (sizeAffected) {
				calculateAndSetPopupBounds(Wicket.$(ajaxAttributes.c), menu.parentNode);
			} // update stuff related to bounds if needed
		}

		function getStyle(obj, cssRule) {
			return window.getComputedStyle(obj).getPropertyValue(cssRule);
		}

		function isVisible(obj) {
			return getStyle(obj,"visibility");
		}

		function getOffsetParentZIndex(obj) {
		obj=typeof obj === "string" ? Wicket.$(obj):obj;
			obj=obj.offsetParent;
			let index="auto";
			do {
				const pos=getStyle(obj,"position");
				if(pos === "relative"||pos === "absolute"||pos === "fixed") {
					index=getStyle(obj,"z-index");
				}
				obj=obj.offsetParent;
			} while (obj && index === "auto");
			return index;
		}

		initialize();
	};
})();
