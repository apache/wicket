package wicket.examples.portlet;

import wicket.Page;
import wicket.markup.html.link.Link;
import wicket.protocol.http.portlet.PortletPage;

/**
 * @author Janne Hietam&auml;ki
 *
 */
public class ExamplePortlet2 extends PortletPage
{

	/**
	 * @param page
	 */
	public ExamplePortlet2(final Page page){
		add(new Link("link"){
			@Override
			public void onClick()
			{
				setResponsePage(page);
			}
		});
	}
}
