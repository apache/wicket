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

import {Channel} from "./Channel";
import {isUndef} from "./WicketUtils";
import {FunctionsExecuter} from "./FunctionsExecuter";

/**
 * Channel manager maintains a map of channels.
 */
export class ChannelManager {

    channels:Channel[];

    public static FunctionsExecuter = FunctionsExecuter;

    constructor() {
        this.channels = [];
    }

    // Schedules the callback to channel with given name.
    public schedule (channel: string, callback: () => any): any {
        let parsed = new Channel(channel);
        let c = this.channels[parsed.name];
        if (isUndef(c)) {
            c = parsed;
            this.channels[c.name] = c;
        } else {
            c.type = parsed.type;
        }
        return c.schedule(callback);
    }

    // Tells the ChannelManager that the current callback in channel with given name
    // has finished processing and another scheduled callback can be executed (if any).
    public done (channel: string): void {
        let parsed = new Channel(channel);
        let c: Channel = this.channels[parsed.name];
        if (!isUndef(c)) {
            c.done();
            if (!c.busy) {
                delete this.channels[parsed.name];
            }
        }
    }
}

export const channelManager = new ChannelManager();