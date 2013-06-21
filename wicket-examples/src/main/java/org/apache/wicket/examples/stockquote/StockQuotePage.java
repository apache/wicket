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
package org.apache.wicket.examples.stockquote;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.io.IClusterable;


/**
 * Stock quote webservice custom component example.
 * 
 * @author Martijn Dashorst
 */
public class StockQuotePage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public StockQuotePage()
	{
		// code for the first example, directly using the
		// stock quote component.
		add(new StockQuoteLabel("stockIBM", "IBM"));

		// use the second example component
		add(new StockQuoteLabel2("stock2IBM", "IBM"));

		// code for second example: using a form for selecting the
		// symbol of the stock quote.

		// shared model to get and set the symbol property on the
		// quote instance
		final IModel<String> model = new PropertyModel<>(quote, "symbol");

		// form holding the input field.
		final Form<?> form = new Form("form");
		add(form);
		form.add(new TextField<>("symbol", model));

		// labels for displaying the chosen symbol
		add(new Label("symbol", model));

		// and its quote
		add(new StockQuoteLabel("quote", model));
	}

	/**
	 * Quote instance used for communicating between the form input field and the display labels for
	 * showing the stock quote.
	 */
	private final Quote quote = new Quote();

	/**
	 * POJO to hold the symbol for the quote query.
	 */
	public static class Quote implements IClusterable
	{
		private String symbol;

		/**
		 * Gets the symbol.
		 * 
		 * @return the symbol
		 */
		public String getSymbol()
		{
			return symbol;
		}

		/**
		 * Sets the symbol.
		 * 
		 * @param symbol
		 *            the symbol
		 */
		public void setSymbol(String symbol)
		{
			this.symbol = symbol;
		}
	}
}