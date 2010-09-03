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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JDOQLResultSetMetaData
extends ResultSetMetaData
implements IJDOQLQueryMetaData
{
	
	private static final long serialVersionUID = 1L;
	
	
	public JDOQLResultSetMetaData() {
		
	}
	
	public JDOQLResultSetMetaData(Collection collection) {
		setCollectionMetaData((List)null, collection);
	}

	/**
	 * 
	 * @param colNames
	 * @param collection
	 */
	public void setCollectionMetaData(List colNames, Collection collection) {
	}

	/**
	 * Build the metadata on basis of the given metaData and resultSet.
	 */
	@SuppressWarnings("unchecked")
	public void setCollectionMetaData(IResultSetMetaData metaData, Collection collection) {
		Iterator it = collection.iterator();
		if (!it.hasNext()) {
//			try {
//				setColumnCount(metaData.getColumnCount());
//			} catch (OdaException e) {
//				throw new RuntimeException(e);
//			}
			// TODO:
			try {
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					setColumn(
						i+1,
						metaData.getColumnName(i+1),
						metaData.getColumnType(i+1),
						java.sql.ResultSetMetaData.columnNullable
						
					);
				}
			} catch (JFireOdaException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			Object o = it.next();
			List row = null;
			if (o instanceof List)
				row = (List)it.next();
			else {
				row = new ArrayList();
				row.add(o);
			}
			int i = 1;
			for (Iterator iter = row.iterator(); iter.hasNext();) {
				Object col = iter.next();
				String colName = "No colname";
				try {
					if (metaData != null && metaData.getColumnCount() >= i)
						colName = metaData.getColumnName(i);
				} catch (JFireOdaException e) {
					throw new RuntimeException(e);
				}
				setColumn(i, colName, col.getClass(), java.sql.ResultSetMetaData.columnNullable);
				i++;
			}
//			setColumnCount(row.size());
		}
	}

}
