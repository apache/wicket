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
package org.apache.wicket.markup.html.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that {@link PopupSettings} writes the configured target into the generated JavaScript as
 * given, for both of the target forms its javadoc documents.
 */
class PopupSettingsTest
{
	@Test
	void writesAQuotedUrlTargetAsGiven()
	{
		PopupSettings settings = new PopupSettings();
		settings.setTarget("'some/url'");

		assertThat(settings.getPopupJavaScript()).contains("window.open('some/url', ");
	}

	@Test
	void writesAJavaScriptExpressionTargetAsGiven()
	{
		PopupSettings settings = new PopupSettings();
		settings.setTarget("this.dataset['popup-url']");

		assertThat(settings.getPopupJavaScript())
			.contains("window.open(this.dataset['popup-url'], ");
	}
}
