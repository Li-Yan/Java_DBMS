package qifeng.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import qifeng.file.SeqFile;
import qifeng.file.Sequential;
import qifeng.lowlevel.File;
import qifeng.schema.Schema;
import qifeng.schema.Symbol;

/**
 * 
 * a representation of an existed table. The structure cannot be changed here
 * 
 */
public class DbTable implements Iterable<byte[]> {
	private File mtd;
	private File dtm;
	private File idx;
	private Schema sc;
	private SeqFile seq;
	private HashIndices hi;
	private String name;
	private int prevCount;

	public DbTable() {
	}

	public void updateIndices() throws IOException {
		idx.seek(0, File.SEEK_SET);
		ObjectOutputStream oos = new ObjectOutputStream(Channels
				.newOutputStream(idx));
		oos.writeObject(hi);
		oos.flush();
	}

	/**
	 * update the record count (AKA. row count)
	 */
	public void updateCount() {
		if (this.prevCount != this.getApproRecordCount()) {
			this.seq.updateCount();
		}
		this.prevCount = this.getApproRecordCount();
	}

	/**
	 * force all meta-data changes to the disk
	 * 
	 * @throws IOException
	 */
	public void syncMeta() throws IOException {
		updateIndices();
		// updateSchema();
		updateCount();
	}

	public Schema getSchema() {
		return this.sc;
	}

	public HashIndices getIndices() {
		return this.hi;
	}

	/**
	 * @return table name in user point of view
	 */
	public String getUserName() {
		return name.substring(name.lastIndexOf('/') + 1);
	}

	public boolean isSysTable() {
		return name.startsWith(DataBase.SYS_PFX);
	}

	/**
	 * @return internal table name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * <b>warning</b><br>
	 * see {@link #getApproRecordCount()} for explanation
	 */
	public int getRowCount() {
		return this.getApproRecordCount();
	}

	/**
	 * @return approximately record count, it is unreliable, and do not write to
	 *         disk unless {@link #updateCount()} or {@link #syncMeta()} is
	 *         called
	 */
	public int getApproRecordCount() {
		return this.seq.getApproCount();
	}

	public int getColumnCount() {
		return this.sc.size();
	}

	public boolean createIndexOn(String s) throws IOException {
		return createIndexOn(new Symbol(s));
	}

	public boolean createIndexOn(Symbol s) throws IOException {
		// add empty index
		if (hi.containsKey(s))
			return false;
		hi.addIndices(s);
		Object obj;
		for (byte[] b : this.seq) {
			obj = sc.getValue(b, s);
			hi.addAnOffset(s, obj, dtm.position());
		}
		updateIndices();
		return true;
	}

	public boolean deleteIndexOn(String s) throws IOException {
		return deleteIndexOn(new Symbol(s));
	}
	
	public boolean deleteIndexOn(Symbol s) throws IOException {
		boolean r = hi.removeIndex(s);
		if (r)
			updateIndices();
		return r;
	}

	public void close() {
		if (this.mtd == null || this.dtm == null || this.idx == null)
			return;
		try {
			syncMeta();
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe(
					"cannot write meta changes to table " + this.getName() + " " + e.getMessage());
		}
		this.mtd.close();
		this.dtm.close();
		this.idx.close();
		this.mtd = null;
		this.dtm = null;
		this.idx = null;
	}

	public void add(byte[] record) throws IOException {
		primaryKeyCheck(record);
		seq.add(record);
		this.addIndex(record);
	}

	public static void main (String []args) {
		HashIndices hi = new HashIndices("a");
		for (Entry<Symbol, ConcurrentHashMap<Object, ArrayList<Integer>>> e : hi
				.entrySet()) {
			Symbol s = e.getKey();
			try {
				hi.putAnOffset(s, (Integer)1, (Integer)5);
			} catch (Exception e1) {
				throw new Error(e1);
			}
		}
		Symbol primary = new Symbol("a");
		if (primary != null) {
			Map<Object, ArrayList<Integer>> m = hi.getIndexOf(primary);
			Integer o = 1;
			if (m.containsKey(o)) {
				throw new IllegalArgumentException("PRIMARY KEY VIOLATION");
			}
		}
	}
	
	@Override
	public Sequential<byte[]> iterator() {
		return new DbTableIterator();
	}

	protected final class DbTableIterator implements Sequential<byte[]> {
		private Sequential<byte[]> seq_ite = seq.iterator();
		private byte[] prevRecord;

		@Override
		public boolean hasNext() {
			return seq_ite.hasNext();
		}

