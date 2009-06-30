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

import java.io.Serializable;

/**
 * An <code>ITimeFrameSource</code> produces <code>TimeFrame</code> values. The value returned by an
 * <code>ITimeFrameSource</code> source may vary over time.
 * 
 * @see TimeFrame
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public interface ITimeFrameSource extends Serializable
{
	/**
	 * Retrieves the current <code>TimeFrame</code> value.
	 * 
	 * @return the current <code>TimeFrame</code> value
	 */
	TimeFrame getTimeFrame();
}
