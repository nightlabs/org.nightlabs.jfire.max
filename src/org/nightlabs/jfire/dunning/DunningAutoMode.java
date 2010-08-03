package org.nightlabs.jfire.dunning;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public enum DunningAutoMode
{
	none,				//Nothing automatic.
	create,				//Automatically create DunningLetters (but do not finalize them).
	createAndFinalize 	//Automatically create and finalize DunningLetters.
}
