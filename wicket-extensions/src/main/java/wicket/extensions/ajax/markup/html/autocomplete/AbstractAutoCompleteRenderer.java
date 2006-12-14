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
package wicket.extensions.ajax.markup.html.autocomplete;

import wicket.Response;

/**
 * A renderer that abstracts autoassist specific details and allows subclasses
 * to only render the visual part of the assist instead of having to also render
 * the necessary autoassist javascript hooks.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractAutoCompleteRenderer implements IAutoCompleteRenderer
{

	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#render(java.lang.Object,
	 *      wicket.Response, String)
	 */
	public final void render(Object object, Response response, String criteria)
	{
		String textValue = getTextValue(object);
		if (textValue == null)
		{
			throw new IllegalStateException(
					"A call to textValue(Object) returned an illegal value: null for object: "
							+ object.toString());
		}
		textValue = textValue.replaceAll("\\\"", "&quot;");
		
		response.write("<li textvalue=\"" + textValue + "\">");
		renderChoice(object, response, criteria);
		response.write("</li>");
	}


	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#renderHeader(wicket.Response)
	 */
	public final void renderHeader(Response response)
	{
		response.write("<ul>");
	}

	/**
	 * @see wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer#renderFooter(wicket.Response)
	 */
	public final void renderFooter(Response response)
	{
		response.write("</ul>");
	}

	/**
	 * Render the visual portion of the assist. Usually the html representing
	 * the assist choice object is written out to the response use
	 * {@link Response#write(CharSequence)}
	 * 
	 * @param object
	 *            current assist choice
	 * @param response
	 * @param criteria 
	 */
	protected abstract void renderChoice(Object object, Response response, String criteria);

	/**
	 * Retrieves the text value that will be set on the textbox if this assist
	 * is selected
	 * 
	 * @param object
	 *            assist choice object
	 * @return the text value that will be set on the textbox if this assist is
	 *         selected
	 */
	protected abstract String getTextValue(Object object);
}
