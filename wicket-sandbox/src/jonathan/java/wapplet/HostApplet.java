package wapplet;

import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

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
				final String modelUrl = getDocumentBase() + getParameter("modelUrl");
				InputStream in = new URL(modelUrl).openStream();
				Object model = new ObjectInputStream(in).readObject();
		    	initializer.init(container, model);
		    }
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
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
