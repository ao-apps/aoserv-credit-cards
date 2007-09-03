package com.aoindustries.aoserv.creditcards;
/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.creditcards.CreditCard;
import java.util.Locale;

/**
 * Creates instances of <code>CreditCard</code>s based on the AOServ object.
 *
 * @author  AO Industries, Inc.
 */
public class CreditCardFactory {

    /**
     * Creates processor CreditCard beans from AOServ CreditCards.
     */
    public static CreditCard getCreditCard(com.aoindustries.aoserv.client.CreditCard creditCard, Locale userLocale) {
        return new CreditCard(
            userLocale,
            Integer.toString(creditCard.getPKey()),
            creditCard.getPrincipalName(),
            creditCard.getGroupName(),
            creditCard.getCreditCardProcessor().getProviderId(),
            creditCard.getProviderUniqueId(),
            null,
            creditCard.getCardInfo(),
            (byte)-1,
            (short)-1,
            null,
            creditCard.getFirstName(),
            creditCard.getLastName(),
            creditCard.getCompanyName(),
            creditCard.getEmail(),
            creditCard.getPhone(),
            creditCard.getFax(),
            null,
            creditCard.getCustomerTaxId(),
            creditCard.getStreetAddress1(),
            creditCard.getStreetAddress2(),
            creditCard.getCity(),
            creditCard.getState(),
            creditCard.getPostalCode(),
            creditCard.getCountryCode().getCode(),
            creditCard.getDescription()
        );
    }

    private CreditCardFactory() {
        // Make no instances
    }
}
