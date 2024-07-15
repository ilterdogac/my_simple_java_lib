import java.io.IOException;

/* The objects below can't be directly accessed and therefore be instantiated, from/in a package
 * different than this is in. There should be public class that can be used to instantiate the
 * below and return in proper types those are public classes such as java.io.InputStream. */
public class ByteIO {
	
	/** A replacement for the Java method that didn't exist in the runtime library of Java 8 and prior. */
	@SuppressWarnings("resource")
	public static byte[] readAllBytes(java.io.InputStream is) throws IOException {
		return (new ByteIO.InputStream(is)).dumpAsArray();
	}
	
	
	/** To let something write something and dump the written data into array */
	public static class OutputStream extends java.io.OutputStream {
		private byte[] data = null;
		private final list<Byte> cache = new linklist<>();
		private boolean closed = false;
		
		public OutputStream() {}
		
		public void write(int b) throws IOException {
			if (closed) throw new java.io.EOFException("Stream is closed");
			if (b != -1) cache.add(fn.toByte(b));
			else close();
		}
		
		/** Warning: Does not copy the original array; so do not modify the returned array! */
		public byte[] dump() {
			if (closed) return data;
			data = new byte[cache.size()];
			{int i = 0; for (byte b: cache) {i++; data[i-1] = b;}}
			return data;
		}
		
		public void close() {
			dump();
			closed = true;
		}
		
	}
	
	
	
	
	// TODO: Still not removed that sh*t???
	
	//	DONE: Implement a constructor taking and storing a list/array of separate byte arrays (byte[][])
	//	rather than a possibly a huge array spanning a whole space on the memory.
	/** @deprecated It is very unlikely to have an iterable of byte arrays and that can be handled implementing
	 *  a custom class there. If you already have a single block of heap space (a byte array) then just instantiate
	 *  {@link java.io.ByteArrayInputStream} or implement a tiny class implementing the interface InputStream,
	 *  without reallocating similarly sized heap spaces. */
	@Deprecated
	private static class InputStream extends java.io.InputStream {
		// Gives bytes by read()
		
	//	@Deprecated private final byte[] data;
		private final Iterable<byte[]> chunks; // [[a, b, c, ...], [d, e, ...], [f, g, h, i, ...], ...]
		private final int size;
	//	private int index1, index2;
		
		@Deprecated private final int hash;
		
		@Deprecated public static int calculateHash(java.util.List<byte[]> ls) {
			return java.util.Arrays.deepHashCode(/*ls.toArray(new byte[][] {})*/ls.toArray(new byte[][] {}));
		}
		@Deprecated public int calculateHash() {return /*calculateHash(chunks)*/ -1;}
		
		public static int calculateSize(Iterable<byte[]> arr) {
			int total = 0; for (byte[] subarr: arr) total += subarr.length; return total;
		}
		
