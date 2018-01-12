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

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PartialPageUpdate} that serializes itself to XML.
 */
public class XmlPartialPageUpdate extends PartialPageUpdate
{
	private static final Logger LOG = LoggerFactory.getLogger(XmlPartialPageUpdate.class);

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
	protected void writeComponent(Response response, String markupId, Component component, String encoding)
	{
		final Page page = component.findParent(Page.class);

		// substitute our encoding response for the old one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		Response oldResponse = RequestCycle.get().setResponse(bodyBuffer);

		try
		{
			// render any associated headers of the component
			writeHeaderContribution(response, component);
			
			bodyBuffer.reset();
			
			try
			{
				component.renderPart();
			}
			catch (RuntimeException e)
			{
				bodyBuffer.reset();
				throw e;
			}
		}
		finally
		{
			// Restore original response
			RequestCycle.get().setResponse(oldResponse);
		}

		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ><![CDATA[");
		response.write(encode(bodyBuffer.getContents()));
		response.write("]]></component>");

		bodyBuffer.reset();
	}

	@Override
	protected void writeFooter(Response response, String encoding)
	{
		response.write(END_ROOT_ELEMENT);
	}

	@Override
	protected void writeHeaderContribution(Response response)
	{
		CharSequence contents = headerBuffer.getContents();
		if (Strings.isEmpty(contents) == false)
		{
			response.write("<header-contribution>");

			// we need to write response as CDATA and parse it on client,
			// because konqueror crashes when there is a <script> element
			response.write("<![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
			response.write(encode(contents));
			response.write("</head>]]>");
			response.write("</header-contribution>");
		}
	}

	@Override
	protected void writeNormalEvaluations(final Response response, final Collection<CharSequence> scripts)
	{
		writeEvaluations(response, "evaluate", scripts);

	}

	@Override
	protected void writePriorityEvaluations(Response response, Collection<CharSequence> scripts)
	{
		writeEvaluations(response, "priority-evaluate", scripts);
	}

	private void writeEvaluations(final Response response, String elementName, Collection<CharSequence> scripts)
	{
		if (scripts.size() > 0)
		{
			StringBuilder combinedScript = new StringBuilder(1024);
			for (CharSequence script : scripts)
			{
				combinedScript.append("(function(){").append(script).append("})();");
			}
			writeEvaluation(elementName, response, combinedScript);
		}
	}

	/**
	* @param invocation
	*            type of invocation tag, usually {@literal evaluate} or
	*            {@literal priority-evaluate}
	* @param response
	* @param js
	*/
	private void writeEvaluation(final String invocation, final Response response, final CharSequence js)
	{
		response.write("<");
		response.write(invocation);
		response.write(">");

		response.write("<![CDATA[");
		response.write(encode(js));
		response.write("]]>");

		response.write("</");
		response.write(invocation);
		response.write(">");

		bodyBuffer.reset();
	}

	protected CharSequence encode(CharSequence str)
	{
		return Strings.replaceAll(str, "]]>", "]]]]><![CDATA[>"); 
	}


}
