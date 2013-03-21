package qifeng.lowlevel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import qifeng.lowlevel.struct.Bitmaps;
import qifeng.lowlevel.struct.DirEntry;
import qifeng.lowlevel.struct.INode;
import qifeng.lowlevel.struct.SuperBlock;

/**
 * no one knows it is a Disk or a FileSystem...
 * 
 * @author qifeng
 * 
 */
public class Disk {
	public static final class PInt {
		int val;
	}

	public static final int BLOCK_SIZE = 4096;
	public static final int DIRENTS_PER_BLOCK = BLOCK_SIZE / DirEntry.SIZE;
	public static final int INODES_PER_BLOCK = BLOCK_SIZE / INode.SIZE;
	public static final int BYTES_PER_INTEGER = Integer.SIZE / Byte.SIZE;
	public static final int INDICES_PER_BLOCK = BLOCK_SIZE / BYTES_PER_INTEGER;
	public static final int SB_NUM = 1;
	public static final int IMB_NUM = 2;
	/**
	 * file name length
	 */
	public static final int MAX_FILE_LENGTH = 60;
	public static final int INODE_COUNT = 5120;

	public final String underlined_device;
	private FileChannel fc;
	/**
	 * for the file channel, a call to position() and a subsequent call is not
	 * atomic (of course);
	 */
	private final Object fcPosLock = new Object();
	/**
	 * the bm is useful when doing directory operations
	 */
	public final BufferManager bm;
	private final SuperBlock sb;
	/**
	 * files opened
	 */
	final Set<File> files = Collections
			.newSetFromMap(new ConcurrentHashMap<File, Boolean>());
	private int free_inode;
	private int free_block;
	private final Object inodeMapLock = new Object();
	private final Object blockMapLock = new Object();

	Disk(String device, BufferManager bm) throws IOException {
		RandomAccessFile f;

		this.underlined_device = device;
		this.bm = bm;
		f = new RandomAccessFile(this.underlined_device, "rw"); /* set this.fc */
		this.fc = f.getChannel();
		this.sb = SuperBlock.init_superblock((int) f.length() / BLOCK_SIZE);

		if (!this.formated()) {
			format();
		}

		fs_init();
	}

	/**
	 * @return device size in blocks
	 */
	public int getDevSize() {
		return this.sb.block_count;
	}

	public Page newPage(int i) {
		Page page = new Page();
		page.disk = this;
		page.pageNumber = i;
		// page.buf = null;
		return page;
	}

	public Page newPageFile(int i, File f) {
		Page page = newPage(i);
		page.file = f;
		return page;
	}

	private void fs_init() throws IOException {
		root_init();
		get_free_inode(null);
		get_free_block(null);
	}

	private void root_init() throws IOException {
		Page page = newPage(sb.inode_block_start);
		/**
		 * cache the page in the buffer, since inode count will be very small,
		 * it will be very useful
		 */
		bm.readPageAsReadOnly(page);
		page = newPage(sb.data_block_start);
		/**
		 * cache root directory page in the buffer
		 */
		bm.readPageAsReadOnly(page);
	}

	private INode get_root() throws IOException {
		INode dir;
		Page page;
		page = newPage(this.sb.inode_block_start);
		bm.readPageAsReadOnly(page);
		dir = new INode(page.buf);
		page = null;
		return dir;
	}

	private int get_free_inode(PInt p) throws IOException {
		synchronized (this.inodeMapLock) {
			int imb_num;

			imb_num = IMB_NUM;

			Page page = newPage(imb_num);

			if (p == null) /* init phase */{
				bm.readPageAsReadOnly(page);
			} else {
				bm.mapPage(page);
				Bitmaps.use(page.buf, this.free_inode);

				bm.setDirty(page);
				p.val = this.free_inode;
			}

			int i = (p != null ? p.val + 1 : 1);

			for (; i < this.sb.inode_count; ++i) {
				if (!Bitmaps.used(page.buf, i)) {
					this.free_inode = i;
					return 0;
				}
			}

			return -1;
		}
	}

	private int get_free_block(PInt p) throws IOException {
		synchronized (this.blockMapLock) {
			int i;
			int up;
			int bmb_num;
			Page page;

			/* current block number of the bitmap */
			bmb_num = get_block_bitmap_number(this.free_block);

			page = newPage(bmb_num);

			if (p == null) /* init phase */{
				bm.readPageAsReadOnly(page);
			} else {
				bm.mapPage(page);
				Bitmaps.use(page.buf, this.free_block
						% bytes_to_bits(this.sb.block_size));
				bm.setDirty(page);
				p.val = this.free_block;
			}

			/* data_block_start is reserved for root directory */
			i = (p != null ? p.val + 1 : this.sb.data_block_start + 1);

			up = (this.free_block + bytes_to_bits(this.sb.block_size))
					& ~(bytes_to_bits(this.sb.block_size) - 1);
			if (up > getDevSize())
				up = getDevSize();

			retry: do {
				for (; i < up; ++i) {
					if (!Bitmaps.used(page.buf, i
							% bytes_to_bits(this.sb.block_size))) {
						this.free_block = i;
						return 0;
					}
				}

				if (bmb_num < getDevSize() - 1) {
					++bmb_num;
					page = newPage(bmb_num);
					bm.readPageAsReadOnly(page);

					up += this.sb.block_size * Byte.SIZE;
					if (up > getDevSize())
						up = getDevSize();

					continue retry;
				}

				return -1;
			} while (true);
		}
	}

