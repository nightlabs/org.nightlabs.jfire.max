package org.nightlabs.jfire.dunning;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
public enum DunningAutoMode
{
	/**
	 * No automatic DunningLetter creation.
	 */
	none,
	
	/**
	 * Automatically create DunningLetters (but do not finalize them).
	 */
	create,
	
	/**
	 * Automatically create and finalize DunningLetters.
	 */
	createAndFinalize
}