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
package org.apache.wicket.extensions.ajax.markup.html.modal;

import static org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog.CONTENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ModalDialogTest extends WicketTestCase
{

	@Test
	void smokeTest()
	{
		// Arrange
		var cut = new ModalDialog("id");

		// Act
		tester.startComponentInPage(cut);

		//Assert
		tester.assertInvisible("id:overlay");
	}

	@Test
	void open_showModal()
	{
		// Arrange
		var cut = new ModalDialog("id");
		tester.startComponentInPage(cut);

		// Act
		final var ajaxRequestHandler = new AjaxRequestHandler(tester.getLastRenderedPage());
		cut.open(new WebMarkupContainer(CONTENT_ID), ajaxRequestHandler);
		tester.processRequest(ajaxRequestHandler);

		//Assert
		tester.assertVisible("id:overlay");
		assertThat(
			tester.getFirstComponentByWicketId(CONTENT_ID).get().isVisibleInHierarchy()).isTrue();
	}
}