/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package wicket.markup.html.form;

import javax.servlet.http.Cookie;

/**
 * THIS INTERFACE IS FOR INTERNAL USE ONLY AND IS NOT MEANT TO BE USED BY
 * FRAMEWORK CLIENTS.<p/>
 * 
 * This is an attempt to abstract the implementation details of cookies away.
 * Wicket users (and developer) should not need to care about Cookies. In that
 * context the persister is responsible to store and retrieve FormComponent
 * data.<p/>
 * 
 * Given the interface different means of maitaining the data may be implemented.
 * A default implementation has been provided using Cookies. 
 * 
 * @see <a href="http://java.sun.com/products/servlet/2.2/javadoc/javax/servlet/http/Cookie.html">for more details about Cookies.</a>
 * 
 * @author Juergen Donnerstag
 */
public interface IFormComponentPersistenceManager
{
    /**
	 * Persist the a key/value pair. The key usually the FormComponent's
	 * page relative path and the value the FormComponent' value. All remaining
	 * parameters will be filled with default value.
	 * 
	 * @param name The FormComponent's name
	 * @param value The FormComponent's value
	 * @return The cookie created, based on defaults and the params provided 
     */
    public abstract Cookie save(String name, String value);

    /**
	 * Persist the a key/value pair applying the 'path' provided. 
	 * The key usually the FormComponent's page relative path and 
	 * value the the FormComponent' value. The path provided can be seen
	 * an additional means create unique primary keys to store the data.
	 * 
	 * @param name The FormComponent's name
	 * @param value The FormComponent's value
	 * @param path @see javax.servlet.http.Cookie#setPath(java.lang.String) for details
	 * @return The cookie created, based on defaults and the params provided 
     */
    public abstract Cookie save(String name, String value, String path);

    /**
	 * Retrieve a persisted Cookie by means of its name which in wicket
	 * context by default is the components page relative path   
	 * ( @see wicket.markup.html.form.FormComponent#getPageRelativePath() ).
	 * 
	 * @param name The "primary key" to find the data
	 * @return the cookie (if found), null if not found
     */
    public abstract Cookie retrieve(String name);

    /**
     * Convenience method to retrieve the value of a cookie right away.
     * 
     * @param name The "primary key" to find the data
     * @return The value related to the name (key) or null if a cookie
     *    with the given name was not found
     */
    public abstract String retrieveValue(String name);

    /**
     * Remove the data related to the key 'name'. 
     * 
     * @param name The "primary key" of the data to be deleted
     * @return the cookie that was removed or null if none was found.
     */
    public abstract Cookie remove(String name);
}