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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractAutoCompleteRendererTest extends WicketTestCase
{
	@Test
	void escapeOnselectJSExpression()
	{
		var renderer = new Renderer("alert('hello');");
		MockWebResponse response = new MockWebResponse();
		renderer.render("foo", response, null);
		assertThat(response.getTextResponse()).contains(
			"<li textvalue=\"foo\" onselect=\"alert(&#039;hello&#039;);\">foo</li>");
	}

	static class Renderer extends AbstractAutoCompleteRenderer<String>
	{

		final String expression;

		Renderer(String expression)
		{
			this.expression = expression;
		}

		@Override
		protected CharSequence getOnSelectJavaScriptExpression(String item)
		{
			return expression;
		}

		@Override
		protected void renderChoice(String object, Response response, String criteria)
		{
			response.write(object);
		}

		@Override
		protected String getTextValue(String object)
		{
			return object;
		}
	}
}
