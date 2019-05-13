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

import {jQuery, isUndef, $, $$} from "./WicketUtils";
import {Event} from "./Event";

/**
 * DOM nodes serialization functionality
 *
 * The purpose of these methods is to return a string representation
 * of the DOM tree.
 */
/* the Dom module */


export function show (e, display?) {
    e = $(e);
    if (e !== null) {
        if (isUndef(display)) {
            // no explicit 'display' value is requested so
            // use jQuery. It has special logic to decide which is the
            // best value for an HTMLElement
            jQuery(e).show();
        } else {
            e.style.display = display;
        }
    }
}

/** hides an element */
export function hide (e) {
    e = $(e);
    if (e !== null) {
        jQuery(e).hide();
    }
}

/**
 * Add or remove one or more classes from each element in the
 * set of matched elements, depending on either the class's presence
 * or the value of the switch argument.
 *
 * @param {String} elementId The markup id of the element that will be manipulated.
 * @param {String} cssClass One or more class names (separated by spaces)
 *        to be toggled for each element in the matched set.
 * @param {Boolean} Switch A Boolean (not just truthy/falsy) value to
 *        determine whether the class should be added or removed.
 */
export function toggleClass(elementId, cssClass, Switch) {
    jQuery('#'+elementId).toggleClass(cssClass, Switch);
}

/** call-counting implementation of DOM.show() */
export function showIncrementally(e) {
    e = $(e);
    if (e === null) {
        return;
    }
    let count = e.getAttribute("showIncrementallyCount");
    count = parseInt(isUndef(count) ? 0 : count, 10);
    if (count >= 0) {
        show(e);
    }
    e.setAttribute("showIncrementallyCount", count + 1);
}

/** call-counting implementation of DOM.hide() */
export function hideIncrementally (e) {
    e = $(e);
    if (e === null) {
        return;
    }
    let count = e.getAttribute("showIncrementallyCount");
    count = parseInt(String(isUndef(count) ? 0 : count - 1), 10);
    if (count <= 0) {
        hide(e);
    }
    e.setAttribute("showIncrementallyCount", count);
}

export function get(arg) {
    return $(arg);
}

/**
 * returns if the element belongs to current document
 * if the argument is not element, function returns true
 */
export function inDoc (element) {
    return $$(element);
}

/**
 * A cross-browser method that replaces the markup of an element. The behavior
 * is similar to calling element.outerHtml=text in internet explorer. However
 * this method also takes care of executing javascripts within the markup on
 * browsers that don't do that automatically.
 * Also this method takes care of replacing table elements (tbody, tr, td, thead)
 * on browser where it's not supported when using outerHTML (IE).
 *
 * This method sends notifications to all subscribers for channels with names
 * '/dom/node/removing' with the element that is going to be replaced and
 * '/dom/node/added' with the newly created element (the replacement).
 *
 * Note: the 'to be replaced' element must have an 'id' attribute
 */
export function replace (element, text) {

    const we = Event;
    const topic = we.Topic;

    we.publish(topic.DOM_NODE_REMOVING, element);

    if (element.tagName.toLowerCase() === "title") {
        // match the text between the tags
        const titleText = />(.*?)</.exec(text)[1];
        document.title = titleText;
        return;
    } else {
        // jQuery 1.9+ expects '<' as the very first character in text
        const cleanedText = jQuery.trim(text);

        const $newElement = jQuery(cleanedText);
        jQuery(element).replaceWith($newElement);
    }

    const newElement = $(element.id);
    if (newElement) {
        we.publish(topic.DOM_NODE_ADDED, newElement);
    }
}

// Method for serializing DOM nodes to string
// original taken from Tacos (http://tacoscomponents.jot.com)
export function serializeNodeChildren(node) {
    if (isUndef(node)) {
        return "";
    }
    const result = [];

    if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
            const thisNode = node.childNodes[i];
            switch (thisNode.nodeType) {
                case 1: // ELEMENT_NODE
                case 5: // ENTITY_REFERENCE_NODE
                    result.push(this.serializeNode(thisNode));
                    break;
                case 8: // COMMENT
                    result.push("<!--");
                    result.push(thisNode.nodeValue);
                    result.push("-->");
                    break;
                case 4: // CDATA_SECTION_NODE
                    result.push("<![CDATA[");
                    result.push(thisNode.nodeValue);
                    result.push("]]>");
                    break;
                case 3: // TEXT_NODE
                case 2: // ATTRIBUTE_NODE
                    result.push(thisNode.nodeValue);
                    break;
                default:
                    break;
            }
        }
    } else {
        result.push(node.textContent || node.text);
    }
    return result.join("");
}

export function serializeNode(node){
    if (isUndef(node)) {
        return "";
    }
    const result = [];
    result.push("<");
    result.push(node.nodeName);

    if (node.attributes && node.attributes.length > 0) {

        for (let i = 0; i < node.attributes.length; i++) {
            // serialize the attribute only if it has meaningful value that is not inherited
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

// Utility function that determines whether given element is part of the current document
export function containsElement (element) {
    const id = element.getAttribute("id");
    if (id) {
        return $(id) !== null;
    }
    else {
        return false;
    }
}

/**
 * Reads the text from the node's children nodes.
 * Used instead of jQuery.text() because it is very slow in IE10/11.
 * WICKET-5132, WICKET-5510
 * @param node {DOMElement} the root node
 */
export function text (node) {
    if (isUndef(node)) {
        return "";
    }

    const result = [];

    if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
            const thisNode = node.childNodes[i];
            switch (thisNode.nodeType) {
                case 1: // ELEMENT_NODE
                case 5: // ENTITY_REFERENCE_NODE
                    result.push(this.text(thisNode));
                    break;
                case 3: // TEXT_NODE
                case 4: // CDATA_SECTION_NODE
                    result.push(thisNode.nodeValue);
                    break;
                default:
                    break;
            }
        }
    } else {
        result.push(node.textContent || node.text);
    }

    return result.join("");
}