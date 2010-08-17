package org.nightlabs.jfire.dunning;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public enum DunningAutoMode
{
	none {	//Nothing automatic.
		@Override
		public String toString() {
			return "None";
		}
	},
	create	//Automatically create DunningLetters (but do not finalize them).
	{
		@Override
		public String toString() {
			return "Create";
		}
	},
	createAndFinalize	//Automatically create and finalize DunningLetters.
	{
		@Override
		public String toString() {
			return "Create and Finalize";
		}				
	}
}
