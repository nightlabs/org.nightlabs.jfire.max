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

package org.nightlabs.jfire.reporting.birt.oda.jdojs;

import org.eclipse.birt.report.model.elements.interfaces.IOdaDataSetModel;
import org.eclipse.datatools.connectivity.oda.IQuery;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface IJDOJSProxy extends IQuery {

//	public static final String PROPERTY_PREPARE_SCRIPT = "prepareScript";
	public static final String PROPERTY_PREPARE_SCRIPT = IOdaDataSetModel.QUERY_TEXT_PROP;
	public static final String PROPERTY_FETCH_SCRIPT = "fetchScript";
//	public static final String PROPERTY_FETCH_SCRIPT = IOdaDataSetModel.QUERY_TEXT_PROP;
	
	
	public String getPrepareScript();
	public String getFetchScript();

}
