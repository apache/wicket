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
import wicket.DefaultClassResolver;
import wicket.IClassResolver;
import wicket.WicketRuntimeException;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.Resource;
import wicket.util.watch.ModificationWatcher;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

/**
 * Extends the default Page Factory to allow for Groovy based classes.
 * Modifications to groovy files are tracked and files are reloaded
 * if modified.
 *  
 * @author Juergen Donnerstag
 */
public class GroovyClassResolver implements IClassResolver
{
    /** Logging */
    private static final Log log = LogFactory.getLog(GroovyClassResolver.class);
    
    /** Caching map of class name to groovy class; not sure if GroovyClassLoader does it as well */
    private final Map classCache = new ConcurrentHashMap();
    
    /** Default class resolver */
    private final IClassResolver defaultClassResolver = new DefaultClassResolver();
    
    /** Application settings */
    private final ApplicationSettings settings;
    
    /**
     * Constructor
     * @param settings Application settings
     */
    public GroovyClassResolver(final ApplicationSettings settings)
    {
        this.settings = settings;
    }
    
    /**
     * Resolve the class for the given classname.  First try standard java 
     * classes, then groovy files. Groovy file name must be &lt;classname&gt;.groovy.
     * 
     * @param classname The object's class name 
     * @return The class
     * @see wicket.IClassResolver#resolveClass(String)
     */
    public Class resolveClass(final String classname)
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
	        catch (WicketRuntimeException ex)
	        {
	            throw new WicketRuntimeException("Unable to load class with name: " + classname, ex);
	        }
        }
        else
        {
            throw new WicketRuntimeException("File not found: " + resource);
        }
        
        throw new WicketRuntimeException("Unable to load class with name: " + classname);
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
			cl = GroovyClassResolver.class.getClassLoader();
		}
		
        final GroovyWarClassLoader groovyClassLoader = new GroovyWarClassLoader(cl);
        Class clazz = null;
        
        try 
        {
            final InputStream in = resource.getInputStream();
            if (in != null)
            {
	            clazz = groovyClassLoader.parseClass(in);
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
            throw new WicketRuntimeException("Error parsing groovy file: " 
                    + resource, e);
        } 
        catch (IOException e) 
        {
            throw new WicketRuntimeException("Error reading groovy file: " 
                    + resource, e);
        }
        catch (Throwable e) 
        {
            throw new WicketRuntimeException("Error while reading groovy file: " 
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
        // Watch file in the future
        final ModificationWatcher watcher = settings.getResourceWatcher();

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
