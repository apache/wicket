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
package org.apache.wicket.extensions.markup.html.basic;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

/**
 * If you have email addresses or web URLs in the data that you are displaying, then you can
 * automatically display those pieces of data as hyperlinks, you will not have to take any action to
 * convert that data.
 * <p>
 * Email addresses will be wrapped with a &lt;a href="mailto:xxx"&gt;xxx&lt;/a&gt; tag, where "xxx"
 * is the email address that was detected.
 * <p>
 * Web URLs will be wrapped with a &lt;a href="xxx"&gt;xxx&lt;/a&gt; tag, where "xxx" is the URL
 * that was detected (it can be any valid URL type, http://, https://, ftp://, etc...)
 * 
 * @author Juergen Donnerstag
 */
public class SmartLinkMultiLineLabel extends MultiLineLabel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see MultiLineLabel#MultiLineLabel(String, String)
	 */
	public SmartLinkMultiLineLabel(final String id, final String label)
	{
		this(id, new Model<>(label));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The non-null id of this component
	 * @param model
	 *            The component's model
	 */
	public SmartLinkMultiLineLabel(final String id, final IModel<String> model)
	{
		super(id, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final CharSequence body = getSmartLink(getDefaultModelObjectAsString());
		replaceComponentTagBody(markupStream, openTag, Strings.toMultilineMarkup(body));
	}

	/**
	 * Get the link parser. You may subclass that methods to provide your own LinkParser.
	 * 
	 * @return ILinkParser
	 */
	protected ILinkParser getLinkParser()
	{
		return new DefaultLinkParser();
	}

	/**
	 * Get the text after parsing by the link parser.
	 * 
	 * @param text
	 * @return smart link
	 */
	protected CharSequence getSmartLink(final CharSequence text)
	{
		return getLinkParser().parse(text.toString());
	}
}
