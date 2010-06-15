package com.aoindustries.aoserv.creditcards;

/*
 * Copyright 2007-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.util.WrappedException;
import java.rmi.RemoteException;
import java.security.Principal;

/**
 * Uses an <code>AOServConnector</code> as a Java <code>Principal</code>.
 *
 * @author  AO Industries, Inc.
 */
final public class AOServConnectorPrincipal implements Principal {

    final private AOServConnector<?,?> conn;
    final private String principalName;

    public AOServConnectorPrincipal(AOServConnector<?,?> conn, String principalName) {
        this.conn = conn;
        this.principalName = principalName;
    }
    
    @Override
    public boolean equals(Object O) {
        try {
            if(O==null || !(O instanceof AOServConnectorPrincipal)) return false;
            AOServConnectorPrincipal other = (AOServConnectorPrincipal)O;
            if(!conn.getThisBusinessAdministrator().equals(other.getAOServConnector().getThisBusinessAdministrator())) return false;
            if(principalName==null) {
                return other.principalName==null;
            } else {
                return principalName.equals(other.principalName);
            }
        } catch(RemoteException err) {
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
            return conn.getThisBusinessAdministrator().hashCode()+(principalName==null ? 0 : (principalName.hashCode()*37));
        } catch(RemoteException err) {
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
    public AOServConnector<?,?> getAOServConnector() {
        return conn;
    }
    
    /**
     * Gets the application-provided principal name.
     */
    public String getPrincipalName() {
        return principalName;
    }
}
