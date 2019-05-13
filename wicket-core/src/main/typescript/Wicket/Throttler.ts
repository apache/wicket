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

import {ThrottlerEntry} from "./ThrottlerEntry";

/**
 * Throttler's purpose is to make sure that ajax requests wont be fired too often.
 */
export class Throttler {

    private readonly postponeTimerOnUpdate: boolean;
    public static entries: ThrottlerEntry[] = [];

    /* "postponeTimerOnUpdate" is an optional parameter. If it is set to true, then the timer is
   reset each time the throttle function gets called. Use this behaviour if you want something
   to happen at X milliseconds after the *last* call to throttle.
   If the parameter is not set, or set to false, then the timer is not reset. */
    constructor(postponeTimerOnUpdate?:boolean) {
        this.postponeTimerOnUpdate = postponeTimerOnUpdate||false;
    }

    throttle(id, millis: number, func: () => any) {
        const entries = Throttler.entries;
        let entry = entries[id];
        const me = this;
        if (typeof (entry) === 'undefined') {
            entry = new ThrottlerEntry(func);
            entry.setTimeoutVar(window.setTimeout(function () {
                me.execute(id);
            }, millis));
            entries[id] = entry;
        } else {
            entry.setFunc(func);
            if (this.postponeTimerOnUpdate) {
                window.clearTimeout(entry.getTimeoutVar());
                entry.setTimeoutVar(window.setTimeout(function () {
                    me.execute(id);
                }, millis));
            }
        }
    }

    execute(id) {
        const entries = Throttler.entries;
        const entry = entries[id];
        if (typeof (entry) !== 'undefined') {
            const func = entry.getFunc();
            entries[id] = undefined;
            return func();
        }
    }
}

export const throttler = new Throttler();