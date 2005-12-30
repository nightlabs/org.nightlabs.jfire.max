/**
 * 
 */
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
