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

declare const Wicket: any;

/**
 * Logging functionality.
 */
export class Log {

    public static enabled() {
        return Wicket.Ajax.DebugWindow && Wicket.Ajax.DebugWindow.enabled;
    }

    public static info(msg: any): void {
        if (Log.enabled()) {
            Wicket.Ajax.DebugWindow.logInfo(msg);
        }
    }

    public static error(msg: any): void {
        if (Log.enabled()) {
            Wicket.Ajax.DebugWindow.logError(msg);
        } else if (typeof(console) !== "undefined" && typeof(console.error) === 'function') {
            console.error('Wicket.Ajax: ', msg);
        }
    }

    public static log(msg: any): void {
        if (Log.enabled()) {
            Wicket.Ajax.DebugWindow.log(msg);
        }
    }

}

