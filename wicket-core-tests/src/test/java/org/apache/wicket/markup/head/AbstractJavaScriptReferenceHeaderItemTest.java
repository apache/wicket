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
package org.apache.wicket.markup.head;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.value.AttributeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AbstractJavaScriptReferenceHeaderItemTest {

	@Test
	void typeDefault() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
				.createAttributeMap("https://wicket.apache.org/");
		assertEquals("text/javascript", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@Test
	void typeNull() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
		  .setType(null)
		  .createAttributeMap("https://wicket.apache.org/");
		assertNull(attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@Test
	void typeTextJavascript() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
				.setType(JavaScriptReferenceType.TEXT_JAVASCRIPT)
				.createAttributeMap("https://wicket.apache.org/");
		assertEquals("text/javascript", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@Test
	void typeModule() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
				.setType(JavaScriptReferenceType.MODULE)
				.createAttributeMap("https://wicket.apache.org/");
		assertEquals("module", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@Test
	void typeCustom() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
				.setType(new JavaScriptReferenceType("custom-type"))
				.createAttributeMap("https://wicket.apache.org/");
		assertEquals("custom-type", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@Test
	public void contentTypeDefault()
	{
		final TestJavascriptReferenceHeaderItem headerItem = new TestJavascriptReferenceHeaderItem();
		final AttributeMap attributeMap = headerItem.createAttributeMap("https://wicket.apache.org/");
		assertEquals("text/javascript", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	@ParameterizedTest
	@MethodSource("javascriptContentTypes")
	public void contentType(final JavaScriptContentType contentType, final String expectedTypeValue)
	{
		final TestJavascriptReferenceHeaderItem headerItem = new TestJavascriptReferenceHeaderItem();
		headerItem.setContentType(contentType);
		final AttributeMap attributeMap = headerItem.createAttributeMap("https://wicket.apache.org/");
		assertEquals(expectedTypeValue, attributeMap.get(JavaScriptUtils.ATTR_TYPE));
	}

	static Stream<Arguments> javascriptContentTypes() {
		return Stream.of(
		  Arguments.of(null, null),
		  Arguments.of(JavaScriptContentType.TEXT_JAVASCRIPT, "text/javascript"),
		  Arguments.of(JavaScriptContentType.MODULE, "module"),
		  Arguments.of(JavaScriptContentType.IMPORT_MAP, "importmap"),
		  Arguments.of(JavaScriptContentType.SPECULATION_RULES, "speculationrules"),
		  Arguments.of((JavaScriptContentType) () -> "custom-type", "custom-type")
		);
	}

	static class TestJavascriptReferenceHeaderItem extends AbstractJavaScriptReferenceHeaderItem {
		@Override
		public Iterable<?> getRenderTokens() {
			return null;
		}

		@Override
		public void render(Response response) {
		}
	}
}
