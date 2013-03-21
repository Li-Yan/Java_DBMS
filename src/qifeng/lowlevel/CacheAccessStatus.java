package qifeng.lowlevel;

import java.util.concurrent.atomic.AtomicInteger;

public class CacheAccessStatus {
	public static final int STATUS_ITEM_COUNT = 4; 
	
	public AtomicInteger AccessRequestCount;
	public AtomicInteger CacheMissCount;
	/**
	 * protected by {@link LRUBufferManager#listMoveLock}
	 */
	public int PageReplaceCount;
	/**
	 * protected by {@link LRUBufferManager#listMoveLock}
	 */
	public int DontNeedUseCount;

	public CacheAccessStatus() {
		AccessRequestCount = new AtomicInteger(0);
		CacheMissCount = new AtomicInteger(0);
		PageReplaceCount = 0;
		DontNeedUseCount = 0;
	}

	public int getAccessRequestCount() {
		return AccessRequestCount.get();
	}

	public int getCacheMissCount() {
		return CacheMissCount.get();
	}

	public int getPageReplaceCount() {
		return PageReplaceCount;
	}

	public int getDontNeedUseCount() {
		return DontNeedUseCount;
	}
	
	public int incrAccessRequestCount() {
		return AccessRequestCount.incrementAndGet();
	}
	
	public int incrCacheMissCount() {
		return CacheMissCount.incrementAndGet();
	}

	public int incrPageReplaceCount() {
		return ++PageReplaceCount;
	}

	public int incrDontNeedUseCount() {
		return ++DontNeedUseCount;
	}

	public int[] getStatus() {
		int[] status = new int[STATUS_ITEM_COUNT];
		status[0] = this.getAccessRequestCount();
		status[1] = this.getCacheMissCount();
		status[2] = this.getDontNeedUseCount();
		status[3] = this.getPageReplaceCount();
		return status;
	}
}