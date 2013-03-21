package qifeng.lowlevel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;

import qifeng.lowlevel.struct.INode;

public class File implements ByteChannel {
	final Disk d;
	final int inum;
	final INode inode;
	int pos;

	File(Disk d, int inum, INode inode) {
		this.d = d;
		this.inum = inum;
		this.inode = inode;
		this.pos = 0;
	}

	/**
	 * Close the file <br>
	 * See interface document for detail
	 */
	public synchronized void close() {
		d.closeFile(this);
	}

	/**
	 * Read data from the file<br>
	 * See interface document for detail
	 */
	public synchronized int read(ByteBuffer b) throws IOException {
		if (isOpen())
			return d.read(this, b);
		else
			throw new ClosedChannelException();
	}

	/**
	 * Write data to the file<br>
	 * See interface document for detail
	 */
	public synchronized int write(ByteBuffer b) throws IOException {
		if (isOpen())
			return d.write(this, b);
		else
			throw new ClosedChannelException();
	}

	/**
	 * Tells the file is open or not
	 */
	@Override
	public boolean isOpen() {
		return d.exists(this);
	}

	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	public int position() {
		return pos;
	}

	public synchronized void seek(int off, int whence) {
		int cur;

		switch (whence) {
		case SEEK_SET:
			cur = 0;
			break;
		case SEEK_CUR:
			cur = pos;
			break;
		case SEEK_END:
			cur = size();
			break;
		default:
			throw new IllegalArgumentException();
		}
		cur += off;
		if (cur < 0)
			throw new ArithmeticException("Overflow");

		pos = cur;
	}

	public void flush() {
		d.bm.sync(d, this);
	}

	public int size() {
		return inode.size;
	}

	public void ftruncate(int length) throws IOException {
		if (length < 0)
			throw new IllegalArgumentException();
		synchronized (this) {
			if (length < this.size())
				d.truncate(inode, length);
		}
	}

	/**
	 * FADV_NORMAL, FADV_SEQUENTIAL, FADV_RANDOM affects the entire file
	 * 
	 * @param off
	 * @param len
	 * @param advise
	 */
	public void fadvise(int off, int len, int advise) {
		if (off < 0 || len < 0)
			return;
		synchronized (this) {
			if (!isOpen())
				return;
			d.fadvise(this, off, len, advise);
		}
	}

	/*
	 * fadvise constants, see posix_fadvise (POSIX SYSCALL). Now only
	 * FADV_WILLNEED and FADV_DONTNEED have real effects
	 */
	/**
	 * normally read one page from the disk is not good. 32K seems nice.
	 */
	public static final int FADV_NORMAL = 0;
	/**
	 * a hint, read ahead even more then FADV_NORMAL(double).
	 */
	public static final int FADV_SEQUENTIAL = 1;
	/**
	 * do not read ahead
	 */
	public static final int FADV_RANDOM = 2;
	/**
	 * the specified range will be used only once, Linux treat it as a no-op, I
	 * don't like this either
	 */
	public static final int FADV_NOREUSE = 3;
	/**
	 * the specified range will be read into cache(if cache memory is available)
	 */
	public static final int FADV_WILLNEED = 4;
	/**
	 * the specified range will be freed from the cache (if they are, and
	 * actually drop them into the `{@link LRUBufferManager#dontneed}' list (for
	 * example)
	 */
	public static final int FADV_DONTNEED = 5;
}