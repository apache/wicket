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
package wicket.examples.groovy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;

import wicket.ApplicationSettings;
import wicket.IApplication;
import wicket.PageFactory;
import wicket.RenderException;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.Resource;
import wicket.util.watch.Watcher;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

/**
 * Extends the default Page Factory to allow for Groovy based classes.
 * Modifications to groovy files are tracked and files are reloaded
 * if modified.
 *  
 * @author Juergen Donnerstag
 */
public class GroovyPageFactory extends PageFactory
{
    /** Logging */
    private static final Log log = LogFactory.getLog(GroovyPageFactory.class);
    
    /** cache: class name to groovy class; not sure GroovyClassLoader does it as well */
    private final Map classCache = new ConcurrentHashMap();

    /**
     * Constructor
     * 
     * @param application Wicket application object
     */
    public GroovyPageFactory(final IApplication application)
    {
        super(application);
    }

    /**
     * Create an object of type 'classname'. First try standard java classes, 
     * than groovy files. Groovy file name must be 'classname'.groovy.
     * 
     * @param classname The object's class name 
     */
    public Class getClassInstance(final String classname)
    {
        // If definition already loaded, ...
        Class clazz = (Class) classCache.get(classname);
        if (clazz != null)
        {
            return clazz;
        }

        // Else, try Groovy.
        final Resource resource = Resource.locate(classname, ".groovy");
        if (resource != null)
        {
	        try
	        {
	            // Load the groovy file, get the Class and watch for changes
	            clazz = loadGroovyFileAndWatchForChanges(classname, resource);
	            if (clazz != null)
	            {
	                return clazz;
	            }
	        }
	        catch (RenderException ex)
	        {
	            throw new RenderException("Unable to load class with name: " + classname, ex);
	        }
        }
        else
        {
            throw new RenderException("File not found: " + resource);
        }
        
        throw new RenderException("Unable to load class with name: " + classname);
    }

    /**
     * Load a Groovy file, create a Class object and put it into the cache
     * 
     * @param classname The class name to be created by the Groovy filename
     * @param resource The Groovy resource
     * @return the Class object created by the groovy resouce
     */
    private final Class loadGroovyFile(String classname, final Resource resource)
    {
		// Ensure that we use the correct classloader so that we can find
		// classes in an application server.
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null)
		{
			cl = GroovyPageFactory.class.getClassLoader();
		}
		
        final GroovyWarClassLoader groovyCl = new GroovyWarClassLoader(cl);
        Class clazz = null;
        
        try 
        {
            final InputStream in = resource.getInputStream();
            if (in != null)
            {
	            clazz = groovyCl.parseClass(in);
	            if (clazz != null)
	            {
	                // this is necessary because with groovy the filename can be 
	                // different from the class definition included.
	                if (false == classname.equals(clazz.getName()))
	                {
	                    log.warn("Though it is possible, the Groovy file name and "
	                            + "the java class name defined in that file SHOULD "
	                            + "match and follow the java rules");
	                }
	                classname = clazz.getName();
	            }
            }
            else
            {
                log.warn("Groovy file not found: " + resource);
            }
        } 
        catch (CompilationFailedException e) 
        {
            throw new RenderException("Error parsing groovy file: " 
                    + resource, e);
        } 
        catch (IOException e) 
        {
            throw new RenderException("Error reading groovy file: " 
                    + resource, e);
        }
        catch (Throwable e) 
        {
            throw new RenderException("Error while reading groovy file: " 
                    + resource, e);
        }
        finally
        {
            if (clazz == null)
            {
                // Groovy file not found; error while compiling etc.. 
                // Remove it from cache
                classCache.remove(classname);
            }
            else
            {
                // Put the new class definition into the cache
                classCache.put(classname, clazz);
            }
        }

        return clazz;
    }
    
    /**
     * Load the groovy file and watch for changes. If changes to the groovy happens,
     * than reload the file.
     * 
     * @param classname
     * @param resource
     * @return Loaded class
     */
    private Class loadGroovyFileAndWatchForChanges(final String classname, final Resource resource)
    {
        final ApplicationSettings settings = getApplication().getSettings();

        // Watch file in the future
        final Watcher watcher = settings.getResourceWatcher();

        if (watcher != null)
        {
            watcher.add(resource, new IChangeListener()
            {
                public void changed()
                {
                    try
                    {
                        log.info("Reloading groovy file from " + resource);
                        
                        // Reload file and update cache
                        final Class clazz = loadGroovyFile(classname, resource);
                        log.debug("Groovy file contained definition for class: " + clazz.getName());
                    }
                    catch (Exception e)
                    {
                        log.error("Unable to load groovyy file: " + resource, e);
                    }                
                }
            });
        }

        log.info("Loading groovy file from " + resource);
        return loadGroovyFile(classname, resource);
    }
}
