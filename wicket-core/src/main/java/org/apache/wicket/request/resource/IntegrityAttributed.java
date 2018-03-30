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
package org.apache.wicket.request.resource;

/**
 * Interface for Resources and other objects that can have integrity and
 * crossorigin attributes.
 *
 * See <a href="https://www.w3.org/TR/cors/">https://www.w3.org/TR/cors/</a>.
 * See
 * <a href="https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity">
 * https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity</a>.
 *
 * @author Dieter Tremel
 */
public interface IntegrityAttributed {

    /**
     * Getter for Integrity attribute value.
     *
     * @return Integrity hash or null. Nothing should be rendered if null.
     */
    public String getIntegrity();

    /**
     * Getter for Crossorigin attribute value.
     * Use {@link CrossOrigin#getAttributeValue() } for rendering.
     *
     * @return Integrity hash or null. Nothing should be rendered if null.
     */
    public CrossOrigin getCrossOrigin();
}
