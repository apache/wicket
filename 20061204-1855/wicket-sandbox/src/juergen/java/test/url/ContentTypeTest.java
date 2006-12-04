/*
 * Created on 17.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test.url;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.resource.UrlResourceStream;

/**
 * @author Juergen Donnerstag
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContentTypeTest
{
    /**
     * 
     * @param args
     */
	public static void main(String[] args)
	{
        try
        {
            // Test 1: file exists 
            File file = new File("src/juergen/java/test/url/Beer.gif");
            if (file.exists() == false)
            {
                System.err.println("File should exist");
            }
            URL url = file.toURL();
            UrlResourceStream stream = new UrlResourceStream(url);
            String contentType = stream.getContentType();
            if (!"image/gif".equals(contentType))
            {
                System.err.println("Expected 'image/gif'");
            }

            // Test 2: file does not exist 
            file = new File("dummy.gif");
            url = file.toURL();
            stream = new UrlResourceStream(url);
            contentType = stream.getContentType();
            if (!"image/gif".equals(contentType))
            {
                System.err.println("Expected 'image/gif'");
            }

            // Test 3: jar file exists 
            url = new URL("jar:file:src/juergen/lib/wicket-examples.war!/WEB-INF/classes/wicket/examples/pub/Beer.gif");
            stream = new UrlResourceStream(url);
            try
            {
                stream.getInputStream();
                stream.close();
            } 
            catch (ResourceStreamNotFoundException e1)
            {
                e1.printStackTrace();
            }
			catch (IOException e)
			{
				e.printStackTrace();
			}
            contentType = stream.getContentType();
            if (!"image/gif".equals(contentType))
            {
                System.err.println("Expected 'image/gif', but was " + contentType);
            }

            // Test 4: jar file does not exists 
            url = new URL("jar:file:src/juergen/lib/wicket-examples.war!/any-dir/Beer.gif");
            stream = new UrlResourceStream(url);
            contentType = stream.getContentType();
            if (!"image/gif".equals(contentType))
            {
                System.err.println("Expected 'image/gif', but was " + contentType);
            }

            // Test 5: jar file does not exists 
            url = new URL("jar:file:any.war!/any-dir/Beer.gif");
            stream = new UrlResourceStream(url);
            contentType = stream.getContentType();
            if (!"image/gif".equals(contentType))
            {
                System.err.println("Expected 'image/gif', but was " + contentType);
            }
        } 
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
	}
}
