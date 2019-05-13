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

import {isUndef, jQuery} from "./WicketUtils";
import {Event} from "./Event";
import {Browser} from "./Browser";

/**
 * Flexible dragging support.
 * TODO move somewhere else from wicket core (for example to the Modal)
 */
export const Drag = {

    current: undefined,

    /**
     * Initializes dragging on the specified element.
     *
     * @param element {Element}
     *            element clicking on which
     *            the drag should begin
     * @param onDragBegin {Function}
     *            called at the begin of dragging - passed element and event as parameters,
     *            may return false to prevent the start
     * @param onDragEnd {Function}
     *            handler called at the end of dragging - passed element as parameter
     * @param onDrag {Function}
     *            handler called during dragging - passed element and mouse deltas as parameters
     */
    init: function(element, onDragBegin, onDragEnd, onDrag) {

        if (typeof(onDragBegin) === "undefined") {
            onDragBegin = jQuery.noop;
        }

        if (typeof(onDragEnd) === "undefined") {
            onDragEnd = jQuery.noop;
        }

        if (typeof(onDrag) === "undefined") {
            onDrag = jQuery.noop;
        }

        element.wicketOnDragBegin = onDragBegin;
        element.wicketOnDrag = onDrag;
        element.wicketOnDragEnd = onDragEnd;


        // set the mousedown handler
        Event.add(element, "mousedown", Drag.mouseDownHandler);
    },

    mouseDownHandler: function (e) {
        e = Event.fix(e);

        const element = this;

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

    /**
     * Deinitializes the dragging support on given element.
     */
    clean: function (element) {
        element.onmousedown = null;
    },

    /**
     * Called when mouse is moved. This method fires the onDrag event
     * with element instance, deltaX and deltaY (the distance
     * between this call and the previous one).

     * The onDrag handler can optionally return an array of two integers
     * - the delta correction. This is used, for example, if there is
     * element being resized and the size limit has been reached (but the
     * mouse can still move).
     *
     * @param {Event} e
     */
    mouseMove: function (e) {
        e = Event.fix(e);
        const o = Drag.current;

        // this happens sometimes in Safari
        if (e.clientX < 0 || e.clientY < 0) {
            return;
        }

        if (o !== null) {
            const deltaX = e.clientX - o.lastMouseX;
            const deltaY = e.clientY - o.lastMouseY;

            let res = o.wicketOnDrag(o, deltaX, deltaY, e);

            if (isUndef(res)) {
                res = [0, 0];
            }

            o.lastMouseX = e.clientX + res[0];
            o.lastMouseY = e.clientY + res[1];
        }

        return false;
    },

    /**
     * Called when the mouse button is released.
     * Cleans all temporary variables and callback methods.
     */
    mouseUp: function (e?) {
        const o = Drag.current;

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

    /**
     * Called when mouse leaves an element. We need this for firefox, as otherwise
     * the dragging would continue after mouse leaves the document.
     * Unfortunately this break dragging in firefox immediately after the mouse leaves
     * page.
     */
    mouseOut: function (e) {
        if (false && Browser.isGecko()) {
            // other browsers handle this more gracefully
            e = Event.fix(e);

            if (e.target.tagName === "HTML") {
                Drag.mouseUp(e);
            }
        }
    }
};