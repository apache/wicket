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
package org.apache.wicket.page;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;

/**
 * A {@link PartialPageUpdate} that serializes itself to XML.
 */
public class XmlPartialPageUpdate extends PartialPageUpdate
{
	/**
	 * The name of the root element in the produced XML document.
	 */
	public static final String START_ROOT_ELEMENT = "<ajax-response>";
	public static final String END_ROOT_ELEMENT = "</ajax-response>";

	public XmlPartialPageUpdate(final Page page)
	{
		super(page);
	}

	@Override
	public void setContentType(WebResponse response, String encoding)
	{
		response.setContentType("text/xml; charset=" + encoding);
	}

	@Override
	protected void writeHeader(Response response, String encoding)
	{
		response.write("<?xml version=\"1.0\" encoding=\"");
		response.write(encoding);
		response.write("\"?>");
		response.write(START_ROOT_ELEMENT);
	}

	@Override
	protected void writeComponent(Response response, String markupId, CharSequence contents)
	{
		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ><![CDATA[");
		response.write(encode(contents));
		response.write("]]></component>");
	}

	@Override
	protected void writeFooter(Response response, String encoding)
	{
		response.write(END_ROOT_ELEMENT);
	}

	@Override
	protected void writePriorityEvaluation(Response response, CharSequence contents)
	{
		writeHeaderContribution(response, "priority-evaluate", contents);
	}
	
	@Override
	protected void writeHeaderContribution(Response response, CharSequence contents)
	{
		writeHeaderContribution(response, "header-contribution", contents);
	}

	@Override
	protected void writeEvaluation(Response response, CharSequence contents)
	{
		writeHeaderContribution(response, "evaluate", contents);
	}

	private void writeHeaderContribution(Response response, String elementName, CharSequence contents)
	{
		if (Strings.isEmpty(contents) == false)
		{
			response.write("<" + elementName + ">");

			// we need to write response as CDATA and parse it on client,
			response.write("<![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
			response.write(encode(contents));
			response.write("</head>]]>");
			response.write("</" + elementName + ">");
		}
	}

	protected CharSequence encode(CharSequence str)
	{
		return Strings.replaceAll(str, "]]>", "]]]]><![CDATA[>"); 
	}


}
