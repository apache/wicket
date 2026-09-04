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
package org.apache.wicket.versioning;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author martin-g
 */
@SuppressWarnings("serial")
public class VersioningTestPage extends WebPage
{

	private static final Logger logger = LoggerFactory.getLogger(VersioningTestPage.class);

	/**
	 * 
	 * Construct.
	 * 
	 * @param pageParameters
	 */
	public VersioningTestPage(PageParameters pageParameters)
	{
		super(pageParameters);

		addNoopLink();

		// a target component which will be manipulated
		final Label label = new Label("label", "Label initial value");
		label.setOutputMarkupId(true);
		add(label);

		ajaxReRenderComponent(label);

		changeComponentModel(label);

		addTemporaryBehavior(label);

		addNonTemporaryBehavior(label);

		changeComponentEnabledState(label);

		changeComponentVisibilityState(label);
	}

	/**
	 * a link that does nothing in its onClick. should not make new version
	 */
	private void addNoopLink()
	{
		final Link<Void> noopLink = new Link<Void>("noopLink")
		{

			@Override
			public void onClick()
			{
				logger.debug("Current page version when clicking the non-ajax link: {}",
					getPage().getPageId());
			}
		};
		add(noopLink);
	}

	/**
	 * ajax link that just re-renders no modified label. should not create new version
	 * 
	 * @param targetComponent
	 */
	private void ajaxReRenderComponent(final Component targetComponent)
	{
		final AjaxLink<Void> ajaxUpdatingLink = new AjaxLink<Void>("ajaxUpdatingLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				target.add(targetComponent);
			}
		};
		add(ajaxUpdatingLink);
	}

	/**
	 * ajax link that changes the model of a component. should create a new version
	 * 
	 * @param targetComponent
	 */
	private void changeComponentModel(final Component targetComponent)
	{
		final AjaxLink<Void> ajaxUpdatingChangeModelLink = new AjaxLink<Void>(
			"ajaxUpdatingChangeModelLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				targetComponent.setDefaultModelObject("label new value");
				target.add(targetComponent);
			}
		};
		add(ajaxUpdatingChangeModelLink);
	}

	/**
	 * a link that adds a temporary behavior to a component. should not create a new version
	 * 
	 * @param targetComponent
	 */
	private void addTemporaryBehavior(final Component targetComponent)
	{
		final Link<Void> addTemporaryBehaviorLink = new Link<Void>("addTemporaryBehaviorLink")
		{
			@Override
			public void onClick()
			{
				targetComponent.add(new Behavior()
				{

					/**
					 * @see org.apache.wicket.behavior.Behavior#isTemporary(Component)
					 */
					@Override
					public boolean isTemporary(Component c)
					{
						return true;
					}

				});
			}
		};
		add(addTemporaryBehaviorLink);
	}

	/**
	 * adds a link that adds a non-temporary behavior to a component. should create a new version
	 * 
	 * @param targetComponent
	 */
	private void addNonTemporaryBehavior(final Component targetComponent)
	{
		final Link<Void> addBehaviorLink = new Link<Void>("addBehaviorLink")
		{
			@Override
			public void onClick()
			{
				targetComponent.add(new Behavior()
				{

					/**
					 * @see org.apache.wicket.behavior.Behavior#isTemporary(Component)
					 */
					@Override
					public boolean isTemporary(Component c)
					{
						return false;
					}

				});
			}
		};
		add(addBehaviorLink);
	}


	/**
	 * adds a link that changes the 'enabled' state of a component. should create a new version
	 * 
	 * @param targetComponent
	 */
	private void changeComponentVisibilityState(final Component targetComponent)
	{
		final Link<Void> link = new Link<Void>("changeVisibilityStateLink")
		{
			@Override
			public void onClick()
			{
				targetComponent.setVisible(!targetComponent.isVisible());
			}
		};
		add(link);
	}

	/**
	 * adds a link that changes the 'enabled' state of a component. should create a new version
	 * 
	 * @param targetComponent
	 */
	private void changeComponentEnabledState(final Component targetComponent)
	{
		final Link<Void> link = new Link<Void>("changeEnabledStateLink")
		{
			@Override
			public void onClick()
			{
				targetComponent.setEnabled(!targetComponent.isEnabled());
			}
		};
		add(link);
	}
}
