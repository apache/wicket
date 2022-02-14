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
 * A timer class that periodically schedules sending ping-pong heartbeats to all connected clients.
 * If pong message fails to be received for some time then connection is closed. No reconnection mechanism
 * is provided as ping-pong does not define client side listening for ping messages (pong will be generated probably
 * by the browser)
 */
public class PingPongHeartBeatTimer extends AbstractHeartBeatTimer {

    private static final Logger LOG = LoggerFactory.getLogger(PingPongHeartBeatTimer.class);

    public PingPongHeartBeatTimer(WebSocketSettings webSocketSettings)
    {
       super(webSocketSettings);
    }

    @Override
    protected boolean isTimerEnabled() {
        if (webSocketSettings.isUsePingPongHeartBeat() ==  false)
        {
            LOG.info("usePingPongHeartBeat is set to false. Thus we won't start ping pong heartbeat's sending thread");
            return false;
        }
        return true;
    }


    protected void sendHeartBeats(Application application)
    {
        final long heartBeatPace = webSocketSettings.getHeartBeatPace();
        final long networkLatencyThreshold = webSocketSettings.getNetworkLatencyThreshold();
        final Executor heartBeatsExecutor = webSocketSettings.getHeartBeatsExecutor();
        final int maxPingRetries = webSocketSettings.getMaxPingRetries();
        for (IWebSocketConnection connection: webSocketSettings.getConnectionRegistry().getConnections(application))
        {
            // connection didn't receive the PONG from peer terminate it
            if (connection.isAlive() == false)
            {
                if (connection.getLastTimeAlive() - System.currentTimeMillis() > (heartBeatPace + networkLatencyThreshold))
                {
                    heartBeatsExecutor.run(() -> terminateConnection(connection));
                }
            }
            else
            {
                heartBeatsExecutor.run(() -> ping(connection, maxPingRetries));
            }
        }
    }

    private void ping(IWebSocketConnection connection, final int pingRetryCounter)
    {
        try
        {
            connection.ping();
        }
        catch (IOException e)
        {
            if (pingRetryCounter == 0)
            {
                // ping failed enough times kill connection
                terminateConnection(connection);
            }
            else
            {
                ping(connection, pingRetryCounter - 1);
            }
        }
    }

    private void terminateConnection(IWebSocketConnection connection)
    {
        connection.setAlive(false);
        if (LOG.isInfoEnabled())
        {
            LOG.info("Terminating connection with ID {} because ping of remote peer failed {} times",
                    connection.getKey(), webSocketSettings.getMaxPingRetries());
        }
        connection.terminate("Failed to ping remote peer");
        webSocketSettings.getConnectionRegistry().removeConnection(connection.getApplication(), connection.getSessionId(), connection.getKey());
    }
}
