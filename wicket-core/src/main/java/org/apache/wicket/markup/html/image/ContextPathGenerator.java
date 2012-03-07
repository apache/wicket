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
package org.apache.wicket.markup.html.image;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.core.util.string.UrlUtils;

/**
 * A behavior that converts the provider url fragment to a context-relative url. For example if the
 * current url is <code>http://localhost/context/product/1231</code> and the specified url is
 * <code>images/border.jpg</code> the generated url will be <code>../../images/border.jpg</code>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ContextPathGenerator extends Behavior
{
	private static final long serialVersionUID = 1L;

	private final IModel<String> contextRelativePath;

	/**
	 * Constructor
	 * 
	 * @param contextRelativePath
	 *            context-relative path, eg <code>images/border.jpg</code>
	 */
	public ContextPathGenerator(IModel<String> contextRelativePath)
	{
		this.contextRelativePath = contextRelativePath;
	}

	/**
	 * Constructor
	 * 
	 * @param contextRelativePath
	 *            context-relative path, eg <code>images/border.jpg</code>
	 */
	public ContextPathGenerator(String contextRelativePath)
	{
		this.contextRelativePath = new Model<String>(contextRelativePath);
	}

	/** {@inheritDoc} **/
	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		// get path
		final String path = contextRelativePath.getObject();

		final String rewritten = UrlUtils.rewriteToContextRelative(path, RequestCycle.get());

		tag.put("src", rewritten);
	}

	/** {@inheritDoc} **/
	@Override
	public void detach(Component component)
	{
		contextRelativePath.detach();
		super.detach(component);
	}


}
