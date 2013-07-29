package org.apache.wicket.markup.html.form;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Test;

/**
 *
 */
public class TextAreaTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5289
	 */
	@Test
	public void requiredAttribute()
	{
		TestPage testPage = new TestPage();
		testPage.textArea.setOutputMarkupId(true);
		testPage.textArea.setType(String.class);
		testPage.textArea.setRequired(true);
		tester.startPage(testPage);

		TagTester tagTester = tester.getTagById(testPage.textArea.getMarkupId());
		String required = tagTester.getAttribute("required");
		assertEquals("required", required);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Void> form;
		TextArea<String> textArea;
		IModel<String> textModel = Model.of((String) null);

		/** */
		public TestPage()
		{
			add(form = new Form<Void>("form"));
			form.add(textArea = new TextArea<String>("textarea", textModel));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
		                                               Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
					+ "<form wicket:id=\"form\"><textarea wicket:id=\"textarea\"></textarea></form></body></html>");
		}
	}
}
