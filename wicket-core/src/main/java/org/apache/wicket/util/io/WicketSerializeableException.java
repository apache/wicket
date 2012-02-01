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
package org.apache.wicket.util.io;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * @author jcompagner
 * @deprecated Superceded by SerializableChecker.WicketNotSerializableException
 */
@Deprecated
public class WicketSerializeableException extends NotSerializableException
{
	private static final long serialVersionUID = 1L;

	private final List<String> list;

	/**
	 * Construct.
	 * 
	 * @param message
	 */
	public WicketSerializeableException(String message)
	{
		super(message);
		list = new ArrayList<String>();
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 * @param cause
	 */
	public WicketSerializeableException(String message, Throwable cause)
	{
		this(message);
		initCause(cause);
	}

	/**
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage()
	{
		AppendingStringBuffer asb = new AppendingStringBuffer(super.getMessage());
		if (list.size() > 0)
		{
			asb.append("\n");
			for (int i = list.size(); --i >= 0;)
			{
				String element = list.get(i);
				asb.append(element);
				asb.append("->");
			}
			asb.setLength(asb.length() - 2);
		}
		asb.append("\nNOTE: if you feel Wicket is at fault with this exception").append(
			", please report to the mailing list. You can switch to ").append(
			"JDK based serialization by calling: ").append(
			"org.apache.wicket.util.lang.Objects.setObjectStreamFactory(").append(
			"new IObjectStreamFactory.DefaultObjectStreamFactory()) ").append(
			"e.g. in the init method of your application");
		return asb.toString();
	}

	/**
	 * @param traceString
	 */
	public void addTrace(String traceString)
	{
		list.add(traceString);
	}
}
