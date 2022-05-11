/*
 * aoserv-credit-cards - Stores credit card processing data in the AOServ Platform.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-credit-cards.
 *
 * aoserv-credit-cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-credit-cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-credit-cards.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoindustries.aoserv.creditcards;

import com.aoapps.payments.CreditCardProcessor;
import com.aoapps.payments.MerchantServicesProvider;
import com.aoapps.payments.MerchantServicesProviderFactory;
import com.aoindustries.aoserv.client.AoservConnector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates instances of {@link CreditCardProcessor} based on the processor
 * configurations found in {@link com.aoindustries.aoserv.client.payment.ProcessorTable}.
 * Will only create once instance of each processor.  Disabled processors
 * will not be returned.  When multiple processors are enabled for an account,
 * adheres to the weight provided by the processors.
 *
 * @author  AO Industries, Inc.
 */
public final class CreditCardProcessorFactory {

  /** Make no instances. */
  private CreditCardProcessorFactory() {
    throw new AssertionError();
  }

  private static class ProcessorKey {
    private final String providerId;
    private final String className;
    private final String param1;
    private final String param2;
    private final String param3;
    private final String param4;

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
              + className.hashCode() * 7
              + Objects.hashCode(param1) * 17
              + Objects.hashCode(param2) * 37
              + Objects.hashCode(param3) * 103
              + Objects.hashCode(param4) * 149;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ProcessorKey)) {
        return false;
      }
      ProcessorKey other = (ProcessorKey) obj;
      return
          providerId.equals(other.providerId)
              && className.equals(other.className)
              && Objects.equals(param1, other.param1)
              && Objects.equals(param2, other.param2)
              && Objects.equals(param3, other.param3)
              && Objects.equals(param4, other.param4);
    }
  }

  private static final Map<ProcessorKey, CreditCardProcessor> processors = new HashMap<>();

  /**
   * Gets an enabled {@link CreditCardProcessor} from the list of processors for the account
   * of the provided {@link AoservConnector}.  When multiple processors are enabled, those with
   * a higher weight will be returned more often, proportional to weight.  Uses the random source
   * of the {@link AoservConnector} when selecting the processor.<br>
   * <br>
   * Only one instance of each unique {@link CreditCardProcessor} (unique based on providerId, classname and all parameters) will be created.<br>
   * <br>
   * Every processor will use the {@link AoservPersistenceMechanism} for its persistence.
   *
   * @return  the processor or {@code null} if none found
   */
  public static CreditCardProcessor getCreditCardProcessor(AoservConnector conn)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, SQLException {
    // Select the aoserv-client processor before synchronizing on processors
    List<com.aoindustries.aoserv.client.payment.Processor> ccps = conn.getCurrentAdministrator().getUsername().getPackage().getAccount().getCreditCardProcessors();
    // Count the total weight of enabled processors
    int totalEnabledProcessors = 0;
    com.aoindustries.aoserv.client.payment.Processor firstProcessor = null;
    int totalWeight = 0;
    for (com.aoindustries.aoserv.client.payment.Processor ccp : ccps) {
      if (ccp.getEnabled() && ccp.getWeight() > 0) {
        totalEnabledProcessors++;
        if (firstProcessor == null) {
          firstProcessor = ccp;
        }
        totalWeight += ccp.getWeight();
      }
    }
    // No processors ready
    if (totalEnabledProcessors == 0) {
      return null;
    }

    // Pick one by weight
    com.aoindustries.aoserv.client.payment.Processor selectedProcessor;
    if (totalEnabledProcessors == 1) {
      // One processor shortcut
      selectedProcessor = firstProcessor;
    } else {
      // Pick a random one based on this weight
      selectedProcessor = null;
      int randomPosition = AoservConnector.getFastRandom().nextInt(totalWeight);
      int weightSoFar = 0;
      for (com.aoindustries.aoserv.client.payment.Processor ccp : ccps) {
        if (ccp.getEnabled() && ccp.getWeight() > 0) {
          weightSoFar += ccp.getWeight();
          if (weightSoFar > randomPosition) {
            selectedProcessor = ccp;
            break;
          }
        }
      }
      if (selectedProcessor == null) {
        throw new AssertionError("With proper implementation of weighted random select above, this should not happen");
      }
    }

    return getCreditCardProcessor(selectedProcessor);
  }

  /**
   * Gets the processor for the given AOServ processor.<br>
   * <br>
   * Only one instance of each unique {@link CreditCardProcessor} (unique based on providerId, classname and all parameters) will be created.<br>
   * <br>
   * Every processor will use the {@link AoservPersistenceMechanism} for its persistence.
   *
   * @see  MerchantServicesProviderFactory#getMerchantServicesProvider
   */
  public static CreditCardProcessor getCreditCardProcessor(com.aoindustries.aoserv.client.payment.Processor selectedProcessor)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
    // The key in the map
    ProcessorKey processorKey = new ProcessorKey(
        selectedProcessor.getProviderId(),
        selectedProcessor.getClassName(),
        selectedProcessor.getParam1(),
        selectedProcessor.getParam2(),
        selectedProcessor.getParam3(),
        selectedProcessor.getParam4()
    );

    // Now synchronize access to processors
    synchronized (processors) {
      // Look for existing instance
      CreditCardProcessor processorInstance = processors.get(processorKey);
      if (processorInstance == null) {
        MerchantServicesProvider provider = MerchantServicesProviderFactory.getMerchantServicesProvider(
            selectedProcessor.getProviderId(),
            selectedProcessor.getClassName(),
            selectedProcessor.getParam1(),
            selectedProcessor.getParam2(),
            selectedProcessor.getParam3(),
            selectedProcessor.getParam4()
        );

        // Create and add to cache
        processorInstance = new CreditCardProcessor(provider, AoservPersistenceMechanism.getInstance());
        processors.put(processorKey, processorInstance);
      }
      return processorInstance;
    }
  }
}
