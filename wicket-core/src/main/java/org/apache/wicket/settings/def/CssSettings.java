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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.AutoLabelResolver;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.settings.ICssSettings;

/**
 *
 */
public class CssSettings implements ICssSettings
{
	private final Map<String, String> cssClassNames;

	public CssSettings()
	{
		this.cssClassNames = new HashMap<>();

		setCssClass(FormComponentLabel.DISABLED_CSS_CLASS_KEY, "disabled");
		setCssClass(FormComponentLabel.INVALID_CSS_CLASS_KEY, "error");
		setCssClass(FormComponentLabel.REQUIRED_CSS_CLASS_KEY, "required");

		setCssClass(AutoLabelResolver.DISABLED_CSS_CLASS_KEY, "disabled");
		setCssClass(AutoLabelResolver.INVALID_CSS_CLASS_KEY, "error");
		setCssClass(AutoLabelResolver.REQUIRED_CSS_CLASS_KEY, "required");

		setCssClass(OddEvenItem.ODD_CSS_CLASS_KEY, "odd");
		setCssClass(OddEvenItem.EVEN_CSS_CLASS_KEY, "even");

		setCssClass(OddEvenListItem.ODD_CSS_CLASS_KEY, "odd");
		setCssClass(OddEvenListItem.EVEN_CSS_CLASS_KEY, "even");

		setCssClass(FeedbackMessage.UNDEFINED_CSS_CLASS_KEY, "feedbackPanelUNDEFINED");
		setCssClass(FeedbackMessage.DEBUG_CSS_CLASS_KEY, "feedbackPanelDEBUG");
		setCssClass(FeedbackMessage.INFO_CSS_CLASS_KEY, "feedbackPanelINFO");
		setCssClass(FeedbackMessage.SUCCESS_CSS_CLASS_KEY, "feedbackPanelSUCCESS");
		setCssClass(FeedbackMessage.WARNING_CSS_CLASS_KEY, "feedbackPanelWARNING");
		setCssClass(FeedbackMessage.ERROR_CSS_CLASS_KEY, "feedbackPanelERROR");
		setCssClass(FeedbackMessage.FATAL_CSS_CLASS_KEY, "feedbackPanelFATAL");
	}

	@Override
	public String getCssClass(String key)
	{
		return cssClassNames.get(key);
	}

	@Override
	public void setCssClass(String key, String cssClassName)
	{
		cssClassNames.put(key, cssClassName);
	}
}
