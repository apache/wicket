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
package wicket.request.target.coding;

/**
 * Temporary interface to make the transition to 2.0, where
 * {@link IRequestTargetUrlCodingStrategy} has a method that reviels the mount
 * path.
 * 
 * @author eelcohillenius
 */
public interface IMountableRequestTargetUrlCodingStrategy extends IRequestTargetUrlCodingStrategy
{

	/**
	 * Returns the path of the url where this request target is mounted on.
	 * 
	 * @return String The path of the url
	 */
	String getMountPath();
}
