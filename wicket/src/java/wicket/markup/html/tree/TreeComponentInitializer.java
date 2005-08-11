/**
 * 
 */
package wicket.markup.html.tree;

import wicket.Application;
import wicket.IComponentInitializer;
import wicket.markup.html.PackageResource;

/**
 * This component initializer initializes the 3 pictures and one css file for the Tree Component
 * 
 * @author jcompagner
 *
 */
public class TreeComponentInitializer implements IComponentInitializer
{
	/**
	 * @see wicket.IComponentInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, Tree.class, "blank.gif");
		PackageResource.bind(application, Tree.class, "minus.gif");
		PackageResource.bind(application, Tree.class, "plus.gif");
		PackageResource.bind(application, Tree.class, "tree.css");		
	}

}
