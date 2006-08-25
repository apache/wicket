/*
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
package wicket.protocol.http.portlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.request.compound.CompoundRequestCycleProcessor;
import wicket.request.compound.IExceptionResponseStrategy;
import wicket.request.compound.IRequestTargetResolverStrategy;
import wicket.request.compound.IResponseStrategy;

/**
 * A RequestCycleProcessor for portlet action requests. The page is not rendered
 * in the action phase. Render state is stored as portlet render parameters in
 * the PortletActionRequestResponseStrategy.
 * 
 * @see PortletActionRequestResponseStrategy
 * @see PortletRequestCycle
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public class PortletActionRequestCycleProcessor extends CompoundRequestCycleProcessor
{
	/** log. */
	private static final Log log = LogFactory.getLog(PortletRenderRequestCycleProcessor.class);

	/**
	 * Construct.
	 */
	public PortletActionRequestCycleProcessor()
	{
		super(new PortletRequestCodingStrategy());
	}
	
	@Override
	protected IRequestTargetResolverStrategy newRequestTargetResolverStrategy()
	{
		return new PortletRequestTargetResolverStrategy();
	}

	@Override
	protected IResponseStrategy newResponseStrategy()
	{
		return new PortletActionRequestResponseStrategy();
	}
	
	@Override
	protected IExceptionResponseStrategy newExceptionResponseStrategy()
	{
		return new PortletExceptionResponseStrategy();
	}	
}
