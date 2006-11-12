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
package wicket.resource;

import wicket.Application;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupStream;

/**
 * Dummy component used for testing or resource loading funationality.
 * 
 * @param <T>
 *            The type
 * @author Chris Turner
 */
public class DummyComponent<T> extends Component<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Create the component with the given name.
	 * 
	 * @param parent
	 * @param name
	 *            The name of the component
	 * @param application
	 *            The application for this component
	 */
	public DummyComponent(MarkupContainer parent, final String name, final Application application)
	{
		super(parent, name);
	}

	/**
	 * 
	 * @see wicket.Component#onRender(wicket.markup.MarkupStream)
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
	}
}
