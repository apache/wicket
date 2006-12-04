/*
 * $Id: Page2.java 5394 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006) jdonnerstag $
 * $Revision: 5394 $ $Date: 2006-03-14 09:57:54 +0000 (Di, 14 Mrz 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.niceurl;

import wicket.PageParameters;
import wicket.request.target.coding.QueryStringUrlCodingStrategy;

/**
 * Simple bookmarkable page that displays page parameters which is mounted with
 * another parameter encoder.
 * 
 * @see QueryStringUrlCodingStrategy
 * @author Eelco Hillenius
 */
public class Page2QP extends Page2
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page2QP(PageParameters parameters)
	{
		super(parameters);
	}
}
