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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class SimplePage_3 extends SimplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public SimplePage_3()
	{
		get("myLabel").setVisible(false);
		get("test").setVisible(false);
		get("myPanel").setVisible(false);
		get("myBorder").setVisible(false);
		get("myBorder2").setVisible(false);
	}

	/**
	 * @see org.apache.wicket.Page#getMarkup()
	 */
	@Override
	public IMarkupFragment getMarkup()
	{
		return Markup.of("<html xmlns:wicket><body>" //
			+ "<span wicket:id='myLabel'>mein Label</span>" //
			+ "<span wicket:id='test'>body</span>" //
			+ "<span wicket:id='myPanel'>panel</span>" //
			+ "<span wicket:id='myBorder'>border</span>" //
			+ "</body></html>");
	}
}
