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
package org.apache.wicket.protocol.ws.timer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base "timer" class that will schedule sending king alive "ping" messages to clients.
 */
public abstract class AbstractHeartBeatTimer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHeartBeatTimer.class);

    protected final WebSocketSettings webSocketSettings;

    // internal heartbeat's timer.
    private Timer heartBeatsTimer;

    public AbstractHeartBeatTimer(WebSocketSettings webSocketSettings)
    {
        this.webSocketSettings = webSocketSettings;
    }

    public final void start(Application application)
    {
        if (isTimerEnabled() == false)
        {
            return;
        }

        LOG.info("Starting scheduler thread pushing heart beats");

        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    sendHeartBeats(application);
                }
                catch (Exception e)
                {
                    LOG.error("Error sending heartbeats", e);
                }
            }
        };

        this.heartBeatsTimer = new Timer(true);
        this.heartBeatsTimer.schedule(timerTask, new Date(), webSocketSettings.getHeartBeatPace());
    }

    /**
     *
     * @return {@code true} if timer is enable via settings.
     */
    protected abstract boolean isTimerEnabled();

    public final void stop()
    {
        LOG.info("Stopping thread pushing heart beats");

        if (this.heartBeatsTimer != null)
        {
            this.heartBeatsTimer.cancel();
        }
    }

    /**
     * Override to schedule sending the heartbeats
     *
     * @param application the {@link org.apache.wicket.Application}
     */
    protected abstract void sendHeartBeats(Application application);
}
