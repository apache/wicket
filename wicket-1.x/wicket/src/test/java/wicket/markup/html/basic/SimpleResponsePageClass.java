/**
 * 
 */
package wicket.markup.html.basic;

import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;

/**
 * @author jcompagner
 *
 */
public class SimpleResponsePageClass extends WebPage 
{
	private static final long serialVersionUID = 1L;
	
    /**
     * simple test page with a class response page 
     */
    public SimpleResponsePageClass() {
        Form form = new Form("form") {
        	private static final long serialVersionUID = 1L;
            protected void onSubmit() {
                //use Page "class"
                setResponsePage(SimplePage.class, new PageParameters("test=test"));
            }
        };
        add(form);
    }
}
