package qifeng.lowlevel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class BufferManager {
	public static final int WILLNEED = 0;
	public static final int NORMAL = 1;
	public static final int DONTNEED = 2;

	private static BufferManager globalDefault;

	/**
	 * write page data to the underlined cache (and will be eventually to the
	 * media), and manage the page in this BufferManager if not managed, direct
	 * writing seems more preferable? <br>
	 * *This method needs more researching* <br>
	 * implementor may throw {@link IllegalArgumentException} if the buffer
	 * field of the page is not in a page size({@link Disk#BLOCK_SIZE})
	 * 
	 * @param page
	 * 
	 */
	abstract public void writePage(Page page) throws IOException;

	/**
	 * read data to {@link Page#buf}, and manage the page in this BufferManager
	 * if not managed
	 * 
	 * @param page
	 *            after successful call, the page's buffer field will contain a
	 *            copy of the cached data
	 */
	abstract public void readPage(Page page) throws IOException;

	/**
	 * read data to {@link Page#buf}, and manage the page in this BufferManager
	 * if not managed <br>
	 * Think this call is to create a read-only map is more suitable.
	 * 
	 * @param page
	 *            after successful call, the page's buffer field will contain a
	 *            read- only view of the data. call this method if possible,
	 *            readPage is too costful.
	 * 
	 */
	abstract public void readPageAsReadOnly(Page page) throws IOException;

	/**
	 * cache the page if not, and the result page is a map of the cache, changes
	 * to the result page will affect cached data
	 * 
	 * @param page
	 *            after successful call, the {@link Page#buf} field of the page
	 *            is set to the cached page, changes to the page will result
	 *            change to underlined cache( and eventually the underlined
	 *            media if {@link #setDirty(Page)} is called).
	 * @throws IOException
	 */
	abstract public void mapPage(Page page) throws IOException;

	/**
	 * drop the page from this BufferManager, if not managed, nothing happened
	 * 
	 * @param page
	 *            a hint for find the page(that is to say, only disk and
	 *            pageNumber fields are related
	 */
	abstract public void dropPage(Page page);

	/**
	 * set the page's cache policy, if not managed, nothing happened
	 * 
	 * @param page
	 * @param policy
	 */
	abstract public void setPagePolicy(Page page, int policy);

	abstract public void setDirty(Page page);

	/**
	 * 
	 * @return true if the BufferManager cannot manage more buffers
	 */
	abstract public boolean isFull();

	abstract public boolean isDirty(Page page);

	abstract public boolean isCached(Page page);

	/**
	 * various sync method for different range sync();
	 */
	abstract public void sync();

	abstract public void sync(Disk d);

	abstract public void sync(Disk d, File f);

	abstract public void sync(Page... pages);

	abstract public int size();

	public static BufferManager getDefaultBufferManager() {
		if (globalDefault == null) {
			synchronized (BufferManager.class) {
				if (globalDefault == null) {
					LRUBufferManager bm = new LRUBufferManager();
					bm.startSyncThread();
					bm.stopDropThread();
					globalDefault = bm;
				}
			}
		}
		return globalDefault;
	}

	public static void killDefaultBufferManager() {
		if (globalDefault != null) {
			synchronized (BufferManager.class) {
				if (globalDefault != null) {
					globalDefault.sync();
					((LRUBufferManager)globalDefault).stopDropThread();
					((LRUBufferManager)globalDefault).stopSyncThread();
					globalDefault = null;
				}
			}
		}
	}
	
	/**
	 * allocate a byte buffer directly for use, with LITTLE_ENDIAN byte order
	 * 
	 * @see BufferManager#myAllocate(int, ByteOrder)
	 */
	static ByteBuffer myAllocate(int size, boolean direct) {
		return myAllocate(size, direct, ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * allocate a byte buffer directly for use, with specific byte order. <br>
	 * this method is just a wrapper around {@link ByteBuffer#allocate(int)} or
	 * {@link ByteBuffer#allocateDirect(int)}, since I have to set the byte
	 * order.
	 * 
	 * @param size
	 *            size of the buffer
	 * @param o
	 *            {@link ByteOrder#LITTLE_ENDIAN} or
	 *            {@link ByteOrder#BIG_ENDIAN}?
	 * @return the buffer
	 * 
	 */
	static ByteBuffer myAllocate(int size, boolean direct, ByteOrder o) {
		if (direct)
			return ByteBuffer.allocateDirect(size).order(o);
		else
			return ByteBuffer.allocate(size).order(o);
	}
}
