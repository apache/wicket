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
 */
public interface IFormComponentPersistenceManager
{
    /**
     * Convenience method. @see #saveComponent(FormComponent). Fills "missing"
     * parameters with defaults.
     * 
     * @param name The name of the FormCompoennt
     * @param value FormComponent's value
     * @return The cookie created
     */
    public abstract Cookie save(String name, String value);

    /**
     * Convinience method.
     * @param name The name of the FormCompoennt
     * @param value FormComponent's value
     * @param path @see Cookie
     * @return The Cookie created
     */
    public abstract Cookie save(String name, String value, String path);

    /**
     * Retrieve a persisted Cookie by means of its name 
     * (FormComponet.getPageRelativePath())
     * 
     * @param name The "primary key" to find the data
     * @return the cookie (if found), null if not found
     */
    public abstract Cookie retrieve(String name);

    /**
     * Convenience method to retrieve the value of a cookie right away.
     * @param name The "primary key" to find the data
     * @return The value related to the name (key) or null if a cookie
     * with the given name was not found
     */
    public abstract String retrieveValue(String name);

    /**
     * Remove data related to 'name' 
     * 
     * @param name The "primary key" of the data to be deleted
     * @return the cookie that was removed or null if none was found.
     */
    public abstract Cookie remove(String name);
}