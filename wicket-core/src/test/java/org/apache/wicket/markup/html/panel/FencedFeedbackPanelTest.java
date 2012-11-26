package org.apache.wicket.markup.html.panel;

import static org.junit.Assert.*;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests {@link FencedFeedbackPanel}
 * 
 * @author igor
 */
public class FencedFeedbackPanelTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	@Test
	public void fencing()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);
		page.containerInput.error("error");

		// container messages should be visible to container feedbacks but not outside

		assertTrue(page.containerFeedback.anyMessage());
		assertTrue(page.containerFeedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.formInput.error("error");

		// form messages should be visible only to the form feedbacks

		assertFalse(page.containerFeedback.anyMessage());
		assertFalse(page.containerFeedback2.anyMessage());
		assertTrue(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.externalLabel.error("error");

		// external messages should be picked up only by catch-all feedbacks

		assertFalse(page.containerFeedback.anyMessage());
		assertFalse(page.containerFeedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertTrue(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.getSession().error("error");

		// session scoped errors should only be picked up by catch-all feedbacks

		assertFalse(page.containerFeedback.anyMessage());
		assertFalse(page.containerFeedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertTrue(page.externalFeedback.anyMessage());
	}

	@Test
	public void filtering()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);

		// set a filter that will only allow errors or higher

		page.containerFeedback.setFilter(new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR));

		// report an info message - should be filtered out

		page.containerInput.info("info");

		// check info message was filtered out

		assertFalse(page.containerFeedback.anyMessage());
		assertTrue(page.containerFeedback2.anyMessage());

		// ensure filtered out messages dont leak

		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		// same setup

		page = scope.getTester().startPage(TestPage.class);

		page.containerFeedback.setFilter(new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR));

		// but now with an error message that should not be filtered out

		page.containerInput.error("info");

		// check message was not filtered out

		assertTrue(page.containerFeedback.anyMessage());
		assertTrue(page.containerFeedback2.anyMessage());

		// and that it should not leak

		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

	}

	@Test
	public void moving()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);
		page.containerInput.error("error");

		assertTrue(page.containerFeedback.anyMessage());
		assertTrue(page.containerFeedback2.anyMessage());

		// does not propagate out of container
		assertFalse(page.formFeedback.anyMessage());

		// remove one of two fencing feedback panels

		page = scope.getTester().startPage(TestPage.class);
		page.containerFeedback.remove();

		page.containerInput.error("error");

		assertTrue(page.containerFeedback2.anyMessage());

		// still does not propagate out of container because there is still a fencing panel
		assertFalse(page.formFeedback.anyMessage());

		// remove the last fencing feedback panel

		page = scope.getTester().startPage(TestPage.class);
		page.containerFeedback.remove();
		page.containerFeedback2.remove();

		page.containerInput.error("error");

		// now propagates out of container
		assertTrue(page.formFeedback.anyMessage());

	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		FencedFeedbackPanel externalFeedback, formFeedback, containerFeedback, containerFeedback2;
		Component externalLabel, formInput, containerInput;

		public TestPage()
		{
			externalFeedback = new FencedFeedbackPanel("feedback");
			externalLabel = new Label("externalLabel");
			add(externalFeedback, externalLabel);

			Form<?> form = new Form<Void>("form");
			formFeedback = new FencedFeedbackPanel("formFeedback", form);
			form.add(formFeedback);
			formInput = new TextField<String>("formInput");
			form.add(formInput);
			WebMarkupContainer container = new WebMarkupContainer("container");
			containerFeedback = new FencedFeedbackPanel("containerFeedback", container);
			containerFeedback2 = new FencedFeedbackPanel("containerFeedback2", container);
			container.add(containerFeedback, containerFeedback2);
			containerInput = new TextField<String>("containerInput");
			container.add(containerInput);
			form.add(container);
			add(form);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(//
				"    <body>" + //
					"   <div wicket:id='feedback'/>" + //
					"   <div wicket:id='externalLabel'/>" + //
					"   <form wicket:id='form'>" + //
					"       <div wicket:id='formFeedback'/>" + //
					"       <input wicket:id='formInput' type='text'/>" + //
					"       <div wicket:id='container'>" + //
					"           <div wicket:id='containerFeedback'/>" + //
					"           <input wicket:id='containerInput' type='text'/>" + //
					"           <div wicket:id='containerFeedback2'/>" + //
					"       </div>" + //
					"    </form>" + //
					"</body>");
		}
	}
}
