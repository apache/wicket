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
package org.apache.wicket.ajax.markup.html.ajaxLink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.BoxBorder;

/**
 * 
 */
public class AjaxPage2 extends WebPage<Void>
{
	private static final long serialVersionUID = 1L;

	private final Label<String> ajaxLabel;
	private final BoxBorder<?> myBorder;

	/**
	 * Construct.
	 */
	public AjaxPage2()
	{
		super();

		myBorder = new BoxBorder<Void>("pageLayout");
		myBorder.setTransparentResolver(true);
		add(myBorder);

		ajaxLabel = new Label<String>("ajaxLabel", "AAAAAAA");
		ajaxLabel.setOutputMarkupId(true);
		add(ajaxLabel);

		add(new AjaxLink<Void>("ajaxLink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				Label<String> ajaxLabel2 = new Label<String>("ajaxLabel", "BBBBBBB");
				ajaxLabel2.setOutputMarkupId(true);
				ajaxLabel.getParent().replace(ajaxLabel2);
				if (target != null)
				{
					target.addComponent(ajaxLabel2, "ajaxLabel");
				}
			}
		});
	}
}