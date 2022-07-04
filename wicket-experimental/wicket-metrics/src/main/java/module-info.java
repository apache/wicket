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

module org.apache.wicket.metrics {
    requires org.apache.wicket.core;
    requires org.aspectj.runtime;
    requires com.codahale.metrics;
    requires com.codahale.metrics.jmx;
    requires static jakarta.servlet;

    exports org.apache.wicket.metrics;
    exports org.apache.wicket.metrics.aspects;
    exports org.apache.wicket.metrics.aspects.ajax;
    exports org.apache.wicket.metrics.aspects.behavior;
    exports org.apache.wicket.metrics.aspects.component;
    exports org.apache.wicket.metrics.aspects.markup;
    exports org.apache.wicket.metrics.aspects.model;
    exports org.apache.wicket.metrics.aspects.request;
    exports org.apache.wicket.metrics.aspects.requesthandler;
    exports org.apache.wicket.metrics.aspects.resource;
    exports org.apache.wicket.metrics.aspects.session;
}
