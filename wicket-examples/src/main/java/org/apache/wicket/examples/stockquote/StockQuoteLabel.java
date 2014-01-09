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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Gets a stock quote from a webservice. This component is a full blown, built from the ground up
 * component. See the other stock quote examples for alternatives of creating custom components.
 * 
 * The model that is provided is used as the symbol for lookup. When used without an explicit model,
 * the component tries the CompoundPropertyModel method of retrieving the model first. When that
 * results in an empty string, the component will use its ID as symbol.
 * 
 * You can use this component in your webapplication just as you might want to use a
 * <code>Label</code>.
 */
public class StockQuoteLabel extends WebComponent
{
	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 */
	public StockQuoteLabel(String id)
	{
		super(id);
	}

	/**
	 * Convenience constructor. Same as StockQuoteLabel(String, new Model&lt;String&gt;(String))
	 * 
	 * @param id
	 *            See Component
	 * @param symbol
	 *            The symbol to look up
	 * 
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public StockQuoteLabel(String id, String symbol)
	{
		super(id, new Model<>(symbol));
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public StockQuoteLabel(String id, IModel<String> model)
	{
		super(id, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		String symbol = getDefaultModelObjectAsString();
		StockQuote quote = new StockQuote(symbol);
		replaceComponentTagBody(markupStream, openTag, quote.getQuote());
	}
}
