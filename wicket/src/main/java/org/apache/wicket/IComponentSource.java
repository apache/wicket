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
 * Interface for objects that are capable of reconstructing a component. The component and it's
 * children must be in the exact state as they were before "dehydrating".
 * <p>
 * This is useful for parts of page that are memory heavy but easy to reconstruct. Between requests
 * only the {@link IComponentSource} instance is kept, rather then actual component. The component
 * is then reconstructed on first access
 * <p>
 * This feature is experimental.
 * 
 * @author Matej Knopp
 */
public interface IComponentSource extends IClusterable
{
	/**
	 * This method must reconstruct the component as it was before "dehydrating" it. Also it's
	 * children must be reconstructed
	 * 
	 * @param id
	 * @return reconstructed component
	 */
	public Component< ? > restoreComponent(String id);
}
