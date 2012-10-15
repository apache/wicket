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
package org.apache.wicket.devutils.inspector;

import org.apache.wicket.Session;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.lang.Bytes;

/**
 * Calculates
 */
public class SessionSizeModel extends AbstractReadOnlyModel<Bytes>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 */
	public SessionSizeModel()
	{
	}

	/**
	 * Constructor that calculates the size of the passed Session.
	 *
	 * @param ignored
	 *      the session which size to measure. Ignored.
	 */
	@Deprecated
	public SessionSizeModel(@SuppressWarnings("unused")Session ignored)
	{
	}

	@Override
	public Bytes getObject()
	{
		Bytes result = null;
		if (Session.exists())
		{
			long sizeOfSession = WicketObjects.sizeof(Session.get());
			if (sizeOfSession > -1)
			{
				result = Bytes.bytes(sizeOfSession);
			}
		}


		return result;
	}
}
