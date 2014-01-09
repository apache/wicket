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

import org.junit.Assert;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;


/**
 */
public class TraditionalEnclosureAjaxPage extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;
	private final WebMarkupContainer traditionalAjaxVisibilityToggleRequiresPlaceholder;
	private final String toggleableChildId;
	private final WebMarkupContainer toggleable;
	private final AjaxLink<Void> link;

	/** 
	 * 
	 */
	@SuppressWarnings("serial")
	public TraditionalEnclosureAjaxPage()
	{
		add((traditionalAjaxVisibilityToggleRequiresPlaceholder = new WebMarkupContainer(
			"toggleable-placeholder")
		{
			@Override
			public boolean isVisible()
			{
				return get(toggleableChildId).isVisible();
			}
		}).setOutputMarkupPlaceholderTag(true));
		Assert.assertEquals("Ajax requires tag to remain in DOM even if not visible", true,
			traditionalAjaxVisibilityToggleRequiresPlaceholder.getOutputMarkupPlaceholderTag());
		{
			traditionalAjaxVisibilityToggleRequiresPlaceholder.add((toggleable = new WebMarkupContainer(
				toggleableChildId = "toggleable")).setOutputMarkupPlaceholderTag(true));
			add(link = new AjaxLink<Void>("link")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					toggleable.setVisible(!toggleable.isVisible());
					ajaxRepaintOnlyToggleableComponentsContainer(target);
				}
			});
		}
	}

	/**
	 * @param target
	 */
	private void ajaxRepaintOnlyToggleableComponentsContainer(AjaxRequestTarget target)
	{
		target.add(traditionalAjaxVisibilityToggleRequiresPlaceholder); // This is what we
		// repaint and toggleable is inside it
	}

	/**
	 * @return toggleable webmarkupcontainer
	 */
	public WebMarkupContainer getToggleable()
	{
		return toggleable;
	}

	/**
	 * @return toggle link
	 */
	public AjaxLink<Void> getLink()
	{
		return link;
	}

	/**
	 * @return the traditional placeholder
	 */
	public WebMarkupContainer getTraditionalAjaxVisibilityToggleRequiresPlaceholder()
	{
		return traditionalAjaxVisibilityToggleRequiresPlaceholder;
	}
}