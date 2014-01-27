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
package org.apache.wicket.cdi;

import javax.enterprise.context.ConversationScoped;

import org.apache.wicket.util.io.IClusterable;

/**
 * A bean that can be used to override whether the lifecycle of the conversation
 * should be managed automatically or not. See
 * {@link CdiConfiguration#setAutoConversationManagement(boolean)} for details.
 * 
 * @author igor
 */
@ConversationScoped
public class AutoConversation implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private boolean automatic;

	public AutoConversation()
	{
		automatic = false;
	}

	public void setAutomatic(boolean automatic)
	{
		this.automatic = automatic;
	}

	public boolean isAutomatic()
	{
		return automatic;
	}
}
