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
package org.apache.wicket.ajax;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AbstractAjaxResponse that serializes itself to XML.
 * <p>
 *     The elements of such response are:
 *     <ul>
 *         <li>priority-evaluate - an item of the prepend JavaScripts</li>
 *         <li>component - the markup of the updated component</li>
 *         <li>evaluate - an item of the onDomReady and append JavaScripts</li>
 *         <li>header-contribution - all HeaderItems which have been contributed in components'
 *         and their behaviors' #renderHead(Component, IHeaderResponse)</li>
 *     </ul>
 * </p>
 */
public abstract class XmlAjaxResponse extends AbstractAjaxResponse
{
	private static final Logger LOG = LoggerFactory.getLogger(XmlAjaxResponse.class);

	public XmlAjaxResponse(final Page page)
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
		response.write("<ajax-response>");
	}

	@Override
	protected void writeComponent(Response response, String markupId, Component component, String encoding)
	{
		if (component.getRenderBodyOnly() == true)
		{
			throw new IllegalStateException(
					"Ajax render cannot be called on component that has setRenderBodyOnly enabled. Component: " +
							component.toString());
		}

		component.setOutputMarkupId(true);

		// substitute our encoding response for the real one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		encodingBodyResponse.reset();
		RequestCycle.get().setResponse(encodingBodyResponse);

		// Initialize temporary variables
		final Page page = component.findParent(Page.class);
		if (page == null)
		{
			// dont throw an exception but just ignore this component, somehow
			// it got removed from the page.
			LOG.debug("component: " + component + " with markupid: " + markupId +
					" not rendered because it was already removed from page");
			return;
		}

		page.startComponentRender(component);

		try
		{
			component.prepareForRender();

			// render any associated headers of the component
			writeHeaderContribution(response, component);
		}
		catch (RuntimeException e)
		{
			try
			{
				component.afterRender();
			}
			catch (RuntimeException e2)
			{
				// ignore this one could be a result off.
			}
			// Restore original response
			RequestCycle.get().setResponse(response);
			encodingBodyResponse.reset();
			throw e;
		}

		try
		{
			component.render();
		}
		catch (RuntimeException e)
		{
			RequestCycle.get().setResponse(response);
			encodingBodyResponse.reset();
			throw e;
		}

		page.endComponentRender(component);

		// Restore original response
		RequestCycle.get().setResponse(response);

		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ");
		if (encodingBodyResponse.isContentsEncoded())
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\" ");
		}
		response.write("><![CDATA[");
		response.write(encodingBodyResponse.getContents());
		response.write("]]></component>");

		encodingBodyResponse.reset();
	}

	@Override
	protected void writeFooter(Response response, String encoding)
	{
		response.write("</ajax-response>");
	}

	@Override
	protected void writeHeaderContribution(Response response)
	{
		if (encodingHeaderResponse.getContents().length() != 0)
		{
			response.write("<header-contribution");

			if (encodingHeaderResponse.isContentsEncoded())
			{
				response.write(" encoding=\"");
				response.write(getEncodingName());
				response.write("\" ");
			}

			// we need to write response as CDATA and parse it on client,
			// because konqueror crashes when there is a <script> element
			response.write("><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
			response.write(encodingHeaderResponse.getContents());
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
		boolean encoded = false;
		CharSequence javascript = js;

		// encode the response if needed
		if (needsEncoding(js))
		{
			encoded = true;
			javascript = encode(js);
		}

		response.write("<");
		response.write(invocation);
		if (encoded)
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\"");
		}
		response.write(">");

		response.write("<![CDATA[");
		response.write(javascript);
		response.write("]]>");

		response.write("</");
		response.write(invocation);
		response.write(">");

		encodingBodyResponse.reset();
	}

}
