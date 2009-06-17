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
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class RepositoryQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 20080811L;

	private static final Logger logger = Logger.getLogger(RepositoryQuery.class);

	
	private boolean nameRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String nameExpr;
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

	private boolean ownerNameRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String ownerNameExpr;
	/**
	 * the name (or part of it) of the owner
	 */
	private String ownerName = null;

	public static final class FieldName
	{
		public static final String anchorID = "anchorID";
		public static final String anchorTypeID = "anchorTypeID";
		public static final String nameRegex = "nameRegex";
		public static final String name = "name";
		public static final String nameLanguageID = "nameLanguageID";
		public static final String ownerID = "ownerID";
		public static final String ownerNameRegex = "ownerNameRegex";
		public static final String ownerName = "ownerName";
		public static final String repositoryTypeID = "repositoryTypeID";
	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();
		StringBuffer vars = new StringBuffer();
//		StringBuffer imports = new StringBuffer();

		filter.append(" true");

		if (isFieldEnabled(FieldName.anchorTypeID) && anchorTypeID != null)
			filter.append("\n && this.anchorTypeID == :anchorTypeID");

		if (isFieldEnabled(FieldName.anchorID) && anchorID != null)
			filter.append("\n && this.anchorID == :anchorID");

		if (isFieldEnabled(FieldName.repositoryTypeID) && repositoryTypeID != null) {
			repositoryType = (RepositoryType) getPersistenceManager().getObjectById(repositoryTypeID);
			filter.append("\n && this.repositoryType == :repositoryType");
		}

		if (isFieldEnabled(FieldName.name) && name != null && !"".equals(name)) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append(")");
		}

		if (isFieldEnabled(FieldName.ownerID) && ownerID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//		filter.append("\n && JDOHelper.getObjectId(this.owner) == :ownerID");
		// WORKAROUND:
		filter.append("\n && (" +
				"this.owner.organisationID == \""+ownerID.organisationID+"\" && " +
				"this.owner.anchorTypeID == \""+ownerID.anchorTypeID+"\" && " +
				"this.owner.anchorID == \""+ownerID.anchorID+"\"" +
						")");
		}

		if (isFieldEnabled(FieldName.ownerName) && ownerName != null && !"".equals(ownerName)) {
			ownerNameExpr = isOwnerNameRegex() ? ownerName : ".*" + ownerName + ".*";
			filter.append("\n && (this.owner.person.displayName.toLowerCase().matches(:ownerNameExpr.toLowerCase()))");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Vars:");
			logger.debug(vars.toString());
			logger.debug("Filter:");
			logger.debug(filter.toString());
		}

		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
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
		nameExpr = isNameRegex() ? name : ".*" + name + ".*";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:nameExpr.toLowerCase())" +
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
	public void setName(String name)
	{
		final String oldName = this.name;
		this.name = name;
		notifyListeners(FieldName.name, oldName, name);
	}

	/**
	 * @return Whether the value set with {@link #setName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isNameRegex() {
		return nameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setName(String)} represents a 
	 * regular expression.
	 * 
	 * @param nameRegex The nameRegex to search. 
	 */
	public void setNameRegex(boolean nameRegex) {
		final boolean oldnameRegex = this.nameRegex;
		this.nameRegex = nameRegex;
		notifyListeners(FieldName.nameRegex, oldnameRegex, nameRegex);
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
	public void setNameLanguageID(String nameLanguageID)
	{
		final String oldNameLanguageID = this.nameLanguageID;
		this.nameLanguageID = nameLanguageID;
		notifyListeners(FieldName.nameLanguageID, oldNameLanguageID, nameLanguageID);
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
		final RepositoryTypeID oldRepositoryTypeID = this.repositoryTypeID;
		this.repositoryTypeID = repositoryTypeID;
		notifyListeners(FieldName.repositoryTypeID, oldRepositoryTypeID, repositoryTypeID);
	}

	/**
	 * set the anchorID
	 * @param anchorID the anchorID to set
	 */
	public void setAnchorID(String anchorID)
	{
		final String oldAnchorID = this.anchorID;
		this.anchorID = anchorID;
		notifyListeners(FieldName.anchorID, oldAnchorID, anchorID);
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
	public void setAnchorTypeID(String anchorTypeID)
	{
		final String oldAnchorTypeID = this.anchorTypeID;
		this.anchorTypeID = anchorTypeID;
		notifyListeners(FieldName.anchorTypeID, oldAnchorTypeID, anchorTypeID);
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
	public void setOwnerID(AnchorID ownerID)
	{
		final AnchorID oldOwnerID = this.ownerID;
		this.ownerID = ownerID;
		notifyListeners(FieldName.ownerID, oldOwnerID, ownerID);
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
	public void setOwnerName(String ownerName)
	{
		final String oldOwnerName = this.ownerName;
		this.ownerName = ownerName;
		notifyListeners(FieldName.ownerName, oldOwnerName, ownerName);
	}

	/**
	 * @return Whether the value set with {@link #setOwnerName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setOwnerName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isOwnerNameRegex() {
		return ownerNameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setOwnerName(String)} represents a 
	 * regular expression.
	 * 
	 * @param ownerNameRegex The ownerNameRegex to search. 
	 */
	public void setOwnerNameRegex(boolean ownerNameRegex) {
		final boolean oldOwnerNameRegex = this.ownerNameRegex;
		this.ownerNameRegex = ownerNameRegex;
		notifyListeners(FieldName.ownerNameRegex, oldOwnerNameRegex, ownerNameRegex);
	}
	
	@Override
	protected Class<Repository> initCandidateClass()
	{
		return Repository.class;
	}

	@Override
	public void clearQuery() {
		// TODO Auto-generated method stub
		
	}

}
