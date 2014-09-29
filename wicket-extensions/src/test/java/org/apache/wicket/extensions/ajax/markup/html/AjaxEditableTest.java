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
package org.apache.wicket.extensions.ajax.markup.html;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.Arrays;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IWritableRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AjaxEditableLabel}
 * 
 * @author Gerolf Seitz
 */
public class AjaxEditableTest extends WicketTestCase
{

	/**
	 * 
	 */
	@Before
	public void setUp()
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		tester.startPage(AjaxEditableLabelPage.class);
		tester.assertRenderedPage(AjaxEditableLabelPage.class);
	}


	/**
	 * Tests default AjaxEditableLabel behavior
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testAjaxEditableLabel()
	{
		Page page = tester.getLastRenderedPage();
		AjaxEditableLabel<String> ajaxLabel = (AjaxEditableLabel<String>)page.get("ajaxLabel");

		AbstractAjaxBehavior labelBehavior = (AbstractAjaxBehavior)ajaxLabel.get("label")
			.getBehaviors().get(0);
		AbstractAjaxBehavior editorBehavior = (AbstractAjaxBehavior)ajaxLabel.get("editor")
			.getBehaviors().get(0);

		// "click" on the label and check for valid visibility
		tester.executeBehavior(labelBehavior);
		tester.assertVisible("ajaxLabel:editor");
		tester.assertInvisible("ajaxLabel:label");

		// "leave" the editor and check for valid visibility
		tester.executeBehavior(editorBehavior);
		tester.assertInvisible("ajaxLabel:editor");
		tester.assertVisible("ajaxLabel:label");
	}

	/**
	 * Tests whether disabling/enabling an AjaxEditableLabel also disables/enables the
	 * <code>LabelBehavior</code>
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testDisabledAjaxEditableLabel()
	{
		Page page = tester.getLastRenderedPage();
		AjaxEditableLabel<String> ajaxLabel = (AjaxEditableLabel<String>)page.get("ajaxLabel");
		AjaxLink<Void> toggle = (AjaxLink<Void>)page.get("toggle");

		AbstractAjaxBehavior toggleBehavior = (AbstractAjaxBehavior)toggle.getBehaviors().get(0);

		// check for correct rendering
		tester.assertInvisible("ajaxLabel:editor");
		tester.assertVisible("ajaxLabel:label");

		// disable ajaxLabel
		tester.executeBehavior(toggleBehavior);

		// check for the *absence* of the ajax onclick call
		String markup = tester.getTagById(ajaxLabel.getMarkupId()).getMarkup();
		assertFalse(markup.matches(".*onclick=\"var wcall=wicketAjaxGet.*"));

		// enable ajaxLabel
		tester.executeBehavior(toggleBehavior);

		// check for the *presence* of the ajax onclick call

		// TODO Wicket.next - re-enable
		// markup = tester.getTagById(ajaxLabel.getMarkupId()).getMarkup();
		// assertTrue(markup.matches(".*onclick=\"var wcall=Wicket.Ajax.get.*"));
	}

	/**
	 * A test that changes the value of the {@link AjaxEditableLabel}
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testUpdateValue()
	{
		Page page = tester.getLastRenderedPage();
		AjaxEditableLabel<String> ajaxLabel = (AjaxEditableLabel<String>)page.get("ajaxLabel");

		tester.assertInvisible("ajaxLabel:editor");
		tester.assertVisible("ajaxLabel:label");
		// assert the initial value
		tester.assertLabel("ajaxLabel:label", "ajaxTest");

		// click on the label to go to edit mode
		tester.executeAjaxEvent("ajaxLabel:label", "click");

		tester.assertVisible("ajaxLabel:editor");
		tester.assertInvisible("ajaxLabel:label");

		FormComponent<?> editor = (FormComponent<?>)ajaxLabel.get("editor");
		// set some new value and submit it
		tester.getRequest().setParameter(editor.getInputName(), "something");
		tester.getRequest().setParameter("save", "true");
		tester.executeBehavior((AbstractAjaxBehavior)editor.getBehaviorById(0));

		tester.assertInvisible("ajaxLabel:editor");
		tester.assertVisible("ajaxLabel:label");
		tester.assertLabel("ajaxLabel:label", "something");
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4259">WICKET-4259</a>
	 */
	@Test
	public void testModelObjectClassInference()
	{
		class IntegerModel extends Model<Integer> implements IObjectClassAwareModel<Integer>
		{
			@Override
			public Class<Integer> getObjectClass()
			{
				return Integer.class;
			}
		}
		IModel<Integer> integerModel = new IntegerModel();
		AjaxEditableLabel<Integer> editableLabel = new AjaxEditableLabel<Integer>("test",
			integerModel);
		editableLabel.getEditor().setVisible(true);

		IWritableRequestParameters postParameters = (IWritableRequestParameters)tester
			.getRequestCycle().getRequest().getPostParameters();
		postParameters.setParameterValues(editableLabel.getEditor().getInputName(),
			Arrays.asList(new StringValue[] { StringValue.valueOf("5") }));
		editableLabel.getEditor().processInput();

		assertThat(integerModel.getObject(), instanceOf(Integer.class));
	}
}