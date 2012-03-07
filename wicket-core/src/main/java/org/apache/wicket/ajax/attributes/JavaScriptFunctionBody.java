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
package org.apache.wicket.ajax.attributes;

import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;

/**
 * A base class for classes which contribute one way or another JavaScript
 * to the AjaxRequestAttributes.
 * Such classes can contribute to the header response by overriding #renderHead(Component, IHeaderResponse)
 * method.
 */
public class JavaScriptFunctionBody implements IComponentAwareHeaderContributor, IClusterable
{
	private final CharSequence functionBody;

	/**
	 * Constructor.
	 *
	 * @param functionBody
	 *      the body of JavaScript function which will be evaluated in the browser
	 */
	public JavaScriptFunctionBody(final CharSequence functionBody)
	{
		this.functionBody = functionBody;
	}

	@Override
	public String toString()
	{
		return functionBody.toString();
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
	}
}
