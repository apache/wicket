package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.Url;

public class MockRequest extends Request
{
    private final Url url;

    public MockRequest(Url url)
    {
        this.url = url;
    }

    @Override
    public Request requestWithUrl(Url url)
    {
        return new MockRequest(url);
    }

    @Override
    public Url getUrl()
    {
        return url;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MockRequest other = (MockRequest)obj;
        if (url == null)
        {
            if (other.url != null)
                return false;
        }
        else if (!url.equals(other.url))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MockRequest [url=" + url + "]";
    }


}
