/**
 * 
 */
package org.nightlabs.jfire.jbpm.graph.def;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * Simple {@link ActionHandler} for {@link Node}s that will leave the {@link Node}
 * with a leaving {@link Transition} with the same name as the Node was entered.
 * If that's not possible the Node will be left by the default Transition.
 * <p>
 * Note that this {@link ActionHandler} has to be added as action for the node in order to function correctly.
 * </p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class TransitionPassThroughActionHandler implements ActionHandler {

	private static final long serialVersionUID = 20080816L;
	
	private static ThreadLocal<String> transitionThreadLocal = new ThreadLocal<String>();
	
	/**
	 * 
	 */
	public TransitionPassThroughActionHandler() {
	}

	/* (non-Javadoc)
	 * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
	 */
	public void execute(ExecutionContext ctx) throws Exception {
		leaveNode(ctx);
	}
	
	protected void storeEnteringTransition(Transition transition) {
		transitionThreadLocal.set(transition.getName());
	}
	
	protected String getEnteringTransitionName() {
		return transitionThreadLocal.get();
	}
	
	protected void leaveNode(ExecutionContext ctx) {
		String transitionName = ActionHandlerNodeEnter.getLastNodeEnterTransitionName();
		if (transitionName != null && !"".equals(transitionName)) {
			Transition transition = ctx.getToken().getNode().getLeavingTransition(transitionName);
			if (transition != null) {
				ctx.leaveNode(transition);
				return;
			}
		}
		ctx.leaveNode();
	}

}
