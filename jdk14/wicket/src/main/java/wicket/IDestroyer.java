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
package wicket;

/**
 * Optional interface for {@link IInitializer initializers} that can clean up
 * stuff initializers created. Initializers simple have to implement this
 * interface and do their thing in {@link #destroy(Application)}.
 * <p>
 * Destroyers can be used to cleanup code when the application unloads. It only
 * guarantees a best effort of cleaning up. Typically, for web applications,
 * this is called when the Wicket servlet/ filter is unloaded by the container,
 * which may depend on the container's implementation and things like the time
 * out period it uses and whether all threads of the web app were cleared.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IDestroyer
{
	/**
	 * @param application
	 *            The application loading the component
	 */
	void destroy(Application application);
}
