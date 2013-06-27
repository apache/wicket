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
package org.apache.wicket.settings.def;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.settings.ICssSettings;

/**
 *
 */
public class CssSettings implements ICssSettings
{
	private String sortOrderDownCssClass = "wicket_orderDown";

	private String sortOrderUpCssClass = "wicket_orderUp";

	private String sortOrderNoneCssClass = "wicket_orderNone";

	private String requiredCssClass = "required";

	private String invalidCssClass = "error";

	private String disabledCssClass = "disabled";

	private String oddItemCssClass = "odd";

	private String evenItemCssClass = "even";

	private String tabbedPanelSelectedCssClass = "selected";

	private String tabbedPanelLastCssClass = "last";

	private String tabbedPanelTabContainerCssClass = "tab-row";

	@Override
	public String getSortOrderDownCssClass()
	{
		return sortOrderDownCssClass;
	}

	@Override
	public void setSortOrderDownCssClass(String cssClassName)
	{
		this.sortOrderDownCssClass = cssClassName;
	}

	@Override
	public String getSortOrderUpCssClass()
	{
		return sortOrderUpCssClass;
	}

	@Override
	public void setSortOrderUpCssClass(String cssClassName)
	{
		this.sortOrderUpCssClass = cssClassName;
	}

	@Override
	public String getSortOrderNoneCssClass()
	{
		return sortOrderNoneCssClass;
	}

	@Override
	public void setSortOrderNoneCssClass(String cssClassName)
	{
		this.sortOrderNoneCssClass = cssClassName;
	}

	@Override
	public String getFeedbackMessageCssClass(FeedbackMessage message)
	{
		return "feedbackPanel" + message.getLevelAsString();
	}

	@Override
	public void setRequiredCssClass(String cssClassName)
	{
		this.requiredCssClass = cssClassName;
	}

	@Override
	public String getRequiredCssClass()
	{
		return requiredCssClass;
	}

	@Override
	public void setInvalidCssClass(String cssClassName)
	{
		this.invalidCssClass = cssClassName;
	}

	@Override
	public String getInvalidCssClass()
	{
		return invalidCssClass;
	}

	@Override
	public void setDisabledCssClass(String cssClassName)
	{
		this.disabledCssClass = cssClassName;
	}

	@Override
	public String getDisabledCssClass()
	{
		return disabledCssClass;
	}

	@Override
	public void setOddItemCssClass(String cssClassName)
	{
		this.oddItemCssClass = cssClassName;
	}

	@Override
	public String getOddItemCssClass()
	{
		return oddItemCssClass;
	}

	@Override
	public void setEvenItemCssClass(String cssClassName)
	{
		this.evenItemCssClass = cssClassName;
	}

	@Override
	public String getEvenItemCssClass()
	{
		return evenItemCssClass;
	}

	@Override
	public void setTabbedPanelSelectedCssClass(String cssClassName)
	{
		this.tabbedPanelSelectedCssClass = cssClassName;
	}

	@Override
	public String getTabbedPanelSelectedCssClass()
	{
		return tabbedPanelSelectedCssClass;
	}

	@Override
	public void setTabbedPanelLastCssClass(String cssClassName)
	{
		this.tabbedPanelLastCssClass = cssClassName;
	}

	@Override
	public String getTabbedPanelLastCssClass()
	{
		return tabbedPanelLastCssClass;
	}

	@Override
	public void setTabbedPanelTabContainerCssClass(String cssClassName)
	{
		this.tabbedPanelTabContainerCssClass = cssClassName;
	}

	@Override
	public String getTabbedPanelTabContainerCssClass()
	{
		return tabbedPanelTabContainerCssClass;
	}
}
