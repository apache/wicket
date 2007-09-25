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
package org.apache.wicket.protocol.http.portlet;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.RequestContext;
import org.apache.wicket.Response;
import org.apache.wicket.markup.html.internal.HeaderResponse;
import org.apache.wicket.response.StringResponse;

/**
 * @author Ate Douma
 */
public class EmbeddedPortletHeaderResponse extends HeaderResponse
{
	private Response realResponse;
	private StringResponse bufferedResponse;
	
	public EmbeddedPortletHeaderResponse(Response realResponse)
	{
		this.realResponse = realResponse;
		bufferedResponse = new StringResponse();
	}

	public void renderCSSReference(String url, String media)
	{
		if (!isClosed())
		{
			List token = Arrays.asList(new Object[] { "css", url, media });
			if (wasRendered(token) == false)
			{
				getResponse().write("<script type=\"text/javascript\">");
				getResponse().write("var elem=document.createElement(\"link\");");
				getResponse().write("elem.setAttribute(\"rel\",\"stylesheet\");");
				getResponse().write("elem.setAttribute(\"type\",\"text/css\");");
				getResponse().write("elem.setAttribute(\"href\",\""+url+"\");");
				if (media != null)
				{
					getResponse().write("elem.setAttribute(\"media\",\""+media+"\");");
				}
				getResponse().write("document.getElementsByTagName(\"head\")[0].appendChild(elem);");
				getResponse().println("</script>");
				markRendered(token);
			}
		}
	}

	
	/**
	 * @see org.apache.wicket.markup.html.internal.HeaderResponse#close()
	 */
	public void close()
	{
		super.close();
		// Automatically add <head> if necessary
		CharSequence output = bufferedResponse.getBuffer();
		if (output.length() > 0)
		{
			if (output.charAt(0) == '\r')
			{
				for (int i = 2; i < output.length(); i += 2)
				{
					char ch = output.charAt(i);
					if (ch != '\r')
					{
						output = output.subSequence(i - 2, output.length());
						break;
					}
				}
			}
			else if (output.charAt(0) == '\n')
			{
				for (int i = 1; i < output.length(); i++)
				{
					char ch = output.charAt(i);
					if (ch != '\n')
					{
						output = output.subSequence(i - 1, output.length());
						break;
					}
				}
			}
		}
		if (output.length() > 0)
		{
			realResponse.write("<span id=\""+RequestContext.get().getNamespace()+"_embedded_head\" style=\"display:none\">");
			realResponse.write(output);
			realResponse.write("</span>");
		}
		bufferedResponse.reset();
	}

	/**
	 * @see org.apache.wicket.markup.html.internal.HeaderResponse#getRealResponse()
	 */
	protected Response getRealResponse()
	{
		return bufferedResponse;
	}
}
