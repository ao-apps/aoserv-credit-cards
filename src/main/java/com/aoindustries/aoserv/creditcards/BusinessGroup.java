/*
 * Copyright 2007-2009, 2016, 2018 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.aoserv.client.account.Business;
import com.aoindustries.util.WrappedException;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Uses a <code>Business</code> as a Java <code>Group</code>.
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessGroup implements Group {

	final private Business business;
	final private String groupName;

	public BusinessGroup(Business business, String groupName) {
		this.business = business;
		this.groupName = groupName;
	}

	@Override
	public boolean equals(Object O) {
		if(O==null || !(O instanceof BusinessGroup)) return false;
		BusinessGroup other = (BusinessGroup)O;
		if(!business.equals(other.business)) return false;
		if(groupName==null) return other.groupName==null;
		else return groupName.equals(other.groupName);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return business.hashCode()+(groupName==null ? 0 : (groupName.hashCode()*37));
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
	 * Returns true of user is a AOServConnectorPrincipal whose effective business administrator is
	 * either this business or a parent business.
	 */
	@Override
	public boolean isMember(Principal user) {
		try {
			if(user instanceof AOServConnectorPrincipal) {
				AOServConnectorPrincipal acp = (AOServConnectorPrincipal)user;
				return acp.getAOServConnector().getThisBusinessAdministrator().getUsername().getPackage().getBusiness().isBusinessOrParentOf(business);
			}
			return false;
		} catch(IOException err) {
			throw new WrappedException(err);
		} catch(SQLException err) {
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
	 * Gets the business.
	 */
	public Business getBusiness() {
		return business;
	}

	/**
	 * Gets the application-provided group name.
	 */
	public String getGroupName() {
		return groupName;
	}
}
