package qifeng.lowlevel.struct;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import qifeng.lowlevel.Disk;

public class DirEntry implements Serializable {
	private static final long serialVersionUID = -7201187008271451613L;
	public static final int SIZE = 64;
	public static final Charset utf8 = Charset.forName("utf-8");

	public final int inode;
	public final String name;

	public DirEntry(int inode, String name) {
		this.inode = inode;
		this.name = name;
	}

	public DirEntry(ByteBuffer b) {
		inode = b.getInt();
		String t;
		t = utf8.decode((ByteBuffer) b.slice().limit(Disk.MAX_FILE_LENGTH))
				.toString();
		name = t.substring(0, t.indexOf(0) >= 0 ? t.indexOf(0) : t.length());
		b.position(b.position() + Disk.MAX_FILE_LENGTH);
	}

	public DirEntry(ByteBuffer b, int index) {
		this((ByteBuffer) b.position(SIZE * index));
	}

	public ByteBuffer toBuffer(ByteBuffer b) {
		// note: one time I thought if b == null, allocate a new one, but I
		// found
		// the byte order will be a problem
		b.putInt(inode);
		ByteBuffer x = (ByteBuffer) utf8.encode(name).rewind();
		int size = x.remaining();
		if (Disk.MAX_FILE_LENGTH < size)
			x.limit(Disk.MAX_FILE_LENGTH);
		b.put(x);
		if (size < Disk.MAX_FILE_LENGTH) {
			/* do padding */
			b.put((byte) 0);
			++size;
			b.position(b.position() + Disk.MAX_FILE_LENGTH - size);
		}
		return b;
	}

	public ByteBuffer toBuffer(ByteBuffer b, int index) {
		if (b == null)
			throw new NullPointerException();
		return toBuffer((ByteBuffer) b.position(SIZE * index));
	}
}
