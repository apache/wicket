package org.apache.wicket.bean.validation;

import static org.junit.Assert.*;

import javax.validation.constraints.NotNull;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

public class PropertyValidatorRequiredTest {
	@Rule
	public static WicketTesterScope scope = new WicketTesterScope() {
		protected WicketTester create() {
			return new WicketTester(new TestApplication());
		};
	};

	@Test
	public void test() {
		TestPage page = scope.getTester().startPage(TestPage.class);

		// no group
		assertTrue(page.input1.isRequired());
		assertFalse(page.input2.isRequired());
		assertFalse(page.input3.isRequired());
		assertFalse(page.input4.isRequired());

		// group1
		assertFalse(page.input5.isRequired());
		assertTrue(page.input6.isRequired());
		assertFalse(page.input7.isRequired());
		assertTrue(page.input8.isRequired());

		// group2
		assertFalse(page.input9.isRequired());
		assertFalse(page.input10.isRequired());
		assertTrue(page.input11.isRequired());
		assertTrue(page.input12.isRequired());

		// group1+group2
		assertFalse(page.input13.isRequired());
		assertTrue(page.input14.isRequired());
		assertTrue(page.input15.isRequired());
		assertTrue(page.input16.isRequired());

		// group3
		assertFalse(page.input17.isRequired());
		assertFalse(page.input18.isRequired());
		assertFalse(page.input19.isRequired());
		assertFalse(page.input20.isRequired());

	}

	public static class TestApplication extends MockApplication {
		@Override
		protected void init() {
			super.init();
			new BeanValidationConfiguration().configure(this);
		}
	}

	public static class TestPage extends WebPage implements
			IMarkupResourceStreamProvider {

		private TestBean bean = new TestBean();
		private FormComponent<String> input1, input2, input3, input4, input5,
				input6, input7, input8, input9, input10, input11, input12,
				input13, input14, input15, input16, input17, input18, input19,
				input20;

		public TestPage() {
			Form<?> form = new Form<Void>("form");
			add(form);

			input1 = new TextField<String>("input1", new PropertyModel<String>(
					this, "bean.property"))
					.add(new PropertyValidator<String>());
			input2 = new TextField<String>("input2", new PropertyModel<String>(
					this, "bean.propertyOne"))
					.add(new PropertyValidator<String>());
			input3 = new TextField<String>("input3", new PropertyModel<String>(
					this, "bean.propertyTwo"))
					.add(new PropertyValidator<String>());
			input4 = new TextField<String>("input4", new PropertyModel<String>(
					this, "bean.propertyOneTwo"))
					.add(new PropertyValidator<String>());

			input5 = new TextField<String>("input5", new PropertyModel<String>(
					this, "bean.property")).add(new PropertyValidator<String>(
					GroupOne.class));
			input6 = new TextField<String>("input6", new PropertyModel<String>(
					this, "bean.propertyOne"))
					.add(new PropertyValidator<String>(GroupOne.class));
			input7 = new TextField<String>("input7", new PropertyModel<String>(
					this, "bean.propertyTwo"))
					.add(new PropertyValidator<String>(GroupOne.class));
			input8 = new TextField<String>("input8", new PropertyModel<String>(
					this, "bean.propertyOneTwo"))
					.add(new PropertyValidator<String>(GroupOne.class));

			input9 = new TextField<String>("input9", new PropertyModel<String>(
					this, "bean.property")).add(new PropertyValidator<String>(
					GroupTwo.class));
			input10 = new TextField<String>("input10",
					new PropertyModel<String>(this, "bean.propertyOne"))
					.add(new PropertyValidator<String>(GroupTwo.class));
			input11 = new TextField<String>("input11",
					new PropertyModel<String>(this, "bean.propertyTwo"))
					.add(new PropertyValidator<String>(GroupTwo.class));
			input12 = new TextField<String>("input12",
					new PropertyModel<String>(this, "bean.propertyOneTwo"))
					.add(new PropertyValidator<String>(GroupTwo.class));

			input13 = new TextField<String>("input13",
					new PropertyModel<String>(this, "bean.property"))
					.add(new PropertyValidator<String>(GroupOne.class,
							GroupTwo.class));
			input14 = new TextField<String>("input14",
					new PropertyModel<String>(this, "bean.propertyOne"))
					.add(new PropertyValidator<String>(GroupOne.class,
							GroupTwo.class));
			input15 = new TextField<String>("input15",
					new PropertyModel<String>(this, "bean.propertyTwo"))
					.add(new PropertyValidator<String>(GroupOne.class,
							GroupTwo.class));
			input16 = new TextField<String>("input16",
					new PropertyModel<String>(this, "bean.propertyOneTwo"))
					.add(new PropertyValidator<String>(GroupOne.class,
							GroupTwo.class));

			input17 = new TextField<String>("input17",
					new PropertyModel<String>(this, "bean.property"))
					.add(new PropertyValidator<String>(GroupThree.class));
			input18 = new TextField<String>("input18",
					new PropertyModel<String>(this, "bean.propertyOne"))
					.add(new PropertyValidator<String>(GroupThree.class));
			input19 = new TextField<String>("input19",
					new PropertyModel<String>(this, "bean.propertyTwo"))
					.add(new PropertyValidator<String>(GroupThree.class));
			input20 = new TextField<String>("input20",
					new PropertyModel<String>(this, "bean.propertyOneTwo"))
					.add(new PropertyValidator<String>(GroupThree.class));

			form.add(input1, input2, input3, input4, input5, input6, input7,
					input8, input9, input10, input11, input12, input13,
					input14, input15, input16, input17, input18, input19,
					input20);

		}

		@Override
		public IResourceStream getMarkupResourceStream(
				MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream(
					"<form wicket:id='form'><input wicket:id='input1'/><input wicket:id='input2'/><input wicket:id='input3'/><input wicket:id='input4'/><input wicket:id='input5'/><input wicket:id='input6'/><input wicket:id='input7'/><input wicket:id='input8'/><input wicket:id='input9'/><input wicket:id='input10'/><input wicket:id='input11'/><input wicket:id='input12'/><input wicket:id='input13'/><input wicket:id='input14'/><input wicket:id='input15'/><input wicket:id='input16'/><input wicket:id='input17'/><input wicket:id='input18'/><input wicket:id='input19'/><input wicket:id='input20'/></form>");
		}

	}

	public static interface GroupOne {
	}

	public static interface GroupTwo {
	}

	public static interface GroupThree {
	}

	public static class TestBean {
		@NotNull
		String property;

		@NotNull(groups = { GroupOne.class })
		String propertyOne;

		@NotNull(groups = { GroupTwo.class })
		String propertyTwo;

		@NotNull(groups = { GroupOne.class, GroupTwo.class })
		String propertyOneTwo;

	}

}
