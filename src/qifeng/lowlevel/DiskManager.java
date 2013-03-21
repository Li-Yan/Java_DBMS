package qifeng.lowlevel;

import java.io.IOException;

/**
 * 
 * just dropped
 */
@Deprecated
public class DiskManager {
	public Disk createDisk(String filename) throws IOException {
		return Disk.openDisk(filename);
	}
/*	
	public void destroyDisk(String filename) {
		// nothing
	}
	
	*//**
	 * compatibility to the Instruction document <br> 
	 * @return the disk `current'
	 *//*
	public Disk getCurrentDisk() {
		return null;
	}
	
	*//**
	 * compatibility to the Instruction document <br>
	 * @see #createDisk(String)
	 * @param filename
	 * @return
	 * @throws IOException 
	 *//*
	Disk createFile(String filename) throws IOException {
		return createDisk(filename);
	}
	
	*//**
	 * compatibility to the Instruction document 
	 * @param filename
	 *//*
	void destroyFile(String filename) {
		
	}
	
	*//**
	 * compatibility to the Instruction document
	 * @param filename
	 * @return
	 *//*
	File openFile(String filename) {
		return null;
	}
	
	*//**
	 * compatibility to the Instruction document
	 * @param handle
	 *//*
	void closeFile(File handle) {
		
	}*/
}