		public static int calculateElementQty(Iterable<?> itr) {int size = 0; for (Object each: itr) size++; return size;}
		
		
		/** Packs all the bytes from an input stream into a list of byte arrays each sized as chunkSize */
		public static list<byte[]> pack(java.io.InputStream input, int chunkSize) throws IOException {
			if (chunkSize == 0) throw new IllegalArgumentException("The block size must be positive");
			
			list<byte[]> output = new linklist<>();
			
			byte[] currentNewChunk = null;
			int maxCount = 1 << 15;
			boolean assertOn = fn.isAssertOn(); // Break when it passes the limit only during the development phase.
			
						assertOn = false;
			
			int count = 0;
			while (true) {
				count++; if (assertOn && count > maxCount)
					throw new AssertionError("The predefined count run out before the inputstream returned an EOF");
				
				int index = ((count - 1) % chunkSize) + 1; // (1, 4) -> 1 -- (3, 4) -> 3 -- (4, 4) -> 4 -- (5, 4) -> 1 -- (8, 4) -> 4 -- (4, 1) -> 4
				
				// Create new block for output
				if (index == 1) currentNewChunk = new byte[chunkSize];
				
				{
					int value = input.read();
					if (value == -1) { // Do not convert and put -1 on the index-th slot of the current chunk
						input.close();
						// [[1, 2, 3, 4], [5, 6, 7, 8], [9, ?, ?, ?]]
						if (index-1 != 0) { // Narrow it down to index-1 if the value of index is so that the last full chunk was not empty;...
							byte[] lastChunk = new byte[index-1];
							if (currentNewChunk == null) throw new AssertionError();
							for (int i = 1; i <= index-1; i++) lastChunk[i-1] = currentNewChunk[i-1];
							output.add(lastChunk);
						} // ...if empty, just leave the chunk list without adding a 0-sized last chunk.
						break;
					}
					
					if (currentNewChunk == null) throw new AssertionError();
					// Eclipse still warns about the below possibly being null??
					currentNewChunk[index-1] = (byte) value;
				}
				
				if (index == chunkSize) output.add(currentNewChunk);
			}
			
			return output;
		}
		
		
		// [[a, b], [c], [d, e, f]], 3 -> [[a, b, c], [d, e, f]]
		/** Redistributes all the bytes from a list of byte arrays to another one with the specified chunk size */
		@SuppressWarnings("resource")
		public static list<byte[]> reDivide(Iterable<byte[]> input, int newChunkSize) {
			
	//		// Flatten the input into a regular inputstream that does not support 'available'.
	//		java.io.InputStream is = new java.io.InputStream() {
	//			private java.util.Iterator<byte[]> itr = input.iterator();
	//			private byte[] current = null;
	//			private int subNextInd = -1;
	//			
	//			public int read() {
	//				// Get the next chunk if the upnext byte to be returned from
	//				// the current chunk has an index falling outside of that chunk
	//				if (current == null || subNextInd == current.length + 1) {
	//					if (!itr.hasNext()) return -1;
	//					current = itr.next();
	//					subNextInd = 1;
	//				}
	//				return ((int) current[(subNextInd++) - 1]) & 0x000000ff;
	//			}
	//		};
	//		
	//		try {return pack(is, newChunkSize);}
	//		catch (IOException e) {return null;}
			try {return pack(new ByteIO.InputStream(input, 0), newChunkSize);}
			catch (IOException e) {e.printStackTrace(); return null;}
		}
		
		
	//	// [[a, b], [c], [d, e, f]], 3 -> [[a, b, c], [d, e, f]]
	//	@Deprecated public static java.util.List<byte[]> _reDivide(java.util.List<byte[]> input, int newChunkSize) {
	//		int size = calculateSize(input);
	////		int newChunkQty;
	//		
	//		java.util.List<byte[]> output = new list<>();
	//		
	//		if (newChunkSize == 0) return null;
	//		if (newChunkSize < 0) throw new IllegalArgumentException("The block size for the nested array cannot be negative");
	//		
	//		java.util.Iterator<byte[]> itr = input.iterator();
	//		byte[] currentOldChunk = null, currentNewChunk = null;
	//		
	//		int ind = 0, oldind = 0/*, newind = 0*/;
	//		int currentOldSize = 0;
	//		int actualNewChunkSize = -1; // This value must get updated below at least once before the first time it is used for anything.
	//		
	//		while (true) {
	//			// Retrieve the next block from input
	//			if (oldind == currentOldSize) {
	//				if (!itr.hasNext()) break;
	//				currentOldChunk = itr.next();
	//				currentOldSize = currentOldChunk.length;
	//				oldind = 0;
	//			}
	//			
	//			// Create new block for output
	//			if (ind % newChunkSize == 0) {
	//				actualNewChunkSize = size - (ind+1 - 1); if (actualNewChunkSize > newChunkSize) actualNewChunkSize = newChunkSize;
	//				currentNewChunk = new byte[actualNewChunkSize];
	//			}
	//			
	//			// Copy the values
	//			currentNewChunk[ind % newChunkSize] = currentOldChunk[oldind];
	//			ind++; oldind++;
	//			if (ind % actualNewChunkSize == 0) output.add(currentNewChunk);
	//		}
	//		
	//		return output;
	//	}
		
