/**
 * Copyright (C) 2005, Jonathan W. Locke. All Rights Reserved.
 */

package wicket.extensions.signin;

import wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

/**
 * Signs the user out.
 * 
 * @author Jonathan Locke
 */
@AuthorizeInstantiation("USER")
@SuppressWarnings("serial")
public final class SignOut extends VoicetribeWebPage
{
    /**
     * Construct
     */
    public SignOut()
    {
        new SignOutPanel(this,"signOutPanel");
    }
}
