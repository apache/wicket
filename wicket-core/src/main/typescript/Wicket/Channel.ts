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

import {isUndef} from "./WicketUtils";
import {Log} from "./Log";

/**
 * Channel management
 *
 * Wicket Ajax requests are organized in channels. A channel maintain the order of
 * requests and determines, what should happen when a request is fired while another
 * one is being processed. The default behavior (stack) puts the all subsequent requests
 * in a queue, while the drop behavior limits queue size to one, so only the most
 * recent of subsequent requests is executed.
 * The name of channel determines the policy. E.g. channel with name foochannel|s is
 * a stack channel, while barchannel|d is a drop channel.
 *
 * The Channel class is supposed to be used through the ChannelManager.
 */
export class Channel {

    busy: boolean;
    public name: string;
    type: string;
    callbacks: (() => any)[];

    public constructor(name: string) {
        let res = name.match(/^([^|]+)\|(d|s|a)$/);
        if (isUndef(res)) {
            this.name = '0'; // '0' is the default channel name
            this.type = 's'; // default to stack/queue
        } else {
            this.name = res[1];
            this.type = res[2];
        }
        this.callbacks = [];
        this.busy = false;
    }

    public schedule(callback: () => any) {
        if (this.busy === false) {
            this.busy = true;
            try {
                return callback();
            } catch (exception) {
                this.busy = false;
                Log.error("An error occurred while executing Ajax request:" + exception);
            }
        } else {
            const busyChannel = "Channel '" + this.name + "' is busy";
            if (this.type === 's') { // stack/queue
                Log.info(busyChannel + " - scheduling the callback to be executed when the previous request finish.");
                this.callbacks.push(callback);
            } else if (this.type === 'd') { // drop
                Log.info(busyChannel + " - dropping all previous scheduled callbacks and scheduling a new one to be executed when the current request finish.");
                this.callbacks = [];
                this.callbacks.push(callback);
            } else if (this.type === 'a') { // active
                Log.info(busyChannel + " - ignoring the Ajax call because there is a running request.");
            }
            return null;
        }
    }

    public done() {
        let callback = null;

        if (this.callbacks.length > 0) {
            callback = this.callbacks.shift();
        }

        if (callback !== null && typeof (callback) !== "undefined") {
            Log.info("Calling postponed function...");
            // we can't call the callback from this call-stack
            // therefore we set it on timer event
            window.setTimeout(callback, 1);
        } else {
            this.busy = false;
        }
    }

}