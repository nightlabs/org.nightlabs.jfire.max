package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.RepositoryType;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class RepositoryQuery
extends AbstractJDOQuery<Repository>
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

	/**
	 * This is solely used in pepareQuery as parameter because JPOX still has a bug with JDOHelper.getObjectId(...) in JDOQL.
	 * TODO JPOX WORKAROUND use JDOHelper.getObjectId(...) and remove this field.
	 */
	@SuppressWarnings("unused")
	private transient RepositoryType repositoryType = null;

	private RepositoryTypeID repositoryTypeID = null;

	/**
	 * the {@link AnchorID} of the owner
	 */
	private AnchorID ownerID = null;
	
	/**
	 * the name (or part of it) of the owner
	 */
	private String ownerName = null;
	
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

		if (repositoryTypeID != null) {
			repositoryType = (RepositoryType) getPersistenceManager().getObjectById(repositoryTypeID);
			filter.append("\n && this.repositoryType == :repositoryType");
		}

		if (name != null && !"".equals(name)) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append(")");
		}

		if (ownerID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//		filter.append("\n && JDOHelper.getObjectId(this.owner) == :ownerID");
		// WORKAROUND:
		filter.append("\n && (" +
				"this.owner.organisationID == \""+ownerID.organisationID+"\" && " +
				"this.owner.anchorTypeID == \""+ownerID.anchorTypeID+"\" && " +
				"this.owner.anchorID == \""+ownerID.anchorID+"\"" +
						")");
		}
		
		if (ownerName != null && !"".equals(ownerName)) {
			filter.append("\n && (this.owner.person.displayName.toLowerCase().indexOf(\""+ownerName.toLowerCase()+"\") >= 0)");
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

	public RepositoryTypeID getRepositoryTypeID()
	{
		return repositoryTypeID;
	}
	public void setRepositoryTypeID(RepositoryTypeID repositoryTypeID)
	{
		this.repositoryTypeID = repositoryTypeID;
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

	/**
	 * returns the {@link AnchorID} of the owner of the repository
	 * @return the ownerID
	 */
	public AnchorID getOwnerID() {
		return ownerID;
	}

	/**
	 * sets the {@link AnchorID} of the owner of the repository
	 * @param ownerID the ownerID to set
	 */
	public void setOwnerID(AnchorID ownerID) {
		this.ownerID = ownerID;
	}

	/**
	 * returns the name (or part of it) of the owner of repository
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * sets the name (or part of it) of the owner of repository
	 * @param ownerName the ownerName to set
	 */
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	@Override
	protected Class<Repository> init()
	{
		return Repository.class;
	}
		
}
