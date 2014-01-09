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
package org.apache.wicket.examples.wizard;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.io.IClusterable;


/**
 * Domain class for the new user wizard example.
 * 
 * @author Eelco Hillenius
 */
public final class User implements IClusterable
{
	private String department = "";
	private String email;

	private String firstName;
	private String lastName;

	private Set<String> roles = new HashSet<>();

	private String rolesSetName;

	private String userName;

	/**
	 * Gets departement.
	 * 
	 * @return departement
	 */
	public String getDepartment()
	{
		return department;
	}

	/**
	 * Gets email.
	 * 
	 * @return email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * Gets firstName.
	 * 
	 * @return firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * Gets lastName.
	 * 
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Gets roles.
	 * 
	 * @return roles
	 */
	public Set<String> getRoles()
	{
		return roles;
	}

	/**
	 * Gets rolesSetName.
	 * 
	 * @return rolesSetName
	 */
	public String getRolesSetName()
	{
		return rolesSetName;
	}

	/**
	 * Gets userName.
	 * 
	 * @return userName
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * Sets departement.
	 * 
	 * @param departement
	 *            departement
	 */
	public void setDepartment(String departement)
	{
		if (departement == null)
		{
			departement = "";
		}
		department = departement;
	}

	/**
	 * Sets email.
	 * 
	 * @param email
	 *            email
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
	 * Sets firstName.
	 * 
	 * @param firstName
	 *            firstName
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * Sets lastName.
	 * 
	 * @param lastName
	 *            lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * Sets roles.
	 * 
	 * @param roles
	 *            roles
	 */
	public void setRoles(Set<String> roles)
	{
		this.roles = roles;
	}

	/**
	 * Sets rolesSetName.
	 * 
	 * @param rolesSetName
	 *            rolesSetName
	 */
	public void setRolesSetName(String rolesSetName)
	{
		this.rolesSetName = rolesSetName;
	}

	/**
	 * Sets userName.
	 * 
	 * @param userName
	 *            userName
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
}
