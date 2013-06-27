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
package org.apache.wicket.cdi;

/**
 * Marks a component that requires a conversation. This marker is used by the automatic conversation
 * management feature ({@link CdiConfiguration#setAutoConversationManagement(boolean)}) to
 * automatically begin and end conversations based on the presence of these components in the
 * component hierarchy of pages (can be applied to the page itself).
 * 
 * @author igor
 */
public interface ConversationalComponent
{

}
