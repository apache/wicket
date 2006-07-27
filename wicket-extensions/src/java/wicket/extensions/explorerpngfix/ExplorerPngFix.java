package wicket.extensions.explorerpngfix;

import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Response;
import wicket.behavior.AbstractBehavior;
import wicket.markup.html.IHeaderContributor;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.request.WebClientInfo;

/**
 * A behavior that adds the necessary javascript to the page to make ie < 7.0
 * properly work with png transparency.
 * 
 * @author ivaynberg
 * 
 */
public class ExplorerPngFix extends AbstractBehavior implements IHeaderContributor
{

	private static final long serialVersionUID = 1L;

	private static final ResourceReference ref = new ResourceReference(ExplorerPngFix.class,
			"explorerPngFix.js");

	private static final ThreadLocal<Boolean> rendered = new ThreadLocal<Boolean>()
	{

		@Override
		protected Boolean initialValue()
		{
			return Boolean.FALSE;
		}
	};

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
	 */
	public void renderHead(Response response)
	{
		if (Boolean.FALSE.equals(rendered.get()))
		{

			WebClientInfo info = ((WebRequestCycle)RequestCycle.get()).getClientInfo();

			if (info.isBrowserInternetExplorer() && info.getBrowserMajorVersion() < 7)
			{
				response.write("<!--[if lt IE 7.]> <script defer type=\"text/javascript\" src=\"");
				response.write(RequestCycle.get().urlFor(ref));
				response.write("\"></script> <![endif]-->");

				rendered.set(Boolean.TRUE);

			}
		}
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#cleanup()
	 */
	@Override
	public void cleanup()
	{
		rendered.remove();
	}

}
