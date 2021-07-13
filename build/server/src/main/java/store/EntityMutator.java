package store;

import java.util.Collection;

public interface EntityMutator {

	public void save(DatabaseObject obj, boolean internal);

	public void update(DatabaseObject obj, boolean internal);

	public void saveOrUpdate(DatabaseObject obj, boolean internal);

	public boolean finish();

	public <T extends DatabaseObject> boolean delete(T obj, boolean internal);

	public <T extends DatabaseObject> void peformDeleteOrphan(Collection<T> oldList, Collection<T> newList);

	public <T extends DatabaseObject, H extends EntityHelper<T>> H getHelper(String fullType);

	public <T extends DatabaseObject, H extends EntityHelper<T>> H getHelperByInstance(Object fullType);

	public void processOnLoad(Object entity);

	public void preUpdate(DatabaseObject obj);

	public void preDelete(DatabaseObject obj);

	public void markDirty(DatabaseObject obj);

	public boolean isInDelete(Object obj);
}
