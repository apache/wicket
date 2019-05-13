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

import {jQuery, $, nodeListToArray} from "./WicketUtils";

/**
 * Form serialization
 *
 * To post a form using Ajax Wicket first needs to serialize it, which means composing a string
 * from form elments names and values. The string will then be set as body of POST request.
 */
/* the Form module */

export function encode (text) {
    if ((window as any).encodeURIComponent) {
        return (window as any).encodeURIComponent(text);
    } else {
        return (window as any).escape(text);
    }
}

/**
 * Serializes HTMLFormSelectElement to URL encoded key=value string.
 *
 * @param select {HTMLFormSelectElement} - the form element to serialize
 * @return an object of key -> value pair where 'value' can be an array of Strings if the select is .multiple,
 *		or empty object if the form element is disabled.
 */
export function serializeSelect (select){
    const result = [];
    if (select) {
        const $select = jQuery(select);
        if ($select.length > 0 && $select.prop('disabled') === false) {
            const name = $select.prop('name');
            const values = $select.val();
            if (jQuery.isArray(values)) {
                for (let v = 0; v < values.length; v++) {
                    const value = values[v];
                    result.push( { name: name, value: value } );
                }
            } else {
                result.push( { name: name, value: values } );
            }
        }
    }
    return result;
}

/**
 * Serializes a form element to an array with a single element - an object
 * with two keys - <em>name</em> and <em>value</em>.
 *
 * Example: [{"name": "searchTerm", "value": "abc"}].
 *
 * Note: this function intentionally ignores image and submit inputs.
 *
 * @param input {HtmlFormElement} - the form element to serialize
 * @return the URL encoded key=value pair or empty string if the form element is disabled.
 */
export function serializeInput (input) {
    let result = [];
    if (input && input.type) {
        const $input = jQuery(input);

        if (input.type === 'file') {
            for (let f = 0; f < input.files.length; f++) {
                result.push({"name" : input.name, "value" : input.files[f]});
            }
        } else if (!(input.type === 'image' || input.type === 'submit')) {
            result = $input.serializeArray();
        }
    }
    return result;
}

/**
 * A hash of HTML form element to exclude from serialization
 * As key the element's id is being used.
 * As value - the string "true".
 */
export let excludeFromAjaxSerialization = {
};

/**
 * Serializes a form element by checking its type and delegating the work to
 * a more specific function.
 *
 * The form element will be ignored if it is registered as excluded in
 * <em>Wicket.Form.excludeFromAjaxSerialization</em>
 *
 * @param element {HTMLFormElement} - the form element to serialize. E.g. HTMLInputElement
 * @param serializeRecursively {Boolean} - a flag indicating whether to collect (submit) the
 * 			name/value pairs for all HTML form elements children of the HTML element with
 * 			the JavaScript listener
 * @return An array with a single element - an object with two keys - <em>name</em> and <em>value</em>.
 */
export function serializeElement (element, serializeRecursively) {

    if (!element) {
        return [];
    }
    else if (typeof(element) === 'string') {
        element = $(element);
    }

    if (excludeFromAjaxSerialization && element.id && excludeFromAjaxSerialization[element.id] === "true") {
        return [];
    }

    const tag = element.tagName.toLowerCase();
    if (tag === "select") {
        return serializeSelect(element);
    } else if (tag === "input" || tag === "textarea") {
        return serializeInput(element);
    } else {
        let result = [];
        if (serializeRecursively) {
            let elements = nodeListToArray(element.getElementsByTagName("input"));
            elements = elements.concat(nodeListToArray(element.getElementsByTagName("select")));
            elements = elements.concat(nodeListToArray(element.getElementsByTagName("textarea")));

            for (let i = 0; i < elements.length; ++i) {
                const el = elements[i];
                if (el.name && el.name !== "") {
                    result = result.concat(serializeElement(el, serializeRecursively));
                }
            }
        }
        return result;
    }
}

export function serializeForm (form) {
    let result = [],
        elements;

    if (form) {
        if (form.tagName.toLowerCase() === 'form') {
            elements = form.elements;
        } else {
            do {
                form = form.parentNode;
            } while (form.tagName.toLowerCase() !== "form" && form.tagName.toLowerCase() !== "body");

            elements = nodeListToArray(form.getElementsByTagName("input"));
            elements = elements.concat(nodeListToArray(form.getElementsByTagName("select")));
            elements = elements.concat(nodeListToArray(form.getElementsByTagName("textarea")));
        }
    }

    for (let i = 0; i < elements.length; ++i) {
        const el = elements[i];
        if (el.name && el.name !== "") {
            result = result.concat(serializeElement(el, false));
        }
    }
    return result;
}

export function serialize (element, dontTryToFindRootForm) {
    if (typeof(element) === 'string') {
        element = $(element);
    }

    if (element.tagName.toLowerCase() === "form") {
        return serializeForm(element);
    } else {
        // try to find a form in DOM parents
        const elementBck = element;

        if (dontTryToFindRootForm !== true) {
            do {
                element = element.parentNode;
            } while(element.tagName.toLowerCase() !== "form" && element.tagName.toLowerCase() !== "body");
        }

        if (element.tagName.toLowerCase() === "form"){
            return serializeForm(element);
        } else {
            // there is not form in dom hierarchy
            // simulate it
            const form = document.createElement("form");
            const parent = elementBck.parentNode;

            parent.replaceChild(form, elementBck);
            form.appendChild(elementBck);
            const result = serializeForm(form);
            parent.replaceChild(elementBck, form);

            return result;
        }
    }
}