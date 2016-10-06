/*
 * Copyright 2007-2012, 2015 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.creditcards.CreditCardProcessor;
import com.aoindustries.creditcards.MerchantServicesProvider;
import com.aoindustries.creditcards.MerchantServicesProviderFactory;
import com.aoindustries.lang.ObjectUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates instances of <code>CreditCardProcessor</code>s based on the processor
 * configurations found in the <code>credit_card_processors</code> table.
 * Will only create once instance of each processor.  Disabled processors
 * will not be returned.  When multiple processors are enabled for a business,
 * adheres to the weight provided by the processors.
 *
 * @author  AO Industries, Inc.
 */
public class CreditCardProcessorFactory {

	private static class ProcessorKey {
		final private String providerId;
		final private String className;
		final private String param1;
		final private String param2;
		final private String param3;
		final private String param4;

		private ProcessorKey(
			String providerId,
			String className,
			String param1,
			String param2,
			String param3,
			String param4
		) {
			this.providerId = providerId;
			this.className = className;
			this.param1 = param1;
			this.param2 = param2;
			this.param3 = param3;
			this.param4 = param4;
		}

		@Override
		public int hashCode() {
			return
				providerId.hashCode()
				+ className.hashCode()*7
				+ (param1==null ? 0 : (param1.hashCode()*17))
				+ (param2==null ? 0 : (param1.hashCode()*37))
				+ (param3==null ? 0 : (param1.hashCode()*103))
				+ (param4==null ? 0 : (param1.hashCode()*149))
			;
		}

		@Override
		public boolean equals(Object O) {
			if(O==null) return false;
			if(!(O instanceof ProcessorKey)) return false;
			ProcessorKey other = (ProcessorKey)O;
			return
				providerId.equals(other.providerId)
				&& className.equals(other.className)
				&& ObjectUtils.equals(param1, other.param1)
				&& ObjectUtils.equals(param2, other.param2)
				&& ObjectUtils.equals(param3, other.param3)
				&& ObjectUtils.equals(param4, other.param4)
			;
		}
	}

	final private static Map<ProcessorKey,CreditCardProcessor> processors = new HashMap<ProcessorKey,CreditCardProcessor>();

	/**
	 * Gets an enabled <code>CreditCardProcessor</code> from the list of processors for the business
	 * of the provided <code>AOServConnector</code>.  When multiple processors are enabled, those with
	 * a higher weight will be returned more often, proportional to weight.  Uses the random source
	 * of the <code>AOServConnector</code> when selecting the processor.<br>
	 * <br>
	 * Only one instance of each unique <code>CreditCardProcessor</code> (unique based on providerId, classname and all parameters) will be created.<br>
	 * <br>
	 * Every processor will use the <code>AOServPersistenceMechanism</code> for its persistence.
	 *
	 * @return  the processor or <code>null</code> if none found
	 */
	public static CreditCardProcessor getCreditCardProcessor(AOServConnector conn) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, SQLException {
		// Select the aoserv-client processor before synchronizing on processors
		List<com.aoindustries.aoserv.client.CreditCardProcessor> ccps = conn.getThisBusinessAdministrator().getUsername().getPackage().getBusiness().getCreditCardProcessors();
		// Count the total weight of enabled processors
		int totalEnabledProcessors = 0;
		com.aoindustries.aoserv.client.CreditCardProcessor firstCCP = null;
		int totalWeight = 0;
		for(com.aoindustries.aoserv.client.CreditCardProcessor ccp : ccps) {
			if(ccp.getEnabled() && ccp.getWeight()>0) {
				totalEnabledProcessors++;
				if(firstCCP==null) firstCCP = ccp;
				totalWeight += ccp.getWeight();
			}
		}
		// No processors ready
		if(totalEnabledProcessors==0) return null;

		// Pick one by weight
		com.aoindustries.aoserv.client.CreditCardProcessor selectedCCP;
		if(totalEnabledProcessors==1) {
			// One processor shortcut
			selectedCCP = firstCCP;
		} else {
			// Pick a random one based on this weight
			selectedCCP = null;
			int randomPosition = AOServConnector.getRandom().nextInt(totalWeight);
			int weightSoFar = 0;
			for(com.aoindustries.aoserv.client.CreditCardProcessor ccp : ccps) {
				if(ccp.getEnabled() && ccp.getWeight()>0) {
					weightSoFar += ccp.getWeight();
					if(weightSoFar>randomPosition) {
						selectedCCP = ccp;
						break;
					}
				}
			}
			if(selectedCCP==null) throw new AssertionError("With proper implementation of weighted random select above, this should not happen");
		}

		return getCreditCardProcessor(selectedCCP);
	}

	/**
	 * Gets the processor for the given AOServ processor.<br>
	 * <br>
	 * Only one instance of each unique <code>CreditCardProcessor</code> (unique based on providerId, classname and all parameters) will be created.<br>
	 * <br>
	 * Every processor will use the <code>AOServPersistenceMechanism</code> for its persistence.
	 *
	 * @see  MerchantServicesProviderFactory#getMerchantServicesProvider
	 */
	public static CreditCardProcessor getCreditCardProcessor(com.aoindustries.aoserv.client.CreditCardProcessor selectedCCP) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
		// The key in the map
		ProcessorKey processorKey = new ProcessorKey(
			selectedCCP.getProviderId(),
			selectedCCP.getClassName(),
			selectedCCP.getParam1(),
			selectedCCP.getParam2(),
			selectedCCP.getParam3(),
			selectedCCP.getParam4()
		);

		// Now synchronize access to processors
		synchronized(processors) {
			// Look for existing instance
			CreditCardProcessor processorInstance = processors.get(processorKey);
			if(processorInstance==null) {
				MerchantServicesProvider provider = MerchantServicesProviderFactory.getMerchantServicesProvider(
					selectedCCP.getProviderId(),
					selectedCCP.getClassName(),
					selectedCCP.getParam1(),
					selectedCCP.getParam2(),
					selectedCCP.getParam3(),
					selectedCCP.getParam4()
				);

				// Create and add to cache
				processorInstance = new CreditCardProcessor(provider, AOServPersistenceMechanism.getInstance());
				processors.put(processorKey, processorInstance);
			}
			return processorInstance;
		}
	}

	private CreditCardProcessorFactory() {
		// Make no instances
	}
}
