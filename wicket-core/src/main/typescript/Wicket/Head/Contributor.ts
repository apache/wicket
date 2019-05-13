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

import {jQuery, isUndef} from "../WicketUtils";
import * as Head from "../Head";
import * as DOM from "../DOM";
import * as Xml from "../Xml";
import {FunctionsExecuter} from "../FunctionsExecuter";
import {Browser} from "../Browser";
import {Log} from "../Log";

// Parses the header contribution element (returns a DOM tree with the contribution)
export function parse(headerNode) {
    // the header contribution is stored as CDATA section in the header-contribution element.
    // even though we need to parse it (and we have aleady parsed the response), header
    // contribution needs to be treated separately. The reason for this is that
    // Konqueror crashes when it there is a <script element in the parsed string. So we
    // need to replace that first

    // get the header contribution text and unescape it if necessary
    let text = DOM.text(headerNode);

    if (Browser.isKHTML()) {
        // konqueror crashes if there is a <script element in the xml, but <SCRIPT is fine.
        text = text.replace(/<script/g, "<SCRIPT");
        text = text.replace(/<\/script>/g, "</SCRIPT>");
    }

    // build a DOM tree of the contribution
    const xmldoc = Xml.parse(text);
    return xmldoc;
}

// checks whether the passed node is the special "parsererror"
// created by DOMParser if there is a error in XML parsing
// TODO: move out of the API section
export function _checkParserError(node) {
    let result = false;

    if (!isUndef(node.tagName) && node.tagName.toLowerCase() === "parsererror") {
        Log.error("Error in parsing: " + node.textContent);
        result = true;
    }
    return result;
}

// Processes the parsed header contribution
export function processContribution(context, headerNode) {
    const xmldoc = this.parse(headerNode);
    const rootNode = xmldoc.documentElement;

    // Firefox and Opera reports the error in the documentElement
    if (this._checkParserError(rootNode)) {
        return;
    }

    // go through the individual elements and process them according to their type
    for (let i = 0; i < rootNode.childNodes.length; i++) {
        let node = rootNode.childNodes[i];

        // Chromium reports the error as a child node
        if (this._checkParserError(node)) {
            return;
        }

        if (!isUndef(node.tagName)) {
            let name = node.tagName.toLowerCase();

            // it is possible that a reference is surrounded by a <wicket:link
            // in that case, we need to find the inner element
            if (name === "wicket:link") {
                for (let j = 0; j < node.childNodes.length; ++j) {
                    const childNode = node.childNodes[j];
                    // try to find a regular node inside wicket:link
                    if (childNode.nodeType === 1) {
                        node = childNode;
                        name = node.tagName.toLowerCase();
                        break;
                    }
                }
            }

            // process the element
            if (name === "link") {
                this.processLink(context, node);
            } else if (name === "script") {
                this.processScript(context, node);
            } else if (name === "style") {
                this.processStyle(context, node);
            } else if (name === "meta") {
                this.processMeta(context, node);
            }
        } else if (node.nodeType === 8) { // comment type
            this.processComment(context, node);
        }
    }
}

