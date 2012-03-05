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
package org.apache.wicket.serialize;

/**
 * A serializer that can be used to convert an object to byte array and back
 */
public interface ISerializer
{

	/**
	 * Converts the object to byte array
	 *
	 * @param object
	 *            the object to serialize
	 * @return the serialized page as byte array
	 */
	byte[] serialize(Object object);

	/**
	 * Reconstructs the object from its serialized state
	 *
	 * @param data
	 *            the serialized state of the object
	 * @return the object reconstructed from its serialized state
	 */
	Object deserialize(byte[] data);
}
