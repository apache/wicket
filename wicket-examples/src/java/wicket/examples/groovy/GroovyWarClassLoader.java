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

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;

import wicket.util.string.Strings;


/**
 * I had some issues with the GroovyClassLoader (1.0.7-beta) which failed to
 * load a groovy files contained in the same war file. E.g.
 * <pre>
 * wicket-examples.war!/web-inf/classes/groovy/A.groovy
 * wicket-examples.war!/web-inf/classes/groovy/B.groovy

 * public class A {
 *    public A() {
 *       B b = new B()
 *    }
 * }
 * </pre>
 * GroovyClassLoader did not load Groovy class B.
 * 
 * @author Juergen Donnerstag
 */
public class GroovyWarClassLoader extends GroovyClassLoader
{
    /** Logger */
    final private static Log log = LogFactory.getLog(GroovyWarClassLoader.class);

    /**
     * Constructor
     * 
     * @param loader The servlet's class loader
     */
    public GroovyWarClassLoader(ClassLoader loader) {
        super(loader);
    }

    /**
     * First try parent's implementation. If it fails, it'll throw a ClassCastException.
     * Catch it and than apply additional means to load the class.
     * 
     * @param name Class name incl. package
     * @return The Class loaded, if found
     * @throws ClassCastException, if class could not be loaded
     */
    protected Class findGroovyClass(final String name) throws ClassNotFoundException 
    {
        log.debug("class name: " + name);

        try
        {
            return super.findGroovyClass(name);
        }
        catch (ClassNotFoundException ex)
        {
            // classname => filename
            String filename = Strings.replaceAll(name, ".", "/") + ".groovy";
            
            // File exists?
            final URL url = getResource(filename);
            if (url != null)
            {
	            try 
	            {
	                // Get Groovy to parse the file and create the Class
	                final InputStream in = url.openStream();
	                if (in != null)
	                {
	    	            Class clazz = parseClass(in);
	    	            if (clazz != null)
	    	            {
	    	                return clazz;
	    	            }
	                }
	                else
	                {
	                    log.warn("Groovy file not found: " + filename);
	                }
	            } 
	            catch (CompilationFailedException e) 
	            {
	                throw new ClassNotFoundException("Error parsing groovy file: " 
	                        + filename, e);
	            } 
	            catch (IOException e) 
	            {
	                throw new ClassNotFoundException("Error reading groovy file: " 
	                        + filename, e);
	            }
	            catch (Throwable e) 
	            {
	                throw new ClassNotFoundException("Error while reading groovy file: " 
	                        + filename, e);
	            }
            }
            
            throw ex;
        }
    }
}
