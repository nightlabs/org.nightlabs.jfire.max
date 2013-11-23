package org.nightlabs.jfire.scripting.condition;

import java.io.Serializable;

public interface ILabelProvider
extends Serializable
{
	/**
	 * 
	 * @param object the object to get a text for
	 * @return the string for the given object
	 */
	String getText(Object object);
}