	private void release_inode(int i) throws IOException {
		synchronized (this.inodeMapLock) {
			Page page = newPage(IMB_NUM);
			bm.mapPage(page);
			Bitmaps.unuse(page.buf, i);
			bm.setDirty(page);

			if (this.free_inode > i)
				this.free_inode = i;
		}
	}

	private void release_block(int b) throws IOException {
		synchronized (this.blockMapLock) {
			int bmb_num, bmb_num_new;

			/* release a block belongs to fs internal usage is not allowed */
			if (b < this.sb.data_block_start)
				throw new IllegalArgumentException();

			/* current block number of the bitmap */
			bmb_num = get_block_bitmap_number(this.free_block);

			bmb_num_new = get_block_bitmap_number(b);

			Page page;

			if (bmb_num != bmb_num_new) {
				page = newPage(bmb_num_new);
			} else {
				page = newPage(bmb_num);

			}

			bm.mapPage(page);

			Bitmaps.unuse(page.buf, b % bytes_to_bits(this.sb.block_size));

			bm.setDirty(page);

			if (this.free_block > b)
				this.free_block = b;
			else if (bmb_num != bmb_num_new) {
				// nothing since the new mechanism
			}
		}
	}

	private int get_block_bitmap_number(int b) {
		int bmb_num_new;
		bmb_num_new = this.sb.block_map_start + b
				/ bytes_to_bits(this.sb.block_size);
		return bmb_num_new;
	}

	private static final int ENOENT = Short.MAX_VALUE + 0;
	private static final int ENOTDIR = Short.MAX_VALUE + 1;

	private int getfile(INode dir, String name, INode Presult, PInt pof)
			throws IOException {
		int dirsize, off, up, i = 0;
		Page page;
		PInt bn = new PInt();
		boolean found;
		int index;

		if (name == null)
			throw new NullPointerException();

		if (dir == null) {
			dir = get_root();
		}

		if (!dir.is_dir())
			return -ENOTDIR;

		found = false;
		off = 0;
		DirEntry d = null;

		dirsize = dir.size;

		while (off < dirsize && !found) {
			getblock(dir, off, bn);

			page = newPage(bn.val);

			bm.readPageAsReadOnly(page);

			up = DIRENTS_PER_BLOCK;
			if (dirsize - off < BLOCK_SIZE)
				up = (dirsize - off) / DirEntry.SIZE;
			for (i = 0; i < up; ++i) {
				d = new DirEntry(page.buf, i);
				if (name.equals(d.name)) {
					found = true;
					break;
				}
			}
			off += BLOCK_SIZE;
		}

		if (found) {
			index = d.inode;
			/* note: the offset has advanced one! */
			if (pof != null)
				pof.val = off - BLOCK_SIZE + i * DirEntry.SIZE;
			return (Presult != null ? getinode(index, Presult) : index);
		}

		return -ENOENT;

	}

	private int pathresolv(String name, INode p, PInt pof) throws IOException {
		if (p == null)
			p = new INode();
		PInt off = new PInt();
		INode parent;
		int in;
		String sub;
		// char *tmp, *sub, *saved;
		StringTokenizer st = new StringTokenizer(name, "/");

		parent = null;

		in = -1;

		while (st.hasMoreTokens()) {
			sub = st.nextToken();
			in = getfile(parent, sub, p, off);
			if (in < 0)
				break;
			parent = p;
		}

		if (p != null && in >= 0) {
			if (pof != null)
				pof.val = off.val;
		}

		return in;
	}

	private void getblock(INode i, int pos, PInt p) throws IOException {
		int index;
		int indir_block;
		Page page;

		if (i == null)
			throw new IllegalArgumentException();

		if (pos >= i.size)
			throw new IllegalArgumentException();

		index = pos / BLOCK_SIZE;

		if (index < INode.DIRECT_INDEX_BLOCKS) {
			p.val = i.blocks[index];
			return;
		}
		index -= INode.DIRECT_INDEX_BLOCKS;
		indir_block = i.indir_block;

		do {
			if (index < INDICES_PER_BLOCK) {
				page = newPage(indir_block);
				bm.readPageAsReadOnly(page);

				p.val = page.buf.asIntBuffer().get(index);
				return;
			}

			index -= INDICES_PER_BLOCK;

			if (index >= INDICES_PER_BLOCK * INDICES_PER_BLOCK) /*
																 * file too
																 * large
																 */
				throw new IllegalArgumentException("file pos too large");
			page = newPage(i.second_level_indir_block);
			bm.readPageAsReadOnly(page);

			indir_block = page.buf.asIntBuffer().get(index / INDICES_PER_BLOCK);
			index %= INDICES_PER_BLOCK;

		} while (true);
	}

