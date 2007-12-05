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

package org.nightlabs.jfire.reporting.oda.jdoql;

import java.util.Collection;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jfire.reporting.oda.ResultSet;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JDOQLResultSet extends ResultSet implements IResultSet {

	private static final long serialVersionUID = 1L;
	
	
	
	public JDOQLResultSet(JDOQLResultSetMetaData guessedMetaData, Collection collection) {
		super(guessedMetaData);
		setMetaData(guessedMetaData, collection);
	}
	
	public JDOQLResultSet(String jdoqlQuery, Collection collection) {
		super(JDOQLMetaDataParser.parseJDOQLMetaData(jdoqlQuery));
		try {
			setMetaData((JDOQLResultSetMetaData)getMetaData(), collection);
		} catch (OdaException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setMetaData(JDOQLResultSetMetaData guessedMetaData, Collection collection) {
		setCollection(collection);
		setIterator(getCollection().iterator());
		if (collection.size() > 0) {
			setMetaData(new JDOQLResultSetMetaData());
			((JDOQLResultSetMetaData)getResultSetMetaData()).setCollectionMetaData(guessedMetaData, collection);
		}
		else {
			setMetaData(guessedMetaData);
		}
	}

}
