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
package org.apache.wicket.protocol.http;

import org.apache.wicket.WicketRuntimeException;

/**
 * Thrown when the {@link org.apache.wicket.protocol.http.IWebApplicationFactory} could not be
 * created for some reason.
 * 
 * @author Seth Ladd
 */
public class WebApplicationFactoryCreationException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param appFactoryClassName
	 *            name of the application factory
	 * @param e
	 *            the cause for the creation problem
	 */
	public WebApplicationFactoryCreationException(String appFactoryClassName, Exception e)
	{
		super("Unable to create application factory of class " + appFactoryClassName, e);
	}
}
