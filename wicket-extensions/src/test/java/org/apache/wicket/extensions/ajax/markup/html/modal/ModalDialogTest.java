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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
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

	@Test
	void dialogIsAModalDialogForAssistiveTechnology()
	{
		startOpen(new ModalDialog("id"));

		TagTester dialog = dialogTag();
		assertThat(dialog.getAttribute("role")).isEqualTo("dialog");
		assertThat(dialog.getAttribute("aria-modal")).isEqualTo("true");
		assertThat(dialog.getAttribute("id")).isNotEmpty();
		assertThat(dialog.getAttribute("aria-labelledby")).isNull();
		assertThat(dialog.getAttribute("aria-label")).isNull();
	}

	@Test
	void roleSetByTheApplicationIsKept()
	{
		var cut = new ModalDialog("id")
		{
			@Override
			protected WebMarkupContainer newDialog(String dialogId)
			{
				WebMarkupContainer dialog = super.newDialog(dialogId);
				dialog.add(AttributeModifier.replace("role", "alertdialog"));
				return dialog;
			}
		};
		startOpen(cut);

		assertThat(dialogTag().getAttribute("role")).isEqualTo("alertdialog");
		assertThat(dialogTag().getAttribute("aria-modal")).isEqualTo("true");
	}

	@Test
	void labelledByRendersTheMarkupIdOfTheTitle()
	{
		var content = new TitledContent();
		var cut = new ModalDialog("id").labelledBy(content.title);
		cut.setContent(content);
		cut.open(null);
		tester.startComponentInPage(cut);

		String titleId = content.title.getMarkupId();
		assertThat(dialogTag().getAttribute("aria-labelledby")).isEqualTo(titleId);
		assertThat(tester.getTagById(titleId).getValue()).isEqualTo("Delete the order");
	}

	@Test
	void titleOfRemovedContentIsForgotten()
	{
		var cut = new ModalDialog("id");
		tester.startComponentInPage(cut);
		var content = new TitledContent();

		cut.labelledBy(content.title).open(content, null);
		cut.close(null);
		cut.open(new WebMarkupContainer(CONTENT_ID), null);
		tester.startComponentInPage(cut);

		assertThat(dialogTag().getAttribute("aria-labelledby")).isNull();
	}

	@Test
	void titleOutsideOfTheDialogIsNotRendered()
	{
		startOpen(new ModalDialog("id").labelledBy(new Label("title", "Elsewhere")));

		assertThat(dialogTag().getAttribute("aria-labelledby")).isNull();
	}

	@Test
	void labelRendersAnEscapedAriaLabel()
	{
		startOpen(new ModalDialog("id").label(Model.of("Delete \"order\" <1>")));

		assertThat(tester.getLastResponseAsString())
			.contains("aria-label=\"Delete &quot;order&quot; &lt;1&gt;\"");
	}

	@Test
	void openDialogRegistersItselfForFocusAndEscape()
	{
		var cut = new ModalDialog("id");
		startOpen(cut);

		String response = document();
		assertThat(response).contains("modal.ModalDialog/modal-dialog.js");
		assertThat(response).contains(String.format(
			"Wicket.ModalDialog.open({overlay: '%s', dialog: '%s', trapFocus: true, escape: {",
			overlay(cut).getMarkupId(), dialogTag().getAttribute("id")));
	}

	@Test
	void closedDialogRendersNoScript()
	{
		tester.startComponentInPage(new ModalDialog("id"));

		assertThat(document()).doesNotContain("Wicket.ModalDialog");
	}

	@Test
	void escapeClosesTheDialog()
	{
		var cut = new ModalDialog("id");
		startOpen(cut);

		tester.executeBehavior(dialogScript(cut));

		assertThat(cut.isOpen()).isFalse();
		tester.assertComponentOnAjaxResponse(cut);
	}

	@Test
	void closeOnEscapeFalseKeepsTheDialogOpen()
	{
		var cut = new ModalDialog("id").closeOnEscape(false);
		startOpen(cut);

		assertThat(document()).contains("trapFocus: true, escape: null});");

		tester.executeBehavior(dialogScript(cut));
		assertThat(cut.isOpen()).isTrue();
	}

	@Test
	void closeOnEscapeTurnsEscapeBackOn()
	{
		startOpen(new ModalDialog("id").closeOnEscape(false).closeOnEscape());

		assertThat(document()).contains("trapFocus: true, escape: {");
	}

	@Test
	void escapeRequestIsPostProcessed()
	{
		var cut = new ModalDialog("id")
		{
			@Override
			protected void postProcessCloseBehaviorAjaxAttributes(AjaxRequestAttributes attributes)
			{
				attributes.setChannel(new AjaxChannel("dialogs", AjaxChannel.Type.DROP));
			}
		};
		startOpen(cut);

		assertThat(document()).contains("\"ch\":\"dialogs|d\"");
	}

	@Test
	void trapFocusFalseLeavesTabToTheBrowser()
	{
		var cut = new ModalDialog("id").trapFocus().trapFocus(false);
		startOpen(cut);

		assertThat(overlay(cut).getBehaviors(TrapFocusBehavior.class)).isEmpty();
		assertThat(document()).contains("trapFocus: false, escape: {")
			.doesNotContain("Wicket.trapFocus(");
	}

	@Test
	void trapFocusAddsOneTrapThatRunsBeforeTheDialogScript()
	{
		var cut = new ModalDialog("id").trapFocus().trapFocus();
		startOpen(cut);

		assertThat(overlay(cut).getBehaviors(TrapFocusBehavior.class)).hasSize(1);
		String response = document();
		assertThat(response.indexOf("Wicket.trapFocus(")).isPositive()
			.isLessThan(response.indexOf("Wicket.ModalDialog.open("));
	}

	@Test
	void dialogIsStatelessOnlyWhenEscapeKeepsItOpen()
	{
		var closable = new ModalDialog("id");
		assertThat(dialogScript(closable).getStatelessHint(overlay(closable))).isFalse();

		var kept = new ModalDialog("id").closeOnEscape(false);
		assertThat(dialogScript(kept).getStatelessHint(overlay(kept))).isTrue();
	}

	private void startOpen(ModalDialog dialog)
	{
		dialog.setContent(new WebMarkupContainer(CONTENT_ID));
		dialog.open(null);
		tester.startComponentInPage(dialog);
	}

	private String document()
	{
		return tester.getLastResponse().getDocument();
	}

	private TagTester dialogTag()
	{
		return TagTester.createTagByAttribute(tester.getLastResponseAsString(), "class",
			"modal-dialog");
	}

	private static MarkupContainer overlay(ModalDialog dialog)
	{
		return (MarkupContainer)dialog.get("overlay");
	}

	private static AbstractDefaultAjaxBehavior dialogScript(ModalDialog dialog)
	{
		return overlay(dialog).getBehaviors(AbstractDefaultAjaxBehavior.class).get(0);
	}

	private static class TitledContent extends Panel implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private final Label title = new Label("title", "Delete the order");

		TitledContent()
		{
			super(CONTENT_ID);

			add(title);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<wicket:panel><h2 wicket:id=\"title\"></h2></wicket:panel>");
		}
	}
}
