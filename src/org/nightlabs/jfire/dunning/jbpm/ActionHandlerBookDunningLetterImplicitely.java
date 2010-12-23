package org.nightlabs.jfire.dunning.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;

public class ActionHandlerBookDunningLetterImplicitely
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerBookDunningLetterImplicitely.class.getName()));
		action.setName(ActionHandlerBookDunningLetterImplicitely.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node booked = jbpmProcessDefinition.getNode(JbpmConstantsDunningLetter.NODE_NAME_BOOKED_IMPLICITELY);
		if (booked == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsDunningLetter.NODE_NAME_BOOKED_IMPLICITELY +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		booked.addEvent(event);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		DunningLetter dunningLetter = (DunningLetter) getStatable();
		Accounting accounting = Accounting.getAccounting(pm);
//		accounting.validateDunningLetter(dunningLetter);
//		User user = SecurityReflector.getUserDescriptor().getUser(pm);
//		dunningLetter.setFinalized(user);
	}

}
