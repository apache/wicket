/**
 * 
 */
package wicket.markup.html.tree;

import wicket.Application;
import wicket.IInitializer;
import wicket.markup.html.PackageResource;

/**
 * This component initializer initializes the 3 pictures and one css file for
 * the Tree Component
 * 
 * @author jcompagner
 * 
 */
public class TreeComponentInitializer implements IInitializer
{
	/**
	 * @param application
	 *            The application
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, Tree.class, "blank.gif");
		PackageResource.bind(application, Tree.class, "minus.gif");
		PackageResource.bind(application, Tree.class, "plus.gif");
		PackageResource.bind(application, Tree.class, "tree.css");
	}
}
