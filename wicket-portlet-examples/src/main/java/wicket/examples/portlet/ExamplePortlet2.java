package wicket.examples.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.protocol.http.portlet.PortletPage;

/**
 * @author Janne Hietam&auml;ki
 * 
 */
public class ExamplePortlet2 extends PortletPage
{
	private static final Log log = LogFactory.getLog(ExamplePortlet.class);

	/**
	 * @param page
	 */
	public ExamplePortlet2(final Page page)
	{
		add(new Link("link")
		{
			public void onClick()
			{
				setResponsePage(page);
			}
		});
		add(new Label("windowState",new PropertyModel(this,"windowState")));
		add(new Label("portletMode",new PropertyModel(this,"portletMode")));
	}
		
	protected void onSetWindowState(WindowState state){
		log.info("Window state changed to "+state);
	}
	
	protected void onSetPortletMode(PortletMode mode){
		log.info("Portlet mode changed to "+mode);
	}	
}
