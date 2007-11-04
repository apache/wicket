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

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;

/**
 * Tests for {@link AjaxEditableLabel}
 * 
 * @author Gerolf Seitz
 */
public class AjaxEditableTest extends WicketTestCase
{

	protected void setUp() throws Exception
	{
		super.setUp();
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		tester.startPage(AjaxEditableLabelPage.class);
		tester.assertRenderedPage(AjaxEditableLabelPage.class);
	}


	/**
	 * Tests default AjaxEditableLabel behavior
	 */
	public void testAjaxEditableLabel()
	{
		Page page = tester.getLastRenderedPage();
		AjaxEditableLabel ajaxLabel = (AjaxEditableLabel)page.get("ajaxLabel");

		AbstractAjaxBehavior labelBehavior = (AbstractAjaxBehavior)ajaxLabel.get("label")
			.getBehaviors()
			.get(0);
		AbstractAjaxBehavior editorBehavior = (AbstractAjaxBehavior)ajaxLabel.get("editor")
			.getBehaviors()
			.get(0);

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
	public void testDisabledAjaxEditableLabel()
	{
		Page page = tester.getLastRenderedPage();
		AjaxEditableLabel ajaxLabel = (AjaxEditableLabel)page.get("ajaxLabel");
		AjaxLink toggle = (AjaxLink)page.get("toggle");

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
		markup = tester.getTagById(ajaxLabel.getMarkupId()).getMarkup();
		assertTrue(markup.matches(".*onclick=\"var wcall=wicketAjaxGet.*"));
	}
}