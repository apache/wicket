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
package wicket.examples.util.hibernate;

import javax.servlet.ServletContext;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper class for interpreting url locations.
 *
 * @author Eelco Hillenius
 */
public final class URLHelper
{
    /**
     * Hidden constructor (utility class).
     */
    private URLHelper()
    {
        // no nada
    }

    /**
     * Interprets some absolute URLs as external paths or from classpath.
     *
     * @param path
     *            path to translate
     * @param caller
     *            caller class of method
     * @return URL the converted URL
     * @throws MalformedURLException
     *             when the path does not follow the URL rules
     */
    public static URL convertToURL(String path, Class caller)
        throws MalformedURLException
    {
        return convertToURL(path, caller, null);
    }

    /**
     * Interprets some absolute URLs as external paths, otherwise generates URL appropriate for
     * loading from internal webapp or, servletContext is null, loading from the classpath.
     *
     * @param pathToTranslate
     *            path to translate
     * @param caller
     *            caller of method
     * @param servletContext
     *            servlet context of webapp
     * @throws MalformedURLException
     *             when the path does not follow the URL rules
     * @return the converted URL
     */
    public static URL convertToURL(String pathToTranslate, Class caller,
        ServletContext servletContext)
        throws MalformedURLException
    {
        String path = pathToTranslate;
        URL url = null;

        if (path.startsWith("file:") || path.startsWith("http:")
            || path.startsWith("https:") || path.startsWith("ftp:")
            || path.startsWith("jar:"))
        {
            url = new URL(path);
        }
        else if (servletContext != null)
        {
            url = getServletContextURL(servletContext, path);
        }
        else
        {
            url = getClasspathURL(caller, path);
        }

        return url;
    }

    /**
     * @param servletContext
     *            the servlet context
     * @param pathToTranslate
     *            the path to translate
     * @return URL the URL as a resource from the servlet context
     * @throws MalformedURLException
     *             when the path does not follow the URL rules
     */
    private static URL getServletContextURL(ServletContext servletContext,
        String pathToTranslate)
        throws MalformedURLException
    {
        String path = pathToTranslate;
        URL url;

        // Quick sanity check
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        url = servletContext.getResource(path);

        return url;
    }

    /**
     * @param caller
     *            class of the caller
     * @param path
     *            the path to get the URL for
     * @return the URL as a resource from the classpath
     */
    private static URL getClasspathURL(Class caller, String path)
    {
        URL url;
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();

        if (clsLoader == null)
        {
            if (caller != null)
            {
                url = caller.getResource(path);
            }
            else
            {
                url = ClassLoader.getSystemResource(path);
            }
        }
        else
        {
            url = clsLoader.getResource(path);

            // fallthrough
            if (url == null)
            {
                if (caller != null)
                {
                    url = caller.getResource(path);
                }
                else
                {
                    url = ClassLoader.getSystemResource(path);
                }
            }
        }

        return url;
    }
}
