/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.request.IRequestEncoder;

/**
 * Default implementation of {@link
 * wicket.request.compound.AbstractCompoundRequestCycleProcessor} that expects
 * the delegate strategies to be set once at construction time.
 * <p>
 * This class call the appropriate factory methods to lazily create the
 * strategies if they were not set at construction time. After that the
 * strategies are cached and used for multiple thread access. Hence, if you use
 * this class, your strategies need to be thread safe. If this is not what you
 * want, consider overriding one of the getXXX methods, in which case the newXXX
 * methods will obviously not be used unless you call them directly.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class CompoundRequestCycleProcessor extends AbstractCompoundRequestCycleProcessor
{
	/** the cached strategy for constructing request parameters. */
	private IRequestEncoder requestEncoder;

	/** the cached strategy for the target resolver method. */
	private IRequestTargetResolverStrategy requestTargetResolverStrategy;

	/** the cached strategy for the event processor method. */
	private IEventProcessorStrategy eventProcessorStrategy;

	/** the cached strategy for the response method. */
	private IResponseStrategy responseStrategy;

	/** the cached strategy for the exception response method. */
	private IExceptionResponseStrategy exceptionResponseStrategy;

	/**
	 * Default constructor. If you use this constructor, please note that it
	 * will fall back on calling the factory methods (newXXX), which may or may
	 * not provide a default. Some implementations need to be provided still; if
	 * they are not, an exception will be thrown.
	 */
	public CompoundRequestCycleProcessor()
	{
	}

	/**
	 * Constructor with the only strategy we don't have a default for.
	 * 
	 * @param requestEncoder
	 *            the strategy for constructing request parameters
	 */
	public CompoundRequestCycleProcessor(IRequestEncoder requestEncoder)
	{
		this.requestEncoder = requestEncoder;
	}

	/**
	 * Bulk constructor. Constructs using the given strategies; all of which may
	 * be null in which case the factory methods will be used.
	 * 
	 * @param requestEncoder
	 *            the strategy for constructing request parameters
	 * @param requestTargetResolverStrategy
	 *            the strategy for the target resolver method
	 * @param eventProcessorStrategy
	 *            the strategy for the event processor method
	 * @param responseStrategy
	 *            the strategy for the response method
	 * @param exceptionResponseStrategy
	 *            the strategy for the exception response method
	 */
	public CompoundRequestCycleProcessor(IRequestEncoder requestEncoder,
			IRequestTargetResolverStrategy requestTargetResolverStrategy,
			IEventProcessorStrategy eventProcessorStrategy, IResponseStrategy responseStrategy,
			IExceptionResponseStrategy exceptionResponseStrategy)
	{
		this.requestEncoder = requestEncoder;
		this.requestTargetResolverStrategy = requestTargetResolverStrategy;
		this.eventProcessorStrategy = eventProcessorStrategy;
		this.responseStrategy = responseStrategy;
		this.exceptionResponseStrategy = exceptionResponseStrategy;
	}

	/**
	 * Gets the cached request encoder instance or create one by calling
	 * {@link #newRequestEncoder()}.
	 * 
	 * @see wicket.request.IRequestCycleProcessor#getRequestEncoder()
	 */
	public IRequestEncoder getRequestEncoder()
	{
		// lazily create
		if (requestEncoder == null)
		{
			requestEncoder = newRequestEncoder();
		}

		// still null?
		if (requestEncoder == null)
		{
			throw new IllegalStateException("no requestEncoder set");
		}

		// return the strategy
		return requestEncoder;
	}

	/**
	 * Overridable factory method for creating the request encoder. Called by
	 * {@link #getRequestEncoder()}. <strong>as there is no generic default for
	 * the request encoder, this method throws an exception by default. You
	 * either have to provide an instance as a constructor argument, or override
	 * this method or {@link #getRequestEncoder()}</strong>.
	 * 
	 * @return a new target resolver instance
	 */
	protected IRequestEncoder newRequestEncoder()
	{
		throw new IllegalStateException(
				"there is no default for the request encoder. Please provide your strategy by either "
						+ "providing it as a constructor argument, or by overriding newRequestEncoder "
						+ "or getRequestEncoder");
	}

	/**
	 * Gets the cached target resolver instance or create one by calling
	 * {@link #newRequestTargetResolverStrategy()}.
	 * 
	 * @see wicket.request.compound.AbstractCompoundRequestCycleProcessor#getRequestTargetResolverStrategy()
	 */
	protected IRequestTargetResolverStrategy getRequestTargetResolverStrategy()
	{
		// lazily create
		if (requestTargetResolverStrategy == null)
		{
			requestTargetResolverStrategy = newRequestTargetResolverStrategy();
		}

		// still null?
		if (requestTargetResolverStrategy == null)
		{
			throw new IllegalStateException("no requestTargetResolverStrategy set");
		}

		// return the strategy
		return requestTargetResolverStrategy;
	}

	/**
	 * Overridable factory method for creating the target resolver strategy.
	 * Called by {@link #getRequestTargetResolverStrategy()}.
	 * 
	 * @return a new target resolver instance
	 */
	protected IRequestTargetResolverStrategy newRequestTargetResolverStrategy()
	{
		return new DefaultRequestTargetResolver();
	}

	/**
	 * Gets the cached event processor instance or create one by calling
	 * {@link #newEventProcessorStrategy()}.
	 * 
	 * @see wicket.request.compound.AbstractCompoundRequestCycleProcessor#getEventProcessorStrategy()
	 */
	protected IEventProcessorStrategy getEventProcessorStrategy()
	{
		// lazily create
		if (eventProcessorStrategy == null)
		{
			eventProcessorStrategy = newEventProcessorStrategy();
		}

		// still null?
		if (eventProcessorStrategy == null)
		{
			throw new IllegalStateException("no eventProcessorStrategy set");
		}

		// return the strategy
		return eventProcessorStrategy;
	}

	/**
	 * Overridable factory method for creating the event processor. Called by
	 * {@link #getEventProcessorStrategy()}.
	 * 
	 * @return a new event processor instance
	 */
	protected IEventProcessorStrategy newEventProcessorStrategy()
	{
		return new DefaultEventProcessorStrategy();
	}

	/**
	 * Gets the cached response strategy instance or create one by calling
	 * {@link #newResponseStrategy()}.
	 * 
	 * @see wicket.request.compound.AbstractCompoundRequestCycleProcessor#getResponseStrategy()
	 */
	protected IResponseStrategy getResponseStrategy()
	{
		// lazily create
		if (responseStrategy == null)
		{
			responseStrategy = newResponseStrategy();
		}

		// still null?
		if (responseStrategy == null)
		{
			throw new IllegalStateException("no responseStrategy set");
		}

		// return the strategy
		return responseStrategy;
	}

	/**
	 * Overridable factory method for creating the response strategy. Called by
	 * {@link #getResponseStrategy()}.
	 * 
	 * @return a new response strategy instance
	 */
	protected IResponseStrategy newResponseStrategy()
	{
		return new DefaultResponseProcessor();
	}

	/**
	 * Gets the cached exception response strategy instance or create one by
	 * calling {@link #newExceptionResponseStrategy()}.
	 * 
	 * @see wicket.request.compound.AbstractCompoundRequestCycleProcessor#getExceptionResponseStrategy()
	 */
	protected IExceptionResponseStrategy getExceptionResponseStrategy()
	{
		// lazily create
		if (exceptionResponseStrategy == null)
		{
			exceptionResponseStrategy = newExceptionResponseStrategy();
		}

		// still null?
		if (exceptionResponseStrategy == null)
		{
			throw new IllegalStateException("no exceptionResponseStrategy set");
		}

		// return the strategy
		return exceptionResponseStrategy;
	}

	/**
	 * Overridable factory method for creating the exception response strategy.
	 * Called by {@link #getExceptionResponseStrategy()}.
	 * 
	 * @return a new response strategy instance
	 */
	protected IExceptionResponseStrategy newExceptionResponseStrategy()
	{
		return new DefaultExceptionResponseProcessor();
	}
}
