/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jdoql.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.reporting.oda.jdoql.AbstractJDOQLProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLMetaDataParser;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLResultSet;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLResultSetMetaData;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector.UserDescriptor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOQLProxy extends AbstractJDOQLProxy {

//	private String organisationID;
	
//	public ServerJDOQLProxy(String organisationID) {
//		this.organisationID = organisationID;
//	}

	public void prepare(String queryText) throws OdaException {
		super.prepare(queryText);
	}
	
	private JDOQLResultSetMetaData metaData;
	
	public IResultSetMetaData getMetaData() throws OdaException {
		if (metaData == null)
			metaData = JDOQLMetaDataParser.parseJDOQLMetaData(getQuery());
		return metaData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException {
		try {
			return executeQuery(getQuery(), getParameters(), (JDOQLResultSetMetaData)getMetaData(), false, null);
		} catch (Exception e) {
			throw new OdaException("Could not execute JDOQLQuery "+e.getClass().getName()+": "+e.getMessage());
		}
	}
	
	public static IResultSet executeQuery(
			String query, 
			Map parameters,
			JDOQLResultSetMetaData metaData,
			boolean doDetach,
			String[] detachFetchGroups
		) 
	throws ModuleException 
	{
		SecurityReflector securityReflector = null;
		try {
			securityReflector = SecurityReflector.lookupSecurityReflector(new InitialContext());
		} catch (NamingException e) {
			throw new ModuleException(e);
		}
		UserDescriptor userDescriptor = securityReflector.whoAmI();

		Lookup lookup = null;
		lookup = new Lookup(userDescriptor.getOrganisationID());
		PersistenceManager pm = null;
		try {
			pm = lookup.getPersistenceManager();
//			Query q = pm.newQuery(IJDOQueryProxy.LANGUAGE_JDOQL, getQuery());
			Query q = pm.newQuery(query);
			Object o = null;
			try {
				o = q.executeWithMap(parameters);
			} catch (Exception e) {			
				System.out.println("Exception executing Query: ");
				e.printStackTrace();			
			}
			JDOQLResultSet resultSet = null;
			Collection collection = null;
			if (o instanceof Collection)
				collection = (Collection)o;
			else {
				collection = new ArrayList(1);
				collection.add(o);
			}
			if (doDetach) {
				if (detachFetchGroups != null)
					pm.getFetchPlan().setGroups(detachFetchGroups);
				// WORKAROUND: use detachCopyAll again when it checks for null values
//				collection = pm.detachCopyAll(collection);
				Collection tmpCollection = new LinkedList();
				for (Iterator iter = collection.iterator(); iter.hasNext();) {
					Object element = (Object) iter.next();
					if (element != null && (element instanceof PersistenceCapable))
						tmpCollection.add(pm.detachCopy(element));
					else {
						if (element != null)
							tmpCollection.add(element);
						else
							tmpCollection.add(null);
					}
				}
				collection = tmpCollection;
				
			}
			resultSet = new JDOQLResultSet(metaData, collection);
			return resultSet;
		} finally {
			pm.close();
		}
	}

}
