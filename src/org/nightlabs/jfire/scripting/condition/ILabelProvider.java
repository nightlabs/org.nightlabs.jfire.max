package org.nightlabs.jfire.scripting.condition;

public interface ILabelProvider 
{
	/**
	 * 
	 * @param object the object to get a text for
	 * @return the string for the given object
	 */
	String getText(Object object);
}
