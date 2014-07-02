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
package org.apache.wicket.ajax;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

/**
 * A Channel used to define how Ajax requests are processed at the client side.
 * 
 * Channels are either:
 * <ul>
 * <li>queueing - Ajax requests are kept in a Queue at the client side and processed one at a time.
 * Default.</li>
 * <li>dropping - only the last Ajax request is processed, all previously scheduled requests are discarded</li>
 * <li>active - discards any Ajax requests if there is a running Ajax request on the same channel</li>
 * </ul>
 * 
 * @author Martin Dilger
 */
public class AjaxChannel implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The type of an {@link AjaxChannel}
	 */
	public static enum Type {

		/**
		 * Ajax requests are kept in a Queue at the client side and processed one at a time
		 */
		QUEUE,

		/**
		 * dropping - only the last Ajax request is processed, the others are discarded
		 */
		DROP,

		/**
		 * the ajax call will discarded if there is an active/running request on the same channel
		 */
		ACTIVE
	}

	/**
	 * The default channel for all Ajax calls
	 */
	public static final AjaxChannel DEFAULT = new AjaxChannel("0", Type.QUEUE);

	private final String name;

	private final Type type;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name of the channel
	 */
	public AjaxChannel(final String name)
	{
		this(name, Type.QUEUE);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name of the channel
	 * @param type
	 *            the behavior type of this channel
	 */
	public AjaxChannel(final String name, final Type type)
	{
		this.name = Args.notNull(name, "name");
		this.type = Args.notNull(type, "type");
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the type of this channel
	 * @see AjaxChannel.Type
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Calculates the ChannelName.
	 * 
	 * @return a String in the format channelName|d for DropChannels, channelName|s for Stackable
	 *         Channels.
	 */
	String getChannelName()
	{
		return toString();
	}

	@Override
	public String toString()
	{
		return String.format("%s|%s", name, getShortType(type));
	}

	private String getShortType(Type t)
	{
		String shortType;
		switch (t)
		{
			case DROP:
				shortType = "d";
				break;
			case ACTIVE:
				shortType = "a";
				break;
			case QUEUE:
			default:
				// 's' comes from 'stack', but it really acts as a queue.
				shortType = "s";
		}
		return shortType;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AjaxChannel that = (AjaxChannel) o;

		if (!name.equals(that.name)) return false;
		if (type != that.type) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}
}
