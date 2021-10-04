/*
 * aoserv-credit-cards - Stores credit card processing data in the AOServ Platform.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2018, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-credit-cards.
 *
 * aoserv-credit-cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-credit-cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-credit-cards.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoapps.lang.exception.WrappedException;
import com.aoindustries.aoserv.client.account.Account;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Uses a {@link Account} as a Java {@link Group}.
 *
 * @author  AO Industries, Inc.
 */
public final class AccountGroup implements Group {

	private final Account account;
	private final String groupName;

	public AccountGroup(Account account, String groupName) {
		this.account = account;
		this.groupName = groupName;
	}

	@Override
	public boolean equals(Object O) {
		if(O==null || !(O instanceof AccountGroup)) return false;
		AccountGroup other = (AccountGroup)O;
		if(!account.equals(other.account)) return false;
		if(groupName==null) return other.groupName==null;
		else return groupName.equals(other.groupName);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return account.hashCode()+(groupName==null ? 0 : (groupName.hashCode()*37));
	}

	/**
	 * Gets the group name.
	 */
	@Override
	public String getName() {
		return groupName;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public boolean addMember(Principal user) {
		throw new RuntimeException("Not allowed to modify group membership through this interface.");
	}

	/**
	 * Not implemented.
	 */
	@Override
	public boolean removeMember(Principal user) {
		throw new RuntimeException("Not allowed to modify group membership through this interface.");
	}

	/**
	 * Returns true of user is an {@link AOServConnectorPrincipal} whose effective administrator is
	 * either this account or a parent account.
	 */
	@Override
	public boolean isMember(Principal user) {
		try {
			if(user instanceof AOServConnectorPrincipal) {
				AOServConnectorPrincipal acp = (AOServConnectorPrincipal)user;
				return acp.getAOServConnector().getCurrentAdministrator().getUsername().getPackage().getAccount().isAccountOrParentOf(account);
			}
			return false;
		} catch(IOException | SQLException err) {
			throw new WrappedException(err);
		}
	}

	/**
	 * Not implemented.
	 */
	@Override
	public Enumeration<? extends Principal> members() {
		throw new RuntimeException("Method not implemented");
	}

	/**
	 * Gets the account.
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Gets the application-provided group name.
	 */
	public String getGroupName() {
		return groupName;
	}
}
