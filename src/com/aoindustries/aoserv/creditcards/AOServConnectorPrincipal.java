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

    public AOServConnectorPrincipal(AOServConnector conn) {
        this.conn = conn;
    }
    
    public boolean equals(Object O) {
        return
            O!=null
            && (O instanceof AOServConnectorPrincipal)
            && conn.getThisBusinessAdministrator().equals(((AOServConnectorPrincipal)O).getAOServConnector().getThisBusinessAdministrator())
        ;
    }
    
    public String toString() {
        return conn.getThisBusinessAdministrator().toString();
    }

    public int hashCode() {
        return conn.getThisBusinessAdministrator().hashCode();
    }

    /**
     * Gets their effective username.
     */
    public String getName() {
        return conn.getThisBusinessAdministrator().getUsername().getUsername();
    }
    
    /**
     * Gets the connector.
     */
    public AOServConnector getAOServConnector() {
        return conn;
    }
}
