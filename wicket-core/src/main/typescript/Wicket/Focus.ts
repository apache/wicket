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

import {$} from "./WicketUtils";
import {Event} from "./Event";
import {Log} from "./Log";

export class Focus {
    public static lastFocusId = "";
    public static refocusLastFocusedComponentAfterResponse = false;
    public static focusSetFromServer = false;

    public static focusin(event) {
        event = Event.fix(event);

        const target = event.target;
        if (target) {
            const WF = Focus;
            WF.refocusLastFocusedComponentAfterResponse = false;
            const id = target.id;
            WF.lastFocusId = id;
            Log.info("focus set on " + id);
        }
    }

    public static focusout(event) {
        event = Event.fix(event);

        const target = event.target;
        const WF = Focus;
        if (target && WF.lastFocusId === target.id) {
            const id = target.id;
            if (WF.refocusLastFocusedComponentAfterResponse) {
                // replaced components seem to blur when replaced only on Safari - so do not modify lastFocusId so it gets refocused
                Log.info("focus removed from " + id + " but ignored because of component replacement");
            } else {
                WF.lastFocusId = null;
                Log.info("focus removed from " + id);
            }
        }
    }

    public static getFocusedElement() {
        const lastFocusId = Focus.lastFocusId;
        if (lastFocusId) {
            const focusedElement = $(lastFocusId);
            Log.info("returned focused element: " + focusedElement);
            return focusedElement;
        }
    }

    public static setFocusOnId(id) {
        const WF = Focus;
        if (id) {
            WF.refocusLastFocusedComponentAfterResponse = true;
            WF.focusSetFromServer = true;
            WF.lastFocusId = id;
            Log.info("focus set on " + id + " from server side");
        } else {
            WF.refocusLastFocusedComponentAfterResponse = false;
            Log.info("refocus focused component after request stopped from server side");
        }
    }

    // mark the focused component so that we know if it has been replaced or not by response
    public static markFocusedComponent() {
        const WF = Focus;
        const focusedElement = WF.getFocusedElement();
        if (focusedElement) {
            // create a property of the focused element that would not remain there if component is replaced
            focusedElement.wasFocusedBeforeComponentReplacements = true;
            WF.refocusLastFocusedComponentAfterResponse = true;
            WF.focusSetFromServer = false;
        } else {
            WF.refocusLastFocusedComponentAfterResponse = false;
        }
    }

    // detect if the focused component was replaced
    public static checkFocusedComponentReplaced() {
        const WF = Focus;
        if (WF.refocusLastFocusedComponentAfterResponse) {
            const focusedElement = WF.getFocusedElement();
            if (focusedElement) {
                if (typeof (focusedElement.wasFocusedBeforeComponentReplacements) !== "undefined") {
                    // focus component was not replaced - no need to refocus it
                    WF.refocusLastFocusedComponentAfterResponse = false;
                }
            } else {
                // focused component dissapeared completely - no use to try to refocus it
                WF.refocusLastFocusedComponentAfterResponse = false;
                WF.lastFocusId = "";
            }
        }
    }

    public static requestFocus() {
        // if the focused component is replaced by the ajax response, a re-focus might be needed
        // (if focus was not changed from server) but if not, and the focus component should
        // remain the same, do not re-focus - fixes problem on IE6 for combos that have
        // the popup open (refocusing closes popup)
        const WF = Focus;
        if (WF.refocusLastFocusedComponentAfterResponse && WF.lastFocusId) {
            const toFocus = $(WF.lastFocusId);

            if (toFocus) {
                Log.info("Calling focus on " + WF.lastFocusId);

                const safeFocus = function () {
                    try {
                        toFocus.focus();
                    } catch (ignore) {
                        // WICKET-6209 IE fails if toFocus is disabled
                    }
                };

                if (WF.focusSetFromServer) {
                    // WICKET-5858
                    window.setTimeout(safeFocus, 0);
                } else {
                    // avoid loops like - onfocus triggering an event the modifies the tag => refocus => the event is triggered again
                    const temp = toFocus.onfocus;
                    toFocus.onfocus = null;

                    // IE needs setTimeout (it seems not to call onfocus sync. when focus() is called
                    window.setTimeout(function () {
                        safeFocus();
                        toFocus.onfocus = temp;
                    }, 0);
                }
            } else {
                WF.lastFocusId = "";
                Log.info("Couldn't set focus on element with id '" + WF.lastFocusId + "' because it is not in the page anymore");
            }
        } else if (WF.refocusLastFocusedComponentAfterResponse) {
            Log.info("last focus id was not set");
        } else {
            Log.info("refocus last focused component not needed/allowed");
        }
        Focus.refocusLastFocusedComponentAfterResponse = false;
    }
}