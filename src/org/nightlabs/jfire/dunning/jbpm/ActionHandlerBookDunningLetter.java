package org.nightlabs.jfire.dunning.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.User;

public class ActionHandlerBookDunningLetter
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		User user = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUser(pm);
		DunningLetter dunningLetter = (DunningLetter) getStatable();
//		dunningLetter.bookDunningLetter(user);

		// send asynchronously
		DunningLetterID dunningLetterID = (DunningLetterID) JDOHelper.getObjectId(dunningLetter);
//		if (!State.hasState(pm, dunningLetterID, JbpmConstantsDunningLetter.Both.NODE_NAME_SENT))
//			executionContext.leaveNode(JbpmConstantsDunningLetter.Vendor.TRANSITION_NAME_SEND);
	}

}