	/* truncate or extend file to size */
	void truncate(INode i, int size) throws IOException {
		int dir_e;
		int indir_e;
		int sec_indir_e, sec_indir_le = 0;

		int old_dir_e;
		int old_indir_e;
		int old_sec_indir_e, old_sec_indir_le = 0;

		int old_size;
		int allocated;
		int needed;

		PInt pt = new PInt();

		old_size = i.size;
		allocated = (old_size + BLOCK_SIZE - 1) / BLOCK_SIZE;
		needed = (size + BLOCK_SIZE - 1) / BLOCK_SIZE;

		if (needed <= 7) {
			dir_e = needed;
			indir_e = 0;
			sec_indir_e = 0;
		} else if ((needed -= 7) != 0 && needed <= INDICES_PER_BLOCK) {
			dir_e = 7;
			indir_e = needed;
			sec_indir_e = 0;
		} else if ((needed -= INDICES_PER_BLOCK) != 0
				&& needed <= INDICES_PER_BLOCK * INDICES_PER_BLOCK) {
			dir_e = 7;
			indir_e = BLOCK_SIZE / 4;
			sec_indir_e = (needed + INDICES_PER_BLOCK - 1) / INDICES_PER_BLOCK;
			sec_indir_le = needed % INDICES_PER_BLOCK;
		} else
			throw new IllegalArgumentException("file too large");

		/* now check allocated */
		if (allocated <= 7) {
			old_dir_e = allocated;
			old_indir_e = 0;
			old_sec_indir_e = 0;
		} else if ((allocated -= 7) != 0 && allocated <= INDICES_PER_BLOCK) {
			old_dir_e = 7;
			old_indir_e = allocated;
			old_sec_indir_e = 0;
		} else if ((allocated -= INDICES_PER_BLOCK) != 0
				&& allocated <= INDICES_PER_BLOCK * INDICES_PER_BLOCK) {
			old_dir_e = 7;
			old_indir_e = INDICES_PER_BLOCK;
			old_sec_indir_e = (allocated + INDICES_PER_BLOCK - 1)
					/ INDICES_PER_BLOCK;
			old_sec_indir_le = allocated % INDICES_PER_BLOCK;
		} else
			throw new Error();

		/* now start working */
		acquire_release_range(i.blocks, old_dir_e, dir_e);
		pt.val = i.indir_block;
		acquire_release_range_indirect(pt, old_indir_e, indir_e);
		i.indir_block = pt.val;
		pt.val = i.second_level_indir_block;
		acquire_release_range_indirect2(pt, old_sec_indir_e, old_sec_indir_le,
				sec_indir_e, sec_indir_le);
		i.second_level_indir_block = pt.val;

		/* update size */
		i.size = size;
	}

	private int getinode(int inum, INode p) throws IOException {
		int blk_num;
		int sub_i;
		Page page;

		if (p == null)
			throw new IllegalArgumentException();

		blk_num = get_inode_block_number(inum);
		sub_i = inum % INODES_PER_BLOCK;

		page = newPage(blk_num);
		bm.readPageAsReadOnly(page);

		p.set(page.buf, sub_i);
		return inum;
	}

	private void setinode(int inum, INode i) throws IOException {
		int blk_num;
		int sub_i;
		Page page;

		if (i == null)
			return;

		blk_num = get_inode_block_number(inum);
		sub_i = inum % INODES_PER_BLOCK;

		page = newPage(blk_num);
		bm.mapPage(page);

		i.toBuffer(page.buf, sub_i);

		bm.setDirty(page);
	}

	int get_inode_block_number(int inum) {
		int blk_num;
		blk_num = this.sb.inode_block_start + inum / INODES_PER_BLOCK;
		return blk_num;
	}

	private void acquire_release_range(int[] blocks, int old_e, int new_e)
			throws IOException {
		int i;
		int err;
		PInt p = new PInt();

		if (old_e < new_e) /* acquire */{
			for (i = old_e; i < new_e; ++i) {
				err = get_free_block(p);
				if (err < 0) {
					// System.err.printf("can not extend %h from %u to %u\n",
					// blocks, old_e, new_e);
					throw new IOException("disk space not enough");
				}
				blocks[i] = p.val;
			}
		} else if (old_e > new_e) /* release */{
			for (i = new_e; i < old_e; ++i) {
				release_block(blocks[i]);
			}
		}

	}

	private void acquire_release_range(IntBuffer blocks, int old_e, int new_e)
			throws IOException {
		int i;
		int err;
		PInt p = new PInt();

		if (old_e < new_e) /* acquire */{
			for (i = old_e; i < new_e; ++i) {
				err = get_free_block(p);
				if (err < 0) {
					// System.err.printf("can not extend %h from %u to %u\n",
					// blocks, old_e, new_e);
					throw new IOException("disk space not enough");
				}
				blocks.put(i, p.val);
			}
		} else if (old_e > new_e) /* release */{
			for (i = new_e; i < old_e; ++i) {
				release_block(blocks.get(i));
			}
		}

	}

