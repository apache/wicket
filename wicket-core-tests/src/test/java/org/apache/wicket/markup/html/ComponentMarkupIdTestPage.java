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
package org.apache.wicket.markup.html;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;

/** */
public class ComponentMarkupIdTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	final Label markupLabel = new Label("markupLabel", "Hello, World!");
	final EmptyPanel markupPanel = new EmptyPanel("markupPanel");

	final Label generatedLabel = new Label("generatedLabel", "Hello, World!");
	final EmptyPanel generatedPanel = new EmptyPanel("generatedPanel");

	final String generatedLabelMarkupId;
	final String generatedPanelMarkupId;

	final Label fixedLabel = new Label("fixedLabel", "Hello, World!");
	final EmptyPanel fixedPanel = new EmptyPanel("fixedPanel");

	/** */
	public ComponentMarkupIdTestPage()
	{
		add(markupLabel);
		add(markupPanel);

		generatedLabelMarkupId = generatedLabel.getMarkupId();
		generatedPanelMarkupId = generatedPanel.getMarkupId();

		add(generatedLabel);
		add(generatedPanel);

		fixedLabel.setMarkupId("javaLabel");
		fixedPanel.setMarkupId("javaPanel");

		add(fixedLabel);
		add(fixedPanel);
	}
}
