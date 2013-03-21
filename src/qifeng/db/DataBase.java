package qifeng.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;

import qifeng.lowlevel.Disk;
import qifeng.lowlevel.File;
import qifeng.schema.Schema;
import qifeng.schema.TypeHelper;

/**
 * abstract of a database
 * 
 */
public class DataBase {
	static final String DBM_INITED_SIGNATURE = "/.dbm";
	static final String SYS_PFX = "/sys";
	static final String USR_PFX = "/usr";
	static final String TBL_PFX = "/tbl";
	static final String VIEW_PFX = "/view";
	// at low level, it is a Disk
	private Disk storage;

	public static void createEmptyFile(String name, int size) throws IOException {
		RandomAccessFile f = new RandomAccessFile(name, "rw");
		FileChannel fc = f.getChannel();
		ByteBuffer bb = ByteBuffer.allocateDirect(Disk.BLOCK_SIZE * 16);
		size /= Disk.BLOCK_SIZE * 16;
		for (int i = 0; i < size; ++i) {
			fc.write(bb);
			bb.rewind();
		}
		fc.write(bb);
		fc.close();
	}
	
	public void createTable(String name, Schema sc, String... hashKeys)
			throws IOException {
		name = DataBase.tblNameOnDisk(name, false);
		createTableInternal(name, sc, hashKeys);
	}

	protected void createSysTable(String name, Schema sc, String... hashKeys)
			throws IOException {
		name = DataBase.tblNameOnDisk(name, true);
		createTableInternal(name, sc, hashKeys);
	}

	protected void createTableInternal(String interName, Schema sc,
			String... hashKeys) throws IOException {
		System.out.println(interName);
		try {
			File f = storage.createFile(interName + ".mtd");
			ObjectOutputStream oos = new ObjectOutputStream(Channels
					.newOutputStream(f));
			oos.writeObject(sc);
			oos.close();
			f.close();
			f = storage.createFile(interName + ".dtm");
			TableHelper.createTable(f);
			f.close();
			HashIndices hi = new HashIndices(hashKeys);
			f = storage.createFile(interName + ".idx");
			oos = new ObjectOutputStream(Channels.newOutputStream(f));
			oos.writeObject(hi);
			oos.close();
			f.close();
			storage.sync();
		} catch (Throwable e) {
			throw new IOException("cannot create table " + interName);
		}
	}

	public DbTable openTable(String name) throws IOException {
		name = DataBase.tblNameOnDisk(name, false);
		return openTableInternal(name);
	}

	public DbTable openSysTable(String name) throws IOException {
		name = DataBase.tblNameOnDisk(name, true);
		return openTableInternal(name);
	}

	protected DbTable openTableInternal(String name) throws IOException {
		DbTable dbt = new DbTable();
		try {
			dbt.setName(name);
			dbt.setMtd(storage.openFile(name + ".mtd"));
			dbt.setDtm(storage.openFile(name + ".dtm"));
			dbt.setIdx(storage.openFile(name + ".idx"));
		} catch (IOException e) {
			throw new IOException("cannot open table " + name);
		}
		return dbt;
	}

	public void delTable(String name) throws IOException {
		name = DataBase.tblNameOnDisk(name, false);
		delTableInternal(name);
	}

	protected void delSysTable(String name) throws IOException {
		name = DataBase.tblNameOnDisk(name, true);
		delTableInternal(name);
	}

	protected void delTableInternal(String name) throws IOException {
		try {
			storage.remove(name + ".mtd");
			storage.remove(name + ".dtm");
			storage.remove(name + ".idx");
		} catch (IOException e) {
			throw new IOException("cannot remove table " + name);
		}
	}
	
	public Collection<String> lsTable() throws IOException {
		Object[] ss = storage.lsdir(USR_PFX + TBL_PFX);
		TreeSet<String> ts = new TreeSet<String>();
		for (Object o: ss) {
			String s = o.toString();
			ts.add(s.substring(0, s.lastIndexOf('.')));
		}
		return ts;
	}
	
