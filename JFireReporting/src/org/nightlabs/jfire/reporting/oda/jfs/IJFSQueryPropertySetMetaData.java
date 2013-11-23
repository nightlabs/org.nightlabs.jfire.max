package org.nightlabs.jfire.reporting.oda.jfs;

import java.io.Serializable;
import java.util.Collection;


/**
 * An instance of {@link IJFSQueryPropertySetMetaData} is used to describe the set of 
 * properties (passed as {@link JFSQueryPropertySet}) a {@link ScriptExecutorJavaClassReportingDelegate}
 * needs/accepts when executing or defining its own meta-data.
 *   
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IJFSQueryPropertySetMetaData extends Serializable {
	/**
	 * An {@link IEntry} represents a property in an {@link IJFSQueryPropertySet}.
	 */
	public interface IEntry extends Serializable {		
		/**
		 * Returns the property name.
		 * @return The property name.
		 */
		String getName();
		
		/**
		 * Whether the property represented by this {@link IEntry}
		 * is required for the execution of the corresponding
		 * {@link ScriptExecutorJavaClassReportingDelegate}.
		 * 
		 * @return Whether the property represented by this {@link IEntry} is required.
		 */
		boolean isRequired();
	}
	

	/**
	 * Returns the entries of this {@link IJFSQueryPropertySetMetaData}.
	 *  
	 * @return The {@link IEntry}s of this {@link IJFSQueryPropertySetMetaData}.
	 */
	Collection<IEntry> getEntries();
}