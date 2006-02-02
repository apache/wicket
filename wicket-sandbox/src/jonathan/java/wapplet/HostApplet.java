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
public class HostApplet extends JApplet implements IAppletServer
{
	IApplet applet;
	
	public void init()
	{
		try
		{
			final String initializerClassName = getParameter("appletInitializerClassName");		
		    Container container = getContentPane();
		    Class c = Class.forName(initializerClassName);
		    if (c != null)
		    {
		    	applet = (IApplet)c.newInstance();
		    	applet.init(container, this, get());
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
	
	public void set()
	{
		set(applet.getModel());
	}

	public void set(Object model)
	{
	}

	public Object get()
	{
		try
		{
			final String modelUrl = getDocumentBase() + getParameter("modelUrl");
			InputStream in = new URL(modelUrl).openStream();
			return new ObjectInputStream(in).readObject();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);			
		}
	}
}
