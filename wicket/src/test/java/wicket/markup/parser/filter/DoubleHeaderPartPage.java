/**
 * 
 */
package wicket.markup.parser.filter;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;

/**
 * @author jcompagner
 * 
 */
public class DoubleHeaderPartPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DoubleHeaderPartPage()
	{
		new Label(this, "title", "Header Part Test");
		new PanelWithHeaderPart(this, "panelwithheadercomponents1");
		new PanelWithHeaderPart(this, "panelwithheadercomponents2");
	}
}
