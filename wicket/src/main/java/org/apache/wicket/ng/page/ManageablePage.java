package org.apache.wicket.ng.page;

import org.apache.wicket.model.IDetachable;

public interface ManageablePage extends IDetachable
{
    /**
     * Gets whether the page is stateless. Components on stateless page must not render any stateful
     * urls. Stateful urls are urls, which refer to a certain (current) page instance and don't
     * contain enough information to reconstruct page if it's not available (page class).
     * 
     * @return Whether this page is stateless
     */
    // note that this has different semantics than Component#isStateless()
    public boolean isPageStateless();

    /**
     * @return A unique identifier for this page map entry
     */
    public int getPageId();


}
