/**
 * Copyright (C) 2005, Jonathan W. Locke. All Rights Reserved.
 */

package wicket.extensions.signin;


import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ImageButton;
import wicket.markup.html.form.PasswordTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.markup.html.panel.Panel;
import wicket.model.CompoundPropertyModel;

/**
 * Reusable user sign in panel with username and password as well as support for
 * cookie persistence of the both. When the SignInPanel's form is submitted, the
 * abstract method signIn(String, String) is called, passing the username and
 * password submitted. The signIn() method should sign the user in and return
 * null if no error ocurred, or a descriptive String in the event that the sign
 * in fails.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
@SuppressWarnings("serial")
public class SignInPanel extends Panel
{
    /**
     * Sign in form.
     */
    @SuppressWarnings("serial")
    public final class SignInForm extends Form
    {
        private String password;
        private PasswordTextField passwordTextField;
        private boolean rememberMe;
        private CheckBox rememberMeCheckBox;
        private String username;
        private TextField usernameTextField;

        /**
         * Constructor.
         * 
         * @param id
         *            id of the form component
         * @param includeRememberMe
         *            True to include checkbox
         */
        public SignInForm(MarkupContainer parent,final String id, boolean includeRememberMe)
        {
            super(parent,id);

            setModel(new CompoundPropertyModel(this));

            // Components working on compound property model
            usernameTextField = new TextField(this,idUsername);
            passwordTextField = new PasswordTextField(this,idPassword);
            final WebMarkupContainer rememberMeRow = new WebMarkupContainer(this,"rememberMeRow");
            rememberMeRow.setVisible(includeRememberMe);
            rememberMeCheckBox = new CheckBox(rememberMeRow,idRememberMe);

            // Add validators
            usernameTextField.add(EmailAddressPatternValidator.getInstance());

            // Add components

            // Add button image
            new ImageButton(this,"signInButton", new ResourceReference("signInButton"));
        }

        /**
         * @return Returns the password.
         */
        public String getPassword()
        {
            return password;
        }

        /**
         * @return Returns the rememberMe.
         */
        public boolean getRememberMe()
        {
            return rememberMe;
        }

        /**
         * @return Returns the username.
         */
        public String getUsername()
        {
            return username;
        }

        /**
         * @see wicket.markup.html.form.Form#onSubmit()
         */
        public final void onSubmit()
        {
            signIn.signIn(getPage(), getUsername(), getPassword());
            final CookieManager cookieManager = CookieManager.get();
            if (getRememberMe())
            {
                cookieManager.save(usernameTextField);
                cookieManager.save(passwordTextField);
                cookieManager.save(rememberMeCheckBox);
            }
            else
            {
                cookieManager.clear(usernameTextField);
                cookieManager.clear(passwordTextField);
                cookieManager.clear(rememberMeCheckBox);
            }
        }

        /**
         * @param password
         *            The password to set.
         */
        public void setPassword(String password)
        {
            this.password = password;
        }

        /**
         * @param rememberMe
         *            The rememberMe to set.
         */
        public void setRememberMe(boolean rememberMe)
        {
            this.rememberMe = rememberMe;
        }

        /**
         * @param username
         *            The username to set.
         */
        public void setUsername(String username)
        {
            this.username = username;
        }

        /**
         * @see wicket.Component#onAttach()
         */
        @Override
        protected void onAttach()
        {
            // Load values
            final CookieManager cookieManager = CookieManager.get();
            cookieManager.load(usernameTextField);
            cookieManager.load(passwordTextField);
            cookieManager.load(rememberMeCheckBox);
        }
    }
    
    static final String idPassword = "password";
    static final String idRememberMe = "rememberMe";
    static final String idUsername = "username";

    /**
     * This method is designed to sign the user in during authentication using
     * cookies set by the sign in panel.
     */
    public static boolean signIn(final ISignIn signIn)
    {
        final CookieManager cookieManager = CookieManager.get();
        return signIn.signIn(null, cookieManager.getValue(idUsername), cookieManager
                .getValue(idPassword));
    }

    /**
     * Sign in form for use in sign out panel
     */
    protected final SignInForm signInForm;

    /**
     * Sign in implementation
     */
    private ISignIn signIn;

    /**
     * @see wicket.Component#Component(String)
     */
    public SignInPanel(MarkupContainer parent,final String id, final ISignIn signIn)
    {
        this(parent,id, signIn, false);
    }

    /**
     * @param id
     *            See Component constructor
     * @param signIn
     *            Implementation of sign-in code
     * @param includeRememberMe
     *            True to include remember me functionality using cookie manager
     * @see wicket.Component#Component(String)
     */
    public SignInPanel(MarkupContainer parent,final String id, final ISignIn signIn, final boolean includeRememberMe)
    {
        super(parent,id);
        signInForm = new SignInForm(this,"signInForm", includeRememberMe);
        this.signIn = signIn;
    }
}
