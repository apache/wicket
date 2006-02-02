package wapplet;

import java.awt.Container;

import javax.swing.JApplet;

/**
 * The applet implementation used to host the user's content.
 * 
 * @author Jonathan Locke
 */
public class HostApplet extends JApplet
{
	public void init()
	{
		try
		{
			final String initializerClassName = getParameter("appletInitializerClassName");		
		    Container container = getContentPane();
		    Class c = Class.forName(initializerClassName);
		    if (c != null)
		    {
		    	final IAppletInitializer initializer = (IAppletInitializer)c.newInstance();
		    	initializer.init(container, initializer);
		    }
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
