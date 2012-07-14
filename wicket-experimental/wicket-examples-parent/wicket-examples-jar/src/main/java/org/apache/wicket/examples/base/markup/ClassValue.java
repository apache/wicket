package org.apache.wicket.examples.base.markup;

import java.util.LinkedHashSet;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class ClassValue {
	private static final Splitter splitter = Splitter.onPattern("\\s+");
	private final LinkedHashSet<String> values;

	public static ClassValue of(CharSequence value) {
		return new ClassValue(splitter.split(value == null ? "" : value));
	}

	private ClassValue(Iterable<String> values) {
		this.values = Sets.newLinkedHashSet(values);
	}

	public ClassValue without(String clz) {
		values.remove(clz);
		return this;
	}

	public ClassValue with(String clz) {
		values.add(clz);
		return this;
	}

	@Override
	public String toString() {
		return Joiner.on(' ').join(values);
	}
}
