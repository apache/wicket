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
package org.apache.wicket.resource;

import javax.script.Bindings;

import org.apache.wicket.request.resource.AbstractResource.ResourceResponse;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Creates a nashorn resource reference to accept java script code from the client side
 * 
 * @author Tobias Soloschenko
 *
 */
public class NashornResourceReference extends ResourceReference
{

	/**
	 * Creates a nashorn resource reference with the given name
	 * 
	 * @param name
	 *            the name of the nashorn resource reference
	 */
	public NashornResourceReference(String name)
	{
		super(name);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public IResource getResource()
	{
		return new NashornResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void setup(Attributes attributes, Bindings bindings)
			{
				NashornResourceReference.this.setup(attributes, bindings);
			}

			@Override
			protected ResourceResponse processError(Exception e)
			{
				return NashornResourceReference.this.processError(e);
			}
		};
	}

	/**
	 * Customize the error response sent to the client
	 * 
	 * @param e
	 *            the exception occurred
	 * @return the error response
	 */
	protected ResourceResponse processError(Exception e)
	{
		return null;
	}

	/**
	 * Setup the bindings and make information available to the scripting context
	 * 
	 * @param attributes
	 *            the attributes of the request
	 * @param bindings
	 *            the bindings to add java objects to
	 */
	protected void setup(Attributes attributes, Bindings bindings)
	{
		// NOOP
	}
}
