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
package org.apache.wicket.util.resource;

import java.io.OutputStream;

/**
 * Special IResourceStream implementation that a Resource can return when it directly wants to write
 * to an output stream instead of return the {@link IResourceStream#getInputStream()}. That one can
 * return null for a IResourceStreamWriter.
 *
 * This behavior comes in use when the resource is generated on the fly and should be streamed
 * directly to the client so that it won't be buffered completely if the generated resource is
 * really big.
 *
 * @author jcompagner
 */
public interface IResourceStreamWriter extends IResourceStream
{
	/**
	 * Implement this method to write the resource data directly the the given {@link OutputStream}.
	 *
	 * @param output
	 *            The response where the resource can write its content into.
	 */
	void write(OutputStream output);
}