	public Collection<String> lsView() throws IOException {
		Object[] ss = storage.lsdir(USR_PFX + VIEW_PFX);
		TreeSet<String> ts = new TreeSet<String>();
		for (Object o: ss) {
			String s = o.toString();
			ts.add(s);
		}
		return ts;
	}
	
	public void createView(String name, String statement) throws IOException {
		createViewInternal(name, statement, false);
	}

	protected void createSysView(String name, String statement)
			throws IOException {
		createViewInternal(name, statement, true);
	}

	protected void createViewInternal(String name, String statement, boolean b)
			throws IOException {
		name = viewNameOnDisk(name, b);
		File f = storage.createFile(name);
		ObjectOutputStream oos = new ObjectOutputStream(Channels
				.newOutputStream(f));
		oos.writeUTF(statement);
		oos.close();
		// XXX future may store compiled form of view
	}

	public View openView(String name) throws IOException {
		return openViewInternal(name, false);
	}

	public View openSysView(String name)
			throws IOException {
		return openViewInternal(name, true);
	}

	protected View openViewInternal(String name, boolean b)
			throws IOException {
		String iname = viewNameOnDisk(name, b);
		File f = storage.openFile(iname);
		View v = View.newView(name, f);
		f.close();
		return v;
	}
	
	public void delView(String name) throws IOException {
		name = DataBase.viewNameOnDisk(name, false);
		delViewInternal(name);
	}

	protected void delSysView(String name) throws IOException {
		name = DataBase.viewNameOnDisk(name, true);
		delViewInternal(name);
	}

	protected void delViewInternal(String name) throws IOException {
		try {
			storage.remove(name);
		} catch (IOException e) {
			throw new IOException("cannot remove table " + name);
		}
	}
	
	/**
	 * 
	 * @param name table/view name
	 * @return 0 if not existed, 1 if exist as a table, 2 if exist as a view. <br>
	 * 3 if view and table both existed
	 * @throws IOException
	 */
	public int exists (String name) throws IOException {
		String tblname = tblNameOnDisk(name, false);
		String viewname = viewNameOnDisk(name, false);
		int ret = 0;
		if (storage.exists(tblname+".dtm")) {
			ret |= 1;
		}
		if (storage.exists(viewname)) {
			ret |= 2;
		}
		return ret;
	}
	
	public void sync() {
		try {
			this.storage.sync();
		} catch (Throwable e) {
			//
		}
	}

	public static DataBase open(String pathname) throws IOException {
		DataBase db = new DataBase();
		Disk d = (Disk.openDisk(pathname));
		try {
			d.openFile(DBM_INITED_SIGNATURE).close();
		} catch (FileNotFoundException e) {
			// OK
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine(e.getMessage());
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(
					"seems an uninitilized new database, begin initilization");
			DataBase.initialize(d, pathname);
		}
		db.setStorage(d);
		return db;
	}

	protected static String tblNameOnDisk(String tblname, boolean isSys) {
		if (isSys) {
			return SYS_PFX + TBL_PFX + '/' + tblname;
		} else {
			return USR_PFX + TBL_PFX + '/' + tblname;
		}
	}

	protected static String viewNameOnDisk(String viewname, boolean isSys) {
		if (isSys) {
			return SYS_PFX + VIEW_PFX + '/' + viewname;
		} else {
			return USR_PFX + VIEW_PFX + '/' + viewname;
		}
	}

	protected static void initialize(Disk d, String pathname)
			throws IOException {
		File f = d.createFile(DBM_INITED_SIGNATURE);
		f.write(ByteBuffer.wrap(TypeHelper.newBytesFromString(pathname, -1)));
		f.close();
		d.mkdir(SYS_PFX);
		d.mkdir(SYS_PFX + VIEW_PFX);
		d.mkdir(SYS_PFX + TBL_PFX);
		d.mkdir(USR_PFX);
		d.mkdir(USR_PFX + VIEW_PFX);
		d.mkdir(USR_PFX + TBL_PFX);
	}

	protected void setStorage(Disk d) {
		this.storage = d;
	}

	public Disk getStorage() {
		return storage;
	}

}
