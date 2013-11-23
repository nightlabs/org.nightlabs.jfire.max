package org.nightlabs.jfire.issue.project;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

/**
 * An implementation of {@link TreeNodeParentResolver} that used for creating an <a href="https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectTreeController">Active UI</a>. 
 * <p>
 * 
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class ProjectParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	public ObjectID getParentObjectID(Object jdoObject)
	{
		Project p = (Project)jdoObject;
		return p.getParentProject() == null ? null : p.getParentProject().getObjectId();
	}
}