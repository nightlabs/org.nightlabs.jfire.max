package org.nightlabs.jfire.issue.project;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.progress.NullProgressMonitor;

public class ProjectParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		Project project = (Project)jdoObject;
		Project p = ProjectDAO.sharedInstance().getProject((ProjectID)JDOHelper.getObjectId(project), new String[]{Project.FETCH_GROUP_PARENT_PROJECT, Project.FETCH_GROUP_SUBPROJECTS, FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		return p.getParentProject() == null ? null : p.getParentProject().getObjectId();
	}
}