// Process an external stylesheet element
export function processLink(context, node) {
    context.steps.push(function (notify) {
        const res = Head.containsElement(node, "href");
        const oldNode = res.oldNode;
        if (res.contains) {
            // an element with same href attribute is in document, skip it
            return FunctionsExecuter.DONE;
        } else if (oldNode) {
            // remove another external element with the same id but different href
            oldNode.parentNode.removeChild(oldNode);
        }

        // create link element
        const css = Head.createElement("link") as HTMLLinkElement;

        // copy supplied attributes only.
        const attributes = jQuery(node).prop("attributes");
        const $css = jQuery(css);
        jQuery.each(attributes, function () {
            $css.attr(this.name, this.value);
        });

        // add element to head
        Head.addElement(css);

        // cross browser way to check when the css is loaded
        // taken from http://www.backalleycoder.com/2011/03/20/link-tag-css-stylesheet-load-event/
        // this makes a second GET request to the css but it gets it either from the cache or
        // downloads just the first several bytes and realizes that the MIME is wrong and ignores the rest
        const img = document.createElement('img');
        let notifyCalled = false;
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

// Process an inline style element
export function processStyle(context, node) {
    context.steps.push(function (notify) {
        // if element with same id is already in document, skip it
        if (DOM.containsElement(node)) {
            return FunctionsExecuter.DONE;
        }
        // serialize the style to string
        const content = DOM.serializeNodeChildren(node);

        // create stylesheet
        if (Browser.isIELessThan11()) {
            try {
                (document as any).createStyleSheet().cssText = content;
                return FunctionsExecuter.DONE;
            } catch (ignore) {
                const run = function () {
                    try {
                        (document as any).createStyleSheet().cssText = content;
                    } catch (e) {
                        Log.error("Wicket.Head.Contributor.processStyle: " + e);
                    }
                    notify();
                };
                window.setTimeout(run, 1);
                return FunctionsExecuter.ASYNC;
            }
        } else {
            // create style element
            const style = Head.createElement("style");

            // copy id attribute
            style.id = node.getAttribute("id");

            const textNode = document.createTextNode(content);
            style.appendChild(textNode);

            Head.addElement(style);
        }

        // continue to next step
        return FunctionsExecuter.DONE;
    });
}

// Process a script element (both inline and external)
export function processScript(context, node) {
    context.steps.push(function (notify) {

        if (!node.getAttribute("src") && DOM.containsElement(node)) {
            // if an inline element with same id is already in document, skip it
            return FunctionsExecuter.DONE;
        } else {
            const res = Head.containsElement(node, "src");
            const oldNode = res.oldNode;
            if (res.contains) {
                // an element with same src attribute is in document, skip it
                return FunctionsExecuter.DONE;
            } else if (oldNode) {
                // remove another external element with the same id but different src
                oldNode.parentNode.removeChild(oldNode);
            }
        }

        // determine whether it is external javascript (has src attribute set)
        const src = node.getAttribute("src");

        if (src !== null && src !== "") {

            // convert the XML node to DOM node
            const scriptDomNode = document.createElement("script");

            const attrs = node.attributes;
            for (let a = 0; a < attrs.length; a++) {
                const attr = attrs[a];
                scriptDomNode[attr.name] = attr.value;
            }

            const onScriptReady = function () {
                notify();
            };

            // first check for feature support
            if (typeof (scriptDomNode.onload) !== 'undefined') {
                scriptDomNode.onload = onScriptReady;
            } else if (typeof ((scriptDomNode as any).onreadystatechange) !== 'undefined') {
                (scriptDomNode as any).onreadystatechange = function () {
                    if ((scriptDomNode as any).readyState === 'loaded' || (scriptDomNode as any).readyState === 'complete') {
                        onScriptReady();
                    }
                };
            } else if (Browser.isGecko()) {
                // Firefox doesn't react on the checks above but still supports 'onload'
                scriptDomNode.onload = onScriptReady;
            } else {
                // as a final resort notify after the current function execution
                window.setTimeout(onScriptReady, 10);
            }

            Head.addElement(scriptDomNode);

            return FunctionsExecuter.ASYNC;
        } else {
            // serialize the element content to string
            let text = DOM.serializeNodeChildren(node);
            // get rid of prefix and suffix, they are not eval-d correctly
            text = text.replace(/^\n\/\*<!\[CDATA\[\*\/\n/, "");
            text = text.replace(/\n\/\*\]\]>\*\/\n$/, "");

            const id = node.getAttribute("id");
            const type = node.getAttribute("type");

            if (typeof (id) === "string" && id.length > 0) {
                // add javascript to document head
                Head.addJavascript(text, id, "", type);
            } else {
                try {
                    // do the evaluation in global scope
                    (window as any).eval(text);
                } catch (e) {
                    Log.error("Wicket.Head.Contributor.processScript: " + e + ": eval -> " + text);
                }
            }

            // continue to next step
            return FunctionsExecuter.DONE;
        }
    });
}

export function processMeta(context, node) {
    context.steps.push(function (notify) {
        const meta = Head.createElement("meta"),
            $meta = jQuery(meta),
            attrs = jQuery(node).prop("attributes"),
            name = node.getAttribute("name");

        if (name) {
            jQuery('meta[name="' + name + '"]').remove();
        }
        jQuery.each(attrs, function () {
            $meta.attr(this.name, this.value);
        });

        Head.addElement(meta);

        return FunctionsExecuter.DONE;
    });
}

// process (conditional) comments
export function processComment(context, node) {
    context.steps.push(function (notify) {
        const comment = document.createComment(node.nodeValue);
        Head.addElement(comment);
        return FunctionsExecuter.DONE;
    });
}
