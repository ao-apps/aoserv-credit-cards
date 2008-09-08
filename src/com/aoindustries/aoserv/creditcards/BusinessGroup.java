package com.aoindustries.aoserv.creditcards;
/*
 * Copyright 2007-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
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
    final private String groupName;

    public BusinessGroup(Business business, String groupName) {
        this.business = business;
        this.groupName = groupName;
    }
    
    public boolean equals(Object O) {
        if(O==null || !(O instanceof BusinessGroup)) return false;
        BusinessGroup other = (BusinessGroup)O;
        if(!business.equals(other.business)) return false;
        if(groupName==null) return other.groupName==null;
        else return groupName.equals(other.groupName);
    }
    
    public String toString() {
        return getName();
    }

    public int hashCode() {
        return business.hashCode()+(groupName==null ? 0 : (groupName.hashCode()*37));
    }

    /**
     * Gets the group name.
     */
    public String getName() {
        return groupName;
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
    
    /**
     * Gets the application-provided group name.
     */
    public String getGroupName() {
        return groupName;
    }
}