		// TODO: Copy those into ByteOutputStream class
		// These are meant to be used to get an existing input stream into array
		/** Gets the copy of the underlying data as a list of byte arrays
		 *  <p>Hint: There is a replacement method here named readAllBytes for JVMs with the version not
		 *  having the same named method on InputStream. */
		@Deprecated
		public list<byte[]> dump() {
			return new linklist<>(chunks);
		}
		/** Gets the copy of the underlying data as a list of byte arrays sized with the given parameter */
		public list<byte[]> dump(int newChunkSize) {
			return reDivide(chunks, newChunkSize);
		}
		
		/** Gets the copy of the underlying data as a byte array */
		public byte[] dumpAsArray() {
			if (size == 0) return new byte[] {};
			list<byte[]> redivided = reDivide(chunks, size);
			assert redivided.size() == 1;
			return redivided.g(1);
		}
		
		
		/** Copies and redistributes the bytes the array iterable gives, into a new list of arrays with the specified chunk size. */
		public InputStream(Iterable<byte[]> input, int newChunkSize) {
			if (input == null) throw new NullPointerException("Given input is null");
			size = calculateSize(input);
			if (newChunkSize != 0) // Average block size
				chunks = reDivide(input, newChunkSize);
			else chunks = input; // If the given block size is 0
			hash = calculateHash();
			reset();
		}
		
		// TODO: Consider that definitely rely on the input as it is
		/** <strong>Warning:</strong> Most probably relies (is dependent) on the arrays in the given list. */
		public InputStream(Iterable<byte[]> input) {
			if (input == null) throw new NullPointerException("Given input is null");
			size = calculateSize(input);
			{
				int elementQty = calculateElementQty(input);
				if (elementQty != 0 && size / calculateElementQty(input) > 0x100)
					chunks = reDivide(input, 0x100);
				else chunks = input;
			}
			hash = calculateHash();
			reset();
		}
		
		
		/** <strong>Warning:</strong> Is dependent on the given array. */
		public InputStream(byte[] input) {
			this(new linklist<>(fn.noNull(input)), 0);
		}
		
		
	//	// TODO: Move those and implement into a proper class like dict, list or GeneralNumber.
	//	private void writeObject(java.io.ObjectOutputStream s) throws IOException {s.writeObject(this);}
	//	private void readObject(java.io.ObjectInputStream   s) {}
		
		// The chunkSize must be positive because a buffer-like object with indeterminable end like InputStream
		public InputStream(java.io.InputStream input, int chunkSize) throws IOException {
			if (input == null) throw new NullPointerException("Given input is null");
			chunks = pack(input, chunkSize);
			size = calculateSize(chunks);
			hash = calculateHash();
			reset();
	//		
	//		if (chunkSize == 0) throw new IllegalArgumentException("The block size must be positive");
	//		
	//		chunks = new list<>();
	//		
	//		byte[] currentNewChunk = null;
	//		int maxCount = 10000;
	//		boolean assertOn; // Break when it passes the limit only during the development phase.
	//		{boolean[] _assertOn = {false}; fn.doIfAssertModeOn(() -> {_assertOn[0] = true;}); assertOn = _assertOn[0];}
	//		
	//		int count = 0;
	//		while (true) {
	//			count++; if (assertOn && count > maxCount)
	//				throw new AssertionError("The predefined count run out before the inputstream returned an EOF");
	//			
	//			int index = ((count - 1) % chunkSize) + 1; // (1, 4) -> 1 -- (3, 4) -> 3 -- (4, 4) -> 4 -- (5, 4) -> 1 -- (8, 4) -> 4 -- (4, 1) -> 4
	//			
	//			// Create new block for output
	//			if (index == 1) currentNewChunk = new byte[chunkSize];
	//			
	//			{
	//				int value = input.read();
	//				if (value == -1) { // Do not convert and put -1 on the index-th slot of the current chunk
	//					// [[1, 2, 3, 4], [5, 6, 7, 8], [9, ?, ?, ?]]
	//					if (index-1 != 0) { // Narrow it down to index-1 if the value of index is so that the last full chunk was not empty;...
	//						byte[] lastChunk = new byte[index-1];
	//						for (int i = 1; i <= index-1; i++) lastChunk[i-1] = currentNewChunk[i-1];
	//						chunks.add(lastChunk);
	//					} // ...if empty, just leave the chunk list without adding a 0-sized last chunk.
	//					break;
	//				}
	//				
	//				currentNewChunk[index-1] = (byte) value;
	//			}
	//			
	//			if (index == chunkSize) chunks.add(currentNewChunk);
	//			
	//		}
	//		
	//		size = count;
		}
		
		
		/** Copies the input into (an) independent array(s), preferably with divided ones */
		public InputStream(byte[] input, int chunkSize) {
			this(new linklist<>(fn.noNull(input)), chunkSize);
		}
		