	private void acquire_release_range_indirect(PInt indir, int old_e, int new_e)
			throws IOException {
		Page page;
		int err;

		if ((old_e | new_e) == 0) {
			return;
		} else if (old_e == 0 && new_e > 0) {
			err = get_free_block(indir);
			if (err < 0)
				throw new IOException("disk space not enough");
		} /* else indirect block should be released */

		page = newPage(indir.val);
		bm.mapPage(page);

		acquire_release_range(page.buf.asIntBuffer(), old_e, new_e);

		if (new_e == 0) {
			release_block(indir.val);
			indir.val = 0;
		} else {
			bm.setDirty(page);
		}
	}

	private void acquire_release_range_indirect2(PInt second_indir, int old_e,
			int old_le, int new_e, int new_le) throws IOException {
		Page page;
		int i;
		int err;

		if (old_le == 0)
			old_le = INDICES_PER_BLOCK;
		if (new_le == 0)
			new_le = INDICES_PER_BLOCK;
		if (old_e == 0)
			old_le = 0;
		if (new_e == 0)
			new_le = 0;

		if ((old_e | new_e) == 0) {
			return;
		} else if (old_e == 0 && new_e > 0) {
			err = get_free_block(second_indir);
			if (err < 0)
				throw new IOException("disk space not enough");
		} /* else second indirect block should be released */

		page = newPage(second_indir.val);
		bm.mapPage(page);

		PInt pt = new PInt();

		for (i = (old_e != 0 ? old_e - 1 : 0); i < new_e; ++i, old_le = 0) {
			pt.val = page.buf.asIntBuffer().get(i);
			acquire_release_range_indirect(pt, old_le, (i == new_e - 1 ? new_le
					: INDICES_PER_BLOCK));
			page.buf.asIntBuffer().put(i, pt.val);
		}
		/*
		 * if (old_e == new_e) this was checked, we should not do that again
		 */
		for (i = (new_e != 0 ? new_e - 1 : 0); new_e != old_e && i < old_e; ++i, new_le = 0) {
			pt.val = page.buf.asIntBuffer().get(i);
			acquire_release_range_indirect(pt, (i == old_e - 1 ? old_le
					: INDICES_PER_BLOCK), new_le);
			page.buf.asIntBuffer().put(i, pt.val);
		}

		if (new_e == 0) {
			release_block(second_indir.val);
			second_indir.val = 0;
		} else {
			bm.setDirty(page);
		}

	}

	private void add_entry(INode dir, DirEntry entry) throws IOException {
		int pos, size;
		PInt blk_num = new PInt();
		Page page;

		if (dir == null) {
			dir = get_root();
		}

		pos = dir.size;
		size = pos + DirEntry.SIZE;

		this.truncate(dir, size);

		getblock(dir, pos, blk_num);

		page = newPage(blk_num.val);
		bm.mapPage(page);
		entry.toBuffer(page.buf, pos % BLOCK_SIZE / DirEntry.SIZE);
		bm.setDirty(page);
	}

	private void remove_entry(INode dir, int pos) throws IOException {
		ByteBuffer last;
		int size;
		PInt blk_num = new PInt();
		PInt last_blk_num = new PInt();
		Page page;

		/* now search is not needed */
		if (dir == null) {
			dir = get_root();
		}

		size = dir.size - DirEntry.SIZE;

		if (pos != size) {
			/* we had to do some moving */
			getblock(dir, size, last_blk_num);

			page = newPage(last_blk_num.val);
			bm.mapPage(page);

			page.buf.position(size % BLOCK_SIZE);

			last = (ByteBuffer) page.buf.slice().limit(DirEntry.SIZE);

			getblock(dir, pos, blk_num);

			if (blk_num.val != last_blk_num.val) {
				page = newPage(blk_num.val);
				bm.mapPage(page);
			}

			((ByteBuffer) page.buf.position(pos % BLOCK_SIZE)).put(last);

			bm.setDirty(page);
		}

		truncate(dir, size);
	}

	private boolean formated() throws IOException {
		ByteBuffer b = this.readPage(SB_NUM, (ByteBuffer) null);
		ByteBuffer z = BufferManager.myAllocate(SuperBlock.SIZE, false);
		sb.toBuffer(z);
		b.rewind().limit(SuperBlock.SIZE);
		z.rewind();
		return z.equals(b);
	}

