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

import {jQuery, isUndef} from "./WicketUtils";
import {Log} from "./Log";

/**
 * Functions executer takes array of functions and executes them.
 * The functions are executed one by one as far as the return value is FunctionsExecuter.DONE.
 * If the return value is FunctionsExecuter.ASYNC or undefined then the execution of
 * the functions will be resumed once the `notify` callback function is called.
 * This is needed because header contributions need to do asynchronous download of JS and/or CSS
 * and they have to let next function to run only after the download.
 * After the FunctionsExecuter is initialized, the start methods triggers the first function.
 */
export class FunctionsExecuter {

    /**
     * Response that should be used by a function when it finishes successfully
     * in synchronous manner
     * @type {number}
     */
    public static DONE = 1;

    /**
     * Response that should be used by a function when it finishes abnormally
     * in synchronous manner
     * @type {number}
     */
    public static FAIL = 2;

    /**
     * Response that may be used by a function when it executes asynchronous
     * code and must wait `notify()` to be executed.
     * @type {number}
     */
    public static ASYNC = 3;

    /**
     * An artificial number used as a limit of the call stack depth to avoid
     * problems like "too much recursion" in the browser.
     * The depth is not easy to be calculated because the memory used by the
     * stack depends on many factors
     * @type {number}
     */
    public static DEPTH_LIMIT = 1000;

    private readonly functions: (() => any)[];

    /**
     * The index of the currently executed function
     * @type {number}
     */
    private current: number;

    /**
     * Tracks the depth of the call stack when `notify` is used for
     * asynchronous notification that a function execution has finished.
     * Should be reset to 0 when at some point to avoid problems like
     * "too much recursion". The reset may break the atomicity by allowing
     * another instance of FunctionExecuter to run its functions
     * @type {number}
     */
    private depth: number;

    /**
     * @param functions {Array} - an array of functions to execute
     */
    public constructor(functions: (() => any)[]) {
        this.functions = functions;
        this.current = 0;
        this.depth = 0; // we need to limit call stack depth
    };

    public processNext(): number {
        if (this.current < this.functions.length) {
            let f, run;

            f = this.functions[this.current];
            run = function () {
                try {
                    const n = jQuery.proxy(this.notify, this);
                    return f(n);
                } catch (e) {
                    Log.error("FunctionsExecuter.processNext: " + e);
                    return FunctionsExecuter.FAIL;
                }
            };
            run = jQuery.proxy(run, this);
            this.current++;

            if (this.depth > FunctionsExecuter.DEPTH_LIMIT) {
                // to prevent stack overflow (see WICKET-4675)
                this.depth = 0;
                window.setTimeout(run, 1);
            } else {
                const retValue = run();
                if (isUndef(retValue) || retValue === FunctionsExecuter.ASYNC) {
                    this.depth++;
                }
                return retValue;
            }
        }
    }

    public start(): void {
        let retValue:number = FunctionsExecuter.DONE;
        while (retValue === FunctionsExecuter.DONE) {
            retValue = this.processNext();
        }
    }

    public notify(): void {
        this.start();
    }

}