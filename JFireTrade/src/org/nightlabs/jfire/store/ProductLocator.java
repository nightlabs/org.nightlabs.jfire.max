/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store;

import java.io.Serializable;

/**
 * This is a tagging interface which doesn't offer any logic. An implementation
 * must carry information with which a {@link ProductTypeActionHandler}
 * is able to locate the desired {@link org.nightlabs.jfire.store.Product}. Hence,
 * the <tt>ProductLocator</tt> implementation is specific and dependent on the
 * <tt>ProductType</tt> / <code>ProductTypeActionHandler</code> implementation.
 * <p>
 * For a seat in a venue, the locator could e.g. specify
 * the section, row and column (or a SeatID which is unique within a certain seating
 * map).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface ProductLocator extends Serializable
{

}
