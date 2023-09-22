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
package org.apache.wicket.ajax.markup.html;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for checking whether or not a component is attached to the component hierarchy when
 * updating through AJAX.
 */
class ComponentNotOnPageTest extends WicketTestCase
{
	/**
	 * Wicket should <b>not</b> trigger an exception signaling the
	 * error on the developers part that a component that is not part of the page is being refreshed
	 * in the AJAX response, resulting in a no-op (which is not the intended result). Instead Wicket
	 * should signal the error in the log, but not prevent the user of the application to continue
	 * (which happened in Wicket 7).
	 */
	@Test
	public void componentNotInPageShouldNotFail()
	{
		ComponentNotOnPage page = tester.startPage(new ComponentNotOnPage(new Label("label")));
		tester.startPage(page);

		// this should not fail
		tester.clickLink("refresher", true);
	}
	
	/**
	 * Wicket should trigger an exception signaling the
	 * error on the developers part that a component that is part of another page is being refreshed
	 * in the AJAX response.
	 */
	@Test
	public void componentOnOtherPageShouldFail()
	{
		Label notOnPage = new Label("label");

		ComponentNotOnPage otherPage = new ComponentNotOnPage(notOnPage);
		otherPage.add(notOnPage);
		
		ComponentNotOnPage page = tester.startPage(new ComponentNotOnPage(notOnPage));
		tester.startPage(page);

                assertThrows(IllegalArgumentException.class, () -> {
                        tester.clickLink("refresher", true);
                });
	}
}

