package applet;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

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
			final String appletClassName = getParameter("appletClassName");
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

	public void setModel()
	{
		setModel(applet.getModel());
	}

	public void setModel(Object model)
	{
		try
		{
			URL url = new URL(getDocumentBase() + getParameter("setModelUrl"));

			// create a boundary string
			String boundary = MultiPartFormOutputStream.createBoundary();
			URLConnection connection = MultiPartFormOutputStream.createConnection(url);
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Content-Type", MultiPartFormOutputStream
					.getContentType(boundary));

			// set some other request headers...
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");

			// no need to connect cuz getOutputStream() does it
			MultiPartFormOutputStream out = new MultiPartFormOutputStream(connection
					.getOutputStream(), boundary);

			// upload a file
			ByteArrayOutputStream modelOut = new ByteArrayOutputStream();
			new ObjectOutputStream(modelOut).writeObject(model);
			out.writeFile("model", "application/x-wicket-model", "model", modelOut.toByteArray());
			out.close();

			// read response from server
			BufferedReader in = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null)
			{
				System.out.println(line);
			}
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Object getModel()
	{
		try
		{
			final String url = getDocumentBase() + getParameter("getModelUrl");
			InputStream in = new URL(url).openStream();
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
