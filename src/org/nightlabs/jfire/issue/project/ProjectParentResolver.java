package org.nightlabs.jfire.issue.project;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class ProjectParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		ProjectItem p = (ProjectItem)jdoObject;
		return p.getProject() == null ? null : p.getProject().getObjectId();
	}
}
