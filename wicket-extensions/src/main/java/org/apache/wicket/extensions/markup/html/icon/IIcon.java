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
package org.apache.wicket.extensions.markup.html.icon;

import org.apache.wicket.util.io.IClusterable;

/**
 * An icon, written into the page as markup.
 * <p>
 * The markup is written as is, without escaping, so it has to be authored by the developer and
 * never contain text from users. Wicket ships {@link SvgIcon}, which needs nothing else, and
 * {@link FontAwesomeIcon}, which needs the Font Awesome style sheet and fonts on the page.
 *
 * @since 11.0.0
 */
@FunctionalInterface
public interface IIcon extends IClusterable
{
	/**
	 * @return the markup of the icon, written into the page as is
	 */
	String getMarkup();
}
