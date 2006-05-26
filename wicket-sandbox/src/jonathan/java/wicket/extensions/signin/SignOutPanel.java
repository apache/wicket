/**
 * Copyright (C) 2006, Jonathan W. Locke. All Rights Reserved.
 */

package wicket.extensions.signin;

import wicket.MarkupContainer;
import wicket.markup.html.panel.Panel;

/**
 * This invisible panel works with SignInPanel to sign you out.
 * 
 * @author Jonathan Locke
 */
@SuppressWarnings("serial")
public class SignOutPanel extends Panel
{
    /**
     * @param id
     */
    public SignOutPanel(MarkupContainer parent,String id)
    {
        super(parent,id);
    }

    public boolean signIn(String username, String password)
    {
        return false;
    }

    @Override
    protected void onAfterRender()
    {
        CookieManager.get().clearValue(SignInPanel.idUsername);
        CookieManager.get().clearValue(SignInPanel.idPassword);
        CookieManager.get().clearValue(SignInPanel.idRememberMe);
    }

    @Override
    protected void onEndRequest()
    {
        getSession().invalidate();
    }
}
