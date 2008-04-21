package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkTypeID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLinkType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueLinkTypeID"
 * 
 * @jdo.fetch-group name="IssueLinkType.this" fetch-groups="default" fields="linkableObjectClassNames, name"
 *
 * @jdo.query
 *		name="getIssueLinkTypesForLinkableObjectClassNames"
 *		query="SELECT WHERE this.linkableObjectClassNames.contains(:linkableObjectClassName)"
 */ 
public class IssueLinkType
implements Serializable 
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_THIS_ISSUE_LINK_TYPE = "IssueLinkType.this";

	@SuppressWarnings("unchecked")
	private static void populateIssueLinkTypes(PersistenceManager pm, Query q, Class<?> linkableObjectClass, Set<IssueLinkType> issueLinkTypes)
	{
		Class<?> loc = linkableObjectClass;
		while (loc != null) {
			issueLinkTypes.addAll((Collection<? extends IssueLinkType>) q.execute(loc.getName()));

			for (Class<?> locInterface : loc.getInterfaces())
				populateIssueLinkTypes(pm, q, locInterface, issueLinkTypes);

			loc = loc.getSuperclass();
		}
	}

	@SuppressWarnings("unchecked")
	public static Set<IssueLinkType> getIssueLinkTypes(PersistenceManager pm, Class<?> linkableObjectClass)
	{
		Query q = pm.newNamedQuery(IssueLinkType.class, "getIssueLinkTypesForLinkableObjectClassNames");

		Set<IssueLinkType> issueLinkTypes = new HashSet<IssueLinkType>();
		populateIssueLinkTypes(pm, q, linkableObjectClass, issueLinkTypes);

		return issueLinkTypes;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String issueLinkTypeID;	
	
	/**
	 * String of the referenced object class names.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true"
	 *		table="JFireIssueTracking_IssueLinkType_linkableObjectClassNames"
	 *
	 * @jdo.join
	 */
	private Set<String> linkableObjectClassNames;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueLinkType"
	 */
	private IssueLinkTypeName name;
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueLinkType() {}

	public IssueLinkType(String organisationID, String issueLinkTypeID) {
		this.organisationID = organisationID;
		this.issueLinkTypeID = issueLinkTypeID;
		
		this.linkableObjectClassNames = new HashSet<String>();
		this.name = new IssueLinkTypeName(this);
	}
	
	public Set<String> getLinkableObjectClassNames() {
		return linkableObjectClassNames;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getIssueLinkTypeID() {
		return issueLinkTypeID;
	}
	
	public IssueLinkTypeName getName() {
		return name;
	}

	/**
	 * Callback method triggered after a new {@link IssueLink} instance has been created and persisted to the datastore.
	 * <p>
	 * Override this method in your subclass of <code>IssueLinkType</code>, if you need to perform some code on creation
	 * of <code>IssueLink</code>s.
	 * </p>
	 *
	 * @param newIssueLink the newly created and already persisted (in the same transaction) IssueLink.
	 * @see #beforeDeleteIssueLink(IssueLink)
	 * @see #afterDeleteIssueLink(IssueLink)
	 */
	protected void afterCreateIssueLink(IssueLink newIssueLink) { }

	/**
	 * Callback method triggered before an {@link IssueLink} instance has been deleted from the datastore.
	 * 
	 * @param issueLinkToBeDeleted the <code>IssueLink</code> that is about to be deleted.
	 * @see #afterCreateIssueLink(IssueLink)
	 * @see #afterDeleteIssueLink(IssueLink)
	 */
	protected void beforeDeleteIssueLink(IssueLink issueLinkToBeDeleted) { }
	/**
	 * Callback method triggered after an {@link IssueLink} instance has been deleted from the datastore.
	 * 
	 * @param issueLinkDeleted the <code>IssueLink</code> that has been deleted.
	 * @see #afterCreateIssueLink(IssueLink)
	 * @see #beforeDeleteIssueLink(IssueLink)
	 */
	protected void afterDeleteIssueLink(IssueLink issueLinkDeleted) { }

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of IssueLinkType is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

//	protected Object detachLinkedObject(IssueLink issueLink) {
//		return getPersistenceManager().detachCopy(issueLink.getLinkedObject());
//	}
}