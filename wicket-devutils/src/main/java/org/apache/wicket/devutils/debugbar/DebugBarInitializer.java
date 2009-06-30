package org.apache.wicket.devutils.debugbar;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;

/**
 * Debug bar module initializer
 * 
 * @author igor.vaynberg
 * 
 */
public class DebugBarInitializer implements IInitializer
{

    /** {@inheritDoc} */
    public void init(Application application)
    {
        // register standard debug contributors
        DebugBar.registerContributor(VersionDebugContributor.DEBUG_BAR_CONTRIB, application);
        DebugBar.registerContributor(InspectorDebugPanel.DEBUG_BAR_CONTRIB, application);
        DebugBar.registerContributor(SessionSizeDebugPanel.DEBUG_BAR_CONTRIB, application);
    }

    @Override
    public String toString()
    {
        return "DevUtils DebugBar Initializer";
    }

}
