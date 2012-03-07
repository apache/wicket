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
package org.apache.wicket.velocity;

import java.util.Map;

import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Packages;

/**
 * A derivation of VelocityContributor that uses
 * {@link org.apache.wicket.markup.head.IHeaderResponse#render(org.apache.wicket.markup.head.HeaderItem)}
 */
public class VelocityJavaScriptContributor extends VelocityContributor
{
	private static final long serialVersionUID = 1L;

	private final String id;

	/**
	 * Use this constructor if you have configured Velocity to use a ClasspathResourceLoader. The
	 * templatePath will then be relative to the package for clazz
	 * 
	 * @param clazz
	 * @param templatePath
	 * @param model
	 * @param id
	 */
	public VelocityJavaScriptContributor(final Class<?> clazz, final String templatePath,
		final IModel<? extends Map<?, ?>> model, final String id)
	{
		super(Packages.absolutePath(clazz, templatePath), model);
		this.id = id;
	}

	/**
	 * Use this constructor when Velocity is configured with the {@link FileResourceLoader}.
	 * templatePath with then be relative to the loader path configured in velocity.properties
	 * 
	 * @param templatePath
	 * @param model
	 * @param id
	 */
	public VelocityJavaScriptContributor(final String templatePath,
		final IModel<? extends Map<?, ?>> model, final String id)
	{
		super(templatePath, model);
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		CharSequence s = evaluate();
		if (s != null)
		{
			response.render(JavaScriptHeaderItem.forScript(s, id));
		}
	}
}
