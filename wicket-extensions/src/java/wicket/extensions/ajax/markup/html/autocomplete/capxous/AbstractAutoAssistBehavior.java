package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.PackageResourceReference;

/**
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractAutoAssistBehavior extends AbstractAjaxBehavior
{
	private final PackageResourceReference AUTOASSIST_JS = new PackageResourceReference(
			AbstractAutoAssistBehavior.class, "autoassist.js");
	private final PackageResourceReference PROTOTYPE_JS = new PackageResourceReference(
			AbstractAutoAssistBehavior.class, "prototype.js");
	private final PackageResourceReference AUTOASSIST_HELPER_JS = new PackageResourceReference(
			AbstractAutoAssistBehavior.class, "autoassist_helper.js");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String getImplementationId()
	{
		return "capxous";
	}

	protected void onBind()
	{
		getComponent().setOutputMarkupId(true);
	}

	protected void onRenderHeadInitContribution(Response response)
	{
		writeJsReference(response, PROTOTYPE_JS);
		writeJsReference(response, AUTOASSIST_JS);
		writeJsReference(response, AUTOASSIST_HELPER_JS);
	}

	protected void onRenderHeadContribution(Response response)
	{
		final String id = getComponent().getMarkupId();
		response.write("<script>registerAutoassist(\"" + id + "\", \"" + getCallbackUrl() + "\");</script>");
	}

	/**
	 * @see wicket.behavior.IBehaviorListener#onRequest()
	 */
	public final void onRequest()
	{
		final RequestCycle requestCycle=RequestCycle.get();
		final String val=requestCycle.getRequest().getParameter("val");
		onRequest(val, requestCycle);
	}

	protected abstract void onRequest(String val, RequestCycle requestCycle);

}