	private void format() throws IOException {
		ByteBuffer b = BufferManager.myAllocate(BLOCK_SIZE, false);

		Bitmaps.use(b, 0);
		writePage(IMB_NUM, b);
		b.rewind();

		for (int i = 0; i <= sb.data_block_start; ++i) {
			Bitmaps.use(b, i);
		}
		writePage(sb.block_map_start, b);
		b.rewind();

		INode i = new INode();
		i.make_dir();
		i.size = 2 * DirEntry.SIZE;
		i.blocks[0] = sb.data_block_start;
		i.toBuffer(b);
		b.rewind();
		writePage(sb.inode_block_start, b);
		b.rewind();

		(new DirEntry(0, ".")).toBuffer(b);
		(new DirEntry(0, "..")).toBuffer(b);
		b.rewind();
		writePage(sb.data_block_start, b);
		b.rewind();

		sb.toBuffer(b);
		b.rewind();
		writePage(SB_NUM, b);
	}

	/**
	 * This is a method for reopen the unexpected closed file It should be no
	 * use if the program is thread safe
	 * 
	 * @return a new file channel representing the disk
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unused")
	private FileChannel openDiskChannel() throws FileNotFoundException {
		RandomAccessFile f = new RandomAccessFile(this.underlined_device, "rw");
		return f.getChannel();
	}

	/**
	 * write buffer to numbered page of disk. write from numbered page and until
	 * the buffer is exhausted is an extension.
	 * 
	 * @param pageNumber
	 * @param b
	 *            b.remaining() should be a multiple of {@link #BLOCK_SIZE}, if
	 *            b is {@code null}, nothing happens
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             if pageNumber and b.remaining() indicate out of the disk
	 *             space, or b.remaining() has incorrect size
	 * 
	 */
	void writePage(int pageNumber, ByteBuffer b) throws IOException {
		if (b == null)
			return;
		if (b.remaining() % BLOCK_SIZE != 0
				|| getDevSize() < pageNumber + b.remaining() / BLOCK_SIZE)
			throw new IllegalArgumentException(illegalBlockSpecificMesg(
					pageNumber, b.remaining()));
		fc.write(b, BLOCK_SIZE * pageNumber);
	}

	/**
	 * read numbered page to buffer. read from numbered page and until the
	 * buffer is exhausted is an extension.
	 * 
	 * @param pageNumber
	 * @param b
	 *            If b is null, allocate an one-page indirect buffer for
	 *            reading. indirect buffer is better for cache access. If b is
	 *            not null, b.remaining() should be a multiple of
	 *            {@link #BLOCK_SIZE}
	 * @return the buffer fulfilled
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             if pageNumber and b.remaining() indicate out of the disk
	 *             space, or b.remaining() has incorrect size
	 */
	ByteBuffer readPage(int pageNumber, ByteBuffer b) throws IOException {
		if (b == null)
			b = BufferManager.myAllocate(BLOCK_SIZE, false);
		if (b.remaining() % BLOCK_SIZE != 0
				|| getDevSize() < pageNumber + b.remaining() / BLOCK_SIZE)
			throw new IllegalArgumentException(illegalBlockSpecificMesg(
					pageNumber, b.remaining()));
		fc.read(b, BLOCK_SIZE * pageNumber);
		return b;
	}

	/**
	 * gather write support, sum of the remaining bytes in the buffers should be
	 * a multiple of {@link #BLOCK_SIZE}
	 * 
	 * @param pageNumber
	 * @param buffers
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             sum of the remaining bytes not suitable or the disk is not
	 *             that large
	 */
	void writePage(int pageNumber, ByteBuffer[] buffers) throws IOException {
		int sum = 0;

		for (ByteBuffer b : buffers) {
			sum += b.remaining();
		}
		if (sum % BLOCK_SIZE != 0
				|| getDevSize() < pageNumber + sum / BLOCK_SIZE)
			throw new IllegalArgumentException(illegalBlockSpecificMesg(
					pageNumber, sum));
		if (sum > 0) {
			synchronized (fcPosLock) {
				int pos = BLOCK_SIZE * pageNumber;
				int c;
				do {
					c = (int) fc.position(pos).write(buffers);
					assert (c % BLOCK_SIZE) == 0;
					sum -= c;
					pos += c;
					buffers = Arrays.copyOfRange(buffers, c / BLOCK_SIZE,
							buffers.length);
				} while (sum > 0);
			}
		}
	}

	/**
	 * scatter read support, sum of the remaining bytes in the buffers should be
	 * a multiple of {@link #BLOCK_SIZE}
	 * 
	 * @param pageNumber
	 * @param buffers
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             sum of the remaining bytes not suitable or the disk is not
	 *             that large
	 */
	void readPage(int pageNumber, ByteBuffer[] buffers) throws IOException {
		int sum = 0;

		for (ByteBuffer b : buffers) {
			sum += b.remaining();
		}
		if (sum % BLOCK_SIZE != 0
				|| getDevSize() < pageNumber + sum / BLOCK_SIZE)
			throw new IllegalArgumentException(illegalBlockSpecificMesg(
					pageNumber, sum));
		if (sum > 0)
			synchronized (fcPosLock) {
				int pos = BLOCK_SIZE * pageNumber;
				int c;
				do {
					c = (int) fc.position(pos).read(buffers);
					assert (c % BLOCK_SIZE) == 0;
					sum -= c;
					pos += c;
					buffers = Arrays.copyOfRange(buffers, c / BLOCK_SIZE,
							buffers.length);
				} while (sum > 0);
			}
	}

