/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax.json;

import java.io.IOException;

import org.apache.wicket.util.io.StringBufferWriter;

import com.sun.istack.internal.Nullable;

/**
 * An efficient implementation of a JSON stringer. The efficiency comes from the fact that istead of
 * a {@link String} this class can return a {@link CharSequence}. This is better for downstream
 * method that can accept a {@link CharSequence} becuase it requires one less memory copy of the
 * internal {@link AppendingStringBufferWriter} to a {@link String} to get the JSON.
 * 
 * @author igor
 */
public class JSONSequenceStringer extends JSONWriter
{
	public JSONSequenceStringer()
	{
		super(new StringBufferWriter());
	}

	/**
	 * @return JSON text as a {@link CharSequence}
	 */
	@Nullable
	public CharSequence toCharSequence()
	{

		if (mode != 'd')
			return null;

		try
		{
			writer.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return ((StringBufferWriter)writer).getStringBuffer();
	}
}
