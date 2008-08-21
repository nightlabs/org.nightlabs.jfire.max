package org.nightlabs.jfire.issue.project;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class ProjectParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		ProjectItem pi = (ProjectItem)jdoObject;
		return pi.getProject() == null ? null : pi.getProject().getObjectId();
	}
}