		@Override
		public byte[] next() {
			prevRecord = seq_ite.next(); // needed for remove
			return prevRecord.clone();
		}

		@Override
		public void remove() {
			seq_ite.remove();
			delIndex(prevRecord);
		}

		@Override
		public boolean replace(byte[] newRecord) {
			primaryKeyCheckWhenReplacing(newRecord);
			boolean r = seq_ite.replace(newRecord);
			if (r) {
				delIndex(prevRecord);
				addIndex(newRecord);
			}
			return r;
		}

		@Override
		public void replaceWithForce(byte[] newRecord) {
			primaryKeyCheckWhenReplacing(newRecord);
			delIndex(prevRecord);
			seq_ite.replaceWithForce(newRecord);
			addIndex(newRecord);
		}

		private void primaryKeyCheckWhenReplacing(byte[] newRecord) {
			try {
				if (getSchema().getPrimaryKey() != null) {
					Object o = getPrimaryKeyValue(prevRecord);
					Object o1 = getPrimaryKeyValue(newRecord);
					if (o != o1) {
						primaryKeyCheck(newRecord);
					}
				}
			} catch (IOException e) {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING,
						e.getMessage());
			}
		}
	}

	public void primaryKeyCheck(byte[] record) throws IOException {
		Symbol primary = getSchema().getPrimaryKey();
		if (primary != null) {
			Map<Object, ArrayList<Integer>> m = this.getIndices().getIndexOf(primary);
			Object o = getPrimaryKeyValue(record);
			if (m.containsKey(o)) {
				throw new IllegalArgumentException("PRIMARY KEY VIOLATION");
			}
		}
	}

	public Object getPrimaryKeyValue(byte[] record) throws IOException {
		Symbol primary = getSchema().getPrimaryKey();
		if (primary != null) {
			return getSchema().getValue(record, primary);
		}
		return null;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setMtd(File mtd) throws IOException {
		mtd.seek(0, File.SEEK_SET);
		ObjectInputStream ois = new ObjectInputStream(Channels
				.newInputStream(mtd));
		try {
			this.sc = (Schema) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
		this.mtd = mtd;
	}

	protected void setDtm(File dtm) throws IOException {
		this.seq = TableHelper.openTable(dtm);
		this.dtm = dtm;
		this.prevCount = this.seq.getApproCount();
	}

	protected void setIdx(File idx) throws IOException {
		idx.seek(0, File.SEEK_SET);
		ObjectInputStream ois = new ObjectInputStream(Channels
				.newInputStream(idx));
		try {
			this.hi = (HashIndices) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
		this.idx = idx;
	}

	/**
	 * note schema type is a constant type, using schema builder build a new one
	 * is needed
	 * 
	 * @throws IOException
	 */
	protected void updateSchema() throws IOException {
		mtd.seek(0, File.SEEK_SET);
		ObjectOutputStream oos = new ObjectOutputStream(Channels
				.newOutputStream(mtd));
		oos.writeObject(sc);
		// TODO schema changing???
	}

	/**
	 * records are not changed, so it won't work correctly now
	 * 
	 * @param sc
	 * @throws IOException
	 * @see {@link #updateSchema()}
	 */
	protected void setSchema(Schema sc) throws IOException {
		this.sc = sc;
		updateSchema();
	}

	/**
	 * correct or necessary ? {@link #getIndices()} return a reference to the
	 * Indices.
	 * 
	 * @param index
	 * @throws IOException
	 */
	protected void setIndices(HashIndices index) throws IOException {
		this.hi = index;
		updateIndices();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.close();
	}

	/**
	 * precondition : data file position is set to the start of the record
	 * 
	 * @param newRecord
	 */
	private void addIndex(byte[] newRecord) {
		for (Entry<Symbol, ConcurrentHashMap<Object, ArrayList<Integer>>> e : hi
				.entrySet()) {
			Symbol s = e.getKey();
			try {
				Object o = sc.getValue(newRecord, s);
				hi.putAnOffset(s, o, (Integer) dtm.position());
			} catch (Exception e1) {
				throw new Error(e1);
			}
		}
	}

	/**
	 * precondition : data file position is set to the start of the record
	 * 
	 * @param prevRecord
	 */
	private void delIndex(byte[] prevRecord) {
		for (Entry<Symbol, ConcurrentHashMap<Object, ArrayList<Integer>>> e : hi
				.entrySet()) {
			Symbol s = e.getKey();
			try {
				Object o = sc.getValue(prevRecord, s);
				hi.delAnOffset(s, o, dtm.position());
			} catch (Exception e1) {
				throw new Error(e1);
			}
		}
	}
}
