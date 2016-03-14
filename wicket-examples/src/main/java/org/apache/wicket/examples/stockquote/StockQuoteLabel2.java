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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * StockQuote example provided by Jonathan Locke. This component shows you how to reuse existing
 * components (the StockQuoteLabel ISA Label), and how to use models internally in your component.
 * 
 * Note that this component doesn't work with CompoundPropertyModel's lazy initialization.
 */
public class StockQuoteLabel2 extends Label
{
	/**
	 * Constructor taking the symbol directly.
	 * 
	 * @param id
	 *            the component id
	 * @param symbol
	 *            the symbol to look up
	 */
	public StockQuoteLabel2(String id, final String symbol)
	{
		super(id, new IModel<String>()
		{
			/**
			 * Gets the stockquote for the given symbol.
			 */
			@Override
			public String getObject()
			{
				final StockQuote quote = new StockQuote(symbol);
				return quote.getQuote();
			}
		});
	}
}
