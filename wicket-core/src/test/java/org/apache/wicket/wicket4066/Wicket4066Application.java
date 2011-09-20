package org.apache.wicket.wicket4066;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see com.mycompany.Start#main(String[])
 */
public class Wicket4066Application extends WebApplication
{       
    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<HomePage> getHomePage()
    {
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init()
    {
        super.init();

        // add your configuration here
    }

    @Override
    public MySession newSession(Request request, Response response)
    {
        return new MySession(request);
    }
}
