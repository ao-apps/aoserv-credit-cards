package com.aoindustries.aoserv.creditcards;

/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.Business;
import com.aoindustries.aoserv.client.CountryCode;
import com.aoindustries.aoserv.client.CreditCardProcessor;
import com.aoindustries.creditcards.CreditCard;
import com.aoindustries.creditcards.PersistenceMechanism;
import com.aoindustries.creditcards.Transaction;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Stores the information in the AOServ system.  The principal sent in to the
 * methods should be an instance of <code>AOServConnectorPrincipal</code> and
 * any group should be a <code>BusinessGroup</code>.
 *
 * All operations will be performed using the connector from the principal,
 * therefore the underlying AOServ security model will apply to these calls.
 *
 * @author  AO Industries, Inc.
 */
public class AOServPersistenceMechanism implements PersistenceMechanism {

    private static final AOServPersistenceMechanism instance = new AOServPersistenceMechanism();

    /**
     * Only one instance is necessary since all calls are on the method
     * parameter objects.
     */
    public static AOServPersistenceMechanism getInstance() {
        return instance;
    }

    private AOServPersistenceMechanism() {
    }

    private static AOServConnector getAOServConnector(Principal principal) throws SQLException {
        if(principal==null) throw new SQLException("principal is null");
        if(!(principal instanceof AOServConnectorPrincipal)) throw new SQLException("principal is not a AOServConnectorPrincipal: "+principal.getName());
        return ((AOServConnectorPrincipal)principal).getAOServConnector();
    }

    private static Business getBusiness(Group group) throws SQLException {
        if(group==null) throw new SQLException("group is null");
        if(!(group instanceof BusinessGroup)) throw new SQLException("group is not a BusinessGroup: "+group.getName());
        return ((BusinessGroup)group).getBusiness();
    }

    public String storeCreditCard(Principal principal, CreditCard creditCard, Locale userLocale) throws SQLException {
        AOServConnector conn = getAOServConnector(principal);
        Business business = conn.businesses.get(creditCard.getGroupName());
        if(business==null) throw new SQLException("Unable to find Business: "+creditCard.getGroupName());
        CreditCardProcessor processor = conn.creditCardProcessors.get(creditCard.getProviderId());
        if(processor==null) throw new SQLException("Unable to find CreditCardProcessor: "+creditCard.getProviderId());
        CountryCode countryCode = conn.countryCodes.get(creditCard.getCountryCode());
        if(countryCode==null) throw new SQLException("Unable to find CountryCode: "+creditCard.getCountryCode());
        int pkey = business.addCreditCard(
            creditCard.getMaskedCardNumber(),
            processor,
            creditCard.getProviderUniqueId(),
            creditCard.getFirstName(),
            creditCard.getLastName(),
            creditCard.getCompanyName(),
            creditCard.getEmail(),
            creditCard.getPhone(),
            creditCard.getFax(),
            creditCard.getCustomerTaxId(),
            creditCard.getStreetAddress1(),
            creditCard.getStreetAddress2(),
            creditCard.getCity(),
            creditCard.getState(),
            creditCard.getPostalCode(),
            countryCode,
            creditCard.getComments()
        );
        return Integer.toString(pkey);
    }

    public void updateMaskedCardNumber(Principal principal, CreditCard creditCard, String maskedCardNumber, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            int pkey = Integer.parseInt(creditCard.getPersistenceUniqueId());
            com.aoindustries.aoserv.client.CreditCard aoservCreditCard = conn.creditCards.get(pkey);
            if(aoservCreditCard==null) throw new SQLException("Unable to find CreditCard: "+pkey);
            aoservCreditCard.updateCardInfo(maskedCardNumber);
        } catch(NumberFormatException err) {
            SQLException sqlErr = new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId());
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    public void deleteCreditCard(Principal principal, CreditCard creditCard, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            int pkey = Integer.parseInt(creditCard.getPersistenceUniqueId());
            com.aoindustries.aoserv.client.CreditCard aoservCreditCard = conn.creditCards.get(pkey);
            if(aoservCreditCard==null) throw new SQLException("Unable to find CreditCard: "+pkey);
            aoservCreditCard.remove();
        } catch(NumberFormatException err) {
            SQLException sqlErr = new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId());
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    public String insertTransaction(Principal principal, Group group, Transaction transaction, Locale userLocale) throws SQLException {
        AOServConnector conn = getAOServConnector(principal);
        Business business = getBusiness(group);

        throw new RuntimeException("TODO: Implement method");
    }

    public void updateTransaction(Principal principal, Transaction transaction, Locale userLocale) throws SQLException {
        AOServConnector conn = getAOServConnector(principal);

        throw new RuntimeException("TODO: Implement method");
    }
}
