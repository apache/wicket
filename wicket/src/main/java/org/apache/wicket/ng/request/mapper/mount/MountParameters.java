package org.apache.wicket.ng.request.mapper.mount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ng.util.string.StringValue;

/**
 * Mount parameters
 * 
 * TODO docs and unit test
 * 
 * @author igor.vaynberg
 * 
 */
public class MountParameters
{
    private final Map<String, String> map = new HashMap<String, String>();

    public final StringValue getValue(String parameterName)
    {
        return StringValue.valueOf(map.get(parameterName));
    }

    public final void setValue(String parameterName, StringValue value)
    {
        map.put(parameterName, value.toString());
    }

    public final Collection<String> getParameterNames()
    {
        return map.keySet();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
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
        MountParameters other = (MountParameters)obj;
        if (map == null)
        {
            if (other.map != null)
                return false;
        }
        else if (!map.equals(other.map))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MountParameters [" + map + "]";
    }
    
    
    
    
}
