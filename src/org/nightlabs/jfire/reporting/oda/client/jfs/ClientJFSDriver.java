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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.oda.client.jfs;

import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSDriver;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryProxyFactory;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ClientJFSDriver extends AbstractJFSDriver {

	/**
	 * 
	 */
	public ClientJFSDriver() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ui.oda.jfs.AbstractJFSDriver#createProxyFactory()
	 */
	@Override
	protected IJFSQueryProxyFactory createProxyFactory() {
		return new ClientJFSQueryProxyFactory();
	}

}
