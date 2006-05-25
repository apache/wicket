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
		add(new Label(this, "title", "Header Part Test"));
		add(new PanelWithHeaderPart(this, "panelwithheadercomponents1"));
		add(new PanelWithHeaderPart(this, "panelwithheadercomponents2"));
	}
}
