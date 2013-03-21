package qifeng.lowlevel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class LRUBufferManager extends BufferManager {
	/**
	 * in pages (also blocks)
	 */
	public static final int CACHE_SIZE = 1024 * 1024 / Disk.BLOCK_SIZE;
	/**
	 * in pages (also blocks)
	 */
	private static final int __READ_SIZE = 32 * 1024 / Disk.BLOCK_SIZE;

	private final ConcurrentHashMap<Page, PageNode> cacheht = new ConcurrentHashMap<Page, PageNode>();
	/**
	 * cache size in pages
	 */
	public final int cachesize;

	private final Semaphore avail;

	/**
	 * if not empty, the page is indicated as `will be used in the future',
	 * actually, the lowest priority from being dropped, also only disk level
	 * important pages will be indicated as this kind
	 */
	private final ListHead willneed;
	/**
	 * if not empty, the page is indicated as `normal page', the BufferManager's
	 * cache-policy will affect this page normally.
	 */
	private final ListHead normal;
	/**
	 * if not empty, it is a page `won't need', these pages will be dropped
	 * periodically (if the buffer manager actually works)
	 */
	private final ListHead dontneed;

	/**
	 * lock this to safely do node transfer between these three list
	 */
	private final Object listMoveLock = new Object();

	/**
	 * if not empty, there are dirty pages
	 */
	private final ListHead dirty;

	private CacheAccessStatus AccessStatus = new CacheAccessStatus();

	public LRUBufferManager() {
		this(CACHE_SIZE);
	}

	public LRUBufferManager(int size) {
		this.cachesize = size;
		this.willneed = new ListHead();
		this.normal = new ListHead();
		this.dontneed = new ListHead();
		this.dirty = new ListHead();
		this.avail = new Semaphore(cachesize);
		// this.AccessStatus and others: constructed when declaration
	}

	@Override
	public void dropPage(Page page) {
		synchronized (listMoveLock) {
			PageNode n = this.getCachedPageNode(page);
			if (n != null)
				// just link it to the `don't need' list

				if (n.head != dontneed || !n.node.linked()) {
					dontneed.move_tail(n.node);
					n.head = dontneed;
				}
		}
	}

	@Override
	public boolean isCached(Page page) {
		return (this.getCachedPageNode(page) != null);
	}

	@Override
	public boolean isDirty(Page page) {
		PageNode n = this.getCachedPageNode(page);
		synchronized (dirty) {
			return n == null ? false : n.dirty.linked();
		}
	}

	@Override
	public boolean isFull() {
		return (this.avail.availablePermits() == 0);
	}

	private PageNode accessRequest(Page page) {
		this.AccessStatus.incrAccessRequestCount();
		PageNode n = this.getCachedPageNode(page);
		return n;
	}

	private PageNode handleCacheMiss(Page page) throws IOException {
		PageNode n;
		this.AccessStatus.incrCacheMissCount();
		n = cacheNewPageTrickly(page);
		return n;
	}

	private void lruListUpdate(PageNode n) {
		/* update LRU list */
		if (n.node.linked() && n.head == dontneed) {
			this.AccessStatus.incrDontNeedUseCount();
			n.head = normal; /*
							 * it is just a joke(?) a page dropped in `dontneed'
							 * list will be used again
							 */
		}
		n.head.move_tail(n.node);
	}

	@Override
	public void mapPage(Page page) throws IOException {
		PageNode n = null;
		synchronized (listMoveLock) {
			n = accessRequest(page);
			if (n == null) /* new page */{
				n = handleCacheMiss(page);
			}
			lruListUpdate(n);
		}
		/* now doing some map page specific operations */
		page.buf = (ByteBuffer) n.buf.duplicate()
				.order(ByteOrder.LITTLE_ENDIAN).rewind();
	}

	@Override
	public void readPage(Page page) throws IOException {
		PageNode n = null;
		/* read page specific operations */
		page.buf = BufferManager.myAllocate(Disk.BLOCK_SIZE, false);
		synchronized (listMoveLock) {
			n = accessRequest(page);
			if (n == null) /* new page */{
				try {
					n = handleCacheMiss(page);
				} catch (IOException e) {
					// try direct reading next
				}
			}
			this.lruListUpdate(n);

		}
		if (n != null) {
			page.buf.put(n.buf); // copy
		} else {
			page.buf = page.disk.readPage(page.pageNumber, page.buf);
			// Oh, if throws again, I'm sorry
		}
		page.buf.rewind();
	}

	@Override
	public void readPageAsReadOnly(Page page) throws IOException {
		PageNode n = null;
		synchronized (listMoveLock) {
			n = accessRequest(page);
			if (n == null) /* new page */{
				try {
					n = handleCacheMiss(page);
				} catch (IOException e) {
					// try direct reading next
				}
			}
			this.lruListUpdate(n);
		}
		/* read-only specific operations */
		if (n != null) {
			page.buf = n.buf.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		} else {
			/* let the readPage routine allocate page */
			page.buf = page.disk.readPage(page.pageNumber, (ByteBuffer) null);
			// Oh, if throws again, I'm sorry
		}
		page.buf.rewind();
	}

	@Override
	public void setDirty(Page page) {
		synchronized (dirty) {
			PageNode n = this.getCachedPageNode(page);
			if (n != null && !n.dirty.linked()) {
				dirty.add_tail(n.dirty);
			}
		}
	}

	@Override
	public void setPagePolicy(Page page, int policy) {
		// add page not cached to list other than `don't need' is definitely
		// wrong
		synchronized (this.listMoveLock) {
			PageNode n = this.getCachedPageNode(page);
			if (n != null) {
				ListHead head;
				switch (policy) {
				case WILLNEED:
					head = this.willneed;
					break;
				case NORMAL:
					head = this.normal;
					break;
				case DONTNEED:
					head = this.dontneed;
					break;
				default:
					throw new IllegalArgumentException();
				}
				if (n.head != head || !n.node.linked()) {
					head.move(n.node);
					n.head = head;
				}
			}
		}
	}

	@Override
	public void sync() {
		synchronized (dirty) {
			dirty.for_each_safe(new ListHead.Operator() {
				@Override
				public boolean on(ListHead node) {
					PageNode.ListHead n = (PageNode.ListHead) node;
					PageNode entry = n.entry();
					try {
						entry.disk.writePage(entry.pageNumber,
								(ByteBuffer) entry.buf.rewind().limit(
										Disk.BLOCK_SIZE));
						entry.dirty.del_init();
					} catch (IOException e) {
						// nothing
					}
					return true;
				}
			});
		}
	}

	/**
	 * just call {@link #sync()} now
	 */
	@Override
	public void sync(Disk d) {
		sync();
	}

	/**
	 * just call {@link #sync()} now
	 */
	@Override
	public void sync(Disk d, File f) {
		sync();
	}

	@Override
	public void sync(Page... pages) {
		synchronized (dirty) {
			PageNode n = null;
			for (Page p : pages) {
				n = this.getCachedPageNode(p);
				if (n != null && n.dirty.linked()) {
					try {
						p.disk.writePage(p.pageNumber, (ByteBuffer) n.buf
								.rewind().position(Disk.BLOCK_SIZE));
						n.dirty.del_init();
					} catch (IOException e) {
						// nothing
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}, and this implementation do throw exception in this case.
	 * 
	 * @throws IOException
	 * 
	 * @throws IllegalArgumentException
	 *             if the buffer field of the page is not in a page size
	 */
	@Override
	public void writePage(Page page) throws IOException {
		if (page.buf.remaining() != Disk.BLOCK_SIZE)
			throw new IllegalArgumentException();
		PageNode n = null;
		synchronized (listMoveLock) {
			n = this.getCachedPageNode(page);
			if (n == null) {
				n = this.acquireOneEntry(page);
			}
			this.lruListUpdate(n);
		}
		synchronized (dirty) {
			/* the original buffer is just lost */
			((ByteBuffer) n.buf.rewind()).put(page.buf);
			dirty.move_tail(n.dirty);
		}

	}

	public int[] getAccessStatus() {
		return AccessStatus.getStatus();
	}

	@Override
	public int size() {
		return this.cachesize;
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.sync();
		this.stopDropThread();
		this.stopSyncThread();
		super.finalize();
	}

	private PageNode getCachedPageNode(Page page) {
		return cacheht.get(page);
	}

	/**
	 * do a trick here, read more pages than needed. GNU cp on Debian read 32768
	 * bytes a time. Here cache up to 8 pages if available. This seems no use to
	 * random-access data pattern, but should be useful for sequential read
	 */
	private PageNode cacheNewPageTrickly(Page page) throws IOException {
		PageNode n;

		int dev_size = page.disk.getDevSize();
		if (page.pageNumber >= dev_size)
			throw new IllegalArgumentException("unvalid page number");
		int count;
		if (dev_size < page.pageNumber + __READ_SIZE) {
			count = dev_size - page.pageNumber;
		} else {
			count = __READ_SIZE;
		}
		/* first, acquire a page */
		n = acquireOneEntry(page);

		/* if we could get more pages follow the page n... */
		ByteBuffer[] all = moreEntryBetter(n, count - 1);

		page.disk.readPage(page.pageNumber, all);

		return n;
	}

	private PageNode acquireOneEntry(final Page page) {
		ListHead x = null;
		PageNode xt;
		if (!this.avail.tryAcquire()) {
			synchronized (listMoveLock) {
				this.AccessStatus.incrPageReplaceCount();
				if (!dontneed.empty())
					x = dontneed.first();
				else if (!normal.empty()) {
					x = normal.first();
				} else {
					assert (!willneed.empty()); /* tragic */
					x = willneed.first();
				}
				xt = ((PageNode.ListHead) x).entry();
				cacheht.remove(xt);
				x.del();
				// dropPage0();
				sync();
			}
		} // else nothing

		synchronized (listMoveLock) {
			PageNode n = new PageNode(page);
			n.buf = BufferManager.myAllocate(Disk.BLOCK_SIZE, false);
			normal.add(n.node);
			n.head = normal;
			cacheht.put(n, n);
			return n;
		}
	}

	private ByteBuffer[] moreEntryBetter(PageNode n, int more) {
		if (more < 0)
			more = 0;
		while (!this.avail.tryAcquire(more)) {
			more /= 2;
		}
		ByteBuffer[] all = new ByteBuffer[1 + more];
		all[0] = n.buf;
		for (int i = 1; i != all.length; ++i) {
			all[i] = BufferManager.myAllocate(Disk.BLOCK_SIZE, false);
		}
		Page t;
		PageNode t1;
		synchronized (listMoveLock) {
			for (int i = 1; i != all.length; ++i) {
				t = n.disk.newPage(n.pageNumber + i);
				if (isCached(t)) { // unlikely if no pages are regularly dropped
					this.avail.release(all.length - i);
					return Arrays.copyOf(all, i);
				}
				t1 = new PageNode(t);
				t1.buf = all[i];
				normal.add(t1.node);
				t1.head = normal;
				cacheht.put(t1, t1);
			}
		}
		return all;
	}

	private void dropPage0() {
		int avail = this.avail.availablePermits();
		final int todrop;
		if (avail < this.cachesize / 15) {
			todrop = this.cachesize / 20;
		} else
			return;
		synchronized (listMoveLock) {
			dontneed.for_each_safe(new ListHead.Operator() {
				int count = todrop;

				@Override
				public boolean on(ListHead node) {
					if (count == 0)
						return false;

					PageNode n = ((PageNode.ListHead) node).entry();
					cacheht.remove(n);
					n.node.del();
					n.head = null;
					LRUBufferManager.this.avail.release();
					--count;
					return true;
				}
			});
		}
	}

	public static final int DEFAULT_PAGE_FLUSH_PERIOD = 500;
	private volatile Thread syncThread = null;

	public void startSyncThread() {
		Thread t = syncThread;
		if (t != null)
			return;
		synchronized (this) {
			t = syncThread;
			if (t != null)
				return;
			syncThread = new Thread() {

				@Override
				public void run() {
					Thread thisThread = Thread.currentThread();

					while (thisThread == syncThread) {
						synchronized (dirty) {
							try {
								dirty.wait(DEFAULT_PAGE_FLUSH_PERIOD);
							} catch (InterruptedException e) {
							}
							if (thisThread != syncThread)
								break;
							if (dirty.empty())
								continue;
						}
						sync();
					}
				}

			};
		}
		syncThread.start();

	}

	public void stopSyncThread() {
		Thread t = syncThread;
		if (t == null)
			return;
		syncThread = null;

		synchronized (dirty) {
			dirty.notify();
		}
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}

	public static final int DEFAULT_PAGE_DROP_PERIOD = 1000;
	private volatile Thread dropThread = null;

	public void startDropThread() {
		Thread t = dropThread;
		if (t != null)
			return;
		synchronized (this) {
			t = dropThread;
			if (t != null)
				return;
			dropThread = new Thread() {

				@Override
				public void run() {
					Thread thisThread = Thread.currentThread();

					while (thisThread == dropThread) {
						synchronized (dontneed) {
							try {
								dontneed.wait(DEFAULT_PAGE_DROP_PERIOD);
							} catch (InterruptedException e) {
							}
						}
						if (thisThread != dropThread)
							break;
						synchronized (listMoveLock) {
							if (dontneed.empty())
								continue;
							dropPage0();
							sync();
						}
					}
				}

			};
			dropThread.start();
		}

	}

	public void stopDropThread() {
		Thread t = dropThread;
		if (t == null)
			return;
		dropThread = null;

		synchronized (dontneed) {
			dontneed.notify();
		}
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}
}