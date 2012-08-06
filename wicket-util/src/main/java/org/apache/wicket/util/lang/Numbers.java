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
package org.apache.wicket.util.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.5.8
 */
public final class Numbers
{
	private static final Logger LOG = LoggerFactory.getLogger(Numbers.class);

	/**
	 * Prevent instantiation.
	 */
	private Numbers()
	{
	}

	/**
	 * Returns the minimum value for the numberType's type
	 * 
	 * @param numberType
	 *            the type of the number for which the minimum value will be returned * @return the
	 *            minimum value of the numberType or {@value Double#MIN_VALUE} if the numberType
	 *            itself is either {@code null} or has no minimum value
	 */
	public static Number getMinValue(Class<? extends Number> numberType)
	{
		Number result;
		if (Integer.class == numberType || int.class == numberType)
		{
			result = Integer.MIN_VALUE;
		}
		else if (Long.class == numberType || long.class == numberType)
		{
			result = Long.MIN_VALUE;
		}
		else if (Float.class == numberType || float.class == numberType)
		{
			result = Float.MIN_VALUE;
		}
		else if (Double.class == numberType || double.class == numberType)
		{
			result = Double.MIN_VALUE;
		}
		else if (Byte.class == numberType || byte.class == numberType)
		{
			result = Byte.MIN_VALUE;
		}
		else if (Short.class == numberType || short.class == numberType)
		{
			result = Short.MIN_VALUE;
		}
		else
		{ // null of any other Number
			LOG.debug("'{}' has no minimum value. Falling back to Double.MIN_VALUE.", numberType);
			result = Double.MIN_VALUE;
		}

		return result;
	}


	/**
	 * Returns the maximum value for the numberType's type
	 * 
	 * @param numberType
	 *            the type of the number for which the maximum value will be returned
	 * @return the maximum value of the numberType or {@value Double#MAX_VALUE} if the numberType
	 *         itself is either {@code null} or has no maximum value
	 */
	public static Number getMaxValue(Class<? extends Number> numberType)
	{
		Number result;
		if (Integer.class == numberType || int.class == numberType)
		{
			result = Integer.MAX_VALUE;
		}
		else if (Long.class == numberType || long.class == numberType)
		{
			result = Long.MAX_VALUE;
		}
		else if (Float.class == numberType || float.class == numberType)
		{
			result = Float.MAX_VALUE;
		}
		else if (Double.class == numberType || double.class == numberType)
		{
			result = Double.MAX_VALUE;
		}
		else if (Byte.class == numberType || byte.class == numberType)
		{
			result = Byte.MAX_VALUE;
		}
		else if (Short.class == numberType || short.class == numberType)
		{
			result = Short.MAX_VALUE;
		}
		else
		{ // null of any other Number
			LOG.debug("'{}' has no maximum value. Falling back to Double.MAX_VALUE.");
			result = Double.MAX_VALUE;
		}

		return result;
	}
}
