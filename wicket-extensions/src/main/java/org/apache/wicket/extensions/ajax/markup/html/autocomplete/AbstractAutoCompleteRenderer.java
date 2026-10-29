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

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * A renderer that abstracts auto-assist specific details and allows subclasses to only render the
 * visual part of the assist instead of having to also render the necessary auto-assist javascript
 * hooks.
 * <p>
 * When a choice is selected the autocompleter dispatches a cancelable
 * {@code wicket-autocomplete-select} event on the input field. Its {@code detail.item} is the
 * element of the selected choice and its {@code detail.value} the text value that will be copied
 * into the input field. A listener may replace {@code detail.value}, or call
 * {@code preventDefault()} to leave the input field unchanged. Data a listener needs about the
 * choice can be written onto its element with {@link #getChoiceData(Object)}:
 * 
 * <pre>
 * document.getElementById('address').addEventListener('wicket-autocomplete-select', function(event) {
 * 	const data = event.detail.item.dataset;
 * 	document.getElementById('zipcode').value = data.zipcode;
 * 	document.getElementById('city').value = data.city;
 * });
 * </pre>
 * 
 * Register such a listener from {@code renderHead()}, for example with an
 * {@code OnDomReadyHeaderItem}, so that it carries the nonce of the Content Security Policy.
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

	private static final Pattern DATA_NAME = Pattern.compile("[a-z][a-z0-9-]*");

	@Override
	public void render(final T object, final Response response, final String criteria)
	{
		String textValue = getTextValue(object);
		if (textValue == null)
		{
			throw new IllegalStateException(
				"A call to textValue(Object) returned an illegal value: null for object: " +
					object.toString());
		}
		textValue = Strings.escapeMarkup(textValue).toString();

		response.write("<li textvalue=\"" + textValue + "\"");
		final CharSequence handler = getOnSelectJavaScriptExpression(object);
		if (handler != null)
		{
			response.write(" onselect=\"" + Strings.escapeMarkup(handler) + '"');
		}
		final Map<String, ?> data = getChoiceData(object);
		if (data != null)
		{
			for (Map.Entry<String, ?> entry : data.entrySet())
			{
				final String name = entry.getKey();
				Args.isTrue(name != null && DATA_NAME.matcher(name).matches(),
					"Invalid data attribute name '%s': use lowercase letters, digits and dashes",
					name);
				if (entry.getValue() != null)
				{
					response.write(" data-" + name + "=\"" +
						Strings.escapeMarkup(entry.getValue().toString()) + '"');
				}
			}
		}
		response.write(">");
		renderChoice(object, response, criteria);
		response.write("</li>");
	}

	@Override
	public void renderHeader(final Response response)
	{
		response.write("<ul>");
	}

	@Override
	public void renderFooter(final Response response, int count)
	{
		response.write("</ul>");
	}

	/**
	 * Render the visual portion of the assist. Usually the html representing the assist choice
	 * object is written out to the response use {@link Response#write(CharSequence)}
	 * 
	 * <p>
	 * <b>Whatever is written to the response reaches the markup as is, without escaping.</b>
	 * Any part of it that comes from user input or other dynamic content must be escaped
	 * before it is written, with {@link Strings#escapeMarkup(CharSequence)}.
	 * </p>
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
	 * 	js.append(&quot;Wicket.DOM.get('street').value ='&quot; + address.getStreet() + &quot;';&quot;);
	 * 	js.append(&quot;Wicket.DOM.get('zipcode').value ='&quot; + address.getZipCode() + &quot;';&quot;);
	 * 	js.append(&quot;Wicket.DOM.get('city').value ='&quot; + address.getCity() + &quot;';&quot;);
	 * 	js.append(&quot;arguments[0]&quot;); // &lt;-- do not use return statement here!
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
	 *         intended
	 * @deprecated The expression is run with {@code eval()}, which the default Content Security
	 *             Policy of Wicket blocks: it only works when the application allows
	 *             {@code 'unsafe-eval'}. Building the expression from item data, as in the
	 *             examples above, is also unsafe: the markup escaping of the attribute does not
	 *             protect the data once the expression is evaluated, so any such data has to be
	 *             escaped for a JavaScript string by the application. Listen for the
	 *             {@code wicket-autocomplete-select} event
	 *             instead (see the class documentation) and pass the data it needs with
	 *             {@link #getChoiceData(Object)}.
	 */
	@Deprecated(since = "10.12.0", forRemoval = true)
	protected CharSequence getOnSelectJavaScriptExpression(final T item)
	{
		return null;
	}

	/**
	 * Returns data to write onto the element of the given choice, for a
	 * {@code wicket-autocomplete-select} listener to read from {@code event.detail.item.dataset}.
	 * <p>
	 * Each entry is written as a {@code data-<name>} attribute. Its value is converted with
	 * {@code toString()} and escaped with {@link Strings#escapeMarkup(CharSequence)}; an entry with
	 * a {@code null} value is skipped. Names must consist of lowercase letters, digits and dashes,
	 * and start with a letter.
	 * 
	 * @param item
	 *            the autocomplete choice
	 * @return the data of the choice, keyed by attribute name without the {@code data-} prefix,
	 *         or {@code null} for none
	 * @since 10.12.0
	 */
	protected Map<String, ?> getChoiceData(final T item)
	{
		return null;
	}
}
