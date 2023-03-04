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
package org.apache.wicket.util.tester;

import org.apache.wicket.Component;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseWicketTesterTest {

	private BaseWicketTester tester;

	@BeforeEach
	public void before() {
		tester = new BaseWicketTester(new MockApplication());
	}

	@Test
	void ggetFirstComponentFromLastRenderedPageByWicketId_whenCallPrematurely_returnEmptyOptional() {
		// Arrange
		var cut = new DemoPanel("id");

		// Act
		var label = tester.getFirstComponentFromLastRenderedPageByWicketId("label");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(label).isEmpty();
		});
	}

	@Test
	void getFirstComponentFromLastRenderedPageByWicketId_whenComponentPresent_returnComponent() {
		// Arrange
		var cut = new DemoPanel("id");
		tester.startComponentInPage(cut);

		// Act
		var label = tester.getFirstComponentFromLastRenderedPageByWicketId("label");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(label).isPresent();
			sa.assertThat(label.get().getPath()).isEqualTo("0:id:label");
		});
	}

	@Test
	void getFirstComponentFromLastRenderedPageByWicketId_whenComponentNotPresent_returnEmptyOptional() {
		// Arrange
		var cut = new DemoPanel("id");
		tester.startComponentInPage(cut);

		// Act
		var label = tester.getFirstComponentFromLastRenderedPageByWicketId("asdf");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(label).isEmpty();
		});
	}

	@Test
	void getAllComponentsFromLastRenderedPageByWicketId_whenCallPrematurely_returnEmptyList() {
		// Arrange
		var cut = new DemoPanel("id");

		// Act
		var components = tester.getAllComponentsFromLastRenderedPageByWicketId("label");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(components).isEmpty();
		});
	}

	@Test
	void getAllComponentsFromLastRenderedPageByWicketId_whenMultipleComponentPresent_returnComponentList() {
		// Arrange
		var cut = new DemoPanel("id");
		tester.startComponentInPage(cut);

		// Act
		var components = tester.getAllComponentsFromLastRenderedPageByWicketId("label");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(components).hasSize(2);
			sa.assertThat(components).extracting(Component::getPath)
					.containsExactly("0:id:label", "0:id:otherPanel:label");
		});
	}

	@Test
	void getAllComponentsFromLastRenderedPageByWicketId_whenMultipleComponentPresent2_returnComponentList() {
		// Arrange
		var cut = new DemoPanel("id");
		tester.startComponentInPage(cut);

		// Act
		var components = tester.getAllComponentsFromLastRenderedPageByWicketId("content");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(components).hasSize(4);
			sa.assertThat(components).extracting(Component::getPath).containsExactly(
					"0:id:repeater:0:content",
					"0:id:repeater:1:content",
					"0:id:repeater:2:content",
					"0:id:repeater:3:content");
		});
	}

	@Test
	void getAllComponentsFromLastRenderedPageByWicketId_whenNoComponentPresent2_returnEmptyList() {
		// Arrange
		var cut = new DemoPanel("id");
		tester.startComponentInPage(cut);

		// Act
		var components = tester.getAllComponentsFromLastRenderedPageByWicketId("asdf");

		// Assert
		SoftAssertions.assertSoftly(sa -> {
			sa.assertThat(components).isEmpty();
		});
	}

}