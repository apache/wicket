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
package org.apache.wicket.behavior;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.ng.resource.JavascriptResourceReference;

/**
 * @author Juergen Donnerstag
 */
public class JavascriptHeaderContributor extends AbstractHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private final Class<?> scope;
	private final String path;

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the caller, or a class
	 *            that lives in the package where the resource lives).
	 * @param path
	 *            The path
	 */
	public JavascriptHeaderContributor(final Class<?> scope, final String path)
	{
		this.scope = scope;
		this.path = path;
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.renderJavascriptReference(new JavascriptResourceReference(scope, path));
	}
}
