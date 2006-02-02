package wicket.ajax;

import wicket.Application;
import wicket.IInitializer;
import wicket.markup.html.PackageResource;


/**
 * Initialized for the wicket.ajax package
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxInitializer implements IInitializer
{
	
	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, AjaxBehavior.class, "wicket-ajax.js");	
	}

}
