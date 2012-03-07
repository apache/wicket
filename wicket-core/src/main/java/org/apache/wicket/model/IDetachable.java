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
package org.apache.wicket.model;

import org.apache.wicket.util.io.IClusterable;

/**
 * Interface to logic for detaching transient information from a detachable model. Detachable models
 * are desirable because they reduce the amount of state required by a model, which makes the model
 * cheaper to replicate in a clustered environment.
 * 
 * @author Jonathan Locke
 */
public interface IDetachable extends IClusterable
{
	/**
	 * Detaches model after use. This is generally used to null out transient references that can be
	 * re-attached later.
	 */
	void detach();
}
