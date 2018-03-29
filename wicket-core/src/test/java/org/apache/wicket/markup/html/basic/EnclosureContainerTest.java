package org.apache.wicket.markup.html.basic;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link EnclosureContainer}.
 * 
 * @author svenmeier
 */
public class EnclosureContainerTest extends WicketTestCase
{

	@Test
	public void test() {
		EnclosureContainerPage page = new EnclosureContainerPage();
			
		tester.startPage(page);
		
		assertFalse(page.container.isVisible());
	}
}
