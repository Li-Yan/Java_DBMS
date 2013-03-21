package qifeng.lowlevel;

/**
 * 
 * A doubly linked list implementation, the idea and manipulation code mostly
 * taken from the Linux source( :) ) though object-oriented code/interface is
 * added. The difference between list head and list node, cannot be seen here.
 * The general usage of this class should be(in which it make sense):
 * 
 * <pre>
 * <code>
 * class MyObject { 
 * public class ListHead extends qifeng.ListHead {
 * 	// this replace the `container_of' macro used in kernel source
 * 	public MyObject container_of() {
 * 		return Myobject.this;
 * 	}
 * }
 * ....
 * }
 * </code>
 * </pre>
 * 
 * method `for_each' family does not take operations on the head, due to this,
 * for_each could be continued if it is broken.
 * 
 * We can get the object linked in the list, it is extremely useful when we have
 * to add an object more than one list. <br>
 * In my implementation, a Page has to be linked in two kinds of list, one is
 * `page dirty' list, the other is for page-replacing(and there are more than
 * one page-replacing queues for different cache policy). <br>
 * Also, in BufferManager, we need to keep a list head, which shall not be a
 * Page object.<br>
 * `is_linked()' makes more sense than empty() when we talk about a node.
 */
class ListHead implements Cloneable {

	public static interface Operator {
		/**
		 * 
		 * @param node node to operate
		 * @return false if you want to break the for loop otherwise true
		 */
		boolean on(ListHead node);
	}

	private ListHead next;
	private ListHead prev;

	public ListHead() {
		init_head();
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}

	final public void init_head() {
		this.next = this.prev = this;
	}

	final public void add(ListHead ne) {
		__list_add(ne, this, this.next);
	}

	final public void add_tail(ListHead ne) {
		__list_add(ne, this.prev, this);
	}

	/**
	 * in Java, call this in the `finalize' method seems `nice', but we don't
	 * know the head, not thread safe. Fortunately Java takes all care about
	 * references.
	 */
	final protected void del() {
		__list_del(this.prev, this.next);
		this.prev = null;
		this.next = null;
	}

	final public void del_init() {
		__list_del(this.prev, this.next);
		this.init_head();
	}

	final public void move(ListHead list) {
		__list_del(list.prev, list.next);
		this.add(list);
	}

	final public void move_tail(ListHead list) {
		__list_del(list.prev, list.next);
		this.add_tail(list);
	}

	final public boolean is_last(final ListHead list) {
		return list.next == this;
	}

	final public boolean empty() {
		return this.next == this;
	}

	final public boolean empty_careful() {
		ListHead next = this.next;
		return (next == this) && (next == this.prev);
	}

	final public boolean linked() {
		return !empty();
	}
	
	final public boolean is_singular() {
		return !list_empty(this) && (this.next == this.prev);
	}

	/**
	 * cut a part of list to a non-empty list is `data-losing', this interface
	 * is better
	 * 
	 * @return a new list (may be empty if nothing at all)
	 */
	final public ListHead cut_position(ListHead entry) {
		ListHead list = new ListHead();

		if (this.empty())
			return list;
		if (this.is_singular() && (this.next != entry && this != entry))
			return list;
		if (entry == this)
			;
		else
			__list_cut_position(list, this, entry);
		return list;
	}

	final public void splice(ListHead list) {
		if (!list.empty()) {
			__list_splice(list, this, this.next);
		}
	}

	final public void splice_tail(ListHead list) {
		if (!list.empty()) {
			__list_splice(list, this.prev, this);
		}
	}

	final public void splice_init(ListHead list) {
		if (!list.empty()) {
			__list_splice(list, this, this.next);
			list.init_head();
		}
	}

	final public void splice_tail_init(ListHead list) {
		if (!list.empty()) {
			__list_splice(list, this.prev, this);
			list.init_head();
		}
	}

	final public ListHead first() {
		return this.next;
	}
	
	final public ListHead last() {
		return this.prev;
	}
	
	final public ListHead for_each(final Operator o) {
		ListHead pos;
		for (pos = this.next; pos != this; pos = pos.next) {
			if (!o.on(pos)) break;
		}
		return pos;
	}

	final public ListHead for_each_prev(final Operator o) {
		ListHead pos;
		for (pos = this.prev; pos != this; pos = pos.prev) {
			if (!o.on(pos)) break;
		}
		return pos;
	}

	final public ListHead for_each_safe(final Operator o) {
		ListHead pos;
		ListHead n;
		for (pos = this.next, n = pos.next; pos != this; pos = n, n = pos.next) {
			if (!o.on(pos)) break;
		}
		return pos;
	}

	final public ListHead for_each_prev_safe(final Operator o) {
		ListHead pos;
		ListHead n;
		for (pos = this.prev, n = pos.prev; pos != this; pos = n, n = pos.prev) {
			if (!o.on(pos)) break;
		}
		return pos;
	}
	// static methods

	public static final void list_init_head(ListHead head) {
		head.next = head.prev = head;
	}

	public static final void list_add(ListHead ne, ListHead head) {
		__list_add(ne, head, head.next);
	}

	public static final void list_add_tail(ListHead ne, ListHead head) {
		__list_add(ne, head.prev, head);
	}

	public static final void list_del(ListHead entry) {
		__list_del(entry.prev, entry.next);
		entry.prev = null;
		entry.next = null;
	}

	public static final void list_replace(ListHead old, ListHead ne) {
		ne.next = old.next;
		ne.next.prev = ne;
		ne.prev = old.prev;
		ne.prev.next = ne;
	}

	public static final void list_del_init(ListHead entry) {
		__list_del(entry.prev, entry.next);
		list_init_head(entry);
	}

	public static final void list_move(ListHead list, ListHead head) {
		__list_del(list.prev, list.next);
		list_add(list, head);
	}

	public static final void list_move_tail(ListHead list, ListHead head) {
		__list_del(list.prev, list.next);
		list_add_tail(list, head);
	}

	public static final boolean list_is_last(final ListHead list, final ListHead head) {
		return list.next == head;
	}

	public static final boolean list_empty(final ListHead head) {
		return head.next == head;
	}

	public static final boolean list_empty_careful(final ListHead head) {
		ListHead next = head.next;
		return (next == head) && (next == head.prev);
	}

	public static final boolean list_is_singular(final ListHead head) {
		return !list_empty(head) && (head.next == head.prev);
	}

	public static final void list_cut_position(ListHead list, ListHead head,
			ListHead entry) {
		if (list_empty(head))
			return;
		if (list_is_singular(head) && (head.next != entry && head != entry))
			return;
		if (entry == head)
			list_init_head(list);
		else
			__list_cut_position(list, head, entry);
	}

	public static final void list_splice(ListHead list, ListHead head) {
		if (!list_empty(list)) {
			__list_splice(list, head, head.next);
		}
	}

	public static final void list_splice_tail(ListHead list, ListHead head) {
		if (!list_empty(list)) {
			__list_splice(list, head.prev, head);
		}
	}

	public static final void list_splice_init(ListHead list, ListHead head) {
		if (!list_empty(list)) {
			__list_splice(list, head, head.next);
			list_init_head(list);
		}
	}

	public static final void list_splice_tail_init(ListHead list, ListHead head) {
		if (!list_empty(list)) {
			__list_splice(list, head.prev, head);
			list_init_head(list);
		}
	}

	// private helpers, yes, all static ones

	private static final void __list_add(ListHead ne, ListHead prev, ListHead next) {
		next.prev = ne;
		ne.next = next;
		ne.prev = prev;
		prev.next = ne;
	}

	private static final void __list_del(ListHead prev, ListHead next) {
		next.prev = prev;
		prev.next = next;
	}

	private static final void __list_cut_position(ListHead list, ListHead head,
			ListHead entry) {
		ListHead new_first = entry.next;
		list.next = head.next;
		list.next.prev = list;
		list.prev = entry;
		entry.next = list;
		head.next = new_first;
		new_first.prev = head;
	}

	private static final void __list_splice(ListHead list, ListHead prev,
			ListHead next) {
		ListHead first = list.next;
		ListHead last = list.prev;
		first.prev = prev;
		prev.next = first;
		last.next = next;
		next.prev = last;
	}
}
