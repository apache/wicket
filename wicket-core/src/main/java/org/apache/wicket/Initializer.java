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

import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.markup.html.form.IOnChangeListener;
import org.apache.wicket.markup.html.link.ILinkListener;

/**
 * Initializer for components in wicket core library.
 * 
 * @author Jonathan Locke
 */
public class Initializer implements IInitializer
{
	/**
	 * @see org.apache.wicket.IInitializer#init(org.apache.wicket.Application)
	 */
	@Override
	public void init(Application application)
	{
		// Register listener interfaces explicitly (even though they implicitly
		// register when loaded) because deserialization of an object that
		// implements an interface does not load the interfaces it implements!
		IBehaviorListener.INTERFACE.register();
		IFormSubmitListener.INTERFACE.register();
		ILinkListener.INTERFACE.register();
		IOnChangeListener.INTERFACE.register();
		IResourceListener.INTERFACE.register();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Wicket core library initializer";
	}

	/**
	 * @see org.apache.wicket.IInitializer#destroy(org.apache.wicket.Application)
	 */
	@Override
	public void destroy(Application application)
	{
	}
}
