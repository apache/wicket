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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;

/**
 * Mock page for testing.
 * 
 */
public class BorderRenderWrappedTestPage_1 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * 
	 */
	public BorderRenderWrappedTestPage_1()
	{
		BoxBorder border = new BoxBorder("box");
// border.setBorderBodyVisible(false);
		border.getBodyContainer().setVisible(false);

		add(border);

		// NOTE in order for body.setVisible(false) to be able to work properly,
		// you must add the body child components to the body. It is only for
		// the developers convinience that he may add all border child
		// components to the border.
// border.add(new TextField("text"));
// border.add(new Image("img"));
		border.getBodyContainer().add(new TextField("text"));
		border.getBodyContainer().add(new Image("img"));
	}
}
