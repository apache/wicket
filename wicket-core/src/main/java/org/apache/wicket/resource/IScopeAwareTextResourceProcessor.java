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
package org.apache.wicket.resource;

/**
 * A {@link org.apache.wicket.resource.ITextResourceCompressor} that receives
 * the scope class and the resource name as a context information that it can
 * use for the processing of the resource
 */
public interface IScopeAwareTextResourceProcessor extends ITextResourceCompressor
{
	/**
	 * Processes/manipulates a text resource.
	 *
	 *
	 * @param input
	 *          The original input to process
	 * @param scope
	 *          The scope class of the package resource
	 * @param name
	 *          The name of the package resource
	 * @return The processed input
	 */
	public String process(String input, Class<?> scope, String name);
}
