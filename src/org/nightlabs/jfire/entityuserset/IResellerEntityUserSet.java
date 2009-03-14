package org.nightlabs.jfire.entityuserset;

public interface IResellerEntityUserSet<Entity>
extends IEntityUserSet<Entity>
{
	IEntityUserSet<Entity> getBackendEntityUserSet();
}
