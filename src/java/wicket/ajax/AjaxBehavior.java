package wicket.ajax;

import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.PackageResourceReference;

public abstract class AjaxBehavior extends AbstractAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private static final PackageResourceReference JAVASCRIPT = new PackageResourceReference(
			AjaxBehavior.class, "wicket-ajax.js");

	protected String getImplementationId()
	{
		return "wicket-default";
	}

	protected String buildAjaxCall()
	{
		return ("wicketAjaxGet('" + getCallbackUrl() + "');");
	}

	protected String buildAjaxCall(String url)
	{
		return buildAjaxCallRaw("'"+url+"'");
	}

	protected String buildAjaxCallRaw(String javascript)
	{
		return ("wicketAjaxGet(" + javascript + ");");
	}

	
	protected void onRenderHeadInitContribution(Response response)
	{
		writeJsReference(response, JAVASCRIPT);
	}

	/**
	 * @see wicket.behavior.IBehaviorListener#onRequest()
	 */
	public void onRequest()
	{
		AjaxRequestTarget target = new AjaxRequestTarget();
		RequestCycle.get().setRequestTarget(target);
		respond(target);
	}


	protected abstract void respond(AjaxRequestTarget target);
}
