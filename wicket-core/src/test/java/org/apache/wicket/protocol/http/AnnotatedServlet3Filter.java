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
package org.apache.wicket.protocol.http;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;

/**
 * Test filter using Servlet 3.0 initialization.
 */
@WebFilter(value = "/web/*", initParams = {@WebInitParam(name = "applicationClassName",
value = "org.apache.wicket.mock.MockApplication")})
public class AnnotatedServlet3Filter extends WicketFilter
{
}
