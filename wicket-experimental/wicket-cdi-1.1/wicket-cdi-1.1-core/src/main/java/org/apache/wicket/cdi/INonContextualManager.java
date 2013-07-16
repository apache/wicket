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
package org.apache.wicket.cdi;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.wicket.Component;

/**
 * Manages lifecycle of non-contextual objects like {@link Component} instances, etc
 *
 * @author igor
 */
public interface INonContextualManager
{
	/**
	 * Inject a noncontextual instance
	 *
	 * @param <T>
	 * @param instance
	 */
	<T> void inject(T instance);

	/**
	 * Inject a noncontextual instance and invokes any {@link PostConstruct} callbacks
	 *
	 * @param <T>
	 * @param instance
	 */
	<T> void postConstruct(T instance);

	/**
	 * Invokes any {@link PreDestroy} callbacks and cleans up
	 *
	 * @param <T>
	 * @param instance
	 */
	<T> void preDestroy(T instance);
}
