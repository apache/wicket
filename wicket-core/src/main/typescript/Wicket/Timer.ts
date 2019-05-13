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

export let TimerHandles = {};

/**
 * Manages the functionality needed by AbstractAjaxTimerBehavior and its subclasses
 */
export const Timer = {
    /**
     * Schedules a timer
     * @param {string} timerId - the identifier for the timer
     * @param {function} f - the JavaScript function to execute after the timeout
     * @param {number} delay - the timeout
     */
    'set': function(timerId, f, delay) {
        // if (typeof(TimerHandles) === 'undefined') {
        //     TimerHandles = {};
        // }

        Timer.clear(timerId);
        TimerHandles[timerId] = setTimeout(function() {
            Timer.clear(timerId);
            f();
        }, delay);
    },

    /**
     * Clears a timer by its id
     * @param {string} timerId - the identifier of the timer
     */
    clear: function(timerId) {
        if (TimerHandles && TimerHandles[timerId]) {
            clearTimeout(TimerHandles[timerId]);
            delete TimerHandles[timerId];
        }
    },

    /**
     * Clear all remaining timers.
     */
    clearAll: function() {
        const WTH = TimerHandles;
        if (WTH) {
            for (let th in WTH) {
                if (WTH.hasOwnProperty(th)) {
                    Timer.clear(th);
                }
            }
        }
    }
};