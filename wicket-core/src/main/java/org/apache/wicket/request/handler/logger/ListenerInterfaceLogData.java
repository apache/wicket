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
package org.apache.wicket.request.handler.logger;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.handler.IPageAndComponentProvider;

/**
 * Contains logging data for component/listener-interface request handlers.
 * 
 * @author Emond Papegaaij
 */
public class ListenerInterfaceLogData extends PageLogData
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends IRequestableComponent> componentClass;
	private final String componentPath;
	private final Integer behaviorIndex;
	private Class<? extends Behavior> behaviorClass;
	private final String interfaceName;
	private final String interfaceMethod;

	/**
	 * Construct.
	 * 
	 * @param pageAndComponentProvider
	 * @param listenerInterface
	 * @param behaviorIndex
	 */
	public ListenerInterfaceLogData(IPageAndComponentProvider pageAndComponentProvider,
		RequestListenerInterface listenerInterface, Integer behaviorIndex)
	{
		super(pageAndComponentProvider);
		componentClass = pageAndComponentProvider.getComponent().getClass();
		componentPath = pageAndComponentProvider.getComponentPath();
		this.behaviorIndex = behaviorIndex;
		if (behaviorIndex != null)
		{
			try
			{
				behaviorClass = pageAndComponentProvider.getComponent()
					.getBehaviorById(behaviorIndex)
					.getClass();
			}
			catch (Exception ignore)
			{
				behaviorClass = null;
			}
		}
		else
		{
			behaviorClass = null;
		}
		interfaceName = listenerInterface.getName();
		interfaceMethod = listenerInterface.getMethod().getName();
	}

	/**
	 * @return componentClass
	 */
	public final Class<? extends IRequestableComponent> getComponentClass()
	{
		return componentClass;
	}

	/**
	 * @return componentPath
	 */
	public final String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return behaviorIndex
	 */
	public final Integer getBehaviorIndex()
	{
		return behaviorIndex;
	}

	/**
	 * @return behaviorClass
	 */
	public final Class<? extends Behavior> getBehaviorClass()
	{
		return behaviorClass;
	}

	/**
	 * @return interfaceName
	 */
	public final String getInterfaceName()
	{
		return interfaceName;
	}

	/**
	 * @return interfaceMethod
	 */
	public final String getInterfaceMethod()
	{
		return interfaceMethod;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.setCharAt(sb.length() - 1, ',');
		sb.append("componentClass=");
		sb.append(getComponentClass().getName());
		sb.append(",componentPath=");
		sb.append(getComponentPath());
		sb.append(",behaviorIndex=");
		sb.append(getBehaviorIndex());
		if (getBehaviorClass() != null)
		{
			sb.append(",behaviorClass=");
			sb.append(getBehaviorClass().getName());
		}
		sb.append(",interfaceName=");
		sb.append(getInterfaceName());
		sb.append(",interfaceMethod=");
		sb.append(getInterfaceMethod());
		sb.append("}");
		return sb.toString();
	}
}