	private String illegalBlockSpecificMesg(int pageNumber, int bytes) {
		return new Formatter().format("from page number %d, bytes %d",
				pageNumber, bytes).toString();
	}

	void sync_file_meta(int inum) {
		Page mpd;
		mpd = newPage(get_inode_block_number(inum));
		bm.sync(mpd);
	}

	void sync_file_meta(int... inums) {
		ArrayList<Integer> s = new ArrayList<Integer>();
		int x;
		for (int i : inums) {
			x = get_inode_block_number(i);
			if (!s.contains(x))
				s.add(x);
		}

		Page[] ps = new Page[s.size()];
		for (int i : s) {
			ps[i] = newPage(i);
		}
		bm.sync(ps);
	}

	/**
	 * translate an error number to an exception
	 * 
	 * @param filename
	 *            the filename of the exception
	 * @param errno
	 *            the error number
	 * @throws FileNotFoundException
	 *             the generated exception
	 */
	private static void file_not_found(String filename, int errno)
			throws FileNotFoundException {
		switch (errno) {
		case ENOENT:
			throw new FileNotFoundException(filename + ": not found");
		case ENOTDIR:
			throw new FileNotFoundException("a component in path of "
					+ filename + " is not directory");
		}
	}

	/**
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public File openFile(String filename) throws IOException {
		int inum;
		INode node = new INode();

		inum = pathresolv(filename, node, null);
		if (inum < 0) {
			file_not_found(filename, -inum);
		}

		if (node.is_dir()) {
			throw new FileNotFoundException(filename + ": directory");
		}

		File f = new File(this, inum, node);
		this.files.add(f);
		return f;
	}

	public File createFile(String filename) throws IOException {
		PInt pinum = create_or_mkdir(filename);

		INode newfile = new INode();
		setinode(pinum.val, newfile);

		File f = new File(this, pinum.val, newfile);

		this.files.add(f);

		// sync_dir_file_meta(dnum, pinum.val);

		// note, sync the disk as a whole
		bm.sync(this);

		return f;
	}

	public boolean exists(String filename) throws IOException {
		int inum;
		INode node = new INode();
		inum = pathresolv(filename, node, null);
		return (inum >= 0);
	}
	
	boolean exists(File file) {
		return this.files.contains(file);
	}

	static final String FILE_CLOSED_OR_NOT_BELONGS_TO_THIS_DISK = "file closed or not belongs to this disk";

	int read(File f, ByteBuffer __user_buf) throws IOException {
		int c;
		PInt blk_num = new PInt();
		Page page;
		int spos;
		int count;

		if (f == null || __user_buf == null)
			throw new NullPointerException();

		if (!exists(f))
			throw new IllegalArgumentException(FILE_CLOSED_OR_NOT_BELONGS_TO_THIS_DISK);

		spos = __user_buf.position();
		count = __user_buf.remaining();

		while (count > 0 && f.pos < f.inode.size) {
			getblock(f.inode, f.pos, blk_num);
			page = newPageFile(blk_num.val, f);
			// note, it caches the page, when the count is very large, it will
			// cache a
			// lot of pages. Though actually in most cases, one record is not
			// too long.
			bm.readPageAsReadOnly(page);

			c = BLOCK_SIZE - f.pos % BLOCK_SIZE;
			c = (c >= count ? count : c);
			c = (c >= f.inode.size - f.pos ? f.inode.size - f.pos : c);

			page.buf.position(page.buf.position() + f.pos % BLOCK_SIZE);
			page.buf.limit(page.buf.position() + c);

			__user_buf.put(page.buf);
			f.pos += c;
			count -= c;
		}

		return __user_buf.position() - spos;
	}

	int write(File f, ByteBuffer __user_buf) throws IOException {
		int c;
		PInt blk_num = new PInt();
		Page page;
		int spos;
		int count;

		if (f == null || __user_buf == null)
			throw new NullPointerException();

		if (!exists(f))
			throw new IllegalArgumentException(FILE_CLOSED_OR_NOT_BELONGS_TO_THIS_DISK);

		count = __user_buf.remaining();

		if (f.pos + count > f.inode.size) {
			try {
				truncate(f.inode, f.pos + count);
			} catch (IOException e) {
				truncate(f.inode, f.pos);
				throw new IOException("disk space not enough", e);
			}
			setinode(f.inum, f.inode);
			// again, sync the disk as a whole
			bm.sync(this);
		}

		spos = __user_buf.position();

		while (count > 0 && f.pos < f.inode.size) {
			getblock(f.inode, f.pos, blk_num);

			page = newPageFile(blk_num.val, f);

			if (f.pos % BLOCK_SIZE != 0 || count < BLOCK_SIZE) {
				bm.mapPage(page); /* this cause a page being cached */
			} else if (bm.isCached(page)) { /* oh, has to do this */
				bm.mapPage(page);
			} // else using direct write (do not pollute cache anyway)

			c = BLOCK_SIZE - f.pos % BLOCK_SIZE;
			c = (c >= count ? count : c);

			__user_buf.limit(__user_buf.position() + c);

			if (page.buf != null) {
				/* write-back */
				page.buf.position(page.buf.position() + f.pos % BLOCK_SIZE);
				page.buf.limit(page.buf.position() + c);
				page.buf.put(__user_buf);
				bm.setDirty(page);
			} else {
				/* direct write */
				assert (__user_buf.remaining() == BLOCK_SIZE);
				try {
					this.writePage(blk_num.val, __user_buf);
				} catch (IOException e) {
					throw new IOException("disk failure", e);
				}
			}

			f.pos += c;
			count -= c;
		}

		// no data sync
		// sync_file_meta(f.inum); /* do meta sync */

		return __user_buf.position() - spos;
	}

	void closeFile(File f) {
		if (this.files.remove(f)) {
			bm.sync(this, f);
		} // else the file is not belonged here
	}

	void fadvise(File file, int off, int len, int advise) {
		if (file == null)
			throw new NullPointerException();

		if (!exists(file))
			throw new IllegalArgumentException();

		if (off < 0 || len < 0)
			throw new IllegalArgumentException();

		if (len == 0)
			len = file.inode.size;
		int until = off + len;
		until = (until > file.inode.size ? file.inode.size : until);
		Page page = null;
		PInt pi = new PInt();
		try {
			switch (advise) {
			case File.FADV_NORMAL:
			case File.FADV_SEQUENTIAL:
			case File.FADV_RANDOM:
			case File.FADV_NOREUSE:
				break; /* this means no operation current */
			case File.FADV_WILLNEED:
				if (this.bm.size() * BLOCK_SIZE / 2 < until - off) {
					until = off + this.bm.size() * BLOCK_SIZE / 2;
				}
				while (off < until) {
					getblock(file.inode, off, pi);
					page = newPageFile(pi.val, file);
					bm.readPageAsReadOnly(page);
					off += BLOCK_SIZE;
				}
				break;
			case File.FADV_DONTNEED:
				while (off < until) {
					getblock(file.inode, off, pi);
					page = newPage(pi.val);
					bm.dropPage(page);
					off += BLOCK_SIZE;
				}
				break;
			default:
				throw new IllegalArgumentException();
			}
		} catch (IOException e) {

		}
	}

	public void remove(String filename) throws IOException {
		remove_file_or_directory(filename, false);
	}

	public void rename(String oldname, String newname) throws IOException {
		int dnum;
		INode pdir;
		int inum;
		INode pf;
		PInt ppos;
		int ndnum;
		INode npdir;

		if (oldname == null || newname == null)
			throw new NullPointerException();

		int sep = sep_path(oldname);
		String dname = sep >= 0 ? oldname.substring(0, sep) : "";
		String bname = oldname.substring(sep + 1);

		if (dname.equals("")) {
			pdir = get_root();
			dnum = 0;
		} else {
			pdir = new INode();
			dnum = pathresolv(dname, pdir, null);
		}

		if (dnum < 0)
			file_not_found(dname, -dnum);

		if (!pdir.is_dir())
			throw new FileNotFoundException(oldname
					+ ": parent not a directory");

		pf = new INode();
		ppos = new PInt();

		inum = getfile(pdir, bname, pf, ppos);

		if (inum < 0)
			file_not_found(oldname, -inum);

		sep = sep_path(newname);
		String ndname = sep >= 0 ? newname.substring(0, sep) : "";
		String nbname = newname.substring(sep + 1);

		if (ndname.equals("")) {
			npdir = get_root();
			ndnum = 0;
		} else {
			npdir = new INode();
			ndnum = pathresolv(ndname, npdir, null);
		}

		if (ndnum < 0)
			file_not_found(ndname, -ndnum);

		if (!npdir.is_dir())
			throw new FileNotFoundException(newname
					+ ": parent not a directory");

		int ninum = getfile(npdir, nbname, null, null);

		if (ninum >= 0)
			throw new IOException(newname + ": destination file existed");

		// record the addition and removal
		add_entry(npdir, new DirEntry(inum, nbname));
		remove_entry(pdir, ppos.val);
		setinode(ndnum, npdir);
		setinode(dnum, pdir);

		bm.sync(this);
	}

	public void mkdir(String dirname) throws IOException {
		PInt pinum = create_or_mkdir(dirname);

		INode newfile = new INode();
		newfile.make_dir();
		setinode(pinum.val, newfile);

		// sync_dir_file_meta(dnum, pinum.val);

		// note, sync the disk as a whole
		bm.sync(this);
	}

	public void rmdir(String dirname) throws IOException {
		remove_file_or_directory(dirname, true);
	}

	public Object[] lsdir(String dirname) throws IOException {
		int inum;
		INode dir = new INode();

		inum = pathresolv(dirname, dir, null);
		if (inum < 0) {
			file_not_found(dirname, -inum);
		}

		if (dir.is_dir()) {
			return list_dir(dir);
		} else {
			throw new FileNotFoundException(dirname + ": is not directory");
		}
	}

	private Object[] list_dir(INode dir) throws IOException {
		int dirsize, off, up, i = 0;
		Page page;
		PInt bn = new PInt();
		boolean found;
		ArrayList<String> names = new ArrayList<String>();
		
		found = false;
		off = 0;
		DirEntry d = null;
		dirsize = dir.size;

		while (off < dirsize && !found) {
			getblock(dir, off, bn);

			page = newPage(bn.val);

			bm.readPageAsReadOnly(page);

			up = DIRENTS_PER_BLOCK;
			if (dirsize - off < BLOCK_SIZE)
				up = (dirsize - off) / DirEntry.SIZE;
			for (i = 0; i < up; ++i) {
				d = new DirEntry(page.buf, i);
				names.add(d.name);
			}
			off += BLOCK_SIZE;
		}
		return names.toArray();
	}
	
	private void remove_file_or_directory(String filename, boolean rmdir)
			throws IOException {
		int dnum;
		int inum;
		INode pdir;
		INode pf;
		PInt ppos;

		if (filename == null)
			throw new NullPointerException();

		int sep = sep_path(filename);
		String dname = sep >= 0 ? filename.substring(0, sep) : "";
		String bname = filename.substring(sep + 1);

		if (dname.equals("")) {
			pdir = get_root();
			dnum = 0;
		} else {
			pdir = new INode();
			dnum = pathresolv(dname, pdir, null);
		}

		if (dnum < 0)
			file_not_found(dname, -dnum);

		if (!pdir.is_dir()) {
			throw new FileNotFoundException(filename
					+ ": parent not a directory");
		}

		pf = new INode();
		ppos = new PInt();

		inum = getfile(pdir, bname, pf, ppos);

		if (inum < 0)
			file_not_found(filename, -inum);

		if (rmdir) /* rmdir */{
			if (!pf.is_dir()) {
				throw new IOException(filename + ": is not directory");
			}
			if (pf.size != 0) {
				throw new IOException(filename + ": directory not empty");
			}
		} else /* remove */{
			if (pf.is_dir()) {
				throw new IOException(filename
						+ ": is directory, use rmdir instead");
			}
			truncate(pf, 0);
		}

		remove_entry(pdir, ppos.val);

		/* record meta changes to directory */
		setinode(dnum, pdir);

		release_inode(inum);

		bm.sync(this);
	}

	private PInt create_or_mkdir(String filename) throws FileNotFoundException,
			IOException {
		int err;
		int dnum;
		PInt pinum;
		INode pdir;
		DirEntry new_ent;

		if (filename == null)
			throw new NullPointerException();

		int sep = sep_path(filename);
		String dname = sep >= 0 ? filename.substring(0, sep) : "";
		String bname = filename.substring(sep + 1);

		if (dname.equals("")) {
			pdir = get_root();
			dnum = 0;
		} else {
			pdir = new INode();
			dnum = pathresolv(dname, pdir, null);
		}

		if (dnum < 0)
			file_not_found(dname, -dnum);

		if (!pdir.is_dir())
			throw new FileNotFoundException(filename
					+ ": parent not a directory");

		pinum = new PInt();

		pinum.val = getfile(pdir, bname, null, null);

		if (pinum.val >= 0)
			throw new IOException(filename + ": existed already");

		/* now begin to new a file */
		err = get_free_inode(pinum);

		if (err < 0)
			throw new IOException(filename
					+ ": max allowed file number exceeded");

		new_ent = new DirEntry(pinum.val, bname);

		add_entry(pdir, new_ent);

		/* record meta changes to directory */
		setinode(dnum, pdir);
		return pinum;
	}
	
	/**
	 * sync the disk. note there are no close() method, just set the object reference to null.
	 * The {@link #finalize()} method will do the job 
	 */
	public void sync() throws Throwable {
		this.bm.sync(this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.bm.sync(this);
		this.fc.close();
		super.finalize();
	}

	private static int bytes_to_bits(int bytes) {
		return bytes * Byte.SIZE;
	}

	private static int sep_path(String filename) {
		int sep = filename.lastIndexOf('/');
		return sep;
	}
	
	public static Disk openDisk(String filename) throws IOException {
		return new Disk(filename, BufferManager.getDefaultBufferManager());
	}
	
	/**
	 * destroy the disk named filename, actually it is filled with `\0' bytes
	 * @param filename
	 * @throws IOException
	 */
	public static void destroy(String filename) throws IOException {
		// only system calls are used
		FileChannel fc = (new RandomAccessFile(filename, "rw")).getChannel();
		long size = fc.size();
		fc.truncate(0);
		fc.truncate(size);
		fc.close();
	}

}
