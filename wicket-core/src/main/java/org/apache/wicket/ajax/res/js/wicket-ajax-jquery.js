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
 * *** DO NOT EDIT ***
 * This is a generated JS code, please DO NOT EDIT.
 * Edit TC files in wicket-core/src/main/typescript instead
 * *** DO NOT EDIT ***
 */
var Wicket = (function (exports) {
    'use strict';

    var Log = (function () {
        function Log() {
        }
        Log.enabled = function () {
            return Wicket.Ajax.DebugWindow && Wicket.Ajax.DebugWindow.enabled;
        };
        Log.info = function (msg) {
            if (Log.enabled()) {
                Wicket.Ajax.DebugWindow.logInfo(msg);
            }
        };
        Log.error = function (msg) {
            if (Log.enabled()) {
                Wicket.Ajax.DebugWindow.logError(msg);
            }
            else if (typeof (console) !== "undefined" && typeof (console.error) === 'function') {
                console.error('Wicket.Ajax: ', msg);
            }
        };
        Log.log = function (msg) {
            if (Log.enabled()) {
                Wicket.Ajax.DebugWindow.log(msg);
            }
        };
        return Log;
    }());

    var jQuery = window.jQuery;
    function isUndef(target) {
        return (typeof (target) === 'undefined' || target === null);
    }
    function $(arg) {
        if (isUndef(arg)) {
            return null;
        }
        if (arguments.length > 1) {
            var e = [];
            for (var i = 0; i < arguments.length; i++) {
                e.push($(arguments[i]));
            }
            return e;
        }
        else if (typeof arg === 'string') {
            return document.getElementById(arg);
        }
        else {
            return arg;
        }
    }
    function $$(element) {
        if (element === window) {
            return true;
        }
        if (typeof (element) === "string") {
            element = $(element);
        }
        if (isUndef(element) || isUndef(element.tagName)) {
            return false;
        }
        var id = element.getAttribute('id');
        if (isUndef(id) || id === "") {
            return element.ownerDocument === document;
        }
        else {
            return document.getElementById(id) === element;
        }
    }
    function merge(object1, object2) {
        return jQuery.extend({}, object1, object2);
    }
    function bind(fn, context) {
        return jQuery.proxy(fn, context);
    }
    function htmlToDomDocument(htmlDocument) {
        var xmlAsString = htmlDocument.body.outerText;
        xmlAsString = xmlAsString.replace(/^\s+|\s+$/g, '');
        xmlAsString = xmlAsString.replace(/(\n|\r)-*/g, '');
        var xmldoc = parseXML(xmlAsString);
        return xmldoc;
    }
    function parseXML(text) {
        var xmlDocument;
        if (window.DOMParser) {
            var parser = new DOMParser();
            xmlDocument = parser.parseFromString(text, "text/xml");
        }
        else if (window.ActiveXObject) {
            try {
                xmlDocument = new ActiveXObject("Msxml2.DOMDocument.6.0");
            }
            catch (err6) {
                try {
                    xmlDocument = new ActiveXObject("Msxml2.DOMDocument.5.0");
                }
                catch (err5) {
                    try {
                        xmlDocument = new ActiveXObject("Msxml2.DOMDocument.4.0");
                    }
                    catch (err4) {
                        try {
                            xmlDocument = new ActiveXObject("MSXML2.DOMDocument.3.0");
                        }
                        catch (err3) {
                            try {
                                xmlDocument = new ActiveXObject("Microsoft.XMLDOM");
                            }
                            catch (err2) {
                                Log.error("Cannot create DOM document: " + err2);
                            }
                        }
                    }
                }
            }
            if (xmlDocument) {
                xmlDocument.async = "false";
                if (!xmlDocument.loadXML(text)) {
                    Log.error("Error parsing response: " + text);
                }
            }
        }
        return xmlDocument;
    }
    function nodeListToArray(nodeList) {
        var arr = [], nodeId;
        if (nodeList && nodeList.length) {
            for (nodeId = 0; nodeId < nodeList.length; nodeId++) {
                arr.push(nodeList.item(nodeId));
            }
        }
        return arr;
    }
    function redirect(url) {
        window.location = url;
    }

    function create() {
        return function () {
            this.initialize.apply(this, arguments);
        };
    }

    var Class = /*#__PURE__*/Object.freeze({
        create: create
    });

    exports.Browser = {
        _isKHTML: null,
        isKHTML: function () {
            var wb = exports.Browser;
            if (wb._isKHTML === null) {
                wb._isKHTML = (/Konqueror|KHTML/).test(window.navigator.userAgent) && !/Apple/.test(window.navigator.userAgent);
            }
            return wb._isKHTML;
        },
        _isSafari: null,
        isSafari: function () {
            var wb = exports.Browser;
            if (wb._isSafari === null) {
                wb._isSafari = !/Chrome/.test(window.navigator.userAgent) && /KHTML/.test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent);
            }
            return wb._isSafari;
        },
        _isChrome: null,
        isChrome: function () {
            var wb = exports.Browser;
            if (wb._isChrome === null) {
                wb._isChrome = (/KHTML/).test(window.navigator.userAgent) && /Apple/.test(window.navigator.userAgent) && /Chrome/.test(window.navigator.userAgent);
            }
            return wb._isChrome;
        },
        _isOpera: null,
        isOpera: function () {
            var wb = exports.Browser;
            if (wb._isOpera === null) {
                wb._isOpera = !exports.Browser.isSafari() && typeof (window["opera"]) !== "undefined";
            }
            return wb._isOpera;
        },
        _isIE: null,
        isIE: function () {
            var wb = exports.Browser;
            if (wb._isIE === null) {
                wb._isIE = !exports.Browser.isSafari() && (typeof (document.all) !== "undefined" || window.navigator.userAgent.indexOf("Trident/") > -1) && typeof (window["opera"]) === "undefined";
            }
            return wb._isIE;
        },
        _isIEQuirks: null,
        isIEQuirks: function () {
            var wb = exports.Browser;
            if (wb._isIEQuirks === null) {
                wb._isIEQuirks = exports.Browser.isIE() && window.document.documentElement.clientHeight === 0;
            }
            return wb._isIEQuirks;
        },
        _isIELessThan9: null,
        isIELessThan9: function () {
            var wb = exports.Browser;
            if (wb._isIELessThan9 === null) {
                var index = window.navigator.userAgent.indexOf("MSIE");
                var version = parseFloat(window.navigator.userAgent.substring(index + 5));
                wb._isIELessThan9 = exports.Browser.isIE() && version < 9;
            }
            return wb._isIELessThan9;
        },
        _isIELessThan11: null,
        isIELessThan11: function () {
            var wb = exports.Browser;
            if (wb._isIELessThan11 === null) {
                wb._isIELessThan11 = !exports.Browser.isSafari() && typeof (document.all) !== "undefined" && typeof (window["opera"]) === "undefined";
            }
            return wb._isIELessThan11;
        },
        _isIE11: null,
        isIE11: function () {
            var wb = exports.Browser;
            if (wb._isIE11 === null) {
                var userAgent = window.navigator.userAgent;
                var isTrident = userAgent.indexOf("Trident") > -1;
                var is11 = userAgent.indexOf("rv:11") > -1;
                wb._isIE11 = isTrident && is11;
            }
            return wb._isIE11;
        },
        _isGecko: null,
        isGecko: function () {
            var wb = exports.Browser;
            if (wb._isGecko === null) {
                wb._isGecko = (/Gecko/).test(window.navigator.userAgent) && !exports.Browser.isSafari();
            }
            return wb._isGecko;
        }
    };

    var Event = (function () {
        function Event() {
        }
        Event.idCounter = 0;
        Event.Topic = {
            DOM_NODE_REMOVING: '/dom/node/removing',
            DOM_NODE_ADDED: '/dom/node/added',
            AJAX_CALL_INIT: '/ajax/call/init',
            AJAX_CALL_BEFORE: '/ajax/call/before',
            AJAX_CALL_PRECONDITION: '/ajax/call/precondition',
            AJAX_CALL_BEFORE_SEND: '/ajax/call/beforeSend',
            AJAX_CALL_SUCCESS: '/ajax/call/success',
            AJAX_CALL_COMPLETE: '/ajax/call/complete',
            AJAX_CALL_AFTER: '/ajax/call/after',
            AJAX_CALL_FAILURE: '/ajax/call/failure',
            AJAX_CALL_DONE: '/ajax/call/done',
            AJAX_HANDLERS_BOUND: '/ajax/handlers/bound'
        };
        Event.getId = function (element) {
            var $el = jQuery(element);
            var id = $el.prop("id");
            if (typeof (id) === "string" && id.length > 0) {
                return id;
            }
            else {
                id = "wicket-generated-id-" + Event.idCounter++;
                $el.prop("id", id);
                return id;
            }
        };
        Event.keyCode = function (evt) {
            return Event.fix(evt).keyCode;
        };
        Event.stop = function (evt, immediate) {
            evt = Event.fix(evt);
            if (immediate) {
                evt.stopImmediatePropagation();
            }
            else {
                evt.stopPropagation();
            }
            return evt;
        };
        Event.fix = function (evt) {
            return jQuery.event.fix(evt || window.event);
        };
        Event.fire = function (element, event) {
            event = (event === 'mousewheel' && exports.Browser.isGecko()) ? 'DOMMouseScroll' : event;
            jQuery(element).trigger(event);
        };
        Event.add = function (element, type, fn, data, selector) {
            if (type === 'domready') {
                jQuery(fn);
            }
            else if (type === 'load' && element === window) {
                jQuery(window).on('load', function () {
                    jQuery(fn);
                });
            }
            else {
                type = (type === 'mousewheel' && exports.Browser.isGecko()) ? 'DOMMouseScroll' : type;
                var el = element;
                if (typeof (element) === 'string') {
                    el = document.getElementById(element);
                }
                if (!el && Log) {
                    Log.error('Cannot bind a listener for event "' + type +
                        '" on element "' + element + '" because the element is not in the DOM');
                }
                jQuery(el).on(type, selector, data, fn);
            }
            return element;
        };
        Event.remove = function (element, type, fn) {
            jQuery(element).off(type, fn);
        };
        Event.subscribe = function (topic, subscriber) {
            if (topic) {
                jQuery(document).on(topic, subscriber);
            }
        };
        Event.unsubscribe = function (topic, subscriber) {
            if (topic) {
                if (subscriber) {
                    jQuery(document).off(topic, subscriber);
                }
                else {
                    jQuery(document).off(topic);
                }
            }
            else {
                jQuery(document).off();
            }
        };
        Event.publish = function (topic) {
            var args = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                args[_i - 1] = arguments[_i];
            }
            if (topic) {
                jQuery(document).triggerHandler(topic, args);
                jQuery(document).triggerHandler('*', args);
            }
        };
        return Event;
    }());

    function show(e, display) {
        e = $(e);
        if (e !== null) {
            if (isUndef(display)) {
                jQuery(e).show();
            }
            else {
                e.style.display = display;
            }
        }
    }
    function hide(e) {
        e = $(e);
        if (e !== null) {
            jQuery(e).hide();
        }
    }
    function toggleClass(elementId, cssClass, Switch) {
        jQuery('#' + elementId).toggleClass(cssClass, Switch);
    }
    function showIncrementally(e) {
        e = $(e);
        if (e === null) {
            return;
        }
        var count = e.getAttribute("showIncrementallyCount");
        count = parseInt(isUndef(count) ? 0 : count, 10);
        if (count >= 0) {
            show(e);
        }
        e.setAttribute("showIncrementallyCount", count + 1);
    }
    function hideIncrementally(e) {
        e = $(e);
        if (e === null) {
            return;
        }
        var count = e.getAttribute("showIncrementallyCount");
        count = parseInt(String(isUndef(count) ? 0 : count - 1), 10);
        if (count <= 0) {
            hide(e);
        }
        e.setAttribute("showIncrementallyCount", count);
    }
    function get(arg) {
        return $(arg);
    }
    function inDoc(element) {
        return $$(element);
    }
    function replace(element, text) {
        var we = Event;
        var topic = we.Topic;
        we.publish(topic.DOM_NODE_REMOVING, element);
        if (element.tagName.toLowerCase() === "title") {
            var titleText = />(.*?)</.exec(text)[1];
            document.title = titleText;
            return;
        }
        else {
            var cleanedText = jQuery.trim(text);
            var $newElement = jQuery(cleanedText);
            jQuery(element).replaceWith($newElement);
        }
        var newElement = $(element.id);
        if (newElement) {
            we.publish(topic.DOM_NODE_ADDED, newElement);
        }
    }
    function serializeNodeChildren(node) {
        if (isUndef(node)) {
            return "";
        }
        var result = [];
        if (node.childNodes.length > 0) {
            for (var i = 0; i < node.childNodes.length; i++) {
                var thisNode = node.childNodes[i];
                switch (thisNode.nodeType) {
                    case 1:
                    case 5:
                        result.push(this.serializeNode(thisNode));
                        break;
                    case 8:
                        result.push("<!--");
                        result.push(thisNode.nodeValue);
                        result.push("-->");
                        break;
                    case 4:
                        result.push("<![CDATA[");
                        result.push(thisNode.nodeValue);
                        result.push("]]>");
                        break;
                    case 3:
                    case 2:
                        result.push(thisNode.nodeValue);
                        break;
                    default:
                        break;
                }
            }
        }
        else {
            result.push(node.textContent || node.text);
        }
        return result.join("");
    }
    function serializeNode(node) {
        if (isUndef(node)) {
            return "";
        }
        var result = [];
        result.push("<");
        result.push(node.nodeName);
        if (node.attributes && node.attributes.length > 0) {
            for (var i = 0; i < node.attributes.length; i++) {
                if (node.attributes[i].nodeValue && node.attributes[i].specified) {
                    result.push(" ");
                    result.push(node.attributes[i].name);
                    result.push("=\"");
                    result.push(node.attributes[i].value);
                    result.push("\"");
                }
            }
        }
        result.push(">");
        result.push(serializeNodeChildren(node));
        result.push("</");
        result.push(node.nodeName);
        result.push(">");
        return result.join("");
    }
    function containsElement(element) {
        var id = element.getAttribute("id");
        if (id) {
            return $(id) !== null;
        }
        else {
            return false;
        }
    }
    function text(node) {
        if (isUndef(node)) {
            return "";
        }
        var result = [];
        if (node.childNodes.length > 0) {
            for (var i = 0; i < node.childNodes.length; i++) {
                var thisNode = node.childNodes[i];
                switch (thisNode.nodeType) {
                    case 1:
                    case 5:
                        result.push(this.text(thisNode));
                        break;
                    case 3:
                    case 4:
                        result.push(thisNode.nodeValue);
                        break;
                    default:
                        break;
                }
            }
        }
        else {
            result.push(node.textContent || node.text);
        }
        return result.join("");
    }

    var DOM = /*#__PURE__*/Object.freeze({
        show: show,
        hide: hide,
        toggleClass: toggleClass,
        showIncrementally: showIncrementally,
        hideIncrementally: hideIncrementally,
        get: get,
        inDoc: inDoc,
        replace: replace,
        serializeNodeChildren: serializeNodeChildren,
        serializeNode: serializeNode,
        containsElement: containsElement,
        text: text
    });

    var Focus = (function () {
        function Focus() {
        }
        Focus.focusin = function (event) {
            event = Event.fix(event);
            var target = event.target;
            if (target) {
                var WF = Focus;
                WF.refocusLastFocusedComponentAfterResponse = false;
                var id = target.id;
                WF.lastFocusId = id;
                Log.info("focus set on " + id);
            }
        };
        Focus.focusout = function (event) {
            event = Event.fix(event);
            var target = event.target;
            var WF = Focus;
            if (target && WF.lastFocusId === target.id) {
                var id = target.id;
                if (WF.refocusLastFocusedComponentAfterResponse) {
                    Log.info("focus removed from " + id + " but ignored because of component replacement");
                }
                else {
                    WF.lastFocusId = null;
                    Log.info("focus removed from " + id);
                }
            }
        };
        Focus.getFocusedElement = function () {
            var lastFocusId = Focus.lastFocusId;
            if (lastFocusId) {
                var focusedElement = $(lastFocusId);
                Log.info("returned focused element: " + focusedElement);
                return focusedElement;
            }
        };
        Focus.setFocusOnId = function (id) {
            var WF = Focus;
            if (id) {
                WF.refocusLastFocusedComponentAfterResponse = true;
                WF.focusSetFromServer = true;
                WF.lastFocusId = id;
                Log.info("focus set on " + id + " from server side");
            }
            else {
                WF.refocusLastFocusedComponentAfterResponse = false;
                Log.info("refocus focused component after request stopped from server side");
            }
        };
        Focus.markFocusedComponent = function () {
            var WF = Focus;
            var focusedElement = WF.getFocusedElement();
            if (focusedElement) {
                focusedElement.wasFocusedBeforeComponentReplacements = true;
                WF.refocusLastFocusedComponentAfterResponse = true;
                WF.focusSetFromServer = false;
            }
            else {
                WF.refocusLastFocusedComponentAfterResponse = false;
            }
        };
        Focus.checkFocusedComponentReplaced = function () {
            var WF = Focus;
            if (WF.refocusLastFocusedComponentAfterResponse) {
                var focusedElement = WF.getFocusedElement();
                if (focusedElement) {
                    if (typeof (focusedElement.wasFocusedBeforeComponentReplacements) !== "undefined") {
                        WF.refocusLastFocusedComponentAfterResponse = false;
                    }
                }
                else {
                    WF.refocusLastFocusedComponentAfterResponse = false;
                    WF.lastFocusId = "";
                }
            }
        };
        Focus.requestFocus = function () {
            var WF = Focus;
            if (WF.refocusLastFocusedComponentAfterResponse && WF.lastFocusId) {
                var toFocus_1 = $(WF.lastFocusId);
                if (toFocus_1) {
                    Log.info("Calling focus on " + WF.lastFocusId);
                    var safeFocus_1 = function () {
                        try {
                            toFocus_1.focus();
                        }
                        catch (ignore) {
                        }
                    };
                    if (WF.focusSetFromServer) {
                        window.setTimeout(safeFocus_1, 0);
                    }
                    else {
                        var temp_1 = toFocus_1.onfocus;
                        toFocus_1.onfocus = null;
                        window.setTimeout(function () {
                            safeFocus_1();
                            toFocus_1.onfocus = temp_1;
                        }, 0);
                    }
                }
                else {
                    WF.lastFocusId = "";
                    Log.info("Couldn't set focus on element with id '" + WF.lastFocusId + "' because it is not in the page anymore");
                }
            }
            else if (WF.refocusLastFocusedComponentAfterResponse) {
                Log.info("last focus id was not set");
            }
            else {
                Log.info("refocus last focused component not needed/allowed");
            }
            Focus.refocusLastFocusedComponentAfterResponse = false;
        };
        Focus.lastFocusId = "";
        Focus.refocusLastFocusedComponentAfterResponse = false;
        Focus.focusSetFromServer = false;
        return Focus;
    }());

    function parse(text) {
        return parseXML(text);
    }

    var Xml = /*#__PURE__*/Object.freeze({
        parse: parse
    });

    function encode(text) {
        if (window.encodeURIComponent) {
            return window.encodeURIComponent(text);
        }
        else {
            return window.escape(text);
        }
    }
    function serializeSelect(select) {
        var result = [];
        if (select) {
            var $select = jQuery(select);
            if ($select.length > 0 && $select.prop('disabled') === false) {
                var name_1 = $select.prop('name');
                var values = $select.val();
                if (jQuery.isArray(values)) {
                    for (var v = 0; v < values.length; v++) {
                        var value = values[v];
                        result.push({ name: name_1, value: value });
                    }
                }
                else {
                    result.push({ name: name_1, value: values });
                }
            }
        }
        return result;
    }
    function serializeInput(input) {
        var result = [];
        if (input && input.type) {
            var $input = jQuery(input);
            if (input.type === 'file') {
                for (var f = 0; f < input.files.length; f++) {
                    result.push({ "name": input.name, "value": input.files[f] });
                }
            }
            else if (!(input.type === 'image' || input.type === 'submit')) {
                result = $input.serializeArray();
            }
        }
        return result;
    }
    var excludeFromAjaxSerialization = {};
    function serializeElement(element, serializeRecursively) {
        if (!element) {
            return [];
        }
        else if (typeof (element) === 'string') {
            element = $(element);
        }
        if (excludeFromAjaxSerialization && element.id && excludeFromAjaxSerialization[element.id] === "true") {
            return [];
        }
        var tag = element.tagName.toLowerCase();
        if (tag === "select") {
            return serializeSelect(element);
        }
        else if (tag === "input" || tag === "textarea") {
            return serializeInput(element);
        }
        else {
            var result = [];
            if (serializeRecursively) {
                var elements = nodeListToArray(element.getElementsByTagName("input"));
                elements = elements.concat(nodeListToArray(element.getElementsByTagName("select")));
                elements = elements.concat(nodeListToArray(element.getElementsByTagName("textarea")));
                for (var i = 0; i < elements.length; ++i) {
                    var el = elements[i];
                    if (el.name && el.name !== "") {
                        result = result.concat(serializeElement(el, serializeRecursively));
                    }
                }
            }
            return result;
        }
    }
    function serializeForm(form) {
        var result = [], elements;
        if (form) {
            if (form.tagName.toLowerCase() === 'form') {
                elements = form.elements;
            }
            else {
                do {
                    form = form.parentNode;
                } while (form.tagName.toLowerCase() !== "form" && form.tagName.toLowerCase() !== "body");
                elements = nodeListToArray(form.getElementsByTagName("input"));
                elements = elements.concat(nodeListToArray(form.getElementsByTagName("select")));
                elements = elements.concat(nodeListToArray(form.getElementsByTagName("textarea")));
            }
        }
        for (var i = 0; i < elements.length; ++i) {
            var el = elements[i];
            if (el.name && el.name !== "") {
                result = result.concat(serializeElement(el, false));
            }
        }
        return result;
    }
    function serialize(element, dontTryToFindRootForm) {
        if (typeof (element) === 'string') {
            element = $(element);
        }
        if (element.tagName.toLowerCase() === "form") {
            return serializeForm(element);
        }
        else {
            var elementBck = element;
            if (dontTryToFindRootForm !== true) {
                do {
                    element = element.parentNode;
                } while (element.tagName.toLowerCase() !== "form" && element.tagName.toLowerCase() !== "body");
            }
            if (element.tagName.toLowerCase() === "form") {
                return serializeForm(element);
            }
            else {
                var form = document.createElement("form");
                var parent_1 = elementBck.parentNode;
                parent_1.replaceChild(form, elementBck);
                form.appendChild(elementBck);
                var result = serializeForm(form);
                parent_1.replaceChild(elementBck, form);
                return result;
            }
        }
    }

    var Form = /*#__PURE__*/Object.freeze({
        encode: encode,
        serializeSelect: serializeSelect,
        serializeInput: serializeInput,
        excludeFromAjaxSerialization: excludeFromAjaxSerialization,
        serializeElement: serializeElement,
        serializeForm: serializeForm,
        serialize: serialize
    });

    var FunctionsExecuter = (function () {
        function FunctionsExecuter(functions) {
            this.functions = functions;
            this.current = 0;
            this.depth = 0;
        }
        FunctionsExecuter.prototype.processNext = function () {
            if (this.current < this.functions.length) {
                var f_1, run = void 0;
                f_1 = this.functions[this.current];
                run = function () {
                    try {
                        var n = jQuery.proxy(this.notify, this);
                        return f_1(n);
                    }
                    catch (e) {
                        Log.error("FunctionsExecuter.processNext: " + e);
                        return FunctionsExecuter.FAIL;
                    }
                };
                run = jQuery.proxy(run, this);
                this.current++;
                if (this.depth > FunctionsExecuter.DEPTH_LIMIT) {
                    this.depth = 0;
                    window.setTimeout(run, 1);
                }
                else {
                    var retValue = run();
                    if (isUndef(retValue) || retValue === FunctionsExecuter.ASYNC) {
                        this.depth++;
                    }
                    return retValue;
                }
            }
        };
        FunctionsExecuter.prototype.start = function () {
            var retValue = FunctionsExecuter.DONE;
            while (retValue === FunctionsExecuter.DONE) {
                retValue = this.processNext();
            }
        };
        FunctionsExecuter.prototype.notify = function () {
            this.start();
        };
        FunctionsExecuter.DONE = 1;
        FunctionsExecuter.FAIL = 2;
        FunctionsExecuter.ASYNC = 3;
        FunctionsExecuter.DEPTH_LIMIT = 1000;
        return FunctionsExecuter;
    }());

    function parse$1(headerNode) {
        var text$1 = text(headerNode);
        if (exports.Browser.isKHTML()) {
            text$1 = text$1.replace(/<script/g, "<SCRIPT");
            text$1 = text$1.replace(/<\/script>/g, "</SCRIPT>");
        }
        var xmldoc = parse(text$1);
        return xmldoc;
    }
    function _checkParserError(node) {
        var result = false;
        if (!isUndef(node.tagName) && node.tagName.toLowerCase() === "parsererror") {
            Log.error("Error in parsing: " + node.textContent);
            result = true;
        }
        return result;
    }
    function processContribution(context, headerNode) {
        var xmldoc = this.parse(headerNode);
        var rootNode = xmldoc.documentElement;
        if (this._checkParserError(rootNode)) {
            return;
        }
        for (var i = 0; i < rootNode.childNodes.length; i++) {
            var node = rootNode.childNodes[i];
            if (this._checkParserError(node)) {
                return;
            }
            if (!isUndef(node.tagName)) {
                var name_1 = node.tagName.toLowerCase();
                if (name_1 === "wicket:link") {
                    for (var j = 0; j < node.childNodes.length; ++j) {
                        var childNode = node.childNodes[j];
                        if (childNode.nodeType === 1) {
                            node = childNode;
                            name_1 = node.tagName.toLowerCase();
                            break;
                        }
                    }
                }
                if (name_1 === "link") {
                    this.processLink(context, node);
                }
                else if (name_1 === "script") {
                    this.processScript(context, node);
                }
                else if (name_1 === "style") {
                    this.processStyle(context, node);
                }
                else if (name_1 === "meta") {
                    this.processMeta(context, node);
                }
            }
            else if (node.nodeType === 8) {
                this.processComment(context, node);
            }
        }
    }
    function processLink(context, node) {
        context.steps.push(function (notify) {
            var res = containsElement$1(node, "href");
            var oldNode = res.oldNode;
            if (res.contains) {
                return FunctionsExecuter.DONE;
            }
            else if (oldNode) {
                oldNode.parentNode.removeChild(oldNode);
            }
            var css = createElement("link");
            var attributes = jQuery(node).prop("attributes");
            var $css = jQuery(css);
            jQuery.each(attributes, function () {
                $css.attr(this.name, this.value);
            });
            addElement(css);
            var img = document.createElement('img');
            var notifyCalled = false;
            img.onerror = function () {
                if (!notifyCalled) {
                    notifyCalled = true;
                    notify();
                }
            };
            img.src = css.href;
            if (img.complete) {
                if (!notifyCalled) {
                    notifyCalled = true;
                    notify();
                }
            }
            return FunctionsExecuter.ASYNC;
        });
    }
    function processStyle(context, node) {
        context.steps.push(function (notify) {
            if (containsElement(node)) {
                return FunctionsExecuter.DONE;
            }
            var content = serializeNodeChildren(node);
            if (exports.Browser.isIELessThan11()) {
                try {
                    document.createStyleSheet().cssText = content;
                    return FunctionsExecuter.DONE;
                }
                catch (ignore) {
                    var run = function () {
                        try {
                            document.createStyleSheet().cssText = content;
                        }
                        catch (e) {
                            Log.error("Wicket.Head.Contributor.processStyle: " + e);
                        }
                        notify();
                    };
                    window.setTimeout(run, 1);
                    return FunctionsExecuter.ASYNC;
                }
            }
            else {
                var style = createElement("style");
                style.id = node.getAttribute("id");
                var textNode = document.createTextNode(content);
                style.appendChild(textNode);
                addElement(style);
            }
            return FunctionsExecuter.DONE;
        });
    }
    function processScript(context, node) {
        context.steps.push(function (notify) {
            if (!node.getAttribute("src") && containsElement(node)) {
                return FunctionsExecuter.DONE;
            }
            else {
                var res = containsElement$1(node, "src");
                var oldNode = res.oldNode;
                if (res.contains) {
                    return FunctionsExecuter.DONE;
                }
                else if (oldNode) {
                    oldNode.parentNode.removeChild(oldNode);
                }
            }
            var src = node.getAttribute("src");
            if (src !== null && src !== "") {
                var scriptDomNode_1 = document.createElement("script");
                var attrs = node.attributes;
                for (var a = 0; a < attrs.length; a++) {
                    var attr = attrs[a];
                    scriptDomNode_1[attr.name] = attr.value;
                }
                var onScriptReady_1 = function () {
                    notify();
                };
                if (typeof (scriptDomNode_1.onload) !== 'undefined') {
                    scriptDomNode_1.onload = onScriptReady_1;
                }
                else if (typeof (scriptDomNode_1.onreadystatechange) !== 'undefined') {
                    scriptDomNode_1.onreadystatechange = function () {
                        if (scriptDomNode_1.readyState === 'loaded' || scriptDomNode_1.readyState === 'complete') {
                            onScriptReady_1();
                        }
                    };
                }
                else if (exports.Browser.isGecko()) {
                    scriptDomNode_1.onload = onScriptReady_1;
                }
                else {
                    window.setTimeout(onScriptReady_1, 10);
                }
                addElement(scriptDomNode_1);
                return FunctionsExecuter.ASYNC;
            }
            else {
                var text = serializeNodeChildren(node);
                text = text.replace(/^\n\/\*<!\[CDATA\[\*\/\n/, "");
                text = text.replace(/\n\/\*\]\]>\*\/\n$/, "");
                var id = node.getAttribute("id");
                var type = node.getAttribute("type");
                if (typeof (id) === "string" && id.length > 0) {
                    addJavascript(text, id, "", type);
                }
                else {
                    try {
                        window.eval(text);
                    }
                    catch (e) {
                        Log.error("Wicket.Head.Contributor.processScript: " + e + ": eval -> " + text);
                    }
                }
                return FunctionsExecuter.DONE;
            }
        });
    }
    function processMeta(context, node) {
        context.steps.push(function (notify) {
            var meta = createElement("meta"), $meta = jQuery(meta), attrs = jQuery(node).prop("attributes"), name = node.getAttribute("name");
            if (name) {
                jQuery('meta[name="' + name + '"]').remove();
            }
            jQuery.each(attrs, function () {
                $meta.attr(this.name, this.value);
            });
            addElement(meta);
            return FunctionsExecuter.DONE;
        });
    }
    function processComment(context, node) {
        context.steps.push(function (notify) {
            var comment = document.createComment(node.nodeValue);
            addElement(comment);
            return FunctionsExecuter.DONE;
        });
    }

    var Contributor = /*#__PURE__*/Object.freeze({
        parse: parse$1,
        _checkParserError: _checkParserError,
        processContribution: processContribution,
        processLink: processLink,
        processStyle: processStyle,
        processScript: processScript,
        processMeta: processMeta,
        processComment: processComment
    });

    function createElement(name) {
        if (isUndef(name) || name === '') {
            Log.error('Cannot create an element without a name');
            return;
        }
        return document.createElement(name);
    }
    function addElement(element) {
        var headItems = document.querySelector('head meta[name="wicket.header.items"]');
        if (headItems) {
            headItems.parentNode.insertBefore(element, headItems);
        }
        else {
            var head = document.querySelector("head");
            if (head) {
                head.appendChild(element);
            }
        }
    }
    function containsElement$1(element, mandatoryAttribute) {
        var attr = element.getAttribute(mandatoryAttribute);
        if (isUndef(attr) || attr === "") {
            return {
                contains: false
            };
        }
        var elementTagName = element.tagName.toLowerCase();
        var elementId = element.getAttribute("id");
        var head = document.getElementsByTagName("head")[0];
        if (elementTagName === "script") {
            head = document;
        }
        var nodes = head.getElementsByTagName(elementTagName);
        for (var i = 0; i < nodes.length; ++i) {
            var node = nodes[i];
            if (node.tagName.toLowerCase() === elementTagName) {
                var loadedUrl = node.getAttribute(mandatoryAttribute);
                var loadedUrl_ = node.getAttribute(mandatoryAttribute + "_");
                if (loadedUrl === attr || loadedUrl_ === attr) {
                    return {
                        contains: true
                    };
                }
                else if (elementId && elementId === node.getAttribute("id")) {
                    return {
                        contains: false,
                        oldNode: node
                    };
                }
            }
        }
        return {
            contains: false
        };
    }
    function addJavascript(content, id, fakeSrc, type) {
        var script = createElement("script");
        if (id) {
            script.id = id;
        }
        if (!type || type.toLowerCase() === "text/javascript") {
            type = "text/javascript";
            content = 'try{' + content + '}catch(e){Wicket.Log.error(e);}';
        }
        script.setAttribute("src_", fakeSrc);
        script.setAttribute("type", type);
        if (null === script.canHaveChildren || script.canHaveChildren) {
            var textNode = document.createTextNode(content);
            script.appendChild(textNode);
        }
        else {
            script.text = content;
        }
        addElement(script);
    }
    function addJavascripts(element, contentFilter) {
        function add(element) {
            var src = element.getAttribute("src");
            var type = element.getAttribute("type");
            if (src !== null && src.length > 0) {
                var e = document.createElement("script");
                if (type) {
                    e.setAttribute("type", type);
                }
                e.setAttribute("src", src);
                addElement(e);
            }
            else {
                var content = serializeNodeChildren(element);
                if (isUndef(content) || content === "") {
                    content = element.text;
                }
                if (typeof (contentFilter) === "function") {
                    content = contentFilter(content);
                }
                addJavascript(content, element.id, "", type);
            }
        }
        if (typeof (element) !== "undefined" &&
            typeof (element.tagName) !== "undefined" &&
            element.tagName.toLowerCase() === "script") {
            add(element);
        }
        else {
            if (element.childNodes.length > 0) {
                var scripts = element.getElementsByTagName("script");
                for (var i = 0; i < scripts.length; ++i) {
                    add(scripts[i]);
                }
            }
        }
    }

    var Head = /*#__PURE__*/Object.freeze({
        Contributor: Contributor,
        createElement: createElement,
        addElement: addElement,
        containsElement: containsElement$1,
        addJavascript: addJavascript,
        addJavascripts: addJavascripts
    });

    var Drag = {
        current: undefined,
        init: function (element, onDragBegin, onDragEnd, onDrag) {
            if (typeof (onDragBegin) === "undefined") {
                onDragBegin = jQuery.noop;
            }
            if (typeof (onDragEnd) === "undefined") {
                onDragEnd = jQuery.noop;
            }
            if (typeof (onDrag) === "undefined") {
                onDrag = jQuery.noop;
            }
            element.wicketOnDragBegin = onDragBegin;
            element.wicketOnDrag = onDrag;
            element.wicketOnDragEnd = onDragEnd;
            Event.add(element, "mousedown", Drag.mouseDownHandler);
        },
        mouseDownHandler: function (e) {
            e = Event.fix(e);
            var element = this;
            if (element.wicketOnDragBegin(element, e) === false) {
                return;
            }
            if (e.preventDefault) {
                e.preventDefault();
            }
            element.lastMouseX = e.clientX;
            element.lastMouseY = e.clientY;
            element.old_onmousemove = document.onmousemove;
            element.old_onmouseup = document.onmouseup;
            element.old_onselectstart = document.onselectstart;
            element.old_onmouseout = document.onmouseout;
            document.onselectstart = function () {
                return false;
            };
            document.onmousemove = Drag.mouseMove;
            document.onmouseup = Drag.mouseUp;
            document.onmouseout = Drag.mouseOut;
            Drag.current = element;
        },
        clean: function (element) {
            element.onmousedown = null;
        },
        mouseMove: function (e) {
            e = Event.fix(e);
            var o = Drag.current;
            if (e.clientX < 0 || e.clientY < 0) {
                return;
            }
            if (o !== null) {
                var deltaX = e.clientX - o.lastMouseX;
                var deltaY = e.clientY - o.lastMouseY;
                var res = o.wicketOnDrag(o, deltaX, deltaY, e);
                if (isUndef(res)) {
                    res = [0, 0];
                }
                o.lastMouseX = e.clientX + res[0];
                o.lastMouseY = e.clientY + res[1];
            }
            return false;
        },
        mouseUp: function (e) {
            var o = Drag.current;
            if (o) {
                o.wicketOnDragEnd(o);
                o.lastMouseX = null;
                o.lastMouseY = null;
                document.onmousemove = o.old_onmousemove;
                document.onmouseup = o.old_onmouseup;
                document.onselectstart = o.old_onselectstart;
                document.onmouseout = o.old_onmouseout;
                o.old_mousemove = null;
                o.old_mouseup = null;
                o.old_onselectstart = null;
                o.old_onmouseout = null;
                Drag.current = null;
            }
        },
        mouseOut: function (e) {
        }
    };

    var TimerHandles = {};
    var Timer = {
        'set': function (timerId, f, delay) {
            Timer.clear(timerId);
            TimerHandles[timerId] = setTimeout(function () {
                Timer.clear(timerId);
                f();
            }, delay);
        },
        clear: function (timerId) {
            if (TimerHandles && TimerHandles[timerId]) {
                clearTimeout(TimerHandles[timerId]);
                delete TimerHandles[timerId];
            }
        },
        clearAll: function () {
            var WTH = TimerHandles;
            if (WTH) {
                for (var th in WTH) {
                    if (WTH.hasOwnProperty(th)) {
                        Timer.clear(th);
                    }
                }
            }
        }
    };

    var Channel = (function () {
        function Channel(name) {
            var res = name.match(/^([^|]+)\|(d|s|a)$/);
            if (isUndef(res)) {
                this.name = '0';
                this.type = 's';
            }
            else {
                this.name = res[1];
                this.type = res[2];
            }
            this.callbacks = [];
            this.busy = false;
        }
        Channel.prototype.schedule = function (callback) {
            if (this.busy === false) {
                this.busy = true;
                try {
                    return callback();
                }
                catch (exception) {
                    this.busy = false;
                    Log.error("An error occurred while executing Ajax request:" + exception);
                }
            }
            else {
                var busyChannel = "Channel '" + this.name + "' is busy";
                if (this.type === 's') {
                    Log.info(busyChannel + " - scheduling the callback to be executed when the previous request finish.");
                    this.callbacks.push(callback);
                }
                else if (this.type === 'd') {
                    Log.info(busyChannel + " - dropping all previous scheduled callbacks and scheduling a new one to be executed when the current request finish.");
                    this.callbacks = [];
                    this.callbacks.push(callback);
                }
                else if (this.type === 'a') {
                    Log.info(busyChannel + " - ignoring the Ajax call because there is a running request.");
                }
                return null;
            }
        };
        Channel.prototype.done = function () {
            var callback = null;
            if (this.callbacks.length > 0) {
                callback = this.callbacks.shift();
            }
            if (callback !== null && typeof (callback) !== "undefined") {
                Log.info("Calling postponed function...");
                window.setTimeout(callback, 1);
            }
            else {
                this.busy = false;
            }
        };
        return Channel;
    }());

    var ChannelManager = (function () {
        function ChannelManager() {
            this.channels = [];
        }
        ChannelManager.prototype.schedule = function (channel, callback) {
            var parsed = new Channel(channel);
            var c = this.channels[parsed.name];
            if (isUndef(c)) {
                c = parsed;
                this.channels[c.name] = c;
            }
            else {
                c.type = parsed.type;
            }
            return c.schedule(callback);
        };
        ChannelManager.prototype.done = function (channel) {
            var parsed = new Channel(channel);
            var c = this.channels[parsed.name];
            if (!isUndef(c)) {
                c.done();
                if (!c.busy) {
                    delete this.channels[parsed.name];
                }
            }
        };
        ChannelManager.FunctionsExecuter = FunctionsExecuter;
        return ChannelManager;
    }());
    var channelManager = new ChannelManager();

    var Call = (function () {
        function Call() {
        }
        Call.prototype._initializeDefaults = function (attrs) {
            if (typeof (attrs.ch) !== 'string') {
                attrs.ch = '0|s';
            }
            if (typeof (attrs.wr) !== 'boolean') {
                attrs.wr = true;
            }
            if (typeof (attrs.dt) !== 'string') {
                attrs.dt = 'xml';
            }
            if (typeof (attrs.m) !== 'string') {
                attrs.m = 'GET';
            }
            if (attrs.async !== false) {
                attrs.async = true;
            }
            if (!jQuery.isNumeric(attrs.rt)) {
                attrs.rt = 0;
            }
            if (attrs.pd !== true) {
                attrs.pd = false;
            }
            if (!attrs.sp) {
                attrs.sp = "bubble";
            }
            if (!attrs.sr) {
                attrs.sr = false;
            }
        };
        Call.prototype._getTarget = function (attrs) {
            var target;
            if (attrs.event) {
                target = attrs.event.target;
            }
            else if (!jQuery.isWindow(attrs.c)) {
                target = $(attrs.c);
            }
            else {
                target = window;
            }
            return target;
        };
        Call.prototype._executeHandlers = function (handlers) {
            var args = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                args[_i - 1] = arguments[_i];
            }
            if (jQuery.isArray(handlers)) {
                var attrs = args[0];
                var that = this._getTarget(attrs);
                for (var i = 0; i < handlers.length; i++) {
                    var handler = handlers[i];
                    if (jQuery.isFunction(handler)) {
                        handler.apply(that, args);
                    }
                    else {
                        new Function(handler).apply(that, args);
                    }
                }
            }
        };
        Call.prototype._asParamArray = function (parameters) {
            var result = [], value, name;
            if (jQuery.isArray(parameters)) {
                result = parameters;
            }
            else if (jQuery.isPlainObject(parameters)) {
                for (name in parameters) {
                    if (name && parameters.hasOwnProperty(name)) {
                        value = parameters[name];
                        result.push({ name: name, value: value });
                    }
                }
            }
            for (var i = 0; i < result.length; i++) {
                if (result[i] === null) {
                    result.splice(i, 1);
                    i--;
                }
            }
            return result;
        };
        Call.prototype._calculateDynamicParameters = function (attrs) {
            var deps = attrs.dep, params = [];
            for (var i = 0; i < deps.length; i++) {
                var dep = deps[i], extraParam = void 0;
                if (jQuery.isFunction(dep)) {
                    extraParam = dep(attrs);
                }
                else {
                    extraParam = new Function('attrs', dep)(attrs);
                }
                extraParam = this._asParamArray(extraParam);
                params = params.concat(extraParam);
            }
            return params;
        };
        Call.prototype.ajax = function (attrs) {
            this._initializeDefaults(attrs);
            var res = channelManager.schedule(attrs.ch, bind(function () {
                this.doAjax(attrs);
            }, this));
            return res !== null ? res : true;
        };
        Call.prototype._isPresent = function (id) {
            if (isUndef(id)) {
                return true;
            }
            var element = $(id);
            if (isUndef(element)) {
                return false;
            }
            return (!element.hasAttribute || !element.hasAttribute('data-wicket-placeholder'));
        };
        Call.prototype.doAjax = function (attrs) {
            var headers = {
                'Wicket-Ajax': 'true',
                'Wicket-Ajax-BaseURL': Ajax.getAjaxBaseUrl()
            }, url = attrs.u, data = this._asParamArray(attrs.ep), self = this, defaultPrecondition = [function (attributes) {
                    return self._isPresent(attributes.c) && self._isPresent(attributes.f);
                }], context = {
                attrs: attrs,
                steps: []
            }, we = Event, topic = we.Topic;
            if (Focus.lastFocusId) {
                headers["Wicket-FocusedElementId"] = encode(Focus.lastFocusId);
            }
            self._executeHandlers(attrs.bh, attrs);
            we.publish(topic.AJAX_CALL_BEFORE, attrs);
            var preconditions = attrs.pre || [];
            preconditions = defaultPrecondition.concat(preconditions);
            if (jQuery.isArray(preconditions)) {
                var that = this._getTarget(attrs);
                for (var p = 0; p < preconditions.length; p++) {
                    var precondition = preconditions[p];
                    var result = void 0;
                    if (jQuery.isFunction(precondition)) {
                        result = precondition.call(that, attrs);
                    }
                    else {
                        result = new Function(precondition).call(that, attrs);
                    }
                    if (result === false) {
                        Log.info("Ajax request stopped because of precondition check, url: " + attrs.u);
                        self.done(attrs);
                        return false;
                    }
                }
            }
            we.publish(topic.AJAX_CALL_PRECONDITION, attrs);
            if (attrs.f) {
                var form = $(attrs.f);
                data = data.concat(serializeForm(form));
                if (attrs.sc) {
                    var scName = attrs.sc;
                    data = data.concat({ name: scName, value: 1 });
                }
            }
            else if (attrs.c && !jQuery.isWindow(attrs.c)) {
                var el = $(attrs.c);
                data = data.concat(serializeElement(el, attrs.sr));
            }
            if (jQuery.isArray(attrs.dep)) {
                var dynamicData = this._calculateDynamicParameters(attrs);
                if (attrs.m.toLowerCase() === 'post') {
                    data = data.concat(dynamicData);
                }
                else {
                    var separator = url.indexOf('?') > -1 ? '&' : '?';
                    url = url + separator + jQuery.param(dynamicData);
                }
            }
            var wwwFormUrlEncoded;
            if (attrs.mp) {
                try {
                    var formData = new FormData();
                    for (var i = 0; i < data.length; i++) {
                        formData.append(data[i].name, data[i].value || "");
                    }
                    data = formData;
                    wwwFormUrlEncoded = false;
                }
                catch (exception) {
                    Log.error("Ajax multipat not supported:" + exception);
                }
            }
            var jqXHR = jQuery.ajax({
                url: url,
                type: attrs.m,
                context: self,
                processData: wwwFormUrlEncoded,
                contentType: wwwFormUrlEncoded,
                beforeSend: function (jqXHR, settings) {
                    self._executeHandlers(attrs.bsh, attrs, jqXHR, settings);
                    we.publish(topic.AJAX_CALL_BEFORE_SEND, attrs, jqXHR, settings);
                    if (attrs.i) {
                        showIncrementally(attrs.i);
                    }
                },
                data: data,
                dataType: attrs.dt,
                async: attrs.async,
                timeout: attrs.rt,
                cache: false,
                headers: headers,
                success: function (data, textStatus, jqXHR) {
                    if (attrs.wr) {
                        self.processAjaxResponse(data, textStatus, jqXHR, context);
                    }
                    else {
                        self._executeHandlers(attrs.sh, attrs, jqXHR, data, textStatus);
                        we.publish(topic.AJAX_CALL_SUCCESS, attrs, jqXHR, data, textStatus);
                    }
                },
                error: function (jqXHR, textStatus, errorMessage) {
                    if (jqXHR.status === 301 && jqXHR.getResponseHeader('Ajax-Location')) {
                        self.processAjaxResponse(data, textStatus, jqXHR, context);
                    }
                    else {
                        self.failure(context, jqXHR, errorMessage, textStatus);
                    }
                },
                complete: function (jqXHR, textStatus) {
                    context.steps.push(jQuery.proxy(function (notify) {
                        if (attrs.i && context.isRedirecting !== true) {
                            hideIncrementally(attrs.i);
                        }
                        self._executeHandlers(attrs.coh, attrs, jqXHR, textStatus);
                        we.publish(topic.AJAX_CALL_COMPLETE, attrs, jqXHR, textStatus);
                        self.done(attrs);
                        return FunctionsExecuter.DONE;
                    }, self));
                    var executer = new FunctionsExecuter(context.steps);
                    executer.start();
                }
            });
            self._executeHandlers(attrs.ah, attrs);
            we.publish(topic.AJAX_CALL_AFTER, attrs);
            return jqXHR;
        };
        Call.prototype.process = function (data) {
            var context = {
                attrs: {},
                steps: []
            };
            var xmlDocument = parse(data);
            this.loadedCallback(xmlDocument, context);
            var executer = new FunctionsExecuter(context.steps);
            executer.start();
        };
        Call.prototype.processAjaxResponse = function (data, textStatus, jqXHR, context) {
            if (jqXHR.readyState === 4) {
                var redirectUrl = void 0;
                try {
                    redirectUrl = jqXHR.getResponseHeader('Ajax-Location');
                }
                catch (ignore) {
                }
                if (typeof (redirectUrl) !== "undefined" && redirectUrl !== null && redirectUrl !== "") {
                    this.success(context);
                    var withScheme = /^[a-z][a-z0-9+.-]*:\/\//;
                    if (redirectUrl.charAt(0) === '/' || withScheme.test(redirectUrl)) {
                        context.isRedirecting = true;
                        redirect(redirectUrl);
                    }
                    else {
                        var urlDepth = 0;
                        while (redirectUrl.substring(0, 3) === "../") {
                            urlDepth++;
                            redirectUrl = redirectUrl.substring(3);
                        }
                        var calculatedRedirect = window.location.pathname;
                        while (urlDepth > -1) {
                            urlDepth--;
                            var i = calculatedRedirect.lastIndexOf("/");
                            if (i > -1) {
                                calculatedRedirect = calculatedRedirect.substring(0, i);
                            }
                        }
                        calculatedRedirect += "/" + redirectUrl;
                        if (exports.Browser.isGecko()) {
                            calculatedRedirect = window.location.protocol + "//" + window.location.host + calculatedRedirect;
                        }
                        context.isRedirecting = true;
                        redirect(calculatedRedirect);
                    }
                }
                else {
                    if (Log.enabled()) {
                        var responseAsText = jqXHR.responseText;
                        Log.info("Received ajax response (" + responseAsText.length + " characters)");
                        Log.info("\n" + responseAsText);
                    }
                    return this.loadedCallback(data, context);
                }
            }
        };
        Call.prototype.loadedCallback = function (envelope, context) {
            try {
                var root = envelope.getElementsByTagName("ajax-response")[0];
                if (isUndef(root) && envelope.compatMode === 'BackCompat') {
                    envelope = htmlToDomDocument(envelope);
                    root = envelope.getElementsByTagName("ajax-response")[0];
                }
                if (isUndef(root) || root.tagName !== "ajax-response") {
                    this.failure(context, null, "Could not find root <ajax-response> element", null);
                    return;
                }
                var steps = context.steps;
                for (var i = 0; i < root.childNodes.length; ++i) {
                    var childNode = root.childNodes[i];
                    if (childNode.tagName === "header-contribution") {
                        this.processHeaderContribution(context, childNode);
                    }
                    else if (childNode.tagName === "priority-evaluate") {
                        this.processEvaluation(context, childNode);
                    }
                }
                var stepIndexOfLastReplacedComponent = -1;
                for (var c = 0; c < root.childNodes.length; ++c) {
                    var node = root.childNodes[c];
                    if (node.tagName === "component") {
                        if (stepIndexOfLastReplacedComponent === -1) {
                            this.processFocusedComponentMark(context);
                        }
                        stepIndexOfLastReplacedComponent = steps.length;
                        this.processComponent(context, node);
                    }
                    else if (node.tagName === "evaluate") {
                        this.processEvaluation(context, node);
                    }
                    else if (node.tagName === "redirect") {
                        this.processRedirect(context, node);
                    }
                }
                if (stepIndexOfLastReplacedComponent !== -1) {
                    this.processFocusedComponentReplaceCheck(steps, stepIndexOfLastReplacedComponent);
                }
                this.success(context);
            }
            catch (exception) {
                this.failure(context, null, exception, null);
            }
        };
        Call.prototype.success = function (context) {
            context.steps.push(jQuery.proxy(function (notify) {
                Log.info("Response processed successfully.");
                var attrs = context.attrs;
                this._executeHandlers(attrs.sh, attrs, null, null, 'success');
                Event.publish(Event.Topic.AJAX_CALL_SUCCESS, attrs, null, null, 'success');
                Focus.requestFocus();
                return FunctionsExecuter.DONE;
            }, this));
        };
        Call.prototype.failure = function (context, jqXHR, errorMessage, textStatus) {
            context.steps.push(jQuery.proxy(function (notify) {
                if (errorMessage) {
                    Log.error("Wicket.Ajax.Call.failure: Error while parsing response: " + errorMessage);
                }
                var attrs = context.attrs;
                this._executeHandlers(attrs.fh, attrs, jqXHR, errorMessage, textStatus);
                Event.publish(Event.Topic.AJAX_CALL_FAILURE, attrs, jqXHR, errorMessage, textStatus);
                return FunctionsExecuter.DONE;
            }, this));
        };
        Call.prototype.done = function (attrs) {
            this._executeHandlers(attrs.dh, attrs);
            Event.publish(Event.Topic.AJAX_CALL_DONE, attrs);
            channelManager.done(attrs.ch);
        };
        Call.prototype.processComponent = function (context, node) {
            context.steps.push(function (notify) {
                var compId = node.getAttribute("id");
                var element = $(compId);
                if (isUndef(element)) {
                    Log.error("Wicket.Ajax.Call.processComponent: Component with id [[" +
                        compId + "]] was not found while trying to perform markup update. " +
                        "Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
                }
                else {
                    var text$1 = text(node);
                    replace(element, text$1);
                }
                return FunctionsExecuter.DONE;
            });
        };
        Call.prototype.processEvaluation = function (context, node) {
            var scriptWithIdentifierR = new RegExp("\\(function\\(\\)\\{([a-zA-Z_]\\w*)\\|((.|\\n)*)?\\}\\)\\(\\);$");
            var scriptSplitterR = new RegExp("\\(function\\(\\)\\{[\\s\\S]*?}\\)\\(\\);", 'gi');
            var text$1 = text(node);
            var steps = context.steps;
            var log = Log;
            var evaluateWithManualNotify = function (parameters, body) {
                return function (notify) {
                    var toExecute = "(function(" + parameters + ") {" + body + "})";
                    try {
                        var f = window.eval(toExecute);
                        f(notify);
                    }
                    catch (exception) {
                        log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text$1);
                    }
                    return FunctionsExecuter.ASYNC;
                };
            };
            var evaluate = function (script) {
                return function (notify) {
                    try {
                        window.eval(script);
                    }
                    catch (exception) {
                        log.error("Wicket.Ajax.Call.processEvaluation: Exception evaluating javascript: " + exception + ", text: " + text$1);
                    }
                    return FunctionsExecuter.DONE;
                };
            };
            if (scriptWithIdentifierR.test(text$1)) {
                var scripts = [];
                var scr = void 0;
                while ((scr = scriptSplitterR.exec(text$1)) !== null) {
                    scripts.push(scr[0]);
                }
                for (var s = 0; s < scripts.length; s++) {
                    var script = scripts[s];
                    if (script) {
                        var scriptWithIdentifier = script.match(scriptWithIdentifierR);
                        if (scriptWithIdentifier) {
                            steps.push(evaluateWithManualNotify(scriptWithIdentifier[1], scriptWithIdentifier[2]));
                        }
                        else {
                            steps.push(evaluate(script));
                        }
                    }
                }
            }
            else {
                steps.push(evaluate(text$1));
            }
        };
        Call.prototype.processHeaderContribution = function (context, node) {
            var c = Contributor;
            c.processContribution(context, node);
        };
        Call.prototype.processRedirect = function (context, node) {
            var text$1 = text(node);
            Log.info("Redirecting to: " + text$1);
            context.isRedirecting = true;
            redirect(text$1);
        };
        Call.prototype.processFocusedComponentMark = function (context) {
            context.steps.push(function (notify) {
                Focus.markFocusedComponent();
                return FunctionsExecuter.DONE;
            });
        };
        Call.prototype.processFocusedComponentReplaceCheck = function (steps, lastReplaceComponentStep) {
            steps.splice(lastReplaceComponentStep + 1, 0, function (notify) {
                Focus.checkFocusedComponentReplaced();
                return FunctionsExecuter.DONE;
            });
        };
        return Call;
    }());

    var ThrottlerEntry = (function () {
        function ThrottlerEntry(func) {
            this.func = func;
            this.timestamp = new Date().getTime();
            this.timeoutVar = undefined;
        }
        ThrottlerEntry.prototype.getTimestamp = function () {
            return this.timestamp;
        };
        ThrottlerEntry.prototype.getFunc = function () {
            return this.func;
        };
        ThrottlerEntry.prototype.setFunc = function (func) {
            this.func = func;
        };
        ThrottlerEntry.prototype.getTimeoutVar = function () {
            return this.timeoutVar;
        };
        ThrottlerEntry.prototype.setTimeoutVar = function (timeoutVar) {
            this.timeoutVar = timeoutVar;
        };
        return ThrottlerEntry;
    }());

    var Throttler = (function () {
        function Throttler(postponeTimerOnUpdate) {
            this.postponeTimerOnUpdate = postponeTimerOnUpdate || false;
        }
        Throttler.prototype.throttle = function (id, millis, func) {
            var entries = Throttler.entries;
            var entry = entries[id];
            var me = this;
            if (typeof (entry) === 'undefined') {
                entry = new ThrottlerEntry(func);
                entry.setTimeoutVar(window.setTimeout(function () {
                    me.execute(id);
                }, millis));
                entries[id] = entry;
            }
            else {
                entry.setFunc(func);
                if (this.postponeTimerOnUpdate) {
                    window.clearTimeout(entry.getTimeoutVar());
                    entry.setTimeoutVar(window.setTimeout(function () {
                        me.execute(id);
                    }, millis));
                }
            }
        };
        Throttler.prototype.execute = function (id) {
            var entries = Throttler.entries;
            var entry = entries[id];
            if (typeof (entry) !== 'undefined') {
                var func = entry.getFunc();
                entries[id] = undefined;
                return func();
            }
        };
        Throttler.entries = [];
        return Throttler;
    }());
    var throttler = new Throttler();

    var Ajax = (function () {
        function Ajax() {
        }
        Ajax.baseUrl = undefined;
        Ajax.redirect = redirect;
        Ajax.Call = Call;
        Ajax._handleEventCancelation = function (attrs) {
            var evt = attrs.event;
            if (evt) {
                if (attrs.pd) {
                    try {
                        evt.preventDefault();
                    }
                    catch (ignore) {
                    }
                }
                if (attrs.sp === "stop") {
                    Event.stop(evt);
                }
                else if (attrs.sp === "stopImmediate") {
                    Event.stop(evt, true);
                }
            }
        };
        Ajax.getAjaxBaseUrl = function () {
            return Ajax.baseUrl || '.';
        };
        Ajax.get = function (attrs) {
            attrs.m = 'GET';
            return Ajax.ajax(attrs);
        };
        Ajax.post = function (attrs) {
            attrs.m = 'POST';
            return Ajax.ajax(attrs);
        };
        Ajax.ajax = function (attrs) {
            attrs.c = attrs.c || window;
            attrs.e = attrs.e || ['domready'];
            if (!jQuery.isArray(attrs.e)) {
                attrs.e = [attrs.e];
            }
            jQuery.each(attrs.e, function (idx, evt) {
                Event.add(attrs.c, evt, function (jqEvent, data) {
                    var call = new Call();
                    var attributes = jQuery.extend({}, attrs);
                    if (evt !== "domready") {
                        attributes.event = Event.fix(jqEvent);
                        if (data) {
                            attributes.event.extraData = data;
                        }
                    }
                    call._executeHandlers(attributes.ih, attributes);
                    Event.publish(Event.Topic.AJAX_CALL_INIT, attributes);
                    var throttlingSettings = attributes.tr;
                    if (throttlingSettings) {
                        var postponeTimerOnUpdate = throttlingSettings.p || false;
                        var throttler = new Throttler(postponeTimerOnUpdate);
                        throttler.throttle(throttlingSettings.id, throttlingSettings.d, bind(function () {
                            call.ajax(attributes);
                        }, this));
                    }
                    else {
                        call.ajax(attributes);
                    }
                    if (evt !== "domready") {
                        Ajax._handleEventCancelation(attributes);
                    }
                }, null, attrs.sel);
            });
        };
        Ajax.process = function (data) {
            var call = new Call();
            call.process(data);
        };
        return Ajax;
    }());

    jQuery.event.special.inputchange = {
        keys: {
            BACKSPACE: 8,
            TAB: 9,
            ENTER: 13,
            ESC: 27,
            LEFT: 37,
            UP: 38,
            RIGHT: 39,
            DOWN: 40,
            SHIFT: 16,
            CTRL: 17,
            ALT: 18,
            END: 35,
            HOME: 36
        },
        keyDownPressed: false,
        setup: function () {
            if (exports.Browser.isIE()) {
                jQuery(this).on('keydown', function (event) {
                    jQuery.event.special.inputchange.keyDownPressed = true;
                });
                jQuery(this).on("cut paste", function (evt) {
                    var self = this;
                    if (false === jQuery.event.special.inputchange.keyDownPressed) {
                        window.setTimeout(function () {
                            jQuery.event.special.inputchange.handler.call(self, evt);
                        }, 10);
                    }
                });
                jQuery(this).on("keyup", function (evt) {
                    jQuery.event.special.inputchange.keyDownPressed = false;
                    jQuery.event.special.inputchange.handler.call(this, evt);
                });
            }
            else {
                jQuery(this).on("input", jQuery.event.special.inputchange.handler);
            }
        },
        teardown: function () {
            jQuery(this).off("input keyup cut paste", jQuery.event.special.inputchange.handler);
        },
        handler: function (evt) {
            var WE = Event;
            var k = jQuery.event.special.inputchange.keys;
            var kc = WE.keyCode(WE.fix(evt));
            switch (kc) {
                case k.ENTER:
                case k.UP:
                case k.DOWN:
                case k.ESC:
                case k.TAB:
                case k.RIGHT:
                case k.LEFT:
                case k.SHIFT:
                case k.ALT:
                case k.CTRL:
                case k.HOME:
                case k.END:
                    return WE.stop(evt);
                default:
                    evt.type = "inputchange";
                    var args = Array.prototype.slice.call(arguments, 0);
                    return jQuery(this).trigger(evt.type, args);
            }
        }
    };
    Event.add(window, 'focusin', Focus.focusin);
    Event.add(window, 'focusout', Focus.focusout);
    Event.add(window, "unload", function () {
        Timer.clearAll();
    });

    exports.$ = $;
    exports.$$ = $$;
    exports.Ajax = Ajax;
    exports.Channel = Channel;
    exports.ChannelManager = ChannelManager;
    exports.Class = Class;
    exports.DOM = DOM;
    exports.Drag = Drag;
    exports.Event = Event;
    exports.Focus = Focus;
    exports.Form = Form;
    exports.Head = Head;
    exports.Log = Log;
    exports.Throttler = Throttler;
    exports.ThrottlerEntry = ThrottlerEntry;
    exports.Timer = Timer;
    exports.TimerHandles = TimerHandles;
    exports.Xml = Xml;
    exports.bind = bind;
    exports.channelManager = channelManager;
    exports.merge = merge;
    exports.throttler = throttler;

    return exports;

}({}));
