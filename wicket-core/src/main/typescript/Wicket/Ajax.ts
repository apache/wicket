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

import {jQuery, bind, redirect} from "./WicketUtils"
import {Event} from "./Event";
import {Call} from "./Ajax/Call";
import {Throttler} from "./Throttler";

/**
 * The Ajax.Request class encapsulates a XmlHttpRequest.
 */
/* the Ajax module */
export class Ajax {

    static baseUrl: string = undefined;
    static redirect = redirect;
    static Call = Call;

    private static _handleEventCancelation = function(attrs) {
        let evt = attrs.event;
        if (evt) {
            if (attrs.pd) {
                try {
                    evt.preventDefault();
                } catch (ignore) {
                    // WICKET-4986
                    // jquery fails 'member not found' with calls on busy channel
                }
            }

            if (attrs.sp === "stop") {
                Event.stop(evt);
            } else if (attrs.sp === "stopImmediate") {
                Event.stop(evt, true);
            }
        }
    };

    /**
     * A safe getter for Wicket's Ajax base URL.
     * If the value is not defined or is empty string then
     * return '.' (current folder) as base URL.
     * Used for request header and parameter
     */
    public static getAjaxBaseUrl = function () {
        return Ajax.baseUrl || '.';
    };

    public static get = function (attrs) {
        attrs.m = 'GET';

        return Ajax.ajax(attrs);
    };

    public static post = function (attrs) {
        attrs.m = 'POST';

        return Ajax.ajax(attrs);
    };

    public static ajax = function (attrs) {

        attrs.c = attrs.c || window;
        attrs.e = attrs.e || ['domready'];

        if (!jQuery.isArray(attrs.e)) {
            attrs.e = [attrs.e];
        }

        jQuery.each(attrs.e, function (idx, evt) {
            Event.add(attrs.c, evt, function (jqEvent, data) {
                let call = new Call();
                let attributes = jQuery.extend({}, attrs);

                if (evt !== "domready") {
                    attributes.event = Event.fix(jqEvent);
                    if (data) {
                        attributes.event.extraData = data;
                    }
                }

                call._executeHandlers(attributes.ih, attributes);
                Event.publish(Event.Topic.AJAX_CALL_INIT, attributes);

                let throttlingSettings = attributes.tr;
                if (throttlingSettings) {
                    let postponeTimerOnUpdate = throttlingSettings.p || false;
                    let throttler = new Throttler(postponeTimerOnUpdate);
                    throttler.throttle(throttlingSettings.id, throttlingSettings.d,
                        bind(function () {
                            call.ajax(attributes);
                        }, this));
                } else {
                    call.ajax(attributes);
                }
                if (evt !== "domready") {
                    Ajax._handleEventCancelation(attributes);
                }
            }, null, attrs.sel);
        });
    };

    public static process = function (data) {
        let call = new Call();
        call.process(data);
    };

}





