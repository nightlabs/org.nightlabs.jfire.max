package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;

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
 * @jdo.fetch-group name="IssueLinkType.this" fetch-groups="default" fields="names"
 * 
 */ 
public class IssueLinkType 
implements Serializable 
{
	public static final String FETCH_GROUP_THIS = "IssueLinkType.this";
	
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueLinkTypeID;	
	
	/**
	 * String of the referenced object class name.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true"
	 */
	private Set<String> linkableObjectClassNames;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueLinkType() {}

	public IssueLinkType(String organisationID, long issueLinkTypeID) {
		this.organisationID = organisationID;
		this.issueLinkTypeID = issueLinkTypeID;
	}
	
	public Set<String> getLinkableObjectClassNames() {
		return linkableObjectClassNames;
	}
}
