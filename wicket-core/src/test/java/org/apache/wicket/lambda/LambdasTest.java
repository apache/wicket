package org.apache.wicket.lambda;

import static org.apache.wicket.lambda.Lambdas.onTag;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link Lambdas}
 */
public class LambdasTest extends WicketTestCase
{
	@Test
	public void onTagTest()
	{
		WebMarkupContainer component = new WebMarkupContainer(MockPageWithOneComponent.COMPONENT_ID);
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(component);
		String value = "value";
		String key = "key";
		component.add(onTag(tag -> tag.put(key, value)));

		tester.startPage(page);

		TagTester tagTester = tester.getTagByWicketId(MockPageWithOneComponent.COMPONENT_ID);
		assertThat(tagTester.getAttribute(key), is(equalTo(value)));
	}
}
