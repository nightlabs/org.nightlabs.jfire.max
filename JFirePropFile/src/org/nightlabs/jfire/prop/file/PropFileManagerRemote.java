package org.nightlabs.jfire.prop.file;

import javax.ejb.Remote;

@Remote
public interface PropFileManagerRemote {

	void initialise() throws Exception;

}
