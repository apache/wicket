/*
 * $Id$ $Revision:
 * 5121 $ $Date$
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
package wicket.examples.images;

import wicket.examples.WicketExampleApplication;
import wicket.protocol.http.request.urlcompressing.URLCompressor;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingCodingStrategy;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingTargetResolverStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.request.compound.CompoundRequestCycleProcessor;

/**
 * WicketServlet class for wicket.examples.linkomatic example.
 * 
 * @author Jonathan Locke
 */
public class ImagesApplication extends WicketExampleApplication
{
	/**
	 * Constructor
	 */
	public ImagesApplication()
	{

	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}

	/**
	 * Special overwrite to have url compressing for this example.
	 * 
	 * @see URLCompressor
	 * @see wicket.protocol.http.WebApplication#newRequestCycleProcessor()
	 */
	protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		return new CompoundRequestCycleProcessor(new WebURLCompressingCodingStrategy(),
				new WebURLCompressingTargetResolverStrategy(), null, null, null);
	}
}
