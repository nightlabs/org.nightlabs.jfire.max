package org.nightlabs.jfire.reporting.oda.jdoql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

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
						metaData.getColumnType(i+1)
					);
				}
			} catch (OdaException e) {
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
				Object col = (Object) iter.next();
				String colName = "No colname";
				try {
					if (metaData != null && metaData.getColumnCount() >= i)
						colName = metaData.getColumnName(i);
				} catch (OdaException e) {
					throw new RuntimeException(e);
				}
				setColumn(i, colName, col.getClass());
				i++;
			}
//			setColumnCount(row.size());
		}
	}

}