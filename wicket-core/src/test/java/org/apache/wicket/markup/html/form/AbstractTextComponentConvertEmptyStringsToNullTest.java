package org.apache.wicket.markup.html.form;

import java.util.Locale;

import junit.framework.TestCase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;

public class AbstractTextComponentConvertEmptyStringsToNullTest extends TestCase
{

	public void test() throws Exception
	{
		WicketTester tester = new WicketTester();

		StringArrayPage page = (StringArrayPage)tester.startPage(StringArrayPage.class);

		tester.submitForm("form");

		assertNotNull(page.array);
		assertEquals(0, page.array.length);
	}

	public static class StringArrayPage extends WebPage implements IMarkupResourceStreamProvider
	{

		public String[] array = new String[0];

		public Form<Void> form;

		public StringArrayPage()
		{

			form = new Form<Void>("form");
			add(form);

			form.add(new TextField<String[]>("array", new PropertyModel<String[]>(this, "array"))
			{
				@Override
				@SuppressWarnings("unchecked")
				public <C> IConverter<C> getConverter(Class<C> type)
				{
					return (IConverter<C>)new StringArrayConverter();
				}
			}.setConvertEmptyInputStringToNull(false));
		}

		private class StringArrayConverter implements IConverter<String[]>
		{
			public String[] convertToObject(String value, Locale locale)
			{
				return Strings.split(value, ',');
			}

			public String convertToString(String[] value, Locale locale)
			{
				return Strings.join(",", value);
			}
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id='form'><input type='text' wicket:id='array'/></form></body></html>");
		}
	}

}