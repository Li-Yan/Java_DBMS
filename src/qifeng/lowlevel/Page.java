package qifeng.lowlevel;

import java.nio.ByteBuffer;

/**
 * the most powerful in this part? No synchronization methods provided, if one
 * drops a page from the page cache, reference to underlined buffer outside will
 * still available, {@link BufferManager} implementor should take care, in this
 * case, that page should be directly written to the underlined storage(disk).
 * Nevertheless, if the cache policy drops a page when the page is accessed, the
 * cache size is too small.
 * 
 * @author qifeng
 * 
 */
class Page {
	/**
	 * belongs
	 */
	Disk disk;
	/**
	 * page number in the disk
	 */
	int pageNumber;
	/**
	 * belongs
	 */
	File file;
	/**
	 * the underlined buffer
	 */
	ByteBuffer buf;

	Page() {
		disk = null;
		pageNumber = 0;
		file = null;
		buf = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Page) {
			Page o = (Page) obj;
			return pageNumber == o.pageNumber && disk.equals(o.disk);
		} else
			return super.equals(obj);
	}

	/**
	 * disk and pageNumber together is the ID
	 */
	@Override
	public int hashCode() {
		return pageNumber ^ disk.hashCode();
	}

}

/**
 * Now the part for use in the cache management.
 * 
 * @author qifeng
 * 
 */
class PageNode extends Page {
	public class ListHead extends qifeng.lowlevel.ListHead {
		public PageNode entry() {
			return PageNode.this;
		}
	}

	/*
	 * next is dedicated for cache management
	 */
	/**
	 * linked to cached pages list
	 */
	PageNode.ListHead node;
	
	/**
	 * pointer to head if the node is linked, otherwise it is a garbage
	 */
	qifeng.lowlevel.ListHead head;

	/**
	 * if linked, the page is dirty and needs commit back to the disk
	 */
	PageNode.ListHead dirty;

	PageNode() {
		super();
		init_list_head();
	}

	PageNode(Page p) {
		super();
		this.disk = p.disk;
		this.pageNumber = p.pageNumber;
		init_list_head();
	}

	private void init_list_head() {
		node = new ListHead();
		head = null;
		dirty = new ListHead();
	}
}
