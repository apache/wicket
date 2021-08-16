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

import java.io.Serializable;

public class MultipartFormComponentListenerBean implements Serializable {
	private String textField;
	private String dropDown;

	/**
	 * @return the textField
	 */
	public String getTextField() {
		return textField;
	}


	/**
	 * @return the dropDown
	 */
	public String getDropDown() {
		return dropDown;
	}


	/**
	 * @param textField
	 *                     the textField to set
	 */
	public void setTextField(String textField) {
		this.textField = textField;
	}


	/**
	 * @param dropDown
	 *                    the dropDown to set
	 */
	public void setDropDown(String dropDown) {
		this.dropDown = dropDown;
	}

}
