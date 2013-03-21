package qifeng.lowlevel.struct;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class INode implements Serializable {
	private static final long serialVersionUID = 2214231334595357029L;
	
	public static final int SIZE = 64;
	public static final int DIRECT_INDEX_BLOCKS = 7;

	public short mode; /* mode / type */
	private short unused2; /* links; */
	private short unused3; /* uid; */
	private short unused4; /* gid; */
	public int size;
	private int atime; /* unused */
	private int mtime; /* unused */
	private int ctime; /* unused */
	public int[] blocks;
	public int indir_block;
	public int second_level_indir_block;
	private int unused; /* third level indirect block */

	/* -- 64 byte -- */
	/**
	 * create a fresh new (empty) inode.
	 */
	public INode () {
		/* rely the default value for correctness */
		blocks = new int[DIRECT_INDEX_BLOCKS];
	}
	
	public INode (ByteBuffer b) {
		this.set(b);
	}
	
	public INode (ByteBuffer b, int index) {
		this.set(b, index);
	}
	
	public void set(ByteBuffer b) {
		// if (b == null)
		// throw new NullPointerException();

		mode = b.getShort();
		unused2 = b.getShort();
		unused3 = b.getShort();
		unused4 = b.getShort();
		size = b.getInt();
		atime = b.getInt();
		mtime = b.getInt();
		ctime = b.getInt();
		blocks = new int[DIRECT_INDEX_BLOCKS];
		b.asIntBuffer().get(blocks);
		b.position(b.position() + Integer.SIZE * DIRECT_INDEX_BLOCKS
				/ Byte.SIZE);
		indir_block = b.getInt();
		second_level_indir_block = b.getInt();
		unused = b.getInt();
	}

	public void set(ByteBuffer b, int index) {
		this.set((ByteBuffer) b.position(SIZE * index));
	}

	public ByteBuffer toBuffer(ByteBuffer b) {

		b.putShort(mode).putShort(unused2).putShort(unused3).putShort(unused4);
		b.putInt(size).putInt(atime).putInt(mtime).putInt(ctime);
		b.asIntBuffer().put(blocks);
		b.position(b.position() + Integer.SIZE * DIRECT_INDEX_BLOCKS
				/ Byte.SIZE);
		b.putInt(indir_block).putInt(second_level_indir_block).putInt(unused);

		return b;
	}

	public ByteBuffer toBuffer(ByteBuffer b, int index) {
		if (b == null)
			throw new NullPointerException();
		return toBuffer((ByteBuffer) b.position(SIZE * index));
	}

	public boolean is_dir() {
		return (mode & (1 << 15)) != 0;
	}

	public void make_dir() {
		mode |= (1 << 15);
	}
}
