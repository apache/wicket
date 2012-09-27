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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;

/**
 * A renderer that abstracts autoassist specific details and allows subclasses to only render the
 * visual part of the assist instead of having to also render the necessary autoassist javascript
 * hooks.
 * 
 * @param <T>
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractAutoCompleteRenderer<T> implements IAutoCompleteRenderer<T>
{
	private static final long serialVersionUID = 1L;

	@Override
	public final void render(final T object, final Response response, final String criteria)
	{
		String textValue = getTextValue(object);
		if (textValue == null)
		{
			throw new IllegalStateException(
				"A call to textValue(Object) returned an illegal value: null for object: " +
					object.toString());
		}
		textValue = textValue.replaceAll("\\\"", "&quot;");
		textValue = Strings.escapeMarkup(textValue).toString();

		response.write("<li textvalue=\"" + textValue + "\"");
		final CharSequence handler = getOnSelectJavaScriptExpression(object);
		if (handler != null)
		{
			response.write(" onselect=\"" + handler + '"');
		}
		response.write(">");
		renderChoice(object, response, criteria);
		response.write("</li>");
	}

	@Override
	public final void renderHeader(final Response response)
	{
		response.write("<ul>");
	}

	@Override
	public final void renderFooter(final Response response, int count)
	{
		response.write("</ul>");
	}

	/**
	 * Render the visual portion of the assist. Usually the html representing the assist choice
	 * object is written out to the response use {@link Response#write(CharSequence)}
	 * 
	 * @param object
	 *            current assist choice
	 * @param response
	 * @param criteria
	 */
	protected abstract void renderChoice(T object, Response response, String criteria);

	/**
	 * Retrieves the text value that will be set on the textbox if this assist is selected
	 * 
	 * @param object
	 *            assist choice object
	 * @return the text value that will be set on the textbox if this assist is selected
	 */
	protected abstract String getTextValue(T object);

	/**
	 * Allows the execution of a custom javascript expression when an item is selected in the
	 * autocompleter popup (either by clicking on it or hitting enter on the current selection).
	 * <p/>
	 * The javascript to execute must be a javascript expression that will be processed using
	 * javascript's eval(). The function should return the textvalue to copy it into the
	 * corresponding form input field (the default behavior).
	 * 
	 * the current text value will be in variable 'input'.
	 * 
	 * If the function returns <code>null</code> the chosen text value will be ignored.
	 * <p/>
	 * example 1:
	 * 
	 * <pre>
	 * protected CharSequence getOnSelectJavaScript(Address address)
	 * {
	 * 	final StringBuilder js = new StringBuilder();
	 * 	js.append(&quot;wicketGet('street').value ='&quot; + address.getStreet() + &quot;';&quot;);
	 * 	js.append(&quot;wicketGet('zipcode').value ='&quot; + address.getZipCode() + &quot;';&quot;);
	 * 	js.append(&quot;wicketGet('city').value ='&quot; + address.getCity() + &quot;';&quot;);
	 * 	js.append(&quot;input&quot;); // &lt;-- do not use return statement here!
	 * 	return js.toString();
	 * }
	 * </pre>
	 * 
	 * example 2:
	 * 
	 * <pre>
	 * protected CharSequence getOnSelectJavaScript(Currency currency)
	 * {
	 * 	final StringBuilder js = new StringBuilder();
	 * 	js.append(&quot;val rate = ajaxGetExchangeRateForCurrency(currencySymbol);&quot;);
	 * 	js.append(&quot;if(rate == null) alert('exchange rate service currently not available');&quot;);
	 * 	js.append(&quot;rate&quot;);
	 * 	return js.toString();
	 * }
	 * </pre>
	 * 
	 * Then the autocompleter popup will be closed.
	 * 
	 * @param item
	 *            the autocomplete item to get a custom javascript expression for
	 * @return javascript to execute on selection or <code>null</code> if default behavior is
	 *         intented
	 */
	protected CharSequence getOnSelectJavaScriptExpression(final T item)
	{
		return null;
	}
}
