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

function handleDefaultSubmit(event, submitId, keyStroke) {
    const tag = event.target.tagName.toLowerCase();
    const contenteditable = event.target.getAttribute('contenteditable') === 'true';

    // we're interested only in inputs, textareas and contenteditable fields
    if (tag !== 'input' && tag !== 'textarea' && !contenteditable) return;

    // submit for inputs
    if (tag === 'input' && event.which != 13) return;
    // multi-line inputs (textarea, contenteditable)
    if (tag === 'textarea' || contenteditable) {
        switch (keyStroke) {
            case 'ENTER': if (event.which != 13) return;
            case 'CTRL_ENTER': if (!event.ctrlKey || event.which != 13) return; break;
            case 'SHIFT_ENTER': if (!event.shiftKey || event.which != 13) return; break;
            case 'NONE':
            default: return;
        }
    }

    const b = document.getElementById(submitId);
    if (window.getComputedStyle(b).visibility === 'hidden') return;

    event.stopPropagation();
    event.preventDefault();

    if (b != null && b.onclick != null && typeof (b.onclick) != 'undefined') {
        const r = Wicket.bind(b.onclick, b)();
        if (r != false) b.click();
    } else {
        b.click();
    }

    return false;
}
