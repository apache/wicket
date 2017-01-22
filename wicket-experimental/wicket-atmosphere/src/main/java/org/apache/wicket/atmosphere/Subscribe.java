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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Predicate;

/**
 * Subscribes a method on a component to receive events of a certain type. The method should have 2
 * parameters: the first must be {@link AjaxRequestTarget}, the second defines the type of events to
 * receive. This method will receive any event posted to the {@link EventBus} if it matches the type
 * of the second parameter and the filter accepts it. Any context a Wicket component expects to be
 * available, such as the {@link RequestCycle} and {@link Session}, is accessible on invocation of
 * the method.
 * 
 * <p>
 * Annotated methods will automatically be detected by {@link AtmosphereEventSubscriptionCollector}.
 * The page on which the component is placed will get a {@link AtmosphereBehavior}, which sets up a
 * persistent connection (for example websocket or streaming http).
 * 
 * @author papegaaij
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
	/**
	 * An optional filter on events to be received by the method. The filter cannot rely on any
	 * context. For example, the {@link RequestCycle} may not be available. For events filtered by
	 * this filter, Wicket-Atmosphere will not have to setup initiate the Wicket request cycle,
	 * which is quite expensive.
	 * 
	 * @return The filter on events, defaults to no filter.
	 */
	Class<? extends Predicate<AtmosphereEvent>> filter() default NoFilterPredicate.class;

	/**
	 * An optional filter on events to be received by the method. This filter has access to the
	 * Wicket context, such as the {@link Session} and the {@link RequestCycle}. If your filter does
	 * not require this context, you should use {@link #filter()} to prevent unnecessary setup of
	 * the request cycle.
	 * 
	 * @return The filter on events, defaults to no filter.
	 */
	Class<? extends Predicate<AtmosphereEvent>> contextAwareFilter() default NoFilterPredicate.class;
}
