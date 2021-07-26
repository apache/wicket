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
package org.apache.wicket.core.request.handler.logger;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPageAndComponentProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.request.component.IRequestableComponent;

/**
 * Contains logging data for component/listener-interface request handlers.
 *
 * @author Emond Papegaaij
 */
public class ListenerLogData extends PageLogData
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends IRequestableComponent> componentClass;
	private final String componentPath;
	private final Integer behaviorIndex;
	private final Class<? extends Behavior> behaviorClass;
	private final Class<? extends IRequestableComponent> submittingComponentClass;
	private final String submittingComponentPath;

	/**
	 * Construct.
	 *
	 * @param pageAndComponentProvider
	 * @param behaviorIndex
	 */
	public ListenerLogData(IPageAndComponentProvider pageAndComponentProvider, Integer behaviorIndex)
	{
		super(pageAndComponentProvider);

		this.behaviorIndex = behaviorIndex;

		componentClass = optional(() -> pageAndComponentProvider.getComponent().getClass());
		componentPath = optional(() -> pageAndComponentProvider.getComponentPath());

		if (behaviorIndex != null)
		{
			behaviorClass = optional(() -> pageAndComponentProvider.getComponent()
					.getBehaviorById(behaviorIndex)
					.getClass());
		}
		else
		{
			behaviorClass = null;
		}
		
		final Component formSubmitter = optional(() -> {
			final IRequestableComponent component = pageAndComponentProvider.getComponent();
			if (component instanceof Form)
			{
				final IFormSubmitter submitter = ((Form<?>)component).findSubmittingButton();
				return submitter instanceof Component ? (Component)submitter : null;
			}
			return null;
		});
		if (formSubmitter != null)
		{
			submittingComponentClass = formSubmitter.getClass();
			submittingComponentPath = formSubmitter.getPageRelativePath();
		} else {
			submittingComponentClass = null;
			submittingComponentPath = null;
		}
	}

	/**
	 * @return componentClass
	 */
	public final Class<? extends IRequestableComponent> getComponentClass()
	{
		return componentClass;
	}

	/**
	 * @return componentPath
	 */
	public final String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return behaviorIndex
	 */
	public final Integer getBehaviorIndex()
	{
		return behaviorIndex;
	}

	/**
	 * @return behaviorClass
	 */
	public final Class<? extends Behavior> getBehaviorClass()
	{
		return behaviorClass;
	}

	/**
	 * @return submittingComponentClass
	 */
	public Class<? extends IRequestableComponent> getSubmittingComponentClass()
	{
		return submittingComponentClass;
	}

	/**
	 * @return submittingComponentPath
	 */
	public String getSubmittingComponentPath()
	{
		return submittingComponentPath;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.setCharAt(sb.length() - 1, ',');
		if (getComponentClass() != null)
		{
			sb.append("componentClass=");
			sb.append(getComponentClass().getName());
			sb.append(',');
		}
		if (getComponentPath() != null)
		{
			sb.append("componentPath=");
			sb.append(getComponentPath());
			sb.append(',');
		}
		sb.append("behaviorIndex=");
		sb.append(getBehaviorIndex());
		if (getBehaviorClass() != null)
		{
			sb.append(",behaviorClass=");
			sb.append(getBehaviorClass().getName());
		}
		if (getSubmittingComponentClass() != null)
		{
			sb.append(",submittingComponentClass=");
			sb.append(getSubmittingComponentClass().getName());
		}
		if (getSubmittingComponentPath() != null)
		{
			sb.append(",submittingComponentPath=");
			sb.append(getSubmittingComponentPath());
		}
		sb.append("}");
		return sb.toString();
	}
}
