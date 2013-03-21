package qifeng.lowlevel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class FileSystemTest {
	private static ByteBuffer buffer0 = (ByteBuffer) Charset.forName("utf-8")
			.encode("hello world").rewind();
	private static ByteBuffer buffer1 = BufferManager.myAllocate(1000, false);
	private static ByteBuffer buffer2 = BufferManager.myAllocate(10000, false);
	private static ByteBuffer buffer3 = BufferManager.myAllocate(100000, true);
	private static ByteBuffer buffer4 = BufferManager.myAllocate(1000000, true);
	private static ByteBuffer buffer5 = BufferManager.myAllocate(
			50 * 1024 * 1024, true);
	private static LRUBufferManager bm = new LRUBufferManager();
	private static Disk d = null;

	public static void main(String[] argv) {

		try {
			d = new Disk("/tmp/simulated_device", bm);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		// pass
		bm.startSyncThread();
		bm.startDropThread();
		try {
			boolean first_test = true;

			File fd0, fd1, fd2, fd3, fd4, fd5;

			int max_size = buffer5.capacity();

			if (argv.length >= 1) {
				max_size = Integer.parseInt(argv[0]);
			}

			System.out.format("testing files up to %d bytes\n", max_size);

			try {
				d.mkdir("/foo");
			} catch (IOException e) {
				if (e.getMessage().contains("existed already")) {
					System.out.format("second or subsequent test\n");
					first_test = false;
				} else
					throw e;
			}
			boolean prevErr = false;
			try {
				d.rmdir("/foo");
			} catch (IOException e) {
				if (first_test) {
					System.out.printf("unable to rmdir /foo\n");
					throw e;
				} else {
					if (e.getMessage().contains("directory not empty")) {
						System.out
								.printf("another test after previous error\n");
						prevErr = true;
					}
				}
			}

			if (!first_test)
				System.out.printf("succeeded in removing directory /foo\n");

			try {
				d.mkdir("/foo");
			} catch (IOException e) {
				if (!prevErr) // nothing
					throw e;
			}
			System.out.printf("succeeded in creating directory /foo\n");

			try {
				d.openFile("/foo/bar");
				System.out
						.printf("error, opened nonexistent file /foo/bar, aborting\n");
				throw new IOException();
			} catch (FileNotFoundException e) {
				System.out.printf("my_open correctly failed to open "
						+ "non-existent file /foo/bar\n");
			}
			fd0 = test_file("/foo/bar0", buffer0, buffer0.remaining(), max_size);
			if (fd0 != null)
				fd0.close();
			System.out.printf("successfully closed /foo/bar0\n");

			d.rename("/foo/bar0", "/qqq");
			System.out.printf("successfully renamed /foo/bar0 to /qqq\n");

			d.remove("/qqq");
			System.out.printf("successfully removed /qqq\n");
			d.mkdir("/foo/bar");
			System.out.printf("successfully created directory /foo/bar\n");
			d.rmdir("/foo/bar");

			System.out.printf("successfully removed directory /foo/bar\n");

			/* repeat the test on as many of the larger files as appropriate */
			fd1 = test_file("/foo/bar1", buffer1, buffer1.capacity(), max_size);
			fd2 = test_file("/foo/bar2", buffer2, buffer2.capacity(), max_size);
			fd3 = test_file("/foo/bar3", buffer3, buffer3.capacity(), max_size);
			fd4 = test_file("/foo/bar4", buffer4, buffer4.capacity(), max_size);
			fd5 = test_file("/foo/bar5", buffer5, buffer5.capacity(), max_size);

			close_remove_file("/foo/bar5", fd5);
			close_remove_file("/foo/bar2", fd2);
			close_remove_file("/foo/bar4", fd4);
			close_remove_file("/foo/bar1", fd1);
			close_remove_file("/foo/bar3", fd3);

			System.out.printf("tests completed successfully\n");

			int[] status = bm.getAccessStatus();
			System.out.printf("access request = %d, cache miss = %d,"
					+ " dontneed use = %d, replacement count = %d\n",
					status[0], status[1], status[2], status[3]);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			bm.sync();
			bm.stopDropThread();
			bm.stopSyncThread();
		}
	}

	private static final int _MOD_CONST = 251;

	private static File test_file(String path, ByteBuffer buffer, int size,
			int max_size) throws IOException {
		int i;
		boolean large;
		File fd;

		if (size > max_size) /* skip larger tests */
			return null;
		large = (size > 20); /* small file is "hello world" */

		/* create a file, check that it was saved */
		if (large) {
			for (i = 0; i < size; i++)
				buffer.put(i, (byte) (i % _MOD_CONST));
			buffer.put(0, (byte) 0x77); /* change value in location 0 */
			if (size / 2 > 4097)
				buffer.put(4097, (byte) 0x99); /*
												 * change a value after the
												 * first block
												 */
			if (size / 2 > 4096 * 1024 + 21)
				buffer.put(4096 * 1024 + 21, (byte) 0x42); /*
															 * after first block
															 * of blocks
															 */
		}

		fd = d.createFile(path);
		System.out.printf("successfully created file %s\n", path);
		fd.write(buffer);
		System.out.printf("successfully wrote %d bytes to file %s\n", size,
				path);
		fd.close();
		System.out.printf("successfully closed file %s\n", path);

		/* reset the buffer contents to the values that are easy to check */
		if (large) {
			buffer.put(0, (byte) 0);
			if (size / 2 > 4097)
				buffer.put(4097, (byte) (4097 % _MOD_CONST));
			if (size / 2 > 4096 * 1024 + 21)
				buffer.put(4096 * 1024 + 21, (byte) ((4096 * 1024 + 21) % _MOD_CONST));
		}

		/* now read and write */
		fd = d.openFile(path);
		System.out.printf("successfully reopened file %s\n", path);
		buffer.rewind();
		buffer.limit(size / 2);
		fd.write(buffer);
		System.out.printf("successfully wrote initial %d bytes to file %s\n",
				size / 2, path);
		buffer.limit(size); /* position: size/2 */
		/* clear top part of buffer */
		for (i = size / 2; i < size; i++)
			buffer.put(i, (byte) 0);
		/* now replace it with the contents of what we read */
		fd.read(buffer);
		System.out.printf("successfully read final %d bytes from file %s\n",
				size - size / 2, path);
		fd.close();
		System.out.printf("successfully closed file %s after reading\n", path);

		/* clear the bottom half of the buffer, and read it from the file again */
		for (i = 0; i < size / 2; i++)
			buffer.put(i, (byte) 0);
		fd = d.openFile(path);
		System.out.printf("successfully re-reopened file %s\n", path);
		buffer.rewind().limit(size / 2);

		fd.read(buffer);
		System.out.printf("successfully read initial %d bytes from file %s\n",
				size / 2, path);

		buffer.limit(size);
		if (large) {
			for (i = 0; i < size; i++) {
				if (((buffer.get(i)) & 0xff) != ((i % _MOD_CONST) & 0xff)) {
					System.err.printf("error at index %d (of %d),"
							+ " value %d, expected %d\n", i, size, ((buffer
							.get(i)) & 0xff), ((i % _MOD_CONST) & 0xff));
					throw new IOException();
				}
			}
		} else {
			buffer.rewind();
			String x = Charset.forName("utf-8").decode(buffer).toString();
			if (!x.equals("hello world")) {
				System.err.printf("error, value written was 'hello world',"
						+ " value returned %s\n", x);
				throw new IOException();
			}
		}
		System.out.printf("test completed successfully on a file of size %d\n",
				size);
		return fd;
	}

	private static void close_remove_file(String path, File fd)
			throws IOException {
		if (fd != null) {
			fd.close();
			System.out.printf("successfully closed %s\n", path);
			d.remove(path);
			System.out.printf("successfully removed %s\n", path);
		}
	}
}