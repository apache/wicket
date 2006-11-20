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
package wicket.extensions.util.resource;

import java.util.Map;

import wicket.util.string.JavascriptUtils;

/**
 * Decorates the template with javascript tags.
 * 
 * @author Eelco Hillenius
 */
public final class JavaScriptTemplate extends TextTemplateDecorator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param textTemplate
	 *            The template to decorate
	 */
	public JavaScriptTemplate(TextTemplate textTemplate)
	{
		super(textTemplate);
	}

	/**
	 * @see wicket.extensions.util.resource.TextTemplateDecorator#getBeforeTemplateContents()
	 */
	public String getBeforeTemplateContents()
	{
		return JavascriptUtils.SCRIPT_OPEN_TAG;
	}

	/**
	 * @see wicket.extensions.util.resource.TextTemplateDecorator#getAfterTemplateContents()
	 */
	public String getAfterTemplateContents()
	{
		return JavascriptUtils.SCRIPT_CLOSE_TAG;
	}

	/**
	 * This class decorates another text template class and so
	 * does not allow interpolation.
	 * 
	 * @param variables
	 *            Ignored
	 * @return This for chaining
	 */
	public TextTemplate interpolate(final Map variables)
	{
		return this;
	}
}