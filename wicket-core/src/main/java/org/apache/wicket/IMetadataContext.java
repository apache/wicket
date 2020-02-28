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
package org.apache.wicket;

/**
 * Used to unify all metadata methods across the various objects.
 * <p>
 * This allows for metadata to be mutated at arms length without dealing with the intricacies of
 * each type that implements it.
 * <p>
 * Due to the inability to refer to implementing types (e.g. Self in Rust) we use the {@code R} parameter
 * to return the type of the implementing object.
 *
 * @param <B>
 *            The base type the metadata object must extend. (e.g. {@link java.io.Serializable})
 * @param <R>
 *            The type of the implementing object.
 * @author Jezza
 * @see Application
 * @see Component
 * @see Session
 * @see org.apache.wicket.request.cycle.RequestCycle
 */
public interface IMetadataContext<B, R extends IMetadataContext<B, R>>
{
	<T extends B> T getMetaData(MetaDataKey<T> key);

	<T extends B> R setMetaData(MetaDataKey<T> key, T data);
}