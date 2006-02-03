package sprockets;

import java.awt.Container;

/**
 * The IApplet interface should be implemented by the class passed to the Applet
 * constructor. This class and every class referenced by it will be
 * automatically included in the applet JAR file for this applet. When the
 * applet is loaded by the client browser, the init() method will be called,
 * passing in a Container to populate with components and the model object
 * produced by the Applet component's IModel.
 * 
 * @author Jonathan Locke
 */
public interface IApplet
{
	/**
	 * Interface to code that initializes a Container using a model.
	 * 
	 * @param container
	 *            The Swing container to populate with components
	 * @param server
	 *            The server that this applet communicates with
	 * @param model
	 *            The model to update in the applet
	 */
	void init(IAppletServer server, Container container, Object model);

	/**
	 * @return The model edited by this applet
	 */
	Object getModel();
}
