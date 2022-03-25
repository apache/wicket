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

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timer class that periodically schedules sending custom heartbeats "ping" messages to all connected clients.
 * At client side some JavaScript machinery is listening for those messages. In case messages do not arrive for certain
 * time, client will assume connection to sever is dead and try to reconnect.
 */
public class HeartBeatWithReconnectTimer extends AbstractHeartBeatTimer
{
    private static final Logger LOG = LoggerFactory.getLogger(HeartBeatWithReconnectTimer.class);

    public HeartBeatWithReconnectTimer(WebSocketSettings webSocketSettings)
    {
      super(webSocketSettings);
    }

    @Override
    protected boolean isTimerEnabled() {
        if (webSocketSettings.isUseHeartBeat() ==  false)
        {
            LOG.info("useHeartBeat is set to false. Thus we won't start heartbeat's sending thread");

            return false;
        }
        return true;
    }

    protected void sendHeartBeats(Application application)
    {
        final Executor heartBeatsExecutor = webSocketSettings.getHeartBeatsExecutor();
        final int maxPingRetries = webSocketSettings.getMaxPingRetries();
        for (IWebSocketConnection connection: webSocketSettings.getConnectionRegistry().getConnections(application))
        {
            heartBeatsExecutor.run(() -> ping(connection, maxPingRetries));
        }
    }

    private void ping(IWebSocketConnection connection, final int pingRetryCounter)
    {
        try
        {
            // we just sent a binary message
            connection.sendMessage(new byte[]{10});
            // if client does not receive message it might try to reconnect
            // depending on settings
        }
        catch (IOException e)
        {
            ping(connection, pingRetryCounter - 1);
        }
    }
}
