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
package org.apache.wicket.util.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class Instants 
{
  public static final DateTimeFormatter valueOfFormatter = new DateTimeFormatterBuilder()
      .appendPattern("yyyy.MM.dd-h.mma")
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
      .toFormatter()
      .withZone(ZoneId.of("UTC"));
  
  public static final DateTimeFormatter RFC7231Formatter = DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss O")
      .withZone(ZoneId.of("UTC"));

  public static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.ENGLISH);
  
  public static Instant valueOf(final String dateTime) 
  {
    return valueOfFormatter.parse(dateTime, Instant::from);
  }
  
  public static String toString(final Instant instant) 
  {
    return valueOfFormatter.format(instant);
  }
  
  public static String toRFC7231Format(final Instant instant)
  {
    return RFC7231Formatter.format(instant);
  }
}
