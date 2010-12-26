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
package org.apache.wicket.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * Object stream factory for Wicket's custom serialization.
 * 
 * @see WicketObjectInputStream
 * @see WicketObjectOutputStream
 * 
 * @author eelcohillenius
 */
public class WicketObjectStreamFactory implements IObjectStreamFactory
{
	/**
	 * @see org.apache.wicket.util.io.IObjectStreamFactory#newObjectInputStream(java.io.InputStream)
	 */
	public ObjectInputStream newObjectInputStream(InputStream in) throws IOException
	{
		return new WicketObjectInputStream(in);
	}

	/**
	 * @see org.apache.wicket.util.io.IObjectStreamFactory#newObjectOutputStream(java.io.OutputStream)
	 */
	public ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
	{
		return new WicketObjectOutputStream(out);
	}
}
