package com.aoindustries.aoserv.creditcards;
/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.Business;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

/**
 * Uses a <code>Business</code> as a Java <code>Group</code>.
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessGroup implements Group {

    final private Business business;

    public BusinessGroup(Business business) {
        this.business = business;
    }
    
    public boolean equals(Object O) {
        return
            O!=null
            && (O instanceof BusinessGroup)
            && business.equals(((BusinessGroup)O).business)
        ;
    }
    
    public String toString() {
        return business.toString();
    }

    public int hashCode() {
        return business.hashCode();
    }

    /**
     * Gets the business accounting code.
     */
    public String getName() {
        return business.getAccounting();
    }

    /**
     * Not implemented.
     */
    public boolean addMember(Principal user) {
        throw new RuntimeException("Not allowed to modify group membership through this interface.");
    }

    /**
     * Not implemented.
     */
    public boolean removeMember(Principal user) {
        throw new RuntimeException("Not allowed to modify group membership through this interface.");
    }

    /**
     * Returns true of user is a AOServConnectorPrincipal whose effective business administrator is
     * either this business or a parent business.
     */
    public boolean isMember(Principal user) {
        if(user instanceof AOServConnectorPrincipal) {
            AOServConnectorPrincipal acp = (AOServConnectorPrincipal)user;
            return acp.getAOServConnector().getThisBusinessAdministrator().getUsername().getPackage().getBusiness().isBusinessOrParentOf(business);
        }
        return false;
    }

    /**
     * Not implemented.
     */
    public Enumeration<? extends Principal> members() {
        throw new RuntimeException("Method not implemented");
    }

    /**
     * Gets the business.
     */
    public Business getBusiness() {
        return business;
    }
}
