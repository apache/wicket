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
package org.apache.wicket.core.util.resource.locator.caching;

import org.apache.wicket.util.resource.IResourceStream;

/**
 * A singleton reference that is used for resource streams which do not exists. I.e. if there is a
 * key in the cache which value is NullResourceStreamReference.INSTANCE then there is no need to
 * lookup again for this key anymore.
 */
class NullResourceStreamReference implements IResourceStreamReference
{
	final static NullResourceStreamReference INSTANCE = new NullResourceStreamReference();

	@Override
	public IResourceStream getReference()
	{
		return null;
	}
}