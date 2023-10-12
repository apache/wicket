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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 * 
 * @author Joonas Hamalainen
 * 
 */
public class AjaxEnclosurePage_1 extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Label label1 = new Label("label1", "Test Label 1");
	private final Label label2 = new Label("label2", "Test Label 2");
	private final AjaxLink<Void> toggleLabel1Link;
	private final AjaxLink<Void> toggleLabel2Link;

	/**
	 * Construct.
	 */
	public AjaxEnclosurePage_1()
	{

		toggleLabel1Link = new AjaxLink<Void>("toggleLabel1Link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				label1.setVisible(!label1.isVisible());
				target.add(label1);
			}

		};
		add(toggleLabel1Link);

		toggleLabel2Link = new AjaxLink<Void>("toggleLabel2Link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				label2.setVisible(!label2.isVisible());
				target.add(label2);
			}

		};
		add(toggleLabel2Link);

		label1.setOutputMarkupId(true);
		label2.setOutputMarkupId(true);
		label2.setOutputMarkupPlaceholderTag(true);
		add(label1);
		add(label2);
	}


	/**
	 * @return serialVersionUID
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	/**
	 * @return label1
	 */
	public Label getLabel1()
	{
		return label1;
	}

	/**
	 * @return label2
	 */
	public Label getLabel2()
	{
		return label2;
	}

	/**
	 * @return toggleLabel1Link
	 */
	public AjaxLink<Void> getToggleLabel1Link()
	{
		return toggleLabel1Link;
	}

	/**
	 * @return toggleLabel2Link
	 */
	public AjaxLink<Void> getToggleLabel2Link()
	{
		return toggleLabel2Link;
	}

}
