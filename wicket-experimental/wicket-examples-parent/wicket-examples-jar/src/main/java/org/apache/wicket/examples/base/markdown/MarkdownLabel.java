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
package org.apache.wicket.examples.base.markdown;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import com.petebevin.markdown.MarkdownProcessor;

public class MarkdownLabel extends Label
{
	private static final long serialVersionUID = 1L;

	public MarkdownLabel(String id)
	{
		super(id);
	}

	public MarkdownLabel(String id, IModel<?> model)
	{
		super(id, model);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		setEscapeModelStrings(false);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		String article = getModelObjectAsUnescapedString();

		MarkdownProcessor processor = new MarkdownProcessor();
		String markup;
		String text = processor.markdown(article);

		text = processLiquidTags(text);

		replaceComponentTagBody(markupStream, openTag, text);
	}

	private String processLiquidTags(String text)
	{
		String markup;
		StringBuilder sb = new StringBuilder();

		int previousStart = 0;
		int start = text.indexOf("{% highlight");
		while (start > 0)
		{
			int end = text.indexOf("{% endhighlight %}", start) + 18;
			sb.append(text.substring(previousStart, start));

			String codeblock = text.substring(start, end);

			codeblock = escapeTags(codeblock);
			sb.append(codeblock);
			sb.append("\n");

			previousStart = end;
			start = text.indexOf("{% highlight", end);
		}
		if (previousStart < text.length())
			sb.append(text.substring(previousStart));

		markup = sb.toString();
		return markup;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String getModelObjectAsUnescapedString()
	{
		Object modelObject = getDefaultModelObject();
		if (modelObject != null)
		{
			// Get converter
			final Class<?> objectClass = modelObject.getClass();

			final IConverter converter = getConverter(objectClass);

			// Model string from property
			final String modelString = converter.convertToString(modelObject, getLocale());

			return modelString;
		}
		return null;
	}

	private String escapeTags(String codeblock)
	{
		codeblock = codeblock.replaceAll("\\{\\% highlight (.+) \\%\\}",
			"<pre class=\"prettyprint linenums\" lang=\"$1\">");
		codeblock = codeblock.replaceAll("\\{\\% endhighlight \\%\\}", "</pre>");

		int start = codeblock.indexOf(">") + 1;
		int end = codeblock.indexOf("</pre>");
		String result = codeblock.substring(start, end);
		result = result.replaceAll("\n\\ {4}", "\n");
		if (!codeblock.contains("\"java\""))
		{
			result = result.replaceAll("&", "&amp;");
			result = result.replaceAll("<", "&lt;");
			result = result.replaceAll(">", "&gt;");
		}
		return codeblock.substring(0, start) + result + "</pre>";
	}
}
