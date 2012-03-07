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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.request.mapper.ParentPathReferenceRewriter;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.util.IProvider;

/**
 * Generic {@link ResourceReference} encoder that encodes and decodes non-mounted
 * {@link ResourceReference}s.
 * <p>
 * Decodes and encodes the following URLs:
 *
 * <pre>
 *    /wicket/resource/org.apache.wicket.ResourceScope/name
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?en
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?-style
 *    /wicket/resource/org.apache.wicket.ResourceScope/resource/name.xyz?en_EN-style
 * </pre>
 *
 * @author igor.vaynberg
 */
public class ResourceReferenceMapper extends ParentPathReferenceRewriter
{
	/**
	 * Construct.
	 *
	 * @param pageParametersEncoder
	 * @param parentPathPartEscapeSequence
	 * @param cachingStrategy
	 */
	public ResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
		IProvider<String> parentPathPartEscapeSequence,
		IProvider<IResourceCachingStrategy> cachingStrategy)
	{
		super(new BasicResourceReferenceMapper(pageParametersEncoder, cachingStrategy),
			parentPathPartEscapeSequence);
	}
}
