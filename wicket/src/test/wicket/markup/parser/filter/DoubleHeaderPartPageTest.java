/**
 * 
 */
package wicket.markup.parser.filter;

import wicket.WicketTestCase;

/**
 * @author jcompagner
 * 
 */
public class DoubleHeaderPartPageTest extends WicketTestCase
{

	/**
	 * @param name
	 */
	public DoubleHeaderPartPageTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHeaderPartPage() throws Exception
	{
		executeTest(DoubleHeaderPartPage.class, "DoubleHeaderPartPageExpectedResult.html");
	}

}
