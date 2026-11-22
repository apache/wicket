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
package org.apache.wicket.islands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.islands.FormIslandTestPage.Point;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A {@link FormIsland} taking part in a form.
 */
class FormIslandTest extends IslandTestCase
{
	private static final Pattern STATE = Pattern.compile(
		"<script type=\"application/json\" data-wicket-island-state>(.*?)</script>", Pattern.DOTALL);

	private FormIslandTestPage page;

	@BeforeEach
	void render()
	{
		FormIslandTestPage.updatesOnAnyInstance = 0;
		page = tester.startPage(FormIslandTestPage.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void rendersTheValueAndTheFormState()
	{
		TagTester host = tester.getTagByWicketId("point");
		assertEquals("test-island", host.getAttribute(Island.ATTR_TYPE));
		assertNull(host.getAttribute("name"));

		assertEquals("{\"x\":1,\"y\":2}", valueInput().getAttribute("value"));
		assertEquals(page.island.getInputName(), valueInput().getAttribute("name"));

		Map<String, Object> state = state();
		assertEquals(Map.of("max", 9), state.get("props"));
		Map<String, Object> form = (Map<String, Object>)state.get("form");
		assertEquals(Boolean.TRUE, form.get("required"));
		assertEquals(Boolean.TRUE, form.get("enabled"));
		assertEquals("Point", form.get("label"));
		assertEquals(List.of(), form.get("errors"));
	}

	@Test
	void submittedValueReachesTheModel()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "{\"x\":3,\"y\":4}");
		form.submit();

		assertEquals(new Point(3, 4), page.value.getObject());
	}

	@Test
	void submittedValueIsSanitized()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "{\"x\":30,\"y\":4}");
		form.submit();

		assertEquals(new Point(9, 4), page.value.getObject());
	}

	@Test
	void malformedValueIsAConversionErrorAndKeptForTheUser()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "{\"x\":3,\"admin\":true}");
		form.submit();

		assertEquals(new Point(1, 2), page.value.getObject());
		assertEquals(List.of("The value of 'Point' is not valid."), errors());
		assertEquals("{\"x\":3,\"admin\":true}", valueInput().getAttribute("value"));
	}

	@Test
	void requiredValueIsChecked()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "");
		form.submit();

		assertEquals(1, errors().size());
		assertTrue(errors().get(0).contains("Point"), errors().toString());
	}

	@Test
	void validatorMessagesReachThePeer()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "{\"x\":5,\"y\":5}");
		form.submit();

		assertEquals(List.of("Not on the diagonal"), errors());
		tester.assertErrorMessages("Not on the diagonal");
	}

	@Test
	void ajaxSubmitUpdatesTheIsland()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "{\"x\":5,\"y\":5}");
		form.submit("submit");
		tester.executeAjaxEvent("form:submit", "click");

		assertEquals(List.of("Not on the diagonal"), sentErrors());
	}

	@Test
	void ajaxValidationSendsTheFormStateWithoutReRenderingTheIsland()
	{
		tester.getRequest().getPostParameters().setParameterValue(page.island.getInputName(),
			"{\"x\":4,\"y\":4}");
		tester.executeBehavior(behavior());

		assertFalse(tester.getLastResponseAsString().contains("data-wicket-island-state"),
			tester.getLastResponseAsString());
		assertEquals(List.of("Not on the diagonal"), sentErrors());
	}

	@Test
	void reRenderedIslandGetsNoSeparateFormState()
	{
		page.addIslandOnUpdate = true;
		tester.getRequest().getPostParameters().setParameterValue(page.island.getInputName(),
			"{\"x\":7,\"y\":8}");
		tester.executeBehavior(behavior());

		tester.assertComponentOnAjaxResponse(page.island);
		assertFalse(tester.getLastResponseAsString().contains("<data "),
			tester.getLastResponseAsString());
	}

	@Test
	void noFormStateIsSentWhenUpdateOnValidationIsOff()
	{
		page.island.setUpdateOnValidation(false);
		tester.getRequest().getPostParameters().setParameterValue(page.island.getInputName(),
			"{\"x\":4,\"y\":4}");
		tester.executeBehavior(behavior());

		assertFalse(tester.getLastResponseAsString().contains("<data "),
			tester.getLastResponseAsString());
	}

	@Test
	void ajaxBehaviorIsNotInvokedOnAPageRecreatedAfterExpiry()
	{
		Url url = Url.parse(behavior().getCallbackUrl());
		tester.getSession().getPageManager().clear();

		tester.getRequest().setMethod("POST");
		tester.getRequest().getPostParameters().setParameterValue(page.island.getInputName(),
			"{\"x\":7,\"y\":8}");
		tester.executeAjaxUrl(url);

		assertEquals(0, FormIslandTestPage.updatesOnAnyInstance);
	}

	@Test
	void nullValueIsNotSanitizedAndFailsRequired()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("point", "null");
		form.submit();

		assertEquals(new Point(1, 2), page.value.getObject());
		assertEquals(1, errors().size());
	}

	@Test
	void ajaxBehaviorsSendTheHiddenInput()
	{
		AjaxFormComponentUpdatingBehavior behavior = behavior();
		String script = behavior.getCallbackScript().toString();
		assertTrue(script.contains("\"sr\":true"), script);

		tester.getRequest().getPostParameters().setParameterValue(page.island.getInputName(),
			"{\"x\":7,\"y\":8}");
		tester.executeBehavior(behavior);

		assertEquals(1, page.updates);
		assertEquals(new Point(7, 8), page.value.getObject());
	}

	@Test
	@SuppressWarnings("unchecked")
	void disabledIslandTellsThePeer()
	{
		page.island.setEnabled(false);
		tester.startPage(page);

		assertNull(tester.getTagByWicketId("point").getAttribute("disabled"));
		assertEquals(Boolean.FALSE, ((Map<String, Object>)state().get("form")).get("enabled"));
	}

	private AjaxFormComponentUpdatingBehavior behavior()
	{
		return page.island.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0);
	}

	@SuppressWarnings("unchecked")
	private List<String> sentErrors()
	{
		Map<String, Object> form = (Map<String, Object>)answer().get("form");
		assertEquals(page.island.getKey(), form.get("key"));
		return (List<String>)((Map<String, Object>)form.get("state")).get("errors");
	}

	private TagTester valueInput()
	{
		TagTester input = TagTester.createTagByName(tester.getLastResponseAsString(), "input");
		assertTrue(input.hasAttribute(FormIsland.ATTR_VALUE));
		return input;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> state()
	{
		Matcher matcher = STATE.matcher(tester.getLastResponseAsString());
		assertTrue(matcher.find(), tester.getLastResponseAsString());
		return JSON.readValue(matcher.group(1), Map.class);
	}

	@SuppressWarnings("unchecked")
	private List<String> errors()
	{
		return (List<String>)((Map<String, Object>)state().get("form")).get("errors");
	}
}
