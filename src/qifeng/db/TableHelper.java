package qifeng.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import qifeng.file.SeqFile;
import qifeng.file.Sequential;
import qifeng.lowlevel.BufferManager;
import qifeng.lowlevel.Disk;
import qifeng.lowlevel.File;
import qifeng.schema.Schema;
import qifeng.schema.SchemaBuilder;
import qifeng.schema.Symbol;
import qifeng.schema.Type;
import qifeng.schema.TypeHelper;

public class TableHelper {
	public static final int FIRST_DATA_OFFSET = 512;
	public static final int MAGIC = 0x010a0a01;
	public static final byte[] MAGIC_T = { 0x65, 0x6C, 0x62, 0x61 };
	// address of record count, not necessary and not reliable
	public static final int COUNT_OFFSET = 8;
	public static final int FIRST_OFFSET = 32;

	public static void main(String[] args) {
		testCreateIndices();
	}

	public static void testCreateIndices() {
		BufferManager.getDefaultBufferManager();
		try {
			DataBase db = DataBase.open("test.db");
			Schema sc = schemaCreationSample();
			db.createTable("student", sc);
			DbTable dbt = db.openTable("student");
			// Schema sc = dbt.getSchema();

			// pass
			byte[] n1 = { 'y', 'a', 'n', 'l', 'i' };
			byte[] n2 = { 'z', 'f', 't', 'e', '1' };
			dbt.add(sc.newValues(5, n1));
			dbt.add(sc.newValues(6, n2));
			// dbt.createIndexOn("id");
			// pass
			// for (byte[] bb : dbt) {
			// Integer x = (Integer) sc.getValue(bb, 0);
			// byte[] b = (byte[]) sc.getValue(bb, 1);
			// System.out.println(x);
			// System.out.println(TypeHelper.newStringFromBytes(b));
			// }
			// pass
			// Sequential<byte[]> seq = dbt.iterator();
			// while (seq.hasNext()) {
			// byte[] bb = seq.next();
			// byte[] b = (byte[]) sc.getValue(bb, 1);
			// b[4] = 'e';
			// sc.putValue(bb, 1, b);
			// seq.replace(bb);
			// }

			Sequential<byte[]> seq = dbt.iterator();
			while (seq.hasNext()) {
				seq.next();
				seq.next();
				seq.remove();
				break;
			}
			dbt.add(sc.newValues(14, n1));

			for (byte[] bb : dbt) {
				Integer x = (Integer) sc.getValue(bb, 0);
				byte[] b = (byte[]) sc.getValue(bb, 1);
				System.out.println(x);
				System.out.println(TypeHelper.newStringFromBytes(b));
			}
			// pass
			/*
			 * if (dbt.getIndices().containsKey(new Symbol("id"))) {
			 * System.err.println("OK1"); } for (Entry<Object,
			 * ArrayList<Integer>> e : dbt.getIndices().get( new
			 * Symbol("id")).entrySet()) { System.err.print(e.getKey());
			 * System.err.print(' '); System.err.println(e.getValue()); }
			 */
			dbt.close();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			BufferManager.killDefaultBufferManager();
		}
	}

	public static void environmentSample() throws Throwable {
		BufferManager bm = BufferManager.getDefaultBufferManager();
		try {
			dbUsageSample();
			bm.sync(); // use this to ensure data written
		} finally {
			bm = null; // all cleanups are included in finalize() method
		}
	}

	public static void dbUsageSample() throws Throwable {
		// filename should be an existed DB file, if not, this call will
		// initialize a DB.
		// the DB size is set to the file length (so create a file with specific
		// length)
		DataBase db = DataBase.open("filename");
		dbUsageSample(db);
		db = null; // Yes, needn't close it.
	}

	public static void dbUsageSample(DataBase db) throws IOException,
			ClassNotFoundException {
		Schema sc = schemaCreationSample();
		db.createTable("student", sc);
		// or, with one-to-one index
		// db.createTable("student", sc, "id");
		DbTable dbt = db.openTable("student");
		dbTableUsageSample(dbt);
		dbt.close();
	}

