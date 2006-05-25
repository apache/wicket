/*
 * $Id: AbstractCompoundRequestCycleProcessor.java,v 1.2 2005/11/25 22:03:33
 * eelco12 Exp $ $Revision$ $Date: 2005-12-19 11:44:19 +0000 (Mon, 19 Dec
 * 2005) $
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
package wicket.request.compound;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.request.IRequestCycleProcessor;
import wicket.request.RequestParameters;

/**
 * A request cycle processor implementatation that delegates to pluggable
 * strategies. This processor is abstract so that it can easily be used as a
 * factory if wanted.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractCompoundRequestCycleProcessor implements IRequestCycleProcessor
{
	/**
	 * Construct.
	 */
	public AbstractCompoundRequestCycleProcessor()
	{
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#resolve(wicket.RequestCycle,
	 *      RequestParameters)
	 */
	public IRequestTarget resolve(RequestCycle requestCycle, RequestParameters requestParameters)
	{
		IRequestTargetResolverStrategy strategy = getRequestTargetResolverStrategy();
		return strategy.resolve(requestCycle, requestParameters);
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#processEvents(wicket.RequestCycle)
	 */
	public void processEvents(RequestCycle requestCycle)
	{
		IEventProcessorStrategy strategy = getEventProcessorStrategy();
		strategy.processEvents(requestCycle);
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		IResponseStrategy strategy = getResponseStrategy();
		strategy.respond(requestCycle);
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#respond(java.lang.RuntimeException,
	 *      wicket.RequestCycle)
	 */
	public void respond(RuntimeException e, RequestCycle requestCycle)
	{
		IExceptionResponseStrategy strategy = getExceptionResponseStrategy();
		strategy.respond(requestCycle, e);
	}

	/**
	 * Gets the strategy for the resolve method.
	 * 
	 * @return the strategy for the resolve method
	 */
	protected abstract IRequestTargetResolverStrategy getRequestTargetResolverStrategy();

	/**
	 * Gets the strategy for the event process method.
	 * 
	 * @return the strategy for the event process method
	 */
	protected abstract IEventProcessorStrategy getEventProcessorStrategy();

	/**
	 * Gets the strategy for the response method.
	 * 
	 * @return the strategy for the response method
	 */
	protected abstract IResponseStrategy getResponseStrategy();

	/**
	 * Gets the strategy for the exception response method.
	 * 
	 * @return the strategy for the exception response method
	 */
	protected abstract IExceptionResponseStrategy getExceptionResponseStrategy();
}
