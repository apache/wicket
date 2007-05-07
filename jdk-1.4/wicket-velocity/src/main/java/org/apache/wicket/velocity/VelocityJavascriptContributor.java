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

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Packages;

/**
 * A derivation of VelocityContributor that uses
 * {@link wicket.markup.html.IHeaderResponse#renderJavascript(CharSequence, String)}
 */
public class VelocityJavascriptContributor extends VelocityContributor
{
	private final String id;

	/**
	 * Ctor
	 * 
	 * Use this constructor if you have configured Velocity to use a
	 * ClasspathResourceLoader. The templatePath will then be relative to the
	 * package for clazz
	 * 
	 * @param clazz
	 * @param templatePath
	 * @param model
	 * @param id
	 */
	public VelocityJavascriptContributor(Class clazz, String templatePath, IModel model,
			String id)
	{
		super(Packages.absolutePath(clazz, templatePath), model);
		this.id = id;
	}

	/**
	 * Construct.
	 * 
	 * Use this constructor when Velocity is configured with the
	 * FileResourceLoader. templatePath with then be relative to the loader path
	 * configured in velocity.properties
	 * 
	 * @param templatePath
	 * @param model
	 * @param id
	 */
	public VelocityJavascriptContributor(String templatePath, IModel model, String id)
	{
		super(templatePath, model);
		this.id = id;
	}

	/**
	 * @see org.apache.wicket.velocity.VelocityContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		CharSequence s = evaluate();
		if (s != null)
		{
			response.renderJavascript(s, id);
		}
	}
}
