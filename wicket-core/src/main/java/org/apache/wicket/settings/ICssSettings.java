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
package org.apache.wicket.settings;

import org.apache.wicket.feedback.FeedbackMessage;

/**
 * Provides means to change the default CSS classes and styles used
 * by the components in Wicket distribution.
 */
public interface ICssSettings
{
	/**
	 *
	 * @return
	 */
	String getSortOrderDownCssClass();

	void setSortOrderDownCssClass(String cssClassName);

	String getSortOrderUpCssClass();

	void setSortOrderUpCssClass(String cssClassName);

	String getSortOrderNoneCssClass();

	void setSortOrderNoneCssClass(String cssClassName);

	String getFeedbackMessageCssClass(FeedbackMessage message);

	void setRequiredCssClass(String cssClassName);

	String getRequiredCssClass();

	void setInvalidCssClass(String cssClassName);

	String getInvalidCssClass();

	void setDisabledCssClass(String cssClassName);

	String getDisabledCssClass();

	void setOddItemCssClass(String cssClassName);

	String getOddItemCssClass();

	void setEvenItemCssClass(String cssClassName);

	String getEvenItemCssClass();

	void setTabbedPanelSelectedCssClass(String cssClassName);

	String getTabbedPanelSelectedCssClass();

	void setTabbedPanelLastCssClass(String cssClassName);

	String getTabbedPanelLastCssClass();

	void setTabbedPanelTabContainerCssClass(String cssClassName);

	String getTabbedPanelTabContainerCssClass();
}
