/*
 * aoserv-credit-cards - Stores credit card processing data in the AOServ Platform.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

import com.aoapps.lang.exception.WrappedException;
import com.aoindustries.aoserv.client.AoservConnector;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

/**
 * Uses an {@link AoservConnector} as a Java {@link Principal}.
 *
 * @author  AO Industries, Inc.
 */
public final class AoservConnectorPrincipal implements Principal {

  private final AoservConnector conn;
  private final String principalName;

  /**
   * Creates a new {@link AoservConnectorPrincipal}.
   */
  public AoservConnectorPrincipal(AoservConnector conn, String principalName) {
    this.conn = conn;
    this.principalName = principalName;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (!(obj instanceof AoservConnectorPrincipal)) {
        return false;
      }
      AoservConnectorPrincipal other = (AoservConnectorPrincipal) obj;
      if (!conn.getCurrentAdministrator().equals(other.getAoservConnector().getCurrentAdministrator())) {
        return false;
      }
      if (principalName == null) {
        return other.principalName == null;
      } else {
        return principalName.equals(other.principalName);
      }
    } catch (IOException | SQLException err) {
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
      return conn.getCurrentAdministrator().hashCode() + (principalName == null ? 0 : (principalName.hashCode() * 37));
    } catch (IOException | SQLException err) {
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
  public AoservConnector getAoservConnector() {
    return conn;
  }

  /**
   * Gets the application-provided principal name.
   */
  public String getPrincipalName() {
    return principalName;
  }
}
