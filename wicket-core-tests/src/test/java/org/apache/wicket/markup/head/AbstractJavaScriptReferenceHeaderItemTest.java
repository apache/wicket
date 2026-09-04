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

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.value.AttributeMap;
import org.junit.jupiter.api.Test;

class AbstractJavaScriptReferenceHeaderItemTest {

	@Test
	void typeDefault() {
		final AttributeMap attributeMap = new TestJavascriptReferenceHeaderItem()
				.createAttributeMap("https://wicket.apache.org/");
		assertEquals("text/javascript", attributeMap.get(JavaScriptUtils.ATTR_TYPE));
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
