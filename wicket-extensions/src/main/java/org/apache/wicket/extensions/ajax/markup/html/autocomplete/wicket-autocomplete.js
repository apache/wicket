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

	Wicket.AutoComplete=function(elementId, callbackUrl, cfg, indicatorId){
		var KEY_TAB=9;
		var KEY_ENTER=13;
		var KEY_ESC=27;
		var KEY_LEFT=37;
		var KEY_UP=38;
		var KEY_RIGHT=39;
		var KEY_DOWN=40;
		var KEY_SHIFT=16;
		var KEY_CTRL=17;
		var KEY_ALT=18;

		var selected=-1;	// index of the currently selected item
		var elementCount=0; // number of items on the auto complete list
		var visible=0;		// is the list visible
		
		var ignoreFocus = false;		// ignore focus and gain because menu is showing
		var	ignoreKeyEnter = false;		// ignore key ENTER because is already hid the autocomplete list
		var ignoreOneFocusGain = false; // on FF, clicking an option in the pop-up would make field loose focus; focus() call only has effect in FF after popup is hidden, so the re-focusing must not show popup again in this case
		var ignoreChange = false;		// ignore change event because TAB or ENTER event already triggered a change

		var initialElement;

		// holds the eventual margins, padding, etc. of the menu container.
		// it is computed when the menu is first rendered, and then reused.
		var initialDelta = -1;
		// remember popup container border size so we can use style.width/height = ... correctly; array [horizontal, vertical]
		var usefulDimensionsInitialized = false;
		var containerBorderWidths = [0, 0];
		var scrollbarSize = 0;
		var selChSinceLastRender = false;

		// holds a throttler, for not sending many requests if the user types
		// too quickly.
		var localThrottler = new Wicket.Throttler(true);
		var throttleDelay = cfg.throttleDelay;

		//this is the minimum input length required to display the autocomplete list
		var minInputLength = cfg.showListOnEmptyInput === true ? 0 : cfg.minInputLength || 1;

		function initialize(){
			var isShowing = false;
			// Remove the autocompletion menu if still present from
			// a previous call. This is required to properly register
			// the mouse event handler again (using the new stateful 'ignoreFocus'
			// variable which just gets created)
			var choiceDiv = document.getElementById(getMenuId());
			if (choiceDiv !== null) {
				isShowing = choiceDiv.showingAutocomplete;
				choiceDiv.parentNode.parentNode.removeChild(choiceDiv.parentNode);
			}

			var obj = Wicket.$(elementId);
			initialElement = obj;

			Wicket.Event.add(obj, 'blur', function (jqEvent) {
				if (ignoreFocus) {
					ignoreOneFocusGain = true;
					Wicket.$(elementId).focus();
					return jqEvent.stopPropagation();
				}

				window.setTimeout(hideAutoComplete, 500);
			});

			Wicket.Event.add(obj, 'focus', function (jqEvent) {
				if (ignoreFocus) {
					ignoreOneFocusGain = false;
					return jqEvent.stopPropagation();
				}
				var input = jqEvent.target;
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
				switch(Wicket.Event.keyCode(jqEvent)){
					case KEY_UP:
						if (selected>-1) {
							setSelected(selected-1);
						}

						var searchTerm = Wicket.$(elementId).value;
						if(selected === -1 && searchTerm) {
							// select the last element
							setSelected(elementCount-1);
							showAutoComplete();
						}
						render(true, false);

						if (Wicket.Browser.isSafari()) {
							return jqEvent.stopPropagation();
						}
						break;
					case KEY_DOWN:
						if (selected < elementCount-1) {
							setSelected(selected+1);
						} else if (selected === elementCount-1) {
							// select the first element
							setSelected(0);
						}
						if (visible===0) {
							updateChoices();
						} else {
							render(true, false);
							showAutoComplete();
						}
						if (Wicket.Browser.isSafari()) {
							return jqEvent.stopPropagation();
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
						ignoreChange = false;
						ignoreKeyEnter = false;
						
						if (selected > -1) {
							var value = getSelectedValue();
							value = handleSelection(value);
							
							hideAutoComplete();
							
							if (value) {
								obj.value = value;
								jQuery(obj).triggerHandler('change');
								ignoreChange = true;
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
				if (ignoreFocus || ignoreChange) {
					// don't let any other change handler get this
					jqEvent.stopImmediatePropagation();
				}
			});

			Wicket.Event.add(obj, 'inputchange', function (jqEvent) {
				var kc = Wicket.Event.keyCode(jqEvent);
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
			// the mouse event handler again (using the new stateful 'ignoreFocus'
			// variable which just gets created)
			var choiceDiv=document.getElementById(getMenuId());
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
			var attr = getSelectableElement(selected).attributes.onselect;
			return attr ? eval(attr.value) : input;
		}

		function getSelectableElements() {
			var menu = getAutocompleteMenu();
			var firstChild = menu.firstChild;
			var selectableElements = [];
			if (firstChild.tagName.toLowerCase() === 'table') {
				var selectableInd=0;
				for (var i = 0; i < firstChild.childNodes.length; i++) {
					var tbody = firstChild.childNodes[i];
					for (var j = 0; j < tbody.childNodes.length; j++) {
						selectableElements[selectableInd++]=tbody.childNodes[j];
					}
				}
				return selectableElements;
			} else {
				return firstChild.childNodes;
			}
		}
		function getSelectableElement(selected) {
			var menu = getAutocompleteMenu();
			var firstChild = menu.firstChild;
			if (firstChild.tagName.toLowerCase() === 'table') {
				var selectableInd=0;
				for (var i = 0; i < firstChild.childNodes.length; i++) {
					var tbody = firstChild.childNodes[i];
					for (var j = 0; j < tbody.childNodes.length; j++) {
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
			return elementId+"-autocomplete";
		}

		function getAutocompleteMenu() {
			var choiceDiv=document.getElementById(getMenuId());
			if (choiceDiv === null) {
				var container = document.createElement("div");
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
				choiceDiv.className="wicket-aa";


				// WICKET-1350/WICKET-1351
				container.onmouseout = function() {ignoreFocus = false;};
				container.onmousemove = function() {ignoreFocus = true;};
			}


			return choiceDiv;
		}

		function getAutocompleteContainer() {
			var node=getAutocompleteMenu().parentNode;

			return node;
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
			showIndicator();

			var paramName = cfg.parameterName;
			var attrs = {
				u: callbackUrl,
				pre: [ function (attributes) {
					var activeIsInitial = (document.activeElement === initialElement);
					var elementVal =  Wicket.$(elementId).value;
					var hasMinimumLength = elementVal.length >= minInputLength;

					var result = hasMinimumLength && activeIsInitial;
					if (!result) {
						hideAutoComplete();
					}
					return result;
				}],
				ep: {},
				wr: false,
				dt: 'html',
				sh: [ doUpdateAllChoices ]
			};
			attrs.ep[paramName] = '';
			Wicket.Ajax.ajax(attrs);
		}

		function actualUpdateChoices() {
			showIndicator();

			var paramName = cfg.parameterName;
			var attrs = {
				u: callbackUrl,
				pre: [ function (attributes) {
					var activeIsInitial = (document.activeElement === initialElement);
					var elementVal =  Wicket.$(elementId).value;
					var hasMinimumLength = elementVal.length >= minInputLength;
					var result = hasMinimumLength && activeIsInitial;
					if (!result) {
						hideAutoComplete();
					}
					return result;
				}],
				ep: {},
				wr: false,
				dt: 'html',
				sh: [ doUpdateChoices ]
			};
			attrs.ep[paramName] = Wicket.$(elementId).value;
			Wicket.Ajax.ajax(attrs);
		}

		function showIndicator() {
			Wicket.DOM.show(indicatorId);
		}

		function hideIndicator() {
			Wicket.DOM.hide(indicatorId);
		}

		function showAutoComplete(){
			var input = Wicket.$(elementId);
			var container = getAutocompleteContainer();
			var index=getOffsetParentZIndex(elementId);
			container.show();
			if (!isNaN(Number(index))) {
				container.style.zIndex=(Number(index)+1);
			}
			if (!usefulDimensionsInitialized)
			{
				initializeUsefulDimensions(input, container);
			}
			if (cfg.adjustInputWidth) {
				var newW = input.offsetWidth-containerBorderWidths[0];
				container.style.width = (newW >= 0 ? newW : input.offsetWidth)+'px';
			}

			calculateAndSetPopupBounds(input, container);

			visible = 1;
		}

		function initializeUsefulDimensions(input, container) {
			usefulDimensionsInitialized = true;
			// a few checks to increase the odds that we can count on clientWidth/Height
			if (typeof (container.clientWidth) !== "undefined" && typeof (container.clientHeight) !== "undefined" && container.clientWidth > 0 && container.clientHeight > 0) {
				var tmp = container.style.overflow; // clientWidth & clientHeight exclude border and scollbars
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
			visible = 0;
			setSelected(-1);
			ignoreFocus = false;
			//WICKET-5382
			hideIndicator();
			
			var container = getAutocompleteContainer();
			if (container)
			{
				container.hide();
				if (!cfg.adjustInputWidth && container.style.width !== "auto") {
					container.style.width = "auto"; // let browser auto-set width again next time it is shown
				}
			}
		}

		function getWindowWidthAndHeigth() {
			var myWidth = 0, myHeight = 0;
			if( typeof( window.innerWidth ) === 'number' ) {
				//Non-IE
				myWidth = window.innerWidth;
				myHeight = window.innerHeight;
			} else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
				//IE 6+ in 'standards compliant mode'
				myWidth = document.documentElement.clientWidth;
				myHeight = document.documentElement.clientHeight;
			} else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
				//IE 4 compatible
				myWidth = document.body.clientWidth;
				myHeight = document.body.clientHeight;
			}
			return [ myWidth, myHeight ];
		}

		function getWindowScrollXY() {
			var scrOfX = 0, scrOfY = 0;
			if( typeof( window.pageYOffset ) === 'number' ) {
				//Netscape compliant
				scrOfY = window.pageYOffset;
				scrOfX = window.pageXOffset;
			} else if( document.body && ( document.body.scrollLeft || document.body.scrollTop ) ) {
				//DOM compliant
				scrOfY = document.body.scrollTop;
				scrOfX = document.body.scrollLeft;
			} else if( document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop ) ) {
				//IE6 standards compliant mode
				scrOfY = document.documentElement.scrollTop;
				scrOfX = document.documentElement.scrollLeft;
			}
			return [ scrOfX, scrOfY ];
		}

		function calculateAndSetPopupBounds(input, popup)
		{
			var leftPosition=0;
			var topPosition=0;
			var inputPosition=getPosition(input);
			if (cfg.useSmartPositioning) {
				// there are 4 possible positions for the popup: top-left, top-right, buttom-left, bottom-right
				// relative to the field; we will try to use the position that does not get out of the visible page
				if (popup.style.width === "auto") {
					popup.style.left = "0px"; // allow browser to stretch div as much as needed to see where the popup should be put
					popup.style.top = "0px";
				}
				var windowScrollXY = getWindowScrollXY();
				var windowWH = getWindowWidthAndHeigth();
				var windowScrollX = windowScrollXY[0];
				var windowScrollY = windowScrollXY[1];
				var windowWidth = windowWH[0];
				var windowHeight = windowWH[1];

				var dx1 = windowScrollX + windowWidth - inputPosition[0] - popup.offsetWidth;
				var dx2 = inputPosition[0] + input.offsetWidth - popup.offsetWidth - windowScrollX;
				if (popup.style.width === "auto" && dx1 < 0 && dx2 < 0) {
					// browser determined popup width; if it does not fit either right or left aligned with the input, calculate and set fixed width
					// so that after initial position calculation after popup opens, bounds do not change every time a mouse over or other event happens.
					// The browser can change the width/height when div if repositioned - if they were not already restricted because of maxHeight and field width (and that can result in a relocation of the div and so on).
					var newW = popup.offsetWidth + Math.max(dx1, dx2) - containerBorderWidths[0];
					popup.style.width = (newW >= 0 ? newW : popup.offsetWidth + Math.max(dx1, dx2))+'px';
					dx1 = windowScrollX + windowWidth - inputPosition[0] - popup.offsetWidth;
					dx2 = inputPosition[0] + input.offsetWidth - popup.offsetWidth - windowScrollX;
				}

				var dy1 = windowScrollY + windowHeight - inputPosition[1] - input.offsetHeight - popup.offsetHeight;
				var dy2 = inputPosition[1] - popup.offsetHeight - windowScrollY;
				if (dy1 < 0 && dy2 < 0) {
					// limit height if it gets outside the screen
					var newH = popup.offsetHeight + Math.max(dy1, dy2) - containerBorderWidths[1];
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
					var newWidth = popup.offsetWidth - containerBorderWidths[0];
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
			var rectangle = $(obj).offset();
			
			var leftPosition = rectangle.left || 0;
			var topPosition = rectangle.top || 0;
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
			var input=Wicket.$(elementId);
			if ((input !== initialElement) || (document.activeElement !== input) || !cfg.showListOnEmptyInput && (input.value === null || input.value === "")) {
				hideAutoComplete();
				hideIndicator();
				if (input !== initialElement)
				{
					clearMenu();
				}
				return;
			}

			var element = getAutocompleteMenu();
			if (!cfg.adjustInputWidth && element.parentNode && element.parentNode.style.width !== "auto") {
				element.parentNode.style.width = "auto"; // let browser auto-set width again as displayed elements may change
				selChSinceLastRender = true; // selected item will not have selected style until rendrered
			}
			element.innerHTML=resp;
			var selectableElements = getSelectableElements();
			if (selectableElements) {
				elementCount=selectableElements.length;

				var clickFunc = function(event) {
					ignoreFocus = false;
					ignoreChange = false;
					
					var value = getSelectedValue();
					value = handleSelection(value);
					hideAutoComplete();
					
					var input = Wicket.$(elementId);
					if (value) {
						input.value = value;
						jQuery(input).triggerHandler('change');
					}
					if (document.activeElement !== input) {
						ignoreOneFocusGain = true;
						input.focus();
					}
					return true;
				};

				var mouseOverFunc = function(event) {
					setSelected(getElementIndex(this));
					render(false, false); // don't scroll - breaks mouse wheel scrolling
					showAutoComplete();
				};
				for(var i = 0;i < elementCount; i++) {
					var node = selectableElements[i];
					node.onclick = clickFunc;
					node.onmouseover = mouseOverFunc;
				}
			} else {
				elementCount=0;
			}

			if(elementCount>0){
				if(cfg.preselect === true){
					var selectedIndex = defaultSelection?defaultSelection:0;
					for(var ec = 0; ec < elementCount; ec++) {
						var selectableElement = selectableElements[ec];
						var attr = selectableElement.attributes.textvalue;
						var value;
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
				showAutoComplete();
			} else {
				hideAutoComplete();
			}
			render(false, true);

			scheduleEmptyCheck();

			Wicket.Log.info("Response processed successfully.");
			hideIndicator();

			// hack for a focus issue in IE, WICKET-2279
			if (Wicket.Browser.isIE()) {
				var range = document.selection.createRange();
				if (range !== null) {
					range.select();
				}
			}
		}

		function scheduleEmptyCheck() {
			window.setTimeout(function() {
				var input=Wicket.$(elementId);
				if (!cfg.showListOnEmptyInput && (input.value === null || input.value === "")) {
					hideAutoComplete();
				}
			}, 100);
		}

		function getSelectedValue(){
			getAutocompleteMenu();
			var selectableElement = getSelectableElement(selected);
			var attr=selectableElement.attributes.textvalue;
			var value;
			if (!attr) {
				value=selectableElement.innerHTML;
			} else {
				value=attr.value;
			}
			return value;
		}

		function getElementIndex(element) {
			var selectableElements = getSelectableElements();
			for(var i=0;i<selectableElements.length;i++){
				var node=selectableElements[i];
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
			var menu=getAutocompleteMenu();
			var height=0;
			var node=getSelectableElement(0);
			var re = /\bselected\b/gi;
			var sizeAffected = false;
			for(var i=0;i<elementCount;i++)
			{
				var origClassNames = node.className;
				var classNames = origClassNames.replace(re, "");
				if(selected===i){
					classNames += " selected";
					if (adjustScroll) {
						adjustScrollOffset(menu.parentNode, node);
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
					var newH = cfg.maxHeight - containerBorderWidths[1];
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
				calculateAndSetPopupBounds(Wicket.$(elementId), menu.parentNode);
			} // update stuff related to bounds if needed
		}

		// From http://www.robertnyman.com/2006/04/24/get-the-rendered-style-of-an-element/
		function getStyle(obj,cssRule) {
			var cssRuleAlt = cssRule.replace(/\-(\w)/g,function(strMatch,p1){return p1.toUpperCase();});
			var value=obj.style[cssRuleAlt];
			if (!value) {
				if (document.defaultView && document.defaultView.getComputedStyle) {
					value = document.defaultView.getComputedStyle(obj,"").getPropertyValue(cssRule);
				}
				else if (obj.currentStyle)
				{
					value=obj.currentStyle[cssRuleAlt];
				}
			}
			return value;
		}

		function isVisible(obj) {
			return getStyle(obj,"visibility");
		}

		function getOffsetParentZIndex(obj) {
		obj=typeof obj === "string" ? Wicket.$(obj):obj;
			obj=obj.offsetParent;
			var index="auto";
			do {
				var pos=getStyle(obj,"position");
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
