/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.file.File;
import wicket.util.file.Path;
import wicket.util.string.Strings;
import wicket.util.time.Time;
import wicket.util.watch.IModifiable;

/**
 * A Resource is a File or URL that has a stream and type (determined by the file
 * or URL's extension) and a last modification date.  Thus a Resource is some kind 
 * of typed stream that can change over time.
 * <p> 
 * Resources can be loaded from a file search path or with a ClassLoader.  When 
 * loaded from a search path, they have a File nature which can be watched for changes. 
 * When loaded with a ClassLoader, they have a stream nature and cannot be watched 
 * for changes.
 * 
 * @see wicket.util.resource.IResource
 * @see wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public final class Resource implements IResource, IModifiable
{
    /** Logging  */
    private static Log log = LogFactory.getLog(Resource.class);
    
    /** Any associated file */
    private File file;

    /** Resource stream */
    private InputStream inputStream;

    /** Any resource location */
    private URL url;

    /**
     * Locate a resource based on a class and an extension.
     * @param c Class next to which the resource should be found
     * @param extension Resource extension
     * @return The resource
     * @see Resource#locate(Path, Class, String, Locale, String)
     */
    public static Resource locate(final Class c, final String extension)
    {
        return locate(new Path(), c, extension);
    }

    /**
     * Locate a resource based on a path, a class and an extension.
     * @param path Path to search for resource
     * @param c Class next to which the resource should be found
     * @param extension Resource extension
     * @return The resource
     * @see Resource#locate(Path, Class, String, Locale, String)
     */
    public static Resource locate(final Path path, final Class c, final String extension)
    {
        return locate(path, c, null, Locale.getDefault(), extension);
    }

    /**
     * Locate a resource based on a path, a class, a style, a locale 
     * and an extension.
     * @param path Path to search for resource
     * @param c Class next to which the resource should be found
     * @param style Any resource style, such as a skin style
     * @param locale The locale of the resource to load
     * @param extension Resource extension
     * @return The resource
     * @see Resource#locate(Path, Class, String, Locale, String)
     */
    public static Resource locate(final Path path, final Class c, final String style,
            final Locale locale, final String extension)
    {
        return locate(path, c.getClassLoader(), c.getName(), style, locale, extension);
    }

    /**
     * Loads a resource. This method prefers to load from the path argument first.
     * If the resource cannot be found on the path, the classloader provided is
     * searched.  Resources are located using the style, locale and extension provided
     * and the naming logic encapsulated in ResourceLocator.
     * @param path Path to search for resource
     * @param classloader ClassLoader to search if not found on path
     * @param resourcePath The path of the resource
     * @param style Any resource style, such as a skin style
     * @param locale The locale of the resource to load
     * @param extension The extension of the resource
     * @return The resource
     * @see ResourceLocator
     */
    public static Resource locate(final Path path, final ClassLoader classloader,
            String resourcePath, final String style, final Locale locale, final String extension)
    {
        // If no extension specified, extract extension
        final String extensionString;
        if (extension == null)
        {
            extensionString = "." + Strings.lastPathComponent(resourcePath, '.');
            resourcePath = Strings.beforeLastPathComponent(resourcePath, '.');
        }
        else
        {
            extensionString = "." + extension;
        }

        // Compute string components
        resourcePath = resourcePath.replace('.', '/');

        // 1. Search the path provided
        if (path != null && path.size() > 0)
        {
            final Resource resource = new ResourceLocator()
			{
	            /**
	             * Check if file exists.
	             * @param name Name of the resource to find
	             * @return Resource, or null if file not found
	             */
	            protected Resource locate(final String name)
	            {
                    // Log attempt
	                log.debug("Attempting to locate resource '" + name + "' on path");
                    
                    // Try to find file resource on the path supplied
	                final File file = path.find(name);
                    
                    // Found resource?
	                if (file != null)
	                {
                        // Return file resource
	                    return new Resource(file);
	                }
	                return null;
	            }
	    	}.locate(resourcePath, style, locale, extensionString);
	    	
	    	if (resource != null)
	    	{
	    		return resource;
	    	}
        }

        // 2. Search the ClassLoader provided
        return new ResourceLocator()
		{
            /**
             * Locate resource using classloader
             * @param name Name of resource
             * @return The resource 
             */
            protected Resource locate(final String name)
            {
                // Log attempt
                log.debug("Attempting to locate resource '" + name + "' on classpath");
                
                // Try loading filename on classpath
                final URL url = classloader.getResource(name);
                if (url != null)
                {
                    return new Resource(url);
                }
                return null;
            }
    	}.locate(resourcePath, style, locale, extensionString);
    }

    /**
     * Finds a Resource with a given class name and extension
     * @param classname A fully qualified class name with dotted separators, 
     * such as "com.whatever.MyPage"
     * @param extension The resource extension including '.', such as ".html"
     * @return The Resource, or null if it does not exist
     */
    public static Resource locate(final String classname, final String extension)
    {
        String filename = Strings.replaceAll(classname, ".", "/") + extension;
        final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        return url != null ? new Resource(url) : null;
    }

    /**
     * Private constructor to force use of static factory methods.
     */
    private Resource()
    {
    }

    /**
     * Private constructor to force use of static factory methods.
     * @param file File containing resource
     */
    private Resource(final File file)
    {
        this.file = file;
    }

    /**
     * Private constructor to force use of static factory methods.
     * @param url URL of resource
     */
    private Resource(final URL url)
    {
        // Get filename from URL
        String filename = url.getFile();

        // If there is a filename
        if (filename != null)
        {
            // If a file with the given name exists
            final File file = new File(filename);

            if (file.exists())
            {
                // save that file for future checking
                this.file = file;
            }
        }

        // Save URL
        this.url = url;
    }

    /**
     * Closes this resource.
     * @throws IOException
     */
    public void close() throws IOException
    {
        if (inputStream != null)
        {
            inputStream.close();
            inputStream = null;
        }
    }

    /**
     * @return The extension of this resource, such as "jpeg" or "html"
     */
    public String getExtension()
    {
        if (file != null)
        {
            return Strings.lastPathComponent(file.getName(), '.');
        }
        else
        {
            return Strings.lastPathComponent(url.getPath(), '.');
        }
    }

    /**
     * @return The file this resource resides in, if any.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return A readable input stream for this resource.
     * @throws ResourceNotFoundException
     */
    public InputStream getInputStream() throws ResourceNotFoundException
    {
        if (inputStream == null)
        {
            if (file != null)
            {
                try
                {
                    inputStream = new FileInputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    throw new ResourceNotFoundException
                        ("Resource " + file + " could not be found", e);
                }
            }
            else if (url != null)
            {
                try
                {
                    inputStream = url.openStream();
                }
                catch (IOException e)
                {
                    throw new ResourceNotFoundException
                        ("Resource " + url + " could not be opened", e);
                }
            }
        }

        return inputStream;
    }

    /**
     * @return The URL to this resource (if any)
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * @see wicket.util.watch.IModifiable#lastModifiedTime()
     * @return The last time this resource was modified
     */
    public Time lastModifiedTime()
    {
        if (file != null)
        {
            return file.lastModifiedTime();
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return file != null ? file.toString() : url.toString();
    }
}


