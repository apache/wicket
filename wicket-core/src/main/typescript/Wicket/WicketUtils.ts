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

import {Log} from "./Log";

// TODO import jQuery definition
// declare const jQuery: any;
const jQuery = (window as any).jQuery;
export {jQuery};

export function isUndef(target: any): boolean {
    return (typeof (target) === 'undefined' || target === null);
}

export function $(arg) {
    if (isUndef(arg)) {
        return null;
    }
    if (arguments.length > 1) {
        const e = [];
        for (let i = 0; i < arguments.length; i++) {
            e.push($(arguments[i]));
        }
        return e;
    } else if (typeof arg === 'string') {
        return document.getElementById(arg);
    } else {
        return arg;
    }
}

/**
 * returns if the element belongs to current document
 * if the argument is not element, function returns true
 */
export function $$(element) {
    if (element === window) {
        return true;
    }
    if (typeof (element) === "string") {
        element = $(element);
    }
    if (isUndef(element) || isUndef(element.tagName)) {
        return false;
    }

    const id = element.getAttribute('id');
    if (isUndef(id) || id === "") {
        return element.ownerDocument === document;
    } else {
        return document.getElementById(id) === element;
    }
}

/**
 * Merges two objects. Values of the second will overwrite values of the first.
 *
 * @param {Object} object1 - the first object to merge
 * @param {Object} object2 - the second object to merge
 * @return {Object} a new object with the values of object1 and object2
 */
export function merge(object1, object2) {
    return jQuery.extend({}, object1, object2);
}

/**
 * Takes a function and returns a new one that will always have a particular context, i.e. 'this' will be the passed context.
 *
 * @param {Function} fn - the function which context will be set
 * @param {Object} context - the new context for the function
 * @return {Function} the original function with the changed context
 */
export function bind(fn, context) {
    return jQuery.proxy(fn, context);
}

/**
 * Helper method that serializes HtmlDocument to string and then
 * creates a DOMDocument by parsing this string.
 * It is used as a workaround for the problem described at https://issues.apache.org/jira/browse/WICKET-4332
 * @param htmlDocument (DispHtmlDocument) the document object created by IE from the XML response in the iframe
 */
export function htmlToDomDocument(htmlDocument) {
    let xmlAsString = htmlDocument.body.outerText;
    xmlAsString = xmlAsString.replace(/^\s+|\s+$/g, ''); // trim
    xmlAsString = xmlAsString.replace(/(\n|\r)-*/g, ''); // remove '\r\n-'. The dash is optional.
    let xmldoc = parseXML(xmlAsString);
    return xmldoc;
}

export function parseXML(text) {
    let xmlDocument;
    if ((window as any).DOMParser) {
        const parser = new DOMParser();
        xmlDocument = parser.parseFromString(text, "text/xml");
    } else if ((window as any).ActiveXObject) {
        try {
            xmlDocument = new ActiveXObject("Msxml2.DOMDocument.6.0");
        } catch (err6) {
            try {
                xmlDocument = new ActiveXObject("Msxml2.DOMDocument.5.0");
            } catch (err5) {
                try {
                    xmlDocument = new ActiveXObject("Msxml2.DOMDocument.4.0");
                } catch (err4) {
                    try {
                        xmlDocument = new ActiveXObject("MSXML2.DOMDocument.3.0");
                    } catch (err3) {
                        try {
                            xmlDocument = new ActiveXObject("Microsoft.XMLDOM");
                        } catch (err2) {
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

/**
 * Converts a NodeList to an Array
 *
 * @param nodeList The NodeList to convert
 * @returns {Array} The array with document nodes
 */
export function nodeListToArray(nodeList) {
    let arr = [],
        nodeId;
    if (nodeList && nodeList.length) {
        for (nodeId = 0; nodeId < nodeList.length; nodeId++) {
            arr.push(nodeList.item(nodeId));
        }
    }
    return arr;
}

/**
 * An abstraction over native window.location.replace() to be able to suppress it for unit tests
 *
 * @param url The url to redirect to
 */
export function redirect(url) {
    window.location = url;
}

