package org.nightlabs.jfire.jbpm.aop;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.EndState;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

public class JFireLoggingAspect
{
	private static final Logger logger = Logger.getLogger(JFireLoggingAspect.class);

	public JFireLoggingAspect() {
	}

	public Object onNodeEnter(MethodInvocation invocation) throws Throwable {
//		if (logger.isInfoEnabled()) {
//			logger.info("===============JFireLoggingInterceptor.log======================");
			logger.info("Target Object: " + invocation.getTargetObject());
//			logger.info("Actual Method: " + invocation.getActualMethod());
//			logger.info("---------------JFireLoggingInterceptor.log----------------------");
//		}

		Object targetObject = invocation.getTargetObject();
		if (!(targetObject instanceof org.jbpm.graph.def.Node))
			return invocation.invokeNext();

		org.jbpm.graph.def.Node jbpmNode = (org.jbpm.graph.def.Node) targetObject;

		ExecutionContext executionContext = (ExecutionContext) invocation.getArguments()[0];
		if (executionContext == null)
			return invocation.invokeNext();

		logger.debug("Entering node: " + jbpmNode);

		String statableIDStr = (String) executionContext.getVariable(AbstractActionHandler.VARIABLE_NAME_STATABLE_ID);
		if (statableIDStr == null)
			return invocation.invokeNext();

		ObjectID statableID = ObjectIDUtil.createObjectID(statableIDStr);
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
		Statable statable = (Statable) pm.getObjectById(statableID);

		if (executionContext.getTransition() == null) {
			// TODO JBPM WORKAROUND - this seems to be a jBPM bug - hence we don't throw an exception but only log it.
			logger.warn("executionContext.getTransition() is null!!!"); //, new Exception("StackTrace"));
			AbstractActionHandler.setLastNodeEnterTransitionName(null);
		}
		else {
			logger.debug("Entering via transition: " + executionContext.getTransition().getName());
			AbstractActionHandler.setLastNodeEnterTransitionName(executionContext.getTransition().getName());
		}

		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmNode));

		if (logger.isDebugEnabled())
			logger.debug("doExecute: statable=" + statableID + " user=" + JDOHelper.getObjectId(user) + " stateDefinition=" + JDOHelper.getObjectId(stateDefinition));

		stateDefinition.createState(user, statable);

		if (jbpmNode instanceof EndState) {
			statable.getStatableLocal().setProcessEnded();
		}
		return invocation.invokeNext();
	}
}

