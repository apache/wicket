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
package org.apache.wicket.atmosphere;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Invokes {@link EventSubscription}
 */
public interface EventSubscriptionInvoker
{
	/**
	 * 
	 * @param target
	 *            {@link AjaxRequestTarget} to which {@link EventSubscription} result should be set
	 * @param subscription
	 *            {@link EventSubscription}
	 * @param base
	 *            {@link EventSubscription} object on which {@link EventSubscription} should be
	 *            invoked
	 * @param event
	 * @param ajaxRequestInitializer
	 *            call {@code ajaxRequestInitializer.initialize()} before you are going to invoke
	 *            {@link EventSubscription}
	 * @return true if invocation was successful
	 */
	boolean invoke(AjaxRequestTarget target, EventSubscription subscription, Object base,
		AtmosphereEvent event, AjaxRequestInitializer ajaxRequestInitializer);
}
