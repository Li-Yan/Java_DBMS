package qifeng.lowlevel.struct;

import java.io.Serializable;
import java.nio.ByteBuffer;

import qifeng.lowlevel.Disk;

public class SuperBlock implements Serializable {
	private static final long serialVersionUID = -7925958478940074045L;
	/* we can add more data in superblock */
	public static final int SIZE = (2 * Short.SIZE + 9 * Integer.SIZE) >> 3;
	public static final short VERSION = 0x0001;
	public static final short MAGIC = 0x5052;

	public final short version;
	public final short magic;
	public final int block_size;
	public final int block_count;
	public final int max_file_length;
	public final int block_map_start;
	public final int inode_block_start;
	public final int data_block_start;
	public final int inode_count;
	public final int inode_map_count;
	public final int block_map_count;

	private SuperBlock(int dev_size) {
		this.version = VERSION;
		this.magic = MAGIC;

		this.block_size = Disk.BLOCK_SIZE;
		this.block_count = dev_size;

		this.max_file_length = Disk.MAX_FILE_LENGTH;

		/* 64bytes for each, 256 items, total 16K(4 pages) */
		this.inode_count = Disk.INODE_COUNT;

		this.inode_map_count = 1;
		this.block_map_count = (dev_size + Disk.BLOCK_SIZE * Byte.SIZE - 1)
				/ Disk.BLOCK_SIZE / Byte.SIZE;

		this.block_map_start = Disk.IMB_NUM + 1;
		this.inode_block_start = this.block_map_start + this.block_map_count;
		this.data_block_start = this.inode_block_start
				+ (this.inode_count * INode.SIZE + Disk.BLOCK_SIZE - 1)
				/ Disk.BLOCK_SIZE;
	}

	public SuperBlock(final ByteBuffer b) {
		if (b == null)
			throw new NullPointerException();

		version = b.getShort();
		magic = b.getShort();
		block_size = b.getInt();
		block_count = b.getInt();
		max_file_length = b.getInt();
		block_map_start = b.getInt();
		inode_block_start = b.getInt();
		data_block_start = b.getInt();
		inode_count = b.getInt();
		inode_map_count = b.getInt();
		block_map_count = b.getInt();
	}

	public ByteBuffer toBuffer(ByteBuffer b) {
		if (b == null)
			throw new NullPointerException();

		b.putShort(version).putShort(magic);
		b.putInt(block_size).putInt(block_count);
		b.putInt(max_file_length);
		b.putInt(block_map_start);
		b.putInt(inode_block_start).putInt(data_block_start);
		b.putInt(inode_count);
		b.putInt(inode_map_count).putInt(block_map_count);
		return b;
	}

	public static SuperBlock init_superblock(int dev_size) {
		return new SuperBlock(dev_size);
	}
}
