package org.apache._wicket.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;

public class PageParameters implements Serializable
{

	private static final long serialVersionUID = 1L;

	public PageParameters()
    {

    }

    public PageParameters(PageParameters copy)
    {
        if (copy == null)
        {
            throw new IllegalArgumentException("Copy argument may not be null.");
        }
        if (copy.indexedParameters != null)
            this.indexedParameters = new ArrayList<String>(copy.indexedParameters);

        if (copy.queryStringParameters != null)
            this.queryStringParameters = new ArrayList<Entry>(copy.queryStringParameters);
    }

    private List<String> indexedParameters = null;

    private static class Entry implements Serializable
    {
		private static final long serialVersionUID = 1L;
		
		private String key;
        private String value;
    };

    private List<Entry> queryStringParameters = null;

    public int getIndexedParamsCount()
    {
        return indexedParameters != null ? indexedParameters.size() : 0;
    }

    public void setIndexedParam(int index, Object object)
    {
        if (indexedParameters == null)
            indexedParameters = new ArrayList<String>(index);

        for (int i = indexedParameters.size(); i <= index; ++i)
        {
            indexedParameters.add(null);
        }

        indexedParameters.set(index, object != null ? object.toString() : null);
    }

    public StringValue getIndexedParam(int index)
    {
        if (indexedParameters != null)
        {
            if (index >= 0 && index < indexedParameters.size())
            {
                return StringValue.valueOf(indexedParameters.get(index));
            }
        }
        return StringValue.valueOf((String)null);
    };

    public void removeIndexedParam(int index)
    {
        if (indexedParameters != null)
        {
            if (index >= 0 && index < indexedParameters.size())
            {
                indexedParameters.remove(index);
            }
        }
    }

    public Set<String> getQueryParamKeys()
    {
        if (queryStringParameters == null || queryStringParameters.isEmpty())
        {
            return Collections.emptySet();
        }
        Set<String> set = new TreeSet<String>();
        for (Entry entry : queryStringParameters)
        {
            set.add(entry.key);
        }
        return Collections.unmodifiableSet(set);
    }

    public StringValue getQueryParam(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Parameter name may not be null.");
        }
        if (queryStringParameters != null)
        {
            for (Entry entry : queryStringParameters)
            {
                if (entry.key.equals(name))
                {
                    return StringValue.valueOf(entry.value);
                }
            }
        }
        return StringValue.valueOf((String)null);
    }

    public List<StringValue> getQueryParams(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Parameter name may not be null.");
        }
        if (queryStringParameters != null)
        {
            List<StringValue> result = new ArrayList<StringValue>();
            for (Entry entry : queryStringParameters)
            {
                if (entry.key.equals(name))
                {
                    result.add(StringValue.valueOf(entry.value));
                }
            }
            return Collections.unmodifiableList(result);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public void removeQueryParam(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Parameter name may not be null.");
        }
        if (queryStringParameters != null)
        {
            for (Iterator<Entry> i = queryStringParameters.iterator(); i.hasNext();)
            {
                Entry e = i.next();
                if (e.key.equals(name))
                {
                    i.remove();
                }
            }
        }
    }

    public void addQueryParam(String name, Object value)
    {
        addQueryParam(name, value, -1);
    }

    public void addQueryParam(String name, Object value, int index)
    {

        if (name == null)
        {
            throw new IllegalArgumentException("Parameter name may not be null.");
        }

        if (value == null)
        {
            throw new IllegalArgumentException("Parameter value may not be null.");
        }

        if (queryStringParameters == null)
            queryStringParameters = new ArrayList<Entry>(1);
        Entry entry = new Entry();
        entry.key = name;
        entry.value = value.toString();

        if (index == -1)
            queryStringParameters.add(entry);
        else
            queryStringParameters.add(index, entry);
    }

    public void setQueryParam(String name, Object value, int index)
    {
        removeQueryParam(name);

        if (value != null)
        {
            addQueryParam(name, value);
        }
    }

    public void setQueryParam(String name, Object value)
    {
        setQueryParam(name, value, -1);
    }

    public void clearIndexedParams()
    {
        this.indexedParameters = null;
    }

    public void clearQueryParams()
    {
        this.queryStringParameters = null;
    }

    void assign(PageParameters other)
    {
        if (this != other)
        {
            this.indexedParameters = other.indexedParameters;
            this.queryStringParameters = other.queryStringParameters;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj instanceof PageParameters == false)
        {
            return false;
        }

        PageParameters rhs = (PageParameters)obj;
        if (!Objects.equal(indexedParameters, rhs.indexedParameters))
        {
            return false;
        }

        if (queryStringParameters == null || rhs.queryStringParameters == null)
        {
            return rhs.queryStringParameters == queryStringParameters;
        }

        if (queryStringParameters.size() != rhs.queryStringParameters.size())
        {
            return false;
        }

        for (String key : getQueryParamKeys())
        {
            List<StringValue> values1 = getQueryParams(key);
            Set<String> v1 = new TreeSet<String>();
            List<StringValue> values2 = rhs.getQueryParams(key);
            Set<String> v2 = new TreeSet<String>();
            for (StringValue sv : values1)
            {
                v1.add(sv.toString());
            }
            for (StringValue sv : values2)
            {
                v2.add(sv.toString());
            }
            if (v1.equals(v2) == false)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(PageParameters p1, PageParameters p2)
    {
    	if (Objects.equal(p1, p2))
    	{
    		return true;
    	}
    	if (p1 == null && p2.getIndexedParamsCount() == 0 && p2.getQueryParamKeys().isEmpty())
    	{
    		return true;
    	}
    	if (p2 == null && p1.getIndexedParamsCount() == 0 && p1.getQueryParamKeys().isEmpty())
    	{
    		return true;
    	}
    	return false;
    }
}
