package org.nightlabs.jfire.scripting;

import javax.jdo.JDOHelper;

/**
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ScriptExecutorJavaClass
		extends ScriptExecutor
{
	public static final String LANGUAGE_JAVA_CLASS = "JavaClass";

	private ScriptExecutorJavaClassDelegate delegate = null;

	protected ScriptExecutorJavaClassDelegate getDelegate()
	throws ScriptException
	{
		String delegateClassName = getScript().getText();

		if (delegate != null && delegate.getClass().getName().equals(delegateClassName))
			return delegate;

		try {
			Class delegateClass = Class.forName(delegateClassName);
			Object delegateInstance = delegateClass.newInstance();
			if (!(delegateInstance instanceof ScriptExecutorJavaClassDelegate))
				throw new ClassCastException(
						"Delegate " + delegateClassName + " defined for script " + JDOHelper.getObjectId(getScript())
						+	" does not implement interface " + ScriptExecutorJavaClassDelegate.class.getName());

			delegate = (ScriptExecutorJavaClassDelegate)delegateInstance;
			return delegate;
		} catch (ClassNotFoundException e) {
			throw new ScriptException(e);
		} catch (InstantiationException e) {
			throw new ScriptException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	protected void doPrepare()
			throws ScriptException
	{
		super.doPrepare();
		getDelegate().doPrepare();
	}

	@Override
	protected Object doExecute()
			throws ScriptException
	{
		return getDelegate().doExecute();
	}

}
