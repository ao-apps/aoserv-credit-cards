/*
 * Copyright 2007-2009, 2016, 2018, 2019 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.aoserv.client.account.Account;
import com.aoindustries.util.WrappedException;
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
final public class AccountGroup implements Group {

	final private Account account;
	final private String groupName;

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
