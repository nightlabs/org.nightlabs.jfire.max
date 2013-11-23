package org.nightlabs.jfire.issue.history;

import java.lang.reflect.Field;
import java.util.List;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueLink}s.
 *
 * An {@link IssueLink} is either ADDED or REMOVED in a history action, or to be candidly correct,
 * a link is either CREATED or SEVERED, respectively.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueLinkHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueLinkType")}
	)
})
public class IssueLinkHistoryItem extends IssueHistoryItem
{
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueLinkType issueLinkType;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueHistoryItemAction issueHistoryItemAction;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String linkedObjectClassName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String linkedObjectID;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueLinkHistoryItem() {}

	/**
	 * Creates a new instance of an IssueLinkHistoryItem.
	 */
	public IssueLinkHistoryItem(
			User user,
			Issue issue,
			IssueLinkType issueLinkType,
			IssueHistoryItemAction issueHistoryItemAction,
			Class<?> linkedObjectClass,
			ObjectID linkedObjectID
	)
	{
		super(true, user, issue);
		if (linkedObjectClass == null)
			throw new IllegalArgumentException("linkedObjectClass == null");

		if (linkedObjectID == null)
			throw new IllegalArgumentException("linkedObjectID == null");

		this.issueLinkType = issueLinkType;
		this.issueHistoryItemAction = issueHistoryItemAction;

		// --- 8< --- KaiExperiments: since 08.06.2009 ------------------
		this.linkedObjectClassName = linkedObjectClass.getName();
		this.linkedObjectID = linkedObjectID.toString();
		// ------ KaiExperiments ----- >8 -------------------------------
	}

	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
//		String linkedObjInfoTxt = linkedObjectClassName + " [ID: " + linkedObjectID + "]";
		String linkedObjInfoTxt = getLinkedObjectHumanReadableText();
		switch (issueHistoryItemAction) {
			case ADDED:
				return String.format("Issue link CREATED: This Issue is now \"%s\" to \"%s\".", issueLinkType.getName().getText(), linkedObjInfoTxt);
			case REMOVED:
				return String.format("Issue link SEVERED: This Issue is no longer \"%s\" to \"%s\".", issueLinkType.getName().getText(), linkedObjInfoTxt);
			default:
				throw new IllegalStateException("Unknown issueHistoryItemAction: " + issueHistoryItemAction);
		}
	}

	/**
	 * Get the human readable object name that is part of the description created by {@link #getDescription()}.
	 * <p>
	 * Override this method to provide a nicer representation. The default implementation returns the simple class name
	 * of the linked object (see {@link #getLinkedObjectClassName()}) and a condensed version of the primary key.
	 * </p>
	 */
	protected String getLinkedObjectHumanReadableText()
	{
		StringBuilder result = new StringBuilder();

		String simpleClassName;
		int lastDotIdx = linkedObjectClassName.lastIndexOf('.');
		if (lastDotIdx < 0)
			simpleClassName = linkedObjectClassName;
		else
			simpleClassName = linkedObjectClassName.substring(lastDotIdx + 1);

		result.append(simpleClassName);
		result.append(" [ID: ");

		ObjectID linkedObjectID = ObjectIDUtil.createObjectID(this.linkedObjectID);
		List<Field> fields = ReflectUtil.collectAllFields(linkedObjectID.getClass(), true);
		int visibleFieldIdx = -1;
		for (Field field : fields) {
			String fieldName = field.getName();
			Object value;
			try {
				value = field.get(linkedObjectID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if ("organisationID".equals(fieldName) && SecurityReflector.getUserDescriptor().getOrganisationID().equals(value))
				continue;

			if (++visibleFieldIdx > 0)
				result.append('/');

			result.append(value);
		}

//		String fieldsString = ObjectIDUtil.splitObjectIDString(linkedObjectID)[1];
//		String[] fields = fieldsString.split(ObjectIDUtil.FIELD_SEPARATOR);
//		int fieldIdx = -1;
//		for (String field : fields) {
//			String[] fv = field.split("=");
//			String fieldName = fv[0];
//			String fieldValue;
//			try {
//				fieldValue = URLDecoder.decode(fv[1], IOUtil.CHARSET_NAME_UTF_8);
//			} catch (UnsupportedEncodingException e) {
//				throw new IllegalStateException(e); // Should never happen since UTF-8 must be supported!
//			}
//
//			if ("organisationID".equals(fieldName) && SecurityReflector.getUserDescriptor().getOrganisationID().equals(fieldValue))
//				continue;
//
//			++fieldIdx;
//
//			if (fieldIdx > 0)
//				result.append('/');
//
//			result.append(fieldValue);
//		}

		result.append("]");

		return result.toString();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

	public String getLinkedObjectClassName() {
		return linkedObjectClassName;
	}
	public String getLinkedObjectID() {
		return linkedObjectID;
	}

	public void setLinkedObjectID(String linkedObjectID) {
		this.linkedObjectID = linkedObjectID;
	}
}