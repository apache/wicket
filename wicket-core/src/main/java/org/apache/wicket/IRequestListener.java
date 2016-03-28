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
package org.apache.wicket;


import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.util.io.IClusterable;

/**
 * Interface to be implemented by {@link Component}s or {@link org.apache.wicket.behavior.Behavior}s
 * that listen for requests from the client browser.
 * 
 * @author Jonathan Locke
 */
public interface IRequestListener extends IClusterable
{

	/**
	 * Does invocation of this listener render the page. 
	 * 
	 * @return default {@code true}, i.e. a {@link RenderPageRequestHandler} is schedules after invocation 
	 */
	default boolean rendersPage()
	{
		return true;
	}
	
	/**
	 * Called when a request is received.
	 */
	void onRequest();
}
