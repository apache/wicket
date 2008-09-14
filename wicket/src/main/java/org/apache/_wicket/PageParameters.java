package org.apache._wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache._wicket.request.RequestHandlerEncoder;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;

/**
 * Mutable class that holds parameters of a Page. Page parameters consist of indexed parameters and
 * named parameters. Indexed parameters are URL segments before the query string. Named parameters
 * are usually represented as query string params (i.e. ?arg1=var1&amp;arg2=val)
 * <p>
 * How those parameters are populated depends on the {@link RequestHandlerEncoder}s
 * 
 * @author Matej Knopp
 */
public class PageParameters implements Serializable
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public PageParameters()
	{

	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 */
	public PageParameters(PageParameters copy)
	{
		if (copy == null)
		{
			throw new IllegalArgumentException("Copy argument may not be null.");
		}
		if (copy.indexedParameters != null)
			this.indexedParameters = new ArrayList<String>(copy.indexedParameters);

		if (copy.namedParameters != null)
			this.namedParameters = new ArrayList<Entry>(copy.namedParameters);
	}

	private List<String> indexedParameters = null;

	private static class Entry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String key;
		private String value;
	};

	private List<Entry> namedParameters = null;

	/**
	 * @return count of indexed parameters
	 */
	public int getIndexedParamsCount()
	{
		return indexedParameters != null ? indexedParameters.size() : 0;
	}

	/**
	 * Sets the indexed parameter on given index
	 * 
	 * @param index
	 * @param object
	 */
	public void setIndexedParameter(int index, Object object)
	{
		if (indexedParameters == null)
			indexedParameters = new ArrayList<String>(index);

		for (int i = indexedParameters.size(); i <= index; ++i)
		{
			indexedParameters.add(null);
		}

		indexedParameters.set(index, object != null ? object.toString() : null);
	}

	/**
	 * @param index
	 * @return indexed parameter on given index
	 */
	public StringValue getIndexedParameter(int index)
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

	/**
	 * Removes indexed parameter on given index
	 * 
	 * @param index
	 */
	public void removeIndexedParameter(int index)
	{
		if (indexedParameters != null)
		{
			if (index >= 0 && index < indexedParameters.size())
			{
				indexedParameters.remove(index);
			}
		}
	}

	/**
	 * Return set of all named parameter names.
	 * 
	 * @return named parameter names
	 */
	public Set<String> getNamedParameterKeys()
	{
		if (namedParameters == null || namedParameters.isEmpty())
		{
			return Collections.emptySet();
		}
		Set<String> set = new TreeSet<String>();
		for (Entry entry : namedParameters)
		{
			set.add(entry.key);
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Returns parameter value of named parameter with given name
	 * 
	 * @param name
	 * @return parameter value
	 */
	public StringValue getNamedParameter(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Parameter name may not be null.");
		}
		if (namedParameters != null)
		{
			for (Entry entry : namedParameters)
			{
				if (entry.key.equals(name))
				{
					return StringValue.valueOf(entry.value);
				}
			}
		}
		return StringValue.valueOf((String)null);
	}

	/**
	 * Return list of all values for named parameter with given name
	 * 
	 * @param name
	 * @return list of parameter values
	 */
	public List<StringValue> getNamedParameters(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Parameter name may not be null.");
		}
		if (namedParameters != null)
		{
			List<StringValue> result = new ArrayList<StringValue>();
			for (Entry entry : namedParameters)
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

	/**
	 * Removes named parameter with given name.
	 * 
	 * @param name
	 */
	public void removeNamedParameter(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Parameter name may not be null.");
		}
		if (namedParameters != null)
		{
			for (Iterator<Entry> i = namedParameters.iterator(); i.hasNext();)
			{
				Entry e = i.next();
				if (e.key.equals(name))
				{
					i.remove();
				}
			}
		}
	}

	/**
	 * Adds value to named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void addNamedParameter(String name, Object value)
	{
		addNamedParameter(name, value, -1);
	}

	/**
	 * Adds named parameter to a specified position. The {@link RequestHandlerEncoder}s may or may
	 * not take the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 */
	public void addNamedParameter(String name, Object value, int index)
	{

		if (name == null)
		{
			throw new IllegalArgumentException("Parameter name may not be null.");
		}

		if (value == null)
		{
			throw new IllegalArgumentException("Parameter value may not be null.");
		}

		if (namedParameters == null)
			namedParameters = new ArrayList<Entry>(1);
		Entry entry = new Entry();
		entry.key = name;
		entry.value = value.toString();

		if (index == -1)
			namedParameters.add(entry);
		else
			namedParameters.add(index, entry);
	}

	/**
	 * Sets the named parameter on specified position. The {@link RequestHandlerEncoder}s may or
	 * may not take the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 */
	public void setNamedParameter(String name, Object value, int index)
	{
		removeNamedParameter(name);

		if (value != null)
		{
			addNamedParameter(name, value);
		}
	}

	/**
	 * Sets the value for named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void setNamedParameter(String name, Object value)
	{
		setNamedParameter(name, value, -1);
	}

	/**
	 * Removes all indexed parameters.
	 */
	public void clearIndexedParameters()
	{
		this.indexedParameters = null;
	}

	/**
	 * Removes all named parameters.
	 */
	public void clearNamedParameters()
	{
		this.namedParameters = null;
	}

	/**
	 * Copy the paga parameters
	 * 
	 * @param other
	 */
	public void assign(PageParameters other)
	{
		if (this != other)
		{
			this.indexedParameters = other.indexedParameters;
			this.namedParameters = other.namedParameters;
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

		if (namedParameters == null || rhs.namedParameters == null)
		{
			return rhs.namedParameters == namedParameters;
		}

		if (namedParameters.size() != rhs.namedParameters.size())
		{
			return false;
		}

		for (String key : getNamedParameterKeys())
		{
			List<StringValue> values1 = getNamedParameters(key);
			Set<String> v1 = new TreeSet<String>();
			List<StringValue> values2 = rhs.getNamedParameters(key);
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

	/**
	 * Compares two {@link PageParameters} objects.
	 * 
	 * @param p1
	 * @param p2
	 * @return <code>true</code> if the objects are equal, <code>false</code> otherwise.
	 */
	public static boolean equals(PageParameters p1, PageParameters p2)
	{
		if (Objects.equal(p1, p2))
		{
			return true;
		}
		if (p1 == null && p2.getIndexedParamsCount() == 0 && p2.getNamedParameterKeys().isEmpty())
		{
			return true;
		}
		if (p2 == null && p1.getIndexedParamsCount() == 0 && p1.getNamedParameterKeys().isEmpty())
		{
			return true;
		}
		return false;
	}
}
