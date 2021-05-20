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
package org.apache.wicket.examples.websocket.progress;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.examples.websocket.JSR356Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;

public class ProgressBarTogglePanel extends Panel
{

    private int progress = 0;
    private boolean showProgress = true;
    private ProgressUpdater.ProgressUpdateTask progressUpdateTask;

    public ProgressBarTogglePanel(String id)
    {
        super(id);

        setOutputMarkupId(true);

        add(new WebSocketBehavior()
        {
            @Override
            protected void onConnect(ConnectedMessage message) {
                progressUpdateTask = ProgressBarTogglePanel.startProgressTask(message);
            }
        });

        add(new AjaxLink<Void>("hideShowProgress")
        {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                showProgress = !showProgress;
                target.add(ProgressBarTogglePanel.this);
            }
        }.setBody((IModel<String>) () -> showProgress ? "Hide progress" : "Show progress"));

        add(new AjaxLink<Void>("cancelRestartTask")
        {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                if (progressUpdateTask.isRunning() && !progressUpdateTask.isCanceled())
                {
                    progressUpdateTask.cancel();
                }
                else
                {
                    ScheduledExecutorService service = JSR356Application.get().getScheduledExecutorService();
                    ProgressUpdater.restart(progressUpdateTask, service);
                }
                target.add(ProgressBarTogglePanel.this);
            }
        }.setBody(new IModel<String>()
        {
            @Override
            public String getObject()
            {
                return progressUpdateTask != null && progressUpdateTask.isRunning() && !progressUpdateTask.isCanceled() ? "Cancel task" : "Restart task";
            }
        }));

        add(new Label("progressBar", new IModel<String>()
        {
            @Override
            public String getObject()
            {
                return progressUpdateTask != null && progressUpdateTask.isRunning() ? "Background Task is " + progress + "% completed" : "No task is running";
            }
        })
        {
            @Override
            protected void onConfigure()
            {
                super.onConfigure();
                setVisible(showProgress);
            }
        });
    }

    public static ProgressUpdater.ProgressUpdateTask startProgressTask(ConnectedMessage message)
    {
        ScheduledExecutorService service = JSR356Application.get().getScheduledExecutorService();
        return ProgressUpdater.start(message, service);
    }

    @Override
    public void onEvent(IEvent<?> event)
    {
        if (event.getPayload() instanceof WebSocketPushPayload)
        {
            WebSocketPushPayload wsEvent = (WebSocketPushPayload) event.getPayload();
            if (wsEvent.getMessage() instanceof ProgressUpdater.ProgressUpdate)
            {
                ProgressUpdater.ProgressUpdate progressUpdate = (ProgressUpdater.ProgressUpdate)wsEvent.getMessage();
                progress = progressUpdate.getProgress();
                wsEvent.getHandler().add(this);
            }
        }
    }
}
