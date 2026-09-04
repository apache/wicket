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
package org.apache.wicket.ajax;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

/**
 * Simple page with three {@link Label}s which will be updated via
 * {@link AjaxRequestTarget#add(org.apache.wicket.Component...)} in one pass
 * 
 * WICKET-2543
 * 
 * @author Martin Grigorov
 */
public class VarargsAddComponentPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	static final String INITIAL_CONTENT = "Initial content [%d] ";

	static final String AJAX_APPENDED_SUFFIX = " Ajax updated";

	static final int NUMBER_OF_LABELS = 3;

	/**
	 * Construct.
	 */
	public VarargsAddComponentPage()
	{

		for (int i = 0; i < NUMBER_OF_LABELS; i++)
		{
			final String markupId = "label" + i;
			final String modelObject = String.format(INITIAL_CONTENT, i);
			final Label label = new Label(markupId, new Model<String>(modelObject));
			label.setOutputMarkupId(true);

			add(label);
		}

		final AjaxLink<Void> link = new AjaxLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				final Label label0 = (Label)getPage().get("label0");
				label0.setDefaultModelObject(label0.getDefaultModelObjectAsString() +
					AJAX_APPENDED_SUFFIX);
				final Label label1 = (Label)getPage().get("label1");
				label1.setDefaultModelObject(label1.getDefaultModelObjectAsString() +
					AJAX_APPENDED_SUFFIX);
				final Label label2 = (Label)getPage().get("label2");
				label2.setDefaultModelObject(label2.getDefaultModelObjectAsString() +
					AJAX_APPENDED_SUFFIX);

				target.add(label0, label2, label1);
			}
		};
		add(link);
	}
}
