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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AbstractAutoCompleteRendererTest extends WicketTestCase
{
	@Test
	void escapeOnselectJSExpression()
	{
		var renderer = new Renderer("alert('hello');", null);
		MockWebResponse response = new MockWebResponse();
		renderer.render("foo", response, null);
		assertThat(response.getTextResponse()).contains(
			"<li textvalue=\"foo\" onselect=\"alert(&#039;hello&#039;);\">foo</li>");
	}

	@Test
	void choiceDataIsWrittenAsEscapedDataAttributes()
	{
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("city", "\"' onmouseover='alert(1)");
		data.put("zip-code", 1234);
		data.put("street", null);
		var renderer = new Renderer(null, data);
		MockWebResponse response = new MockWebResponse();
		renderer.render("foo", response, null);
		assertThat(response.getTextResponse()).hasToString(
			"<li textvalue=\"foo\" data-city=\"&quot;&#039; onmouseover=&#039;alert(1)\" " +
				"data-zip-code=\"1234\">foo</li>");
	}

	@Test
	void choiceWithoutDataHasNoExtraAttributes()
	{
		var renderer = new Renderer(null, null);
		MockWebResponse response = new MockWebResponse();
		renderer.render("foo", response, null);
		assertThat(response.getTextResponse()).hasToString("<li textvalue=\"foo\">foo</li>");
	}

	@Test
	void choiceDataNameThatIsNotAPlainDataNameIsRejected()
	{
		var renderer = new Renderer(null, Map.of("x onclick", "alert(1)"));
		MockWebResponse response = new MockWebResponse();
		assertThatThrownBy(() -> renderer.render("foo", response, null))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(response.getTextResponse().toString()).doesNotContain("onclick");
	}

	static class Renderer extends AbstractAutoCompleteRenderer<String>
	{

		final String expression;

		final Map<String, ?> data;

		Renderer(String expression, Map<String, ?> data)
		{
			this.expression = expression;
			this.data = data;
		}

		@Override
		@SuppressWarnings("removal")
		protected CharSequence getOnSelectJavaScriptExpression(String item)
		{
			return expression;
		}

		@Override
		protected Map<String, ?> getChoiceData(String item)
		{
			return data;
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
