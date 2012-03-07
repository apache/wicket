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
package org.apache.wicket.util.instrument;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;

import org.apache.wicket.core.util.lang.WicketObjects.IObjectSizeOfStrategy;

/**
 * Object size of strategy that is based on instrumentation.
 * 
 * @author eelcohillenius
 */
public class InstrumentationObjectSizeOfStrategy implements IObjectSizeOfStrategy
{

	/**
	 * Records the size of an object and it's dependents as if they were serialized but using the
	 * instrumentation API to calculate.
	 */
	private final class SizeRecodingOuputStream extends ObjectOutputStream
	{

		private long totalSize = 0;

		/**
		 * Construct.
		 * 
		 * @throws IOException
		 */
		public SizeRecodingOuputStream() throws IOException
		{
			super(new OutputStream()
			{

				@Override
				public void write(int b) throws IOException
				{
				}
			});
			enableReplaceObject(true);
		}

		/**
		 * Gets the calculated size.
		 * 
		 * @return
		 */
		public long getTotalSize()
		{
			return totalSize;
		}

		@Override
		protected Object replaceObject(Object obj) throws IOException
		{

			if (obj != null)
			{
				totalSize += instrumentation.getObjectSize(obj);
			}

			return obj;
		}
	}

	/**
	 * Instrumentation instance.
	 */
	private final Instrumentation instrumentation;

	/**
	 * Construct.
	 * 
	 * @param instrumentation
	 */
	public InstrumentationObjectSizeOfStrategy(Instrumentation instrumentation)
	{
		this.instrumentation = instrumentation;
	}

	/**
	 * Calculates full size of object iterating over its hierarchy graph.
	 * 
	 * @param obj
	 *            object to calculate size of
	 * @return object size
	 * 
	 * @see org.apache.wicket.core.util.lang.WicketObjects.IObjectSizeOfStrategy#sizeOf(java.io.Serializable)
	 */
	public long sizeOf(Serializable obj)
	{
		if (obj == null)
		{
			return 0;
		}
		try
		{
			SizeRecodingOuputStream recorder = new SizeRecodingOuputStream();
			recorder.writeObject(obj);
			return recorder.getTotalSize();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -1;
		}

	}
}
