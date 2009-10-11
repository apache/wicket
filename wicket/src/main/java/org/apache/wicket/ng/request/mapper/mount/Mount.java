package org.apache.wicket.ng.request.mapper.mount;

import org.apache.wicket.ng.request.Url;

public class Mount
{
    private final Url url;
    private MountParameters mountParameters = new MountParameters();

    public Mount(Url url)
    {
        this.url = url;
    }

    public void setMountParameters(MountParameters mountParameters)
    {
        this.mountParameters = mountParameters;
    }

    public MountParameters getMountParameters()
    {
        return mountParameters;
    }

    public Url getUrl()
    {
        return url;
    }


}
