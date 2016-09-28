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
package org.apache.wicket.markup.html.form;

import static org.hamcrest.CoreMatchers.is;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.onFormValidateModelObjects.AddressFormPanel;
import org.apache.wicket.markup.html.form.onFormValidateModelObjects.OnFormValidateModelObjectsPage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test case for https://issues.apache.org/jira/browse/WICKET-4344
 */
public class OnValidateModelObjectsTest extends WicketTestCase
{
	/**
	 * Asserts that submitting the outer form will call {@link Form#onValidateModelObjects()}
	 * for both the outer form and the inner form(s)
	 */
	@Test
	public void onValidateModelObjects()
	{
		tester.startPage(OnFormValidateModelObjectsPage.class);
		FormTester formTester = tester.newFormTester("parentForm");
		formTester.submit("parentSubmitBtn");

		Page page = tester.getLastRenderedPage();
		OnFormValidateModelObjectsPage.ParentModel parentModel = (OnFormValidateModelObjectsPage.ParentModel) page.getDefaultModelObject();
		assertThat(parentModel.isParentValidated(), is(true));

		AddressFormPanel.ChildModel childPanel = (AddressFormPanel.ChildModel) page.get("parentForm:addressInfo").getDefaultModelObject();
		assertThat(childPanel.isChildValidated(), is(true));
	}
}
