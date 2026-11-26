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

module org.apache.wicket.request {
    requires java.sql;
    requires org.apache.wicket.util;
    requires org.slf4j;
    requires static jakarta.servlet;

    exports org.apache.wicket.request;
    exports org.apache.wicket.request.flow;
    exports org.apache.wicket.request.handler;
    exports org.apache.wicket.request.handler.logger;
    exports org.apache.wicket.request.http;
    exports org.apache.wicket.request.http.flow;
    exports org.apache.wicket.request.http.handler;
    exports org.apache.wicket.request.mapper;
    exports org.apache.wicket.request.mapper.info;
    exports org.apache.wicket.request.mapper.parameter;
    exports org.apache.wicket.request.parameter;
}
