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


/**
 * Throttler entry see {@link Throttler} for details
 */
export class ThrottlerEntry {

    private func: () => any;
    private readonly timestamp: number;
    private timeoutVar: number;

    constructor(func: () => any) {
        this.func = func;
        this.timestamp = new Date().getTime();
        this.timeoutVar = undefined;
    }

    getTimestamp(): number {
        return this.timestamp;
    }

    getFunc(): () => any {
        return this.func;
    }

    setFunc(func: () => any) {
        this.func = func;
    }

    getTimeoutVar(): number {
        return this.timeoutVar;
    }

    setTimeoutVar(timeoutVar: number) {
        this.timeoutVar = timeoutVar;
    }
}