package wicket.markup.html.applet;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JApplet;

import wicket.util.lang.Objects;
import wicket.util.upload.MultiPartFormOutputStream;

/**
 * THIS CLASS IS NOT PART OF THE WICKET PUBLIC API. DO NOT ATTEMPT TO USE IT.
 * 
 * The applet implementation used to host the user's content via the IApplet
 * interface. Implements model updating for the IAppletServer interface and
 * instantiates the user's applet object via the appletClassName property as
 * determined by the Applet component. Also provides a publicly accessible
 * method setModel() which takes no parameters and can be called from JavaScript
 * to push the applet's model back to the server side. This is useful, for
 * example, when a form is about to be submitted and the models of sprockets
 * need to be pushed back to the server side ahead of the form submit.
 * 
 * @author Jonathan Locke
 */
public class HostApplet extends JApplet implements IAppletServer
{
	IApplet applet;
	private URL savedDocumentBase;

	public void init()
	{
		try
		{
			final String appletClassName = getParameter("appletClassName");
			savedDocumentBase = getDocumentBase();
			savedDocumentBase = new URL(savedDocumentBase.getProtocol(),savedDocumentBase.getHost(),savedDocumentBase.getPort(),"");
			System.out.println("document base: " + savedDocumentBase);
			Container container = getContentPane();
			Class c = Class.forName(appletClassName);
			if (c != null)
			{
				applet = (IApplet)c.newInstance();
				applet.init(this, container, getModel());
			}
		}
		catch (ClassNotFoundException e)
		{
			System.err.println("** Unable to initialize.");
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			System.err.println("** Unable to initialize.");
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			System.err.println("** Unable to initialize.");
			e.printStackTrace();
		}
		catch(RuntimeException re)
		{
			System.err.println("** Unable to initialize.");
			re.printStackTrace();
		}
		catch (MalformedURLException ex)
		{
			System.err.println("** Unable to initialize.");
			ex.printStackTrace();
		}
	}

	public void setModel()
	{
		setModel(applet.getModel());
	}

	public void setModel(final Object model)
	{
		try
		{
			final URL url = new URL(savedDocumentBase + getParameter("setModelUrl"));

			System.out.println("setting model on " + url);
			// Create a boundary string
			final String boundary = MultiPartFormOutputStream.createBoundary();
			URLConnection connection = MultiPartFormOutputStream.createConnection(url);
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Content-Type", MultiPartFormOutputStream
					.getContentType(boundary));

			// Set some other request headers...
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");

			// Upload a file
			final MultiPartFormOutputStream out = new MultiPartFormOutputStream(connection
					.getOutputStream(), boundary);
			try
			{
				out.writeFile("model", "application/x-wicket-model", "model", Objects
						.objectToByteArray(model));
			}
			finally
			{
				out.close();
			}

			// Read response from server
			final BufferedReader in = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));
			try
			{
				String line = "";
				while ((line = in.readLine()) != null)
				{
					System.out.println(line);
				}
			}
			finally
			{
				in.close();
			}
		}
		catch (Exception e)
		{
			System.err.println("** Unable to set model.");
			e.printStackTrace();
		}
	}

	public Object getModel()
	{
		try
		{
			final String url = savedDocumentBase + getParameter("getModelUrl");
			System.out.println("getting model on " + url);
			final InputStream in = new URL(url).openStream();
			try
			{
				return new ObjectInputStream(in).readObject();
			}
			finally
			{
				in.close();
			}
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("** Unable to get model.");
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			System.out.println("** Unable to get model.");
			e.printStackTrace();
			return null;
		}
	}
}
