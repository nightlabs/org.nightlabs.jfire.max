package org.nightlabs.jfire.issue.history;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.id.IssueHistoryTextID;

/**
 * An extended class of {@link I18nText} that represents the changed text of each change that made to an {@link Issue}.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueHistoryTextID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueHistoryText"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueHistoryID, issueHistoryTextID"
 *
 */
@PersistenceCapable(
	objectIdClass=IssueHistoryTextID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueHistoryText")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@SuppressWarnings("serial")
public class IssueHistoryText
extends I18nText
{
	/**
	 * This is the organisationID to which the issue history's text belongs. Within one organisation,
	 * all the issue history's texts have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	@SuppressWarnings("unused")
	private long issueHistoryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@SuppressWarnings("unused")
	private IssueHistory issueHistory;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String issueHistoryTextID;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireIssueTracking_IssueHistoryText_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueHistoryText_texts",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> texts = new HashMap<String, String>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueHistoryText()
	{
	}

	public IssueHistoryText(IssueHistory issueHistory)
	{
		this.organisationID = issueHistory.getOrganisationID();
		this.issueHistoryID = issueHistory.getIssueHistoryID();
		this.issueHistory = issueHistory;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Map getI18nMap()
	{
		return texts;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return Issue.getPrimaryKey(organisationID, issue.getIssueID());
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getIssueHistoryTextID()
	{
		return issueHistoryTextID;
	}

	public Issue getIssue()
	{
		return issue;
	}
}
