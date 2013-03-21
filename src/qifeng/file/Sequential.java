package qifeng.file;

import java.util.Iterator;

public interface Sequential<T> extends Iterator<T> {
	public boolean replace(T newRecord);
	public void replaceWithForce(T newRecord);
}
