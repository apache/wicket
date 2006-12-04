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
package wicket.markup;

/**
 * To be implemented by Components which walks up the component tree until a
 * Page or Panel and tries to find a component with a matching wicket id if the
 * component itself does not contain it.
 * <p>
 * Note: MarkupContainer.isTransparent() in combination with ParentResolver
 * tries to resolve the wicket id with the parent only and shall be used to
 * implement transparent containers.
 * 
 * @see wicket.markup.resolver.ScopedComponentResolver
 * @see wicket.MarkupContainer#isTransparentResolver()
 * @see wicket.markup.resolver.ParentResolver
 * 
 * @author Christian Essl
 * @author Juergen Donnerstag
 */
public interface IScopedComponent
{
	/**
	 * @return true, if component shall try its parents to resolve the component
	 *         if the component itself does not contain it.
	 */
	boolean isRenderableInSubContainers();
}
