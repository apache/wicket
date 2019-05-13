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

import {isUndef} from "./WicketUtils";
import * as Contributor from "./Head/Contributor";
import {Log} from "./Log";
import * as DOM from "./DOM";

/**
 * Header contribution allows component to include custom javascript and stylesheet.
 *
 * Header contributor takes the code component would render to page head and
 * interprets it just as browser would when loading a page.
 * That means loading external javascripts and stylesheets, executing inline
 * javascript and aplying inline styles.
 *
 * Header contributor also filters duplicate entries, so that it doesn't load/process
 * resources that have been loaded.
 * For inline styles and javascript, element id is used to filter out duplicate entries.
 * For stylesheet and javascript references, url is used for filtering.
 */
/* the Head module */

export {Contributor};

// Creates an element in document
export function createElement (name: string): HTMLElement {
    if (isUndef(name) || name === '') {
        Log.error('Cannot create an element without a name');
        return;
    }
    return document.createElement(name);
}

// Adds the element to page head
export function addElement (element: Node): void {
    const headItems = document.querySelector('head meta[name="wicket.header.items"]');
    if (headItems) {
        headItems.parentNode.insertBefore(element, headItems);
    } else {
        const head = document.querySelector("head");

        if (head) {
            head.appendChild(element);
        }
    }
}

// Returns true, if the page head contains element that has attribute with
// name mandatoryAttribute same as the given element and their names match.
//
// e.g. Wicket.Head.containsElement(myElement, "src") return true, if there
// is an element in head that is of same type as myElement, and whose src
// attribute is same as myElement.src.
export function containsElement(element: HTMLElement, mandatoryAttribute: string): { contains: boolean, oldNode?: Element } {
    const attr = element.getAttribute(mandatoryAttribute);
    if (isUndef(attr) || attr === "") {
        return {
            contains: false
        };
    }

    const elementTagName = element.tagName.toLowerCase();
    const elementId = element.getAttribute("id");
    let head: any = document.getElementsByTagName("head")[0];

    if (elementTagName === "script") {
        head = document;
    }

    const nodes: HTMLCollectionOf<Element> = head.getElementsByTagName(elementTagName);

    for (let i = 0; i < nodes.length; ++i) {
        const node = nodes[i];

        // check node names and mandatory attribute values
        // we also have to check for attribute name that is suffixed by "_".
        // this is necessary for filtering script references
        if (node.tagName.toLowerCase() === elementTagName) {

            const loadedUrl = node.getAttribute(mandatoryAttribute);
            const loadedUrl_ = node.getAttribute(mandatoryAttribute + "_");
            if (loadedUrl === attr || loadedUrl_ === attr) {
                return {
                    contains: true
                };
            } else if (elementId && elementId === node.getAttribute("id")) {
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

// Adds a javascript element to page header.
// The fakeSrc attribute is used to filter out duplicate javascript references.
// External javascripts are loaded using xmlhttprequest. Then a javascript element is created and the
// javascript body is used as text for the element. For javascript references, wicket uses the src
// attribute to filter out duplicates. However, since we set the body of the element, we can't assign
// also a src value. Therefore we put the url to the src_ (notice the underscore)  attribute.
// Wicket.Head.containsElement is aware of that and takes also the underscored attributes into account.
export function addJavascript (content, id, fakeSrc, type): void {
    const script = createElement("script") as HTMLScriptElement;
    if (id) {
        script.id = id;
    }

    // WICKET-5047: encloses the content with a try...catch... block if the content is javascript
    // content is considered javascript if mime-type is empty (html5's default) or is 'text/javascript'
    if (!type || type.toLowerCase() === "text/javascript") {
        type = "text/javascript";
        content = 'try{'+content+'}catch(e){Wicket.Log.error(e);}';
    }

    script.setAttribute("src_", fakeSrc);
    script.setAttribute("type", type);

    // set the javascript as element content (these canHave... is an IE stuff)
    if (null === (script as any).canHaveChildren || (script as any).canHaveChildren) {
        const textNode = document.createTextNode(content);
        script.appendChild(textNode);
    } else {
        script.text = content;
    }
    addElement(script);
}

// Goes through all script elements contained by the element and add them to head
export function addJavascripts (element, contentFilter) {
    function add(element) {
        const src = element.getAttribute("src");
        const type = element.getAttribute("type");

        // if it is a reference, just add it to head
        if (src !== null && src.length > 0) {
            const e = document.createElement("script");
            if (type) {
                e.setAttribute("type",type);
            }
            e.setAttribute("src", src);
            addElement(e);
        } else {
            let content = DOM.serializeNodeChildren(element);
            if (isUndef(content) || content === "") {
                content = element.text;
            }

            if (typeof(contentFilter) === "function") {
                content = contentFilter(content);
            }

            addJavascript(content, element.id, "", type);
        }
    }
    if (typeof(element) !== "undefined" &&
        typeof(element.tagName) !== "undefined" &&
        element.tagName.toLowerCase() === "script") {
        add(element);
    } else {
        // we need to check if there are any children, because Safari
        // aborts when the element is a text node
        if (element.childNodes.length > 0) {
            const scripts = element.getElementsByTagName("script");
            for (let i = 0; i < scripts.length; ++i) {
                add(scripts[i]);
            }
        }
    }
}