		public InputStream(java.io.InputStream input) throws IOException {
			this(input, 0x200);
		}
		
		
		
		
		private java.util.Iterator<byte[]> chunkIterator;
		private byte[] currentChunk;
		
		private boolean finished() {return index-1 >= size;}
		private int subindex; // Index for which element is going to be returned from the current chunk. Becomes 0 when the current gets null/undefined.
		private int index; // Index (of the next) in total (for “int available()”)
		
		@Override
		public int read() throws java.io.IOException {
			if (finished()) return -1;
			if (available() <= 0) return -1;
			if (currentChunk == null || subindex == currentChunk.length + 1) {
	//			if (!chunkIterator.hasNext()) return -1;
				currentChunk = chunkIterator.next();
				subindex = 1;
			}
			if (currentChunk.length == 0) return read();
			
			index++;
			return ((int) currentChunk[(subindex++) - 1]) & 0x000000ff;
			
			/* [Casting back into] / [returning as] int alters the function because it just makes a signed conversion (0b1******* -> 0b1111...111********)
			 * and therefore bytes (that can only be in [0, 255]∩ℤ) bigger/equal than 128 (0b10000000) become negative,
			 * as opposed to what is expected: a range [0, 255]∩ℤ for any byte and -1 if the end of stream is detected. */
			/* Instead, push to the left just before it loses meaning (until 8 bits left untouched) then push those 8 bits right back
			 * with making the coming bits 0 (shift left “logical”) rather than depending on anything to be either 000... or 111...:
			 * return ((int)i) << 24 >>> 24; */
			/* Or just 'bitwise-and' it with a number:
			 * return ((int)i) & 0x000000ff; // This makes the first 24 bits 0 and leaves the last 8 unchanged. */
			// Selfnote: 13-07-2022 --- I don't know what the f*ck those “∩ℤ”s next to ranges like
			// “[0, 255]nZ” mean ow why the f*ck I wrote or who the f*ck wrote at all.
			// Selfnote: 13-07-2022 --- I'm so stupid as f*ck that didn't remember that I wrote them exactly to mean
			// a motherf.ing intersection (“n”) of a real number interval with the set of integers (“Z”)!!!
			// Therefore all such “n”s are replaced with a “∩”, all “Z”s are replaced by “ℤ”!!
		}
		
		@Override
		public void reset() {
			chunkIterator = chunks.iterator();
			currentChunk = null;
			subindex = 0;
			index = 1;
		}
		
	//	@Override
	//	public void reset() {index1 = 1; index2 = 1; count = 1; finished = chunks[index1-1].length >= index2;}
		
		@Override
		public int available() {
			assert ((size - index + 1) >= 0);
			return size - index + 1;
	//		int sum = chunks[index1-1].length - (index2-1);
	//		for (int i: fn.range(index1+1, chunks.length, 1))
	//			sum += chunks[i-1].length;
	//		return sum;
		}
		
	}
	
}