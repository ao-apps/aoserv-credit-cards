/*
 * Copyright 2007-2009, 2016, 2019, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.exception.WrappedException;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

/**
 * Uses an {@link AOServConnector} as a Java {@link Principal}.
 *
 * @author  AO Industries, Inc.
 */
final public class AOServConnectorPrincipal implements Principal {

	final private AOServConnector conn;
	final private String principalName;

	public AOServConnectorPrincipal(AOServConnector conn, String principalName) {
		this.conn = conn;
		this.principalName = principalName;
	}

	@Override
	public boolean equals(Object O) {
		try {
			if(O==null || !(O instanceof AOServConnectorPrincipal)) return false;
			AOServConnectorPrincipal other = (AOServConnectorPrincipal)O;
			if(!conn.getCurrentAdministrator().equals(other.getAOServConnector().getCurrentAdministrator())) return false;
			if(principalName==null) {
				return other.principalName==null;
			} else {
				return principalName.equals(other.principalName);
			}
		} catch(IOException | SQLException err) {
			throw new WrappedException(err);
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		try {
			return conn.getCurrentAdministrator().hashCode()+(principalName==null ? 0 : (principalName.hashCode()*37));
		} catch(IOException | SQLException err) {
			throw new WrappedException(err);
		}
	}

	/**
	 * Gets the principal name.
	 */
	@Override
	public String getName() {
		return principalName;
	}

	/**
	 * Gets the connector.
	 */
	public AOServConnector getAOServConnector() {
		return conn;
	}

	/**
	 * Gets the application-provided principal name.
	 */
	public String getPrincipalName() {
		return principalName;
	}
}
