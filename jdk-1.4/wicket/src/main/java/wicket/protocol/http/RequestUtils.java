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
package wicket.protocol.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import wicket.util.value.ValueMap;

/**
 * TODO
 */
public class RequestUtils
{
	/**
	 * @param queryString
	 * @param params
	 */
	public static void decodeParameters(String queryString, ValueMap params)
	{
		final String[] paramTuples = queryString.split("&");
		for (int t = 0; t < paramTuples.length; t++)
		{
			final String[] bits = paramTuples[t].split("=");
			try
			{
				if (bits.length == 2)
				{
					params.add(URLDecoder.decode(bits[0], "UTF-8"), URLDecoder.decode(bits[1],
							"UTF-8"));
				}
				else
				{
					params.add(URLDecoder.decode(bits[0], "UTF-8"), "");
				}
			}
			catch (UnsupportedEncodingException e)
			{
				// Should never happen
			}
		}
	}
}
