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

import org.apache.wicket.Application;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.core.util.string.CssUtils;


/**
 * Decorates a <code>TextTemplate</code> with CSS tags.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public final class CssTemplate extends TextTemplateDecorator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param textTemplate
	 *            a <code>TextTemplate</code> to decorate
	 */
	public CssTemplate(TextTemplate textTemplate)
	{
		super(textTemplate);
	}

	/**
	 * @see org.apache.wicket.util.template.TextTemplateDecorator#getBeforeTemplateContents()
	 */
	@Override
	public String getBeforeTemplateContents()
	{
		return CssUtils.INLINE_OPEN_TAG;
	}

	/**
	 * @see org.apache.wicket.util.template.TextTemplateDecorator#getAfterTemplateContents()
	 */
	@Override
	public String getAfterTemplateContents()
	{
		return CssUtils.INLINE_CLOSE_TAG;
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
	public TextTemplate interpolate(final Map<String, ?> variables)
	{
		return this;
	}

	@Override
	public String getString()
	{
		String nonCompressed = super.getString();

		ICssCompressor compressor = null;
		if (Application.exists())
		{
			compressor = Application.get().getResourceSettings().getCssCompressor();
		}

		if (compressor != null)
		{
			return compressor.compress(nonCompressed);
		}
		else
		{
			// don't strip the comments
			return nonCompressed;
		}
	}
}