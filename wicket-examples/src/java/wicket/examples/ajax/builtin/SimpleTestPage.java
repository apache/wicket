package wicket.examples.ajax.builtin;

import wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.model.PropertyModel;
import wicket.util.time.Duration;

//FIXME Temp: IVAYNBERG SHOULD REMOVE ME
/**
 * @author ivaynberg
 *
 */
public class SimpleTestPage extends WebPage
{
	int i=0;
	
	/**
	 * 
	 */
	public SimpleTestPage()
	{
		final Label 							ajaxLabel;
        final AjaxSelfUpdatingTimerBehavior 	timer;

        ajaxLabel 	= new Label("ajaxLabel",new PropertyModel(this, "count"));
        timer 		= new AjaxSelfUpdatingTimerBehavior (Duration.seconds(10));

        ajaxLabel.add(timer);
        add(ajaxLabel);
        
        
        
        Form f = new Form("form") {
        	
        	protected void onSubmit() {
        		
        		i++;
        		System.out.println(i);
        		setModelObject("hui");
        	}
        };
        
        f.setModel(new PropertyModel(this, "zap"));
        f.add(new Button("submit"));
        add(f);
	}
	
	/**
	 * @return a
	 */
	public String getZap()
	{
		return "zap";
	}
	
	/**
	 * @param z
	 */
	public void setZap(String z)
	{
		
	}

	/**
	 * @return a
	 */
	public int getCount()
	{
		return count++;
	}
	
	int count = 0;
}
