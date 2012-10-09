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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.util.string.Strings;

import com.google.common.base.Splitter;
import com.petebevin.markdown.MarkdownProcessor;

/**
 */
public class Markdown
{
	/**
	 * @param markdown
	 * @return
	 */
	public static String markdownToHtml(String markdown)
	{
		MarkdownProcessor processor = new MarkdownProcessor();
		String text = processor.markdown(markdown);

		text = processLiquidTags(text);
		return text;
	}

	private static String processLiquidTags(String text)
	{
		String markup;
		StringBuilder sb = new StringBuilder();

		int previousStart = 0;
		int start = text.indexOf("{% ");
		while (start > 0)
		{
			int end = text.indexOf("{% end", start);
			end = text.indexOf("%}", end + 5) + 2;
			sb.append(text.substring(previousStart, start));

			String block = text.substring(start, end);

			if (block.startsWith("{% highlight"))
			{
				sb.append(handleCodeBlock(block));
			}
			if (block.startsWith("{% alert"))
			{
				sb.append(handleAlert(block));
			}
			sb.append("\n");
			previousStart = end;
			start = text.indexOf("{% ", end);
		}
		if (previousStart < text.length())
			sb.append(text.substring(previousStart));

		markup = sb.toString();
		return markup;
	}

	private static String handleCodeBlock(String codeblock)
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
			result = Strings.escapeMarkup(result).toString();
		}
		return codeblock.substring(0, start) + result + "</pre>";
	}

	private static String handleAlert(String block)
	{
		int openTagStart = 0;
		int openTagEnd = block.indexOf("%}") + 2;
		int closeTagStart = block.indexOf("{%", openTagEnd);
		int closeTagEnd = block.indexOf("%}", closeTagStart) + 2;
		String tag = block.substring(openTagStart + 2, openTagEnd - 2).trim();

		Iterable<String> tagParts = Splitter.on("\\s+").split(tag);
		Iterator<String> partIterator = tagParts.iterator();

		ArrayList<String> arguments = new ArrayList<String>();
		String tagname = partIterator.next();
		for (String tagPart : tagParts)
		{
			arguments.add(tagPart);
		}

		String alertBody = block.substring(openTagEnd, closeTagStart);

		return "<div class=\"alert alert-info\">" + alertBody + "</div>";
	}
}
