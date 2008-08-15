/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Default implementation of {@link IJFSQueryPropertySetMetaData} use this to 
 * implement {@link ScriptExecutorJavaClassReportingDelegate#getJFSQueryPropertySetMetaData()}.
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFSQueryPropertySetMetaData implements IJFSQueryPropertySetMetaData {

	private static final long serialVersionUID = 20080813L;

	/**
	 * An {@link Entry} represents a property in a {@link JFSQueryPropertySet}.
	 */
	public static class Entry implements IEntry {
		private static final long serialVersionUID = 20080813L;
		
		private String name;
		private boolean required;
		/**
		 * Create a new {@link Entry}.
		 * @param name The property name.
		 * @param required Whether the property is required.
		 */
		public Entry(String name, boolean required) {
			super();
			this.name = name;
			this.required = required;
		}
		/*
		 * (non-Javadoc)
		 * @see org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData.IEntry#getName()
		 */
		public String getName() {
			return name;
		}
		/*
		 * (non-Javadoc)
		 * @see org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData.IEntry#isRequired()
		 */
		public boolean isRequired() {
			return required;
		}
	}
	
	private List<IEntry> entries = new LinkedList<IEntry>();
	
	/**
	 * Create a new {@link JFSQueryPropertySetMetaData}. 
	 */
	public JFSQueryPropertySetMetaData() {
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData#getEntries()
	 */
	public Collection<IEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	/**
	 * Add a new {@link Entry} to the list of entries of this {@link JFSQueryPropertySetMetaData}.
	 * 
	 * @param entry The entry to add.
	 */
	public void addEntry(Entry entry) {
		if (entry != null)
			entries.add(entry);
	}
}
