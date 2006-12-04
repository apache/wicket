package wicket.markup.html.applet;

import java.awt.Container;

/**
 * The IApplet interface must be implemented by the class passed to the Applet
 * constructor. That class and every class referenced by it will be
 * automatically included in the applet's JAR file. When the applet is loaded by
 * the client browser, the init() method will be called, passing in an interface
 * to the originating server, a Container to populate with Swing components and
 * the model to edit. At any time, the getModel() method may be called to ask
 * the applet for the current model value.
 * 
 * @see wicket.markup.html.applet.Applet
 * 
 * @author Jonathan Locke
 */
public interface IApplet
{
	/**
	 * Interface to code that initializes a Swing Container using a model.
	 * 
	 * @param server
	 *            The originating server for this applet
	 * @param container
	 *            The Swing container to populate with components
	 * @param model
	 *            The model that this applet should edit
	 */
	void init(IAppletServer server, Container container, Object model);

	/**
	 * Gets the current state of the model being edited by the applet.
	 * 
	 * @return The applet's model
	 */
	Object getModel();
}
