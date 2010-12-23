package org.nightlabs.jfire.dunning.jbpm;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.User;

public class ActionHandlerFinalizeDunningLetter 
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doExecute(ExecutionContext executionContext)
			throws Exception {
		PersistenceManager pm = getPersistenceManager();
		DunningLetter dunningLetter = (DunningLetter) getStatable();

		if (dunningLetter.isFinalized())
			return;

		User user = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUser(pm);
		dunningLetter.setFinalized(user);

		// book synchronously
		DunningLetterID dunningLetterID = (DunningLetterID) JDOHelper.getObjectId(dunningLetter);
		if (!State.hasState(pm, dunningLetterID, JbpmConstantsDunningLetter.NODE_NAME_BOOKED))
			executionContext.leaveNode(JbpmConstantsDunningLetter.TRANSITION_NAME_BOOK);
		
	}

}
