package qifeng.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

import qifeng.db.TableHelper;
import qifeng.lowlevel.File;

public class SeqFile implements Iterable<byte[]> {
	private File f;
	private int firstIndex;
	// private int firstOffset;
	// private int negFirstFree;
	private int approCount; // approximately records count

	public SeqFile(File f, int offset, int curCount) throws IOException {
		if (offset < 0 || offset >= f.size()) {
			throw new IllegalArgumentException();
		}
		synchronized (f) {
			this.f = f;
			this.firstIndex = offset;
			this.approCount = curCount;
			int x = readInt(firstIndex);
			int y = readInt();
			assert x > 0 && 0 >= y;
		}
	}

	public void close() {
		f.close();
	}

	/**
	 * after successful call, file position is set to the start of the inserted
	 * record
	 * 
	 * @param record
	 */
	public void add(byte[] record) {
		try {
			int len = record.length + 4;
			int prevFreeAddr = firstIndex;
			int prev = readInt(firstIndex + 4);
			int curFreeAddr;
			while (prev != 0) {
				curFreeAddr = -readInt(prev);
				int start = curFreeAddr;
				int next = readInt(curFreeAddr + 4);
				int stop = readInt(start);
				if (stop - start >= len) {
					int off = start + len;
					if (stop - off >= 8) {
						// still gap
						writeInt(off, stop);
						writeInt(next);
						writeInt(prevFreeAddr + 4, off);
					} else {
						// no gap
						writeInt(prevFreeAddr + 4, next);
					}
					writeInt(prev, start);

					f.seek(start, File.SEEK_SET);
					writeInt(stop - off >= 8 ? -off : stop);
					f.write(ByteBuffer.wrap(record));
					f.seek(start, File.SEEK_SET);
					this.incApproCount();
					return;
				} else {
					prevFreeAddr = curFreeAddr;
					prev = next;
				}
			}
			add0(record);
			this.incApproCount();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void add0(byte[] record) {
		try {
			f.seek(0, File.SEEK_END);
			int pos = f.position();
			writeInt(pos + record.length + 4);
			f.write(ByteBuffer.wrap(record));
			f.seek(pos, File.SEEK_SET);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public Sequential<byte[]> iterator() {
		try {
			return new SeqFileIterator();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	protected final class SeqFileIterator implements Sequential<byte[]> {
		private int pprev = -1;
		private int prev = firstIndex;
		private int cur;// = firstOffset;
		private int prevFreeAddr = firstIndex;
		private int curFreeAddr = firstIndex; // address stores free space

		// info.
		public SeqFileIterator() throws IOException {
			int pos = readInt(firstIndex);
			if (pos < 0) {
				pos = -pos;
				curFreeAddr = pos;
				pos = readInt(pos);
			}
			assert (pos >= 0);
			cur = pos;
		}

		@Override
		public boolean hasNext() {
			return cur >= 0 && cur < SeqFile.this.f.size();
		}

		/**
		 * after successful call, file position is set to the start of the
		 * returned record
		 */
		@Override
		public byte[] next() {
			try {
				synchronized (SeqFile.this.f) {
					boolean step = false;
					int pos = readInt(cur);
					if (pos < 0) {
						step = true;
						pos = -pos;
					}
					int length = pos - cur - 4;
					if (length < 0)
						throw new Error("record length negative");
					ByteBuffer buf = SeqFile.allocate(length);
					int count = SeqFile.this.f.read(buf);
					if (count < length)
						throw new Error("record truncated???");
					pprev = prev;
					prev = cur;
					cur = step ? readInt(pos) : pos;
					prevFreeAddr = curFreeAddr;
					if (step) {
						curFreeAddr = pos;
					}
					// set position to the start of returned record , may be
					// someone
					// will use this
					SeqFile.this.f.seek(prev, File.SEEK_SET);
					assert cur >= 0;
					return ((ByteBuffer) buf.flip()).array();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * after successful call, file position is set to the start of the
		 * record(also the start of previous record)
		 * 
		 * @return true if success
		 */
		@Override
		public boolean replace(byte[] newRecord) {
			try {
				if (pprev == -1)
					throw new Error("call replace() after next()");
				int remained = cur - prev - 4 - newRecord.length;
				if (remained < 0) {
					return false; // not enough space
				}

				int nextFreeIndex = readInt(curFreeAddr + 4);
				if (remained >= 8) {
					int off = cur - remained;
					if (off != curFreeAddr) {
						writeInt(prev, -off);
						writeInt(prevFreeAddr + 4, prev);
						writeInt(off, cur);
						writeInt(nextFreeIndex);
						curFreeAddr = off;
					} // else the old/new records are the same size
				} else {
					if (prev < curFreeAddr && curFreeAddr < cur) {
						// the previous gap dies away
						writeInt(prev, cur);
						writeInt(prevFreeAddr + 4, nextFreeIndex);
						curFreeAddr = prevFreeAddr;
					}
				}

				SeqFile.this.f.seek(prev + 4, File.SEEK_SET);
				SeqFile.this.f.write(SeqFile.wrap(newRecord));
				// set position to the start of the record , may be someone
				// will use this
				SeqFile.this.f.seek(prev, File.SEEK_SET);
				return true;
			} catch (IOException e) {
				throw new Error(e);
			}
		}

		@Override
		public void replaceWithForce(byte[] newRecord) {
			if (!replace(newRecord)) {
				this.remove();
				SeqFile.this.add0(newRecord);
			}
		}

		/**
		 * after successful call, file position is set to the start of the
		 * deleted record
		 */
		@Override
		public void remove() {
			try {
				if (pprev == -1)
					throw new Error("call remove() after next()");
				if (cur - prev >= 8) {
					int nextFreeIndex = readInt(curFreeAddr + 4);
					if (pprev <= prevFreeAddr) {
						writeInt(prevFreeAddr, cur);
						writeInt(nextFreeIndex);
						curFreeAddr = prevFreeAddr;
					} else {
						writeInt(pprev, -prev);
						writeInt(prev, cur);
						writeInt(nextFreeIndex);
						writeInt(prevFreeAddr + 4, pprev);
						curFreeAddr = prev;
					}
				} else {
					// no removal will make gap narrower
					if (pprev < prevFreeAddr) {
						writeInt(prevFreeAddr, cur);
					} else {
						writeInt(pprev, cur);
					}
				}
				prev = pprev;

				// set position to the start of this deleted , may be someone
				// will use this
				SeqFile.this.f.seek(prev, File.SEEK_SET);
				SeqFile.this.decApproCount();
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}

	private int readInt() throws IOException {
		ByteBuffer buf = SeqFile.allocate(4);
		f.read(buf);
		return ((ByteBuffer) buf.flip()).getInt();
	}

	private int readInt(int addr) throws IOException {
		ByteBuffer buf = SeqFile.allocate(4);
		f.seek(addr, File.SEEK_SET);
		f.read(buf);
		return ((ByteBuffer) buf.flip()).getInt();
	}

	private void writeInt(int i) throws IOException {
		ByteBuffer buf = SeqFile.allocate(4);
		buf.putInt(i).flip();
		f.write(buf);
	}

	private void writeInt(int addr, int i) throws IOException {
		ByteBuffer buf = SeqFile.allocate(4);
		f.seek(addr, File.SEEK_SET);
		buf.putInt(i).flip();
		f.write(buf);
	}

	public static ByteBuffer wrap(byte[] b) {
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
	}

	public static ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
	}

	public void incApproCount() {
		++this.approCount;
	}

	public void decApproCount() {
		--this.approCount;
	}

	public int getApproCount() {
		return approCount;
	}

	/**
	 * force to write new record count to table (note, it is not reliable
	 * nevertheless)
	 */
	public void updateCount() {
		f.seek(TableHelper.COUNT_OFFSET, File.SEEK_SET);
		ByteBuffer bb = SeqFile.allocate(4);
		bb.putInt(this.getApproCount()).flip();
		try {
			f.write(bb);
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning(
					"cannot update record count");
		}
	}
}
