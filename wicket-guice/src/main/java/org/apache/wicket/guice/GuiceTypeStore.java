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
package org.apache.wicket.guice;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.IClusterable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

/**
 * This is a holder, of sorts, for a type store of runtime generified types. The reason this exists
 * is because the generic type information gleaned from fields and methods is not Serializable.
 * Unfortunately, to support all of the features of Guice, we MUST store this type information or go
 * about rewriting large portions of it. It's not terribly nasty and it's just holding references,
 * so it shouldn't be too bad. I hate that it sits in the metadata, but this is the only place to
 * shelter the type information from Serialization.
 * 
 * @author jboyens
 */
public class GuiceTypeStore implements IClusterable
{
	private static final long serialVersionUID = 1L;

	public static MetaDataKey TYPESTORE_KEY = new MetaDataKey(GuiceTypeStore.class)
	{
		private static final long serialVersionUID = 1L;
	};

	private final Map<String, Type> typeStore = new HashMap<String, Type>();

	public Type getType(String typeName)
	{
		return typeStore.get(typeName);
	}

	public void setType(String typeName, Type type)
	{
		typeStore.put(typeName, type);
	}
}
