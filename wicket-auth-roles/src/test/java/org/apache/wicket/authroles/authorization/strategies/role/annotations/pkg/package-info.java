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
/**
 * Fixture package for {@link org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRolePackageTest}:
 * every kind of role annotation, declared on the package. Note the order of the declarations: the
 * package annotations precede the package declaration, and the imports they use follow it.
 */
@AuthorizeInstantiation("role1")
@AuthorizeAction(action = "RENDER", roles = "role1")
@AuthorizeResource("role1")
package org.apache.wicket.authroles.authorization.strategies.role.annotations.pkg;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeResource;
