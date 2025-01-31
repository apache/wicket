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

module org.apache.wicket.auth.roles {
    requires java.base;
    requires org.apache.wicket.util;
    requires org.apache.wicket.request;
    requires org.apache.wicket.core;
    requires jakarta.annotation;

    exports org.apache.wicket.authroles.authentication;
    exports org.apache.wicket.authroles.authentication.pages;
    exports org.apache.wicket.authroles.authentication.panel;
    exports org.apache.wicket.authroles.authorization.strategies.role;
    exports org.apache.wicket.authroles.authorization.strategies.role.annotations;
    exports org.apache.wicket.authroles.authorization.strategies.role.metadata;

    //opening packages to allow resources access
    opens org.apache.wicket.authroles.authentication.pages;
    opens org.apache.wicket.authroles.authentication.panel;
}