	public static Schema schemaCreationSample() {
		SchemaBuilder sb = new SchemaBuilder();
		sb.add("id", Type.Category.INTEGER, 4); // int
		sb.add("name", Type.Category.CHARACTER, 20); // fixed length byte[]
		// ------------------------------------
		// varying length string, the length field is irrelevant now
		// sb.add("desc", Type.Category.VARY_LENGTH_CHARACTER, 100);
		return sb.newSchema();
	}

	/**
	 * suppose the schema is {id: INT, name: byte[]} byte[] is the constant
	 * length CHARACTER TYPE
	 * 
	 * @see Type#primitiveMap
	 */
	public static void dbTableUsageSample(DbTable dbt) throws IOException,
			ClassNotFoundException {
		Schema sc = dbt.getSchema();
		// read only operation
		for (byte[] record : dbt) { // iterate on the table
			// get column type by index
			Type t = sc.getType(0);
			// you can check precise java type
			assert (t.getJavaType() == Type.JavaType.I32);
			// or check the type category and length
			assert (t.getCategory() == Type.Category.INTEGER && t.getLength() == 4);
			// get column by Index
			int id = (Integer) sc.getValue(record, 0);
			// also get column by name
			Symbol s = new Symbol("name");
			assert (sc.getType(s).getCategory() == Type.Category.CHARACTER);
			byte[] name = (byte[]) sc.getValue(record, s);
		}

		// write operation (remove & update)
		Sequential<byte[]> iter = dbt.iterator();
		while (iter.hasNext()) {
			byte[] record = iter.next();
			Integer id = (Integer) sc.getValue(record, 0);
			if (id.equals(20)) { // condition test
				iter.remove(); // removal
				continue;
			}
			if (id.equals(30)) {
				int i = sc.getIndex(new Symbol("name"));
				Type type = sc.getType(i);
				
				int len = sc.getType(i).getLength();
				// convert a string to byte[], at most `len' bytes
				byte[] newName = TypeHelper.newBytesFromString("MSN", len);
				// change the column (in memory)
				record = sc.putValue(record, i, newName);
				// now `record' references new Record
				// replace the old by force (if cannot replace in-place, remove
				// the old and add one record)
				iter.replaceWithForce(record);
				// if the schema is fixed-length, replacing in-place will always
				// succeed
			}
		}

		// append a new record
		byte[] nn = { 'y', 'a', 'n', 'l', 'i' };
		// sc.newValues() method takes care for the length of byte[]
		dbt.add(sc.newValues((Integer) 5, (byte[]) nn));
		// also Object[] is OK
		Object[] oo = { (Integer) 6, nn };
		dbt.add(sc.newValues(oo));
		// of cource,
		java.util.ArrayList<Object> list = new java.util.ArrayList<Object>();
		list.add((Integer) 7);
		list.add(nn);
		dbt.add(sc.newValues(list.toArray()));
	}

	static SeqFile openTable(File f) throws IOException {
		if (f.size() == 0)
			createTable(f);
		ByteBuffer bb = SeqFile.allocate(Disk.BLOCK_SIZE);
		f.seek(0, File.SEEK_SET);
		f.read(bb);
		bb.flip();
		int magic = bb.getInt();
		if (magic != 0x010a0a01) {
			throw new IOException("NOT A DB FILE");
		}
		byte[] magic_t = new byte[MAGIC_T.length];
		bb.get(magic_t);
		if (!Arrays.equals(magic_t, MAGIC_T))
			throw new IOException("NOT A TABLE");
		int approCount = bb.getInt();
		return new SeqFile(f, FIRST_OFFSET, approCount);
	}

	static void createTable(File f) throws IOException {
		f.ftruncate(FIRST_DATA_OFFSET);
		ByteBuffer bb = SeqFile.allocate(FIRST_DATA_OFFSET);
		bb.putInt(MAGIC);
		bb.put(MAGIC_T);
		bb.putInt(0);
		bb.position(FIRST_OFFSET);
		bb.putInt(FIRST_DATA_OFFSET);
		bb.putInt(0);
		bb.rewind();
		f.write(bb);
	}
}
