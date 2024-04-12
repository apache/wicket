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