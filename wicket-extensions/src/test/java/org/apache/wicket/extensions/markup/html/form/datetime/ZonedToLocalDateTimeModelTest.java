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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ZonedToLocalDateTimeModel}.
 * 
 * @author svenmeier
 */
public class ZonedToLocalDateTimeModelTest
{

	@Test
	public void test() {
		
		ZoneId targetZone = ZoneId.of("UTC");
		ZoneId clientZone = ZoneId.of("UTC+2");
		
		IModel<ZonedDateTime> target = Model.of(ZonedDateTime.of(2000, 6, 1, 5, 0, 0, 0, targetZone));
		
		ZonedToLocalDateTimeModel client = new ZonedToLocalDateTimeModel(target) {
			@Override
			protected ZoneId getTargetTimeZone()
			{
				return targetZone;
			}
			
			@Override
			protected ZoneId getClientTimeZone()
			{
				return clientZone;
			}
		};
		
		assertEquals(LocalDateTime.of(2000, 6, 1, 7, 0, 0, 0), client.getObject());
		
		client.setObject(LocalDateTime.of(2000, 6, 1, 7, 30, 0, 0));
		
		assertEquals(ZonedDateTime.of(2000, 6, 1, 5, 30, 0, 0, targetZone), target.getObject());
	}
}
