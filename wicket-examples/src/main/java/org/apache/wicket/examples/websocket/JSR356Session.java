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

package org.apache.wicket.examples.websocket;


import java.util.concurrent.ScheduledExecutorService;

import org.apache.wicket.examples.websocket.progress.ProgressUpdater;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

public class JSR356Session extends WebSession
{
    private ProgressUpdater.ProgressUpdateTask progressUpdateTask;

    public JSR356Session(Request request)
    {
        super(request);
    }

    public ProgressUpdater.ProgressUpdateTask getProgressUpdateTask()
    {
        return progressUpdateTask;
    }

    private synchronized void startTask() {
        if (progressUpdateTask != null && progressUpdateTask.isRunning())
        {
            return;
        }

        JSR356Application application = JSR356Application.get();
        ScheduledExecutorService service = application.getScheduledExecutorService();
        progressUpdateTask = ProgressUpdater.start(application, getId(), service);
    }

    public synchronized void startOrCancelTask() {
        if (progressUpdateTask != null && progressUpdateTask.isRunning() && !progressUpdateTask.isCanceled())
        {
            progressUpdateTask.cancel();
        }
        else
        {
            startTask();
        }
    }

    public static JSR356Session get() {
        return (JSR356Session)WebSession.get();
    }
}
