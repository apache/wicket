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
package org.apache.wicket.jmx;

import org.apache.wicket.feedback.FeedbackMessage;

/**
 * Exposes Application CSS related functionality for JMX.
 */
public class CssSettings implements CssSettingsMBean
{
	private final org.apache.wicket.Application application;

	public CssSettings(org.apache.wicket.Application application)
	{
		this.application = application;
	}

	@Override
	public String getSortOrderDownCssClass()
	{
		return application.getCssSettings().getSortOrderDownCssClass();
	}

	@Override
	public void setSortOrderDownCssClass(String cssClassName)
	{
		application.getCssSettings().setSortOrderDownCssClass(cssClassName);
	}

	@Override
	public String getSortOrderUpCssClass()
	{
		return application.getCssSettings().getSortOrderUpCssClass();
	}

	@Override
	public void setSortOrderUpCssClass(String cssClassName)
	{
		application.getCssSettings().setSortOrderUpCssClass(cssClassName);
	}

	@Override
	public String getSortOrderNoneCssClass()
	{
		return application.getCssSettings().getSortOrderNoneCssClass();
	}

	@Override
	public void setSortOrderNoneCssClass(String cssClassName)
	{
		application.getCssSettings().setSortOrderNoneCssClass(cssClassName);
	}

	@Override
	public String getFeedbackMessageCssClass(FeedbackMessage message)
	{
		return application.getCssSettings().getFeedbackMessageCssClass(message);
	}

	@Override
	public void setRequiredCssClass(String cssClassName)
	{
		application.getCssSettings().setRequiredCssClass(cssClassName);
	}

	@Override
	public String getRequiredCssClass()
	{
		return application.getCssSettings().getRequiredCssClass();
	}

	@Override
	public void setInvalidCssClass(String cssClassName)
	{
		application.getCssSettings().setInvalidCssClass(cssClassName);
	}

	@Override
	public String getInvalidCssClass()
	{
		return application.getCssSettings().getInvalidCssClass();
	}

	@Override
	public void setDisabledCssClass(String cssClassName)
	{
		application.getCssSettings().setDisabledCssClass(cssClassName);
	}

	@Override
	public String getDisabledCssClass()
	{
		return application.getCssSettings().getDisabledCssClass();
	}

	@Override
	public void setOddItemCssClass(String cssClassName)
	{
		application.getCssSettings().setOddItemCssClass(cssClassName);
	}

	@Override
	public String getOddItemCssClass()
	{
		return application.getCssSettings().getOddItemCssClass();
	}

	@Override
	public void setEvenItemCssClass(String cssClassName)
	{
		application.getCssSettings().setEvenItemCssClass(cssClassName);
	}

	@Override
	public String getEvenItemCssClass()
	{
		return application.getCssSettings().getEvenItemCssClass();
	}

	@Override
	public void setTabbedPanelSelectedCssClass(String cssClassName)
	{
		application.getCssSettings().setTabbedPanelSelectedCssClass(cssClassName);
	}

	@Override
	public String getTabbedPanelSelectedCssClass()
	{
		return application.getCssSettings().getTabbedPanelSelectedCssClass();
	}

	@Override
	public void setTabbedPanelLastCssClass(String cssClassName)
	{
		application.getCssSettings().setTabbedPanelLastCssClass(cssClassName);
	}

	@Override
	public String getTabbedPanelLastCssClass()
	{
		return application.getCssSettings().getTabbedPanelLastCssClass();
	}

	@Override
	public void setTabbedPanelTabContainerCssClass(String cssClassName)
	{
		application.getCssSettings().setTabbedPanelTabContainerCssClass(cssClassName);
	}

	@Override
	public String getTabbedPanelTabContainerCssClass()
	{
		return application.getCssSettings().getTabbedPanelTabContainerCssClass();
	}
}
