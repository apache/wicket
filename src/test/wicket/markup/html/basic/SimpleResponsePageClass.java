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
    /**
     * simple test page with a class response page 
     */
    public SimpleResponsePageClass() {
        Form form = new Form("form") {
            protected void onSubmit() {
                //use Page "class"
                setResponsePage(SimplePage.class, new PageParameters("test=test"));
            }
        };
        add(form);
    }
}
