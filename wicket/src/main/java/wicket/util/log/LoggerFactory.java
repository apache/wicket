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
package wicket.util.log;

import org.slf4j.Logger;

/**
 * Create a Logger object for the class of the calling object. Instead of
 * <code>org.slf4j.LoggerFactory.getLogger(myClass.class)</code> you may now
 * call <code>wicket.util.log.LoggerFactory.make()</code>. No need to
 * specific the class anymore, which avoids copy & paste errors.
 * 
 * @author Heinz M. Kabutz
 */
public class LoggerFactory
{
	/**
	 * Create a Logger object for the class of the calling object
	 * 
	 * @return Logger
	 */
	public static Logger make()
	{
		Throwable t = new Throwable();
		StackTraceElement directCaller = t.getStackTrace()[1];
		return org.slf4j.LoggerFactory.getLogger(directCaller.getClassName());
	}
}
