package org.nightlabs.jfire.issue.query;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;

public class IssueLinkQueryElement
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private IssueLinkTypeID issueLinkTypeID;

	private ObjectID linkedObjectID;

	public IssueLinkTypeID getIssueLinkTypeID() {
		return issueLinkTypeID;
	}
	public void setIssueLinkTypeID(IssueLinkTypeID issueLinkTypeID) {
		this.issueLinkTypeID = issueLinkTypeID;
	}
	public ObjectID getLinkedObjectID() {
		return linkedObjectID;
	}
	public void setLinkedObjectID(ObjectID objectID) {
		this.linkedObjectID = objectID;
	}
}
