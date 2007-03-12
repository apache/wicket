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
package wicket.markup.html;

/**
 * WebMarkupContainers that implement this know how to provide header parts for
 * wicket:head fragments.
 * 
 * @author eelcohillenius
 */
public interface IHeaderPartContainerProvider
{
	/**
	 * Create a new HeaderPartContainer. Users may wish to do that to
	 * implemented more sophisticated header scoping stragegies.
	 * 
	 * @param id
	 *            The header component's id
	 * @param scope
	 *            The default scope of the header
	 * @return The new HeaderPartContainer
	 */
	HeaderPartContainer newHeaderPartContainer(String id, String scope);
}