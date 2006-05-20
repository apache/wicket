/**
 * Copyright (C) 2006, Jonathan W. Locke. All Rights Reserved.
 */
package wicket.extensions.signin;

import wicket.Page;


/**
 * Interface to code that signs a user in with a username and password. This
 * method may be called either from authentication code where there is no page
 * available or from the SignInPanel code which resides in a page. The
 * implementation should be prepared to deal with either case.
 * 
 * @author Jonathan Locke
 */
public interface ISignIn
{
    /**
     * Sign in user if possible.
     * 
     * @param page
     *            The sign in page if there is one, or null if this method is
     *            being called during authentication
     * @param username
     *            The username
     * @param password
     *            The password
     * @return True if signin was successful
     */
    boolean signIn(final Page page, final String username, final String password);
}
