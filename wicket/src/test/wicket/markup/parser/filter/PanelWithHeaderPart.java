/**
 * 
 */
package wicket.markup.parser.filter;

import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.markup.html.resources.StyleSheetReference;

/**
 * @author jcompagner
 *
 */
public class PanelWithHeaderPart extends Panel
{
	/**
	 * @param id
	 */
	public PanelWithHeaderPart(String id)
	{
		super(id);
		
		add(new Label("body"));
		add(new StyleSheetReference("testlink", PanelWithHeaderPart.class,"test.css"));
		add(new JavaScriptReference("testscript", PanelWithHeaderPart.class, "test.js"));
		
	}

}
