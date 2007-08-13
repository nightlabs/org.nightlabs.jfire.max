package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.store.Repository;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class RepositoryQuery 
extends JDOQuery<Repository>
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RepositoryQuery.class);
	
	/**
	 * the name of the repository to search for
	 */
	private String name = null;
	private String nameLanguageID = null;
	/**
	 * the anchorID to search for
	 */
	private String anchorID = null;
	/**
	 * the anchorTypeID to search for
	 */
	private String anchorTypeID = null;

	@Override
	protected Query prepareQuery() 
	{
		Query q = getPersistenceManager().newQuery(Repository.class);
		StringBuffer filter = new StringBuffer();
		StringBuffer vars = new StringBuffer();
//		StringBuffer imports = new StringBuffer();
		
		filter.append(" true");

		if (anchorTypeID != null)
			filter.append("\n && this.anchorTypeID == :anchorTypeID");
			
		if (anchorID != null)
			filter.append("\n && this.anchorID == :anchorID");

		if (name != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append(")");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Vars:");
			logger.debug(vars.toString());
			logger.debug("Filter:");
			logger.debug(filter.toString());			
		}
		
		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());		
		
		return q;
	}

	private void addFullTextSearch(StringBuffer filter, StringBuffer vars, String member) 
	{
		if (vars.length() > 0)
			vars.append("; ");
		
		String varName = member+"Var";
		vars.append(String.class.getName()+" "+varName);
		String containsStr = "containsValue("+varName+")";
		if (nameLanguageID != null)
			containsStr = "containsEntry(\""+nameLanguageID+"\","+varName+")";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:name.toLowerCase())" +
				" )");
	}

	/**
	 * returns the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the nameLanguageID.
	 * @return the nameLanguageID
	 */
	public String getNameLanguageID() {
		return nameLanguageID;
	}

	/**
	 * set the nameLanguageID
	 * @param nameLanguageID the nameLanguageID to set
	 */
	public void setNameLanguageID(String nameLanguageID) {
		this.nameLanguageID = nameLanguageID;
	}

	/**
	 * returns the anchorID.
	 * @return the anchorID
	 */
	public String getAnchorID() {
		return anchorID;
	}

	/**
	 * set the anchorID
	 * @param anchorID the anchorID to set
	 */
	public void setAnchorID(String anchorID) {
		this.anchorID = anchorID;
	}

	/**
	 * returns the anchorTypeID.
	 * @return the anchorTypeID
	 */
	public String getAnchorTypeID() {
		return anchorTypeID;
	}

	/**
	 * set the anchorTypeID
	 * @param anchorTypeID the anchorTypeID to set
	 */
	public void setAnchorTypeID(String anchorTypeID) {
		this.anchorTypeID = anchorTypeID;
	}	
		
}
