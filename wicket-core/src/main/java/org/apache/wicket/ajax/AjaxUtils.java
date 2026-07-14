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
package org.apache.wicket.ajax;

import java.util.Optional;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * Some AJAX related utility functions.
 */
public class AjaxUtils
{

    /**
     * Runs action if current request is of type "AJAX". Otherwise, nothing is done.
     *
     * @param ajaxAction
     *            The action to run a {@link SerializableConsumer}
     */
    public static void executeIfAjax(SerializableConsumer<AjaxRequestTarget> ajaxAction)
    {
        Optional<AjaxRequestTarget> target = RequestCycle.get().find(AjaxRequestTarget.class);
        target.ifPresent(ajaxAction);
    }


    /**
     * Runs action if current request is of type "AJAX" or a Websockets request. Otherwise, nothing is done.
     *
     * @param ajaxAction
     *            The action to run a {@link SerializableConsumer}
     */
    public static void executeIfAjaxOrWebSockets(SerializableConsumer<IPartialPageRequestHandler> ajaxAction)
    {
        Optional<IPartialPageRequestHandler> target = RequestCycle.get().find(IPartialPageRequestHandler.class);
        target.ifPresent(ajaxAction);
    }

}
