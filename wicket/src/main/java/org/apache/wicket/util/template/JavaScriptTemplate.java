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
package org.apache.wicket.util.template;

import java.util.Map;

import org.apache.wicket.util.string.JavascriptUtils;


/**
 * Decorates a <code>TextTemplate</code> with JavaScript tags.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public final class JavaScriptTemplate extends TextTemplateDecorator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param textTemplate
	 *            a <code>TextTemplate</code> to decorate
	 */
	public JavaScriptTemplate(TextTemplate textTemplate)
	{
		super(textTemplate);
	}

	/**
	 * @see org.apache.wicket.util.template.TextTemplateDecorator#getBeforeTemplateContents()
	 */
	@Override
	public String getBeforeTemplateContents()
	{
		return JavascriptUtils.SCRIPT_OPEN_TAG;
	}

	/**
	 * @see org.apache.wicket.util.template.TextTemplateDecorator#getAfterTemplateContents()
	 */
	@Override
	public String getAfterTemplateContents()
	{
		return JavascriptUtils.SCRIPT_CLOSE_TAG;
	}

	/**
	 * This class decorates another <code>TextTemplate</code> class and so does not allow
	 * interpolation.
	 * 
	 * @param variables
	 *            ignored
	 * @return <code>this</code>, for chaining purposes
	 */
	@Override
	public TextTemplate interpolate(final Map<String, Object> variables)
	{
		return this;
	}
}