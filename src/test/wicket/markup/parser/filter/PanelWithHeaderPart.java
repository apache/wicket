/**
 * 
 */
package wicket.markup.parser.filter;

import wicket.MarkupContainer;
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
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 */
	public PanelWithHeaderPart(MarkupContainer<?> parent,String id)
	{
		super(parent,id);
		
		add(new Label(this,"body"));
		add(new StyleSheetReference(this,"testlink", PanelWithHeaderPart.class,"test.css"));
		add(new JavaScriptReference(this,"testscript", PanelWithHeaderPart.class, "test.js"));
		
	}

}
