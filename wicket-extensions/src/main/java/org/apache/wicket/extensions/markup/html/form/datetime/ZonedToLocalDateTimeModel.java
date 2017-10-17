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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.util.lang.Args;

/**
 * Model mapping {@link ZonedDateTime} to a {@link LocalDateTime} in {@link #getClientTimeZone()}.
 * 
 * @author svenmeier
 */
public class ZonedToLocalDateTimeModel implements IModel<LocalDateTime>
{
	private static final long serialVersionUID = 1L;
	private IModel<ZonedDateTime> model;

	/**
	 * Map the given {@link ZonedDateTime} to a {@link LocalDateTime} in the client's time zone.
	 *  
	 * @param model zoned date time
	 */
	public ZonedToLocalDateTimeModel(IModel<ZonedDateTime> model)
	{
		Args.notNull(model, "model");
		
		this.model = model;
	}

	@Override
	public void detach()
	{
		model.detach();
	}

	/**
	 * What is the {@link ZoneId} of the client.
	 * 
	 * @see RequestCycleSettings#getGatherExtendedBrowserInfo()
	 * @see ZoneId#systemDefault()
	 */
	protected ZoneId getClientTimeZone()
	{
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo)
		{
			TimeZone timeZone = ((WebClientInfo)info).getProperties().getTimeZone();
			return timeZone != null ? timeZone.toZoneId() : null;
		}
		return ZoneId.systemDefault();
	}

	/**
	 * What is the {@link ZoneId} of created {@link ZonedDateTime} objects. 
	 */
	protected ZoneId getTargetTimeZone()
	{
		return ZoneId.systemDefault();
	}

	@Override
	public LocalDateTime getObject()
	{
		ZonedDateTime zonedDateTime = model.getObject();
		if (zonedDateTime == null)
		{
			return null;
		}
		else
		{
			return zonedDateTime.withZoneSameInstant(getClientTimeZone()).toLocalDateTime();
		}
	}

	@Override
	public void setObject(LocalDateTime dateTime)
	{
		if (dateTime == null)
		{
			model.setObject(null);
		}
		else
		{
			model.setObject(dateTime.atZone(getClientTimeZone()).withZoneSameInstant(getTargetTimeZone()));
		}
	}
}