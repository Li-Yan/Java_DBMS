package qifeng.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.Channels;

import qifeng.lowlevel.File;

/**
 * 
 * a representation of an existed view. Use {@link DataBase#delView(String)} and
 * {@link DataBase#createView(String, String)} for creation/deletion/modification
 *
 */
public class View {
	private String name;
	private String statement;

	public View() {
	}

	public View(String name, String statement) {
		this.setName(name);
		this.setStatement(statement);
	}
	
	/**
	 * @return view name in user point of view
	 */
	public String getUserName() {
		return name.substring(name.lastIndexOf('/')+1);
	}
	
	public boolean isSysView() {
		return name.startsWith(DataBase.SYS_PFX);
	}
	
	/**
	 * @return internal view name
	 */
	public String getName() {
		return name;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

	public static View newView(String name, File f) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(Channels
				.newInputStream(f));
		String s = ois.readUTF();
		ois.close();
		return new View(name, s);
	}

	/**
	 * set internal view name, used by constructors, initializers.
	 */
	protected void setName(String name) {
		this.name = name;
	}

}
