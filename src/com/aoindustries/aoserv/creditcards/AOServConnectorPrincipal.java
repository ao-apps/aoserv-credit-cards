package com.aoindustries.aoserv.creditcards;
/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import java.security.Principal;

/**
 * Uses an <code>AOServConnector</code> as a Java <code>Principal</code>.
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
    
    public boolean equals(Object O) {
        if(O==null || !(O instanceof AOServConnectorPrincipal)) return false;
        AOServConnectorPrincipal other = (AOServConnectorPrincipal)O;
        if(!conn.getThisBusinessAdministrator().equals(other.getAOServConnector().getThisBusinessAdministrator())) return false;
        if(principalName==null) {
            return other.principalName==null;
        } else {
            return principalName.equals(other.principalName);
        }
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return conn.getThisBusinessAdministrator().hashCode()+(principalName==null ? 0 : (principalName.hashCode()*37));
    }

    /**
     * Gets the principal name.
     */
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
