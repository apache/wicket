package wapplet;

import javax.swing.JPanel;

/**
 * The IAppletInitializer interface should be implemented by the class
 * passed to the Applet constructor. This class and every class referenced
 * by it will be automatically included in the applet JAR file for this
 * applet. When the applet is loaded by the client browser, the init()
 * method will be called, passing in a JPanel to populate with components
 * and the model object produced by the Applet component's IModel.
 * 
 * @author Jonathan Locke
 */
public interface IAppletInitializer
{
	/**
	 * Interface to code that initializes a JPanel using a model.
	 * 
	 * @param panel
	 *            The panel to populate with components
	 * @param model
	 *            The model to update in the applet
	 */
	void init(JPanel panel, Object model);
}