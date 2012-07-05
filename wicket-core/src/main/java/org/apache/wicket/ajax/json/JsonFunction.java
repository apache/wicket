/*
 * Copyright 2012 Igor Vaynberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.wicket.ajax.json;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

/**
 * Represents a Json function. When written out these values are not escaped so its possible to write out raw
 * JavaScript.
 */
public class JsonFunction implements JSONString, CharSequence, IClusterable
{
	private final CharSequence value;

	public JsonFunction(CharSequence value)
	{
		this.value = Args.notNull(value, "value");
	}

	@Override
	public String toString()
	{
		return toJSONString();
	}

	@Override
	public String toJSONString()
	{
		return value.toString();
	}

	@Override
	public int length()
	{
		return value.length();
	}

	@Override
	public char charAt(int index)
	{
		return value.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return value.subSequence(start, end);
	}
}
