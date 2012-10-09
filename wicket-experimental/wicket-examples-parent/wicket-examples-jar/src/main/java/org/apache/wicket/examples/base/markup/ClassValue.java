package org.apache.wicket.examples.base.markup;

import java.util.LinkedHashSet;

import org.apache.wicket.markup.ComponentTag;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class ClassValue implements CharSequence
{
	private static final Splitter splitter = Splitter.onPattern("\\s+");

	private final LinkedHashSet<String> values;

	public static ClassValue of(ComponentTag tag)
	{
		return of(tag.getAttribute("class"));
	}

	public static ClassValue of(CharSequence value)
	{
		return new ClassValue(splitter.split(value == null ? "" : value));
	}

	private ClassValue(Iterable<String> values)
	{
		this.values = Sets.newLinkedHashSet(values);
		this.values.remove("");
	}

	private String join()
	{
		return Joiner.on(' ').join(values);
	}

	public ClassValue without(String clz)
	{
		values.remove(clz);
		return this;
	}

	public ClassValue with(String clz)
	{
		values.add(clz);
		return this;
	}

	@Override
	public String toString()
	{
		return Joiner.on(' ').join(values);
	}

	@Override
	public int length()
	{
		return toString().length();
	}

	@Override
	public char charAt(int index)
	{
		return toString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return toString().subSequence(start, end);
	}
}
