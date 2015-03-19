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
package org.apache.wicket.markup.outputTransformer;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.transformer.AbstractTransformerBehavior;


/**
 * 
 * @author Juergen Donnerstag
 */
public class Page_3 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 */
	public Page_3()
	{
		add(new AbstractTransformerBehavior()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * 
			 * @see org.apache.wicket.markup.transformer.AbstractTransformerBehavior#transform(org.apache.wicket.Component,
			 *      java.lang.CharSequence)
			 */
			@Override
			public CharSequence transform(Component component, CharSequence output)
				throws Exception
			{
				// Convert all text to uppercase
				return output.toString().toUpperCase();
			}
		});
	}
}
