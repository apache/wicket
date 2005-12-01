/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.PageParameters;

/**
 * PageParameters encoder that encodes parameters via
 * parameter-name/parameter-value pairs
 * 
 * For example a PageParameters object containing parameters: param1=value1 and
 * param2=value2 will be encoded as /param1/value1/param2/value2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class PairPageParametersEncoder implements IPageParametersEncoder
{

	/**
	 * @see wicket.request.IPageParametersEncoder#encode(wicket.PageParameters)
	 */
	public String encode(PageParameters parameters)
	{
		StringBuffer urlFragment = new StringBuffer();
		if (parameters != null)
		{
			Iterator entries = parameters.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();
				urlFragment.append("/").append(entry.getKey()).append("/").append(entry.getValue());
			}
		}
		return urlFragment.toString();
	}

	/**
	 * @see wicket.request.IPageParametersEncoder#decode(java.lang.String)
	 */
	public PageParameters decode(String urlFragment)
	{
		PageParameters params = new PageParameters();

		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		String[] pairs = urlFragment.split("/");
		// TODO check pairs.length%2==0
		for (int i = 0; i < pairs.length - 1; i += 2)
		{
			params.put(pairs[i], pairs[i + 1]);
		}
		return params;
	}

}
