//import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.regex.*;

//import java.util.Scanner;

public class FileIO {
	
	public static final int KB = 1024;
	public static final int MB = KB*1024;
	public static final int GB = MB*1024;
	
	// TODO: Maybe define methods like these
	/*
	private static class FSEntry {
		private Type type;
		public FSEntry(Path path) {
			Type is determined right now and exception is thrown when no such file exists??????
			Or type is determined right now and is just null when no such file exists??????
			Or type is null and determined when getCurrentType or getType is called??????
		}
		public FSEntry(Path path, Type type) {...}
		public Type getType() {}
		public Type getCurrentType() {}
		
		public boolean exists() {}
		
		public FSEntry then(Path relPath, Type) {
		public FSEntry then(Path relPath) {
		}
	}
	*/
	
	/** Note: Uses class-wide variable to decide about the separator (slash character) and whether the
	 *  first path frame should have a colon following it. That is, if this configuration ever changes
	 *  after this class' static initializer is executed, then it will not behave as expected! */
	// Examples:
	// Windows
	//     “C:/Users/user”
	//     “C:/Users/user/”
	//     “C:/Users/user/../user”
	//     “./Documents”
	//     “./Documents/”
	//     “.”
	//     “../user/”
	// Other:
	//     “/home/user”
	//     “/./home/../home/user”
	//     “user”
	//     “./user”
	//     “./user/”
	//     “./”
	//     “.”
//	public static class File {
//		private final String[] pathFrames;
//		@Deprecated private final boolean startsWithSlash; // Unix absolute (such as “/home/user”) or relative-to-root paths (such as “/./home/../home/user”)
//		// TODO: Instead, abandon the final variables and use initializers!!!
//		@Deprecated private final boolean startsFromRoot;
//		
//		private String[] _pathFrames;
//		private boolean _startsWithSlash; // Unix absolute (such as “/home/user”) or relative-to-root (such as “/./home/../home/user”) paths
//		/** <li> Unix absolute (such as “/home/user”) or relative-to-root (such as “/./home/../home/user”) paths
//		 *  <li> Windows absolute (such as “C:/Users”) or relative-to-root (such as “C:/Users/../Users/user”) paths */
//		private final boolean _startsFromRoot;
//		private String[] pathFrames_duplicate() {if (true) throw new Error(); return _pathFrames.clone();}
//		private boolean startsWithSlash() {return _startsWithSlash;}
//		/** <li> Unix absolute (such as “/home/user”) or relative-to-root (such as “/./home/../home/user”) paths
//		 *  <li> Windows absolute (such as “C:/Users”) or relative-to-root (such as “C:/Users/../Users/user”) paths */
//		public final boolean startsFromRoot() {return _startsFromRoot;}
//		
////		public File(String path) {
////			this(splitIntoArray(path), startsWithSlash(path));
////		}
//		private static class obj_properties<E> {
//			public final E item;
//			public final boolean startsFromRoot, bool2;
//			public obj_properties(E item, boolean startsFromRoot, boolean bool2) {
//				this.item = item;
//				this.startsFromRoot = startsFromRoot;
//				this.bool2 = bool2;
//			}
//		}
//		private File(String path, int any) { // to temporarily satisfy the compiler that complains like these may have not been initialized
//			pathFrames = null;
//			startsWithSlash = false;
//			throw new Error();
//		}
//		public File(String path) {
//			this(path, 0);
//			_pathFrames = splitIntoArray(path);
//			_startsWithSlash = null;
//		}
//		public File(Iterable<String> path, boolean startsWithSlash) {this((new linklist<>(path)).getArray(String.class), startsWithSlash);}
//		public File(String[] path, boolean startsWithSlash) {
//			if (isBackSlash() && atleastOneRootIsLetter())
//				throw new IllegalArgumentException("Filepath starting with a shash on a platform that has letters followed by colons as roots");
//			this.pathFrames = path.clone();
//			if (false) assertAll(); // TODO: delete this line
//			this.startsWithSlash = startsWithSlash;
//			assertAll();
//		}
//		
//		public String name() {return pathFrames[pathFrames.length - 1];}
//		public java.io.File value() {return new java.io.File(joinToString(pathFrames));}
//		
//		public static boolean startsWithSlash(String strn) {return str.slice(strn, 1, 1).equals(separator());}
//		public static boolean startsWithSlash(Iterable<String> strn) {return startsWithSlash(strn.iterator().next(), strn);}
//		
////		public static String separator() {return java.io.File.separator;}
//		public static String separator() {return "/";}
//		
//		private static int rootsAreLetters = -1; public static boolean atleastOneRootIsLetter() {
//			if (true) return false;
//			if (bools.noNull(rootsAreLetters)) return bools.from(rootsAreLetters);
//			boolean retVal; A: {
////			String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//			for (String lettr: str.alphabet().g(2))
////			for (int i: fn.range(alphabet.length()))
////				if ((new java.io.File(str.slice(alphabet, i, i)+":")).exists())
//				if ((new java.io.File(lettr+":")).exists())
//					{retVal = true; break A;}
//			{retVal = false; break A;}}
//			rootsAreLetters = bools.to(retVal); return retVal;
//		}
//		private static int isBackSlash = -1; public static boolean isBackSlash() {
//			if (bools.noNull(isBackSlash)) return bools.from(isBackSlash);
//			boolean retVal; A: {
//			String separator = separator();
//			if (separator.equals("/")) {retVal = false; break A;}
//			else if (separator.equals("\\")) {retVal = true; break A;}
//			else throw new RuntimeException("Path frame separator is neither back nor forward slash");}
//			isBackSlash = bools.to(retVal); return retVal;
//		}
//		
//		
//		static {staticAssertAll();}
//		private static void staticAssertAll() {
//			if (isBackSlash() != atleastOneRootIsLetter()) throw new AssertionError();
//		}
//		
//		public void assertAll() {
//			if (pathFrames.length == 0) throw new AssertionError();
//			if (pathFrames.length == 1) fn.log("Warning: one-frame path in file object --- couldn\'t split????!!");
//		}
//		
//		private static class bools {
//			private bools() {}
//			public static void assertNoNull(int bool) {if (!noNull(bool)) throw new AssertionError();}
//			public static boolean from(int bool) {if (bool == 0) return false; else if (bool == 1) return true; else throw new NullPointerException("Can\'t convert "+bool+" to boolean.");}
//			public static int to(boolean val) {return val ? 1:0;}
//			public static boolean isNull(int bool) {return !noNull(bool);}
//			public static boolean noNull(int bool) {if (bool == 0 || bool == 1) return true; else if (bool == -1) return false; else throw new IllegalStateException();}
//		}
//		
//		public String[] pathFrames() {return pathFrames.clone();}
//		public String toPath() {return joinToString(pathFrames);}
//		public String toString() {return toPath();}
//		
//		// “A:/b/c”, “./../c1” -> “A:/b/c1”
//		// “A:/b/c”, “d”       -> “A:/b/c/d”
//		public File under(File relative) {
//			throw new Error();
//		}
//		
//		// TODO: Reconsider this when Windows finally officially supports the ext4 (and its
//		// predecessors) and filepaths to items in it become like “D:/path/to/item” (under
//		// letter) or “C:\mount\partition1\path/to/item” under a NTFS mount point!!!!
//		// TODO: Shouldn't this be splitting by the OS default separator first?
//		/** Splits a string path into frames firstly by the default delimiter (forward slash),
//		 *  and if that results in a single frame (no split at all) and at the same time the OS'
//		 *  file seperator is backward slash, then splits with backslash. */
//		public static String[] splitIntoArray(String path) {return splitIntoList(path).getArray(String.class);}
//		private static list<String> splitIntoList(String path) {
//			list<String> ls = str.split(path, Pattern.quote("/"), true);
//			if ((ls.size() == 1) && isBackSlash()) {
//				list<String> _ls = str.split(path, Pattern.quote("\\"), true);
//			//	if (_ls.size() > 1) ls = _ls;
//				ls = _ls;
//			}
//			
//			if (atleastOneRootIsLetter()) { // Just remove the colon!
//				// Below is only when letter roots.
//				if (!isBackSlash()) throw new AssertionError(); // Letter roots with forward slash
//				String firstFrame = ls.g(1); // “E:”, “E” or “myFolder”
//				if (str.slice(firstFrame, -1, -1).equals(":")) {
//					firstFrame = str.slice(firstFrame, 1, -2);
//					if (firstFrame.length() != 1) throw new AssertionError("First frame of file path contains a ...");
//					ls.s(1, firstFrame);
//				} else {
//					fn.log("[Debug] Passed a file path string to FileIO.File.splitIntoList(String)");
//				}
//			}
////			if (atleastAnyRootIsLetter()) {
////				if (str.slice(path, -1, -1).equals)
////				if (str.slice(path, ?, ?))
////				ls.s(1, str.slice(ls.g(1)+":"));
////			}
//			return ls;
//		}
//		public static String joinToString(String[] arr) {return joinToString(list.listWrapper.asList(arr));}
//		private static String joinToString(Iterable<String> ls) {return str.join(ls, separator());}
//	}
	
	
//	public static java.io.InputStream getStreamFromFile(String fileName) throws IOException {
//		return new String(readFromFile(fileName), "UTF-8");
//	}
	
	
	
	/** @deprecated Should return a type in my custom datetime class */
	@Deprecated
	public static long getEpochLastModified(String file) throws IOException {
		return (new java.io.File(file)).lastModified();
	}
	
	/** @deprecated Should return a type in my custom datetime class */
	public static long getEpochLastAccessed(String file) throws IOException {
		java.nio.file.attribute.BasicFileAttributes attrs;
		attrs = java.nio.file.Files.readAttributes(
			(new java.io.File(file)).toPath(),
			java.nio.file.attribute.BasicFileAttributes.class
		);
		return attrs.lastAccessTime().toMillis();
	}
	
	
	// TODO: Test that!
	public static boolean move(String from, String to) {
		fn.log("Warning: called untested method\n"+str.getStackTraceText());
		return (new java.io.File(from)).renameTo(new java.io.File(to));
	}
	
	
	// TODO
	// This is probably a temporary entry. When FileIO.File is finished, remove the f*ck out of this!!!!!
	// Does not depend on whether the actual path is a file at that time, just represents a path and
	// whether it is file or dir or what the hell ever.
	public static class FileSystemEntry {
		public final String path;
		public final EntryType type; // not just bool; maybe also symbolic links or folder-like items that intersect some of their files 😱 
		public static enum EntryType {
			file(1), directory(2);
			private EntryType(int type) {this.typecode = type;}
			public final int typecode;
		}
		private FileSystemEntry(String path, EntryType type) {
			this.type = type;
			this.path = path;
		}
		
		public static FileSystemEntry newDirectory(String path) {
			return new FileSystemEntry(path, EntryType.directory);
		}
		public static FileSystemEntry newFile(String path) {
			return new FileSystemEntry(path, EntryType.file);
		}
	}
	
	// Not putting a method returning an iterable that returns an iterator that gives
	// the next file in a depth-first-search
	
	// FIXME: Those do not return folders.
//	protected static Iterable<String> extractRecursiveIterator(String absPath) {
//		return new Iterable<String>() {
//			public Iterator<String> iterator() {
//				
//				return new Iterator<String>() {
//					public boolean hasNext() {
//						
//						return false;
//					}
//					public String next() {
//						
//						return null;
//					}
//				};
//			}};
//	}
	
	
	
	// TODO: The cond parameter is tested with a string path as a relative file path (like “./folder/filename.ext”) wrt. the one that the function is called. Take a look at that sh*t!!!
	public static list<String> extractRecursive(String absPath) {return extractRecursive(absPath, (path) -> true);}
	public static list<String> extractRecursive(String absPath, java.util.function.Function<String, Boolean> cond) {
		list<String> tree = new linklist<>();
		java.io.File dir = new java.io.File(absPath);
		if (!dir.exists()) return null;
		if (dir.isFile()) return null;
		for (String child: dir.list()) {
			child = absPath+"/"+child;
			if ((new java.io.File(child)).isFile()) {
				if (cond.apply(child)) tree.add(child);
			} else {
			//	if (cond.apply(child)) tree.add(child);
				for (String each: extractRecursive(child, cond)) tree.add(each);
			}
		}
		return tree;
	}
	
	
	
	@SuppressWarnings("resource")
	public static java.util.Iterator<String> getLineReader(String fileName) throws IOException {
		return getLineReader(getStreamFromFile(new java.io.File(fileName)));
	}
	public static java.util.Iterator<String> getLineReader(java.io.InputStream inputStream) throws IOException {
		java.util.regex.Pattern pat;
//		try {pat = (Pattern) ClassLoader.getSystemClassLoader().loadClass("ApplicationUtils").getMethod("inputParser").invoke(null);}
//		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
//		     | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			pat = Pattern.compile(str.lineSeparatorRegex); // Use the default if that class is not included!
//			// TODO: Make it so that it does not try to check for the existence of that class every f.ing single time we call this.
//		}
		return getLineReader(inputStream, pat);
	}
	
	
	@SuppressWarnings("resource")
	public static java.util.Iterator<String> getLineReader(String fileName, String lineDelimiter) throws IOException {
		return getLineReader(getStreamFromFile(new java.io.File(fileName)), lineDelimiter);
	}
	
	public static java.util.Iterator<String> getLineReader(java.io.InputStream is, String lineDelimiter) throws IOException {
		return getLineReader(is, java.util.regex.Pattern.compile(lineDelimiter));
	}
	
//	@SuppressWarnings("resource")
//	public static java.util.Iterator<String> getLineReader(String fileName, Pattern lineDelimiter) throws IOException {
//		return getLineReader(getStreamFromFile(new java.io.File(fileName)), lineDelimiter);
//	}
	
	@SuppressWarnings("resource")
	public static java.util.Iterator<String> getLineReader(java.io.InputStream inputStream, Pattern lineDelimiter) throws IOException {
		String encoding = "UTF-8";
		
		if (false) {
			java.io.Reader rd = new java.io.InputStreamReader(inputStream, encoding);
			java.io.BufferedReader brd = new java.io.BufferedReader(rd);
		}
		
		java.util.Scanner sc = new java.util.Scanner(inputStream, encoding);
		sc.useDelimiter(lineDelimiter);
		
		return new java.util.Iterator<String>() {
			private java.util.Scanner enclosed = sc; // Actually implements Iterator<String> but I need to make sure it will finally be closed.
			private String upNext = null;
			private byte hasNext = -1;
			public boolean hasNext() {
				if (hasNext == 0) return false;
				if (hasNext == 1) return true;
				step();
				return hasNext();
			}
			public String next() {
				if (!hasNext()) throw new java.util.NoSuchElementException();
				String current = upNext;
				step();
				return current;
			}
			private void step() {
				if (enclosed.hasNext()) {
					upNext = enclosed.next();
					hasNext = 1;
				} else {
					hasNext = 0;
					enclosed.close();
				}
			}
		};
	}
	
	
	
	
	public static String readFromFileAsString(String fileName) throws IOException {return readString(fileName);}
	/** Returns null if the file does not exist but throws the exception if any access or read error/exception occurs. */
	public static String readString(String fileName) throws IOException {
		return readFromFileAsString(new java.io.File(fileName));
	}
	public static String readFromFileAsString(java.io.File file) throws IOException {return readString(file);}
	/** Returns null if the file does not exist but throws the exception if any access or read error/exception occurs. */
	public static String readString(java.io.File file) throws IOException {
		byte[] bytes = readFromFile(file);
		if (bytes == null) return null;
		return new String(bytes, "UTF-8");
	}
	public static byte[] readFromFile(String fileName) throws IOException {return read(fileName);}
	/** Returns null if the file does not exist but throws the exception if any access or read error/exception occurs. */
	public static byte[] read(String fileName) throws IOException {
		return readFromFile(new java.io.File(fileName));
	}
	public static byte[] readFromFile(java.io.File file) throws IOException {return read(file);}
	/** Returns null if the file does not exist but throws the exception if any access or read error/exception occurs. */
	public static byte[] read(java.io.File file) throws IOException {
		byte[] arr = new byte[(int) file.length()];
	//	java.io.InputStream is = new FileInputStream(file);
		java.io.InputStream is = getStreamFromFile(file);
		if (is == null) return null;
		int read = is.read(arr);
		assert arr.length == read;
		is.close(); // As long as it is open, others can't move it around, delete or open as writing (seems like the last is wrong though). But while one is open as writing, others can read.
		return arr;
	//	return (new ByteIO.InputStream(getStreamFromFile(file), 0x10000)).dumpAsArray();
	}
//	public static byte[] readFromFile(File file, int start, int len) throws IOException {}
	
	public static java.io.InputStream getStreamFromFile(String file) throws IOException {
		return getStreamFromFile(new java.io.File(file));
	}
	/** Returns null if the file does not exist but throws the exception if any access error/exception occurs. */
	public static java.io.InputStream getStreamFromFile(java.io.File file) throws IOException {
//		FileInputStream fs/* = null*/;
//		// TODO: Remove that text below
//		/* Compiler knows that the method exits if the assignment of this variable does not complete and
//		 * not also cause an throwable to be thrown because of catch block that catches it,
//		 * and does not refuse compiling with “variable ... may not have been assigned”; because it is
//		 * certain that the catch throws another exception that terminates the method - even if
//		 * the throw statement is connected within an if clause whose expression evaluates true. Normally,
//		 * java compiler stops refusing compilation because of a return/throw that is making
//		 * certain that the rest will be 'unreachable code' if that statement instead becomes into like
//		 * “if (true) throw new Error();” or “if (true) return ...;” but it does not stop
//		 * accepting when a variable assignment becomes connected with the same kind of if clause like
//		 * “if (true) a = ...; from “a = ...”. */
//		byte[] data = new byte[(int) file.length()];
//		try {fs = new FileInputStream(file);}
//		catch (FileNotFoundException e) {throw new FileNotFoundException("The file \u201c" + file.getName() + "\u201d could not be found or accessed");}
////		catch (IOException e) {throw e;}
//		if (len == -1) len = (int) file.length();
//		fs.read(data, start-1, len);
////		data = fs.readAllBytes(); // An alternative; (as far as I know) it stops until the end of stream/file is detected or an exception is thrown, and returns when the end is detected.
//		fs.close();
		java.io.FileInputStream is/* = null*/;
		if (!file.exists()) {
			fn.log("[Debug] the parameter passed to FileIO.getStreamFromFile (\u201c"+file.getPath()+"\u201d) points to an unexisting item and the method returned null.");
			return null;
		}
		if (file.isDirectory()) {
			fn.log("Warning: The parameter passed to FileIO.getStreamFromFile (\u201c"+file.getPath()+"\u201d) is a directory and the method returned null.");
			fn.log(str.getStackTraceText());
			return null;
		}
		try {return new FileInputStream(file);}
		catch (FileNotFoundException e) {
			throw new FileNotFoundException("The file \u201c"+file.getPath()+"\u201d could not be read: "+e.getMessage());
		}
	}
	
	
	public static void writeStringIntoFile(String filename, String content) throws IOException {writeString(filename, content);}
	public static void writeString(String filename, String content) throws IOException {
		if (filename.length() > 300)
			throw new IllegalArgumentException(
				"Too long file/path name! Did you accidentally switch the filename and the string content parameters??!!!: " + str.slice(filename, 1, 100) + "..."
			);
		try {writeIntoFile(filename, content.getBytes("UTF-8"));} catch (UnsupportedEncodingException e) {throw new InternalError(e);}
	}
	public static void writeIntoFile(String fileName, byte[] content) throws IOException {write(fileName, content);}
	public static void write(String intoFile, byte[] content) throws IOException {
		if (intoFile.length() > 300)
			throw new IllegalArgumentException(
				"Too long file/path name!: " + str.slice(intoFile, 1, 100) + "..."
			);
		writeIntoFile(new java.io.File(intoFile), content);
	}
	public static void writeIntoFile(java.io.File file, byte[] content) throws IOException {write(file, content);}
	@SuppressWarnings("resource")
	public static void write(java.io.File file, byte[] content) throws IOException {
		writeIntoFile(file, new java.io.ByteArrayInputStream(content));
	}
	
	
	public static void writeIntoFile(java.io.File file, java.io.InputStream stream) throws IOException {write(file, stream);}
	public static void write(java.io.File file, java.io.InputStream stream) throws IOException {
		file = file.getCanonicalFile(); // .getCanonicalFile(): new File("A:/c1/c2/../../b") -> new File("A:/b")
//		FileOutputStream fs = null;
//		File file = (new File(fileName)).getCanonicalFile(); // .getCanonicalFile(): new File("A:/b/../c") -> new File("A:/c")
//		if (!file.getParentFile().exists())
//			if (false) throw new FileNotFoundException(
//				"The directory \u201c"+file.getParentFile()+"\u201d in where the file would be created does not exist"
//			);
//			else file.getParentFile().mkdirs();
//		if (false) if (file.exists())
//			throw new IOException("The file \u201c"+file.getName()+"\u201d already exists");
//		if (file.isDirectory())
//			throw new IOException("The path \u201c"+file.getName()+"\u201d supposed as a file is a directory");
//		file.createNewFile();
//		try {fs = new FileOutputStream(file);}
//		catch (IOException e) {e.printStackTrace(); System.exit(1);}
//		fs.write(content);
//		fs.close();
		java.io.FileOutputStream fs/* = null*/;
		if (!file.getParentFile().exists()) // What to do if the directory to contain it does not exist?
			if (false) throw new FileNotFoundException(
				"The directory \u201c"+file.getParentFile().getPath()+"\u201d in where the file would be created does not exist"
			);
			else file.getAbsoluteFile().getParentFile().mkdirs();
		if (false) if (file.exists()) // What to do if it already exists?
			throw new IOException("The file \u201c"+file.getPath()+"\u201d already exists");
		if (file.isDirectory())
			throw new IOException("The path \u201c"+file.getPath()+"\u201d supposed as a file is a directory");
		file.createNewFile();
	//	try {fs = new FileOutputStream(file);}
	//	catch (IOException e) {e.printStackTrace(); System.exit(1);}
	//	catch (IOException e) {e.printStackTrace(); throw e;}
		fs = new FileOutputStream(file);
		byte[] buf = new byte[0x400];
		while (true) {
			int qty = stream.read(buf);
			if (qty == -1) break;
			fs.write(buf, 0, qty);
		}
		fs.close();
	}
	
	
	
	/** Sparse files are files whose large as f*ck continuous parts consisting of zeroes are just
	 *  omitted and not actually spanning any area in the disk, and are supported by modern filesystems
	 *  like NTFS and ext3, ext4 etc. and unlike the legacy ones like FAT32.
	 *  <p>This feature is extremely useful when you need to for example create a very large space
	 *  but do not want to allocate several GBs for it right away just for the almost 100% part of only
	 *  countless bytes all zero, being written to HDD and taking possibly long time or to SSD contributing
	 *  significantly to the SSD write wearing, also making it much harder to copy and transfer.
	 *  <p>Even if you create a very large sparse file with very few actually written (spanning much less
	 *  than its logical size), when you copy it into the same or different space with such a supporting
	 *  filesystem, Windows just reads it just like a regular file and expands while writing the copy and
	 *  makes it a regular very large file. This method takes gigantic amounts of bytes and writes into a
	 *  sparse file, skipping the parts, even if those bytes are from a non-sparse file. */
	public static void writeSparse(String into, byte[] content) throws Exception {
		writeSparse(into, new java.io.ByteArrayInputStream(content));
	}
	
	/** Sparse files are files whose large as f*ck continuous parts consisting of zeroes are just
	 *  omitted and not actually spanning any area in the disk, and are supported by modern filesystems
	 *  like NTFS and ext3, ext4 etc. and unlike the legacy ones like FAT32.
	 *  <p>This feature is extremely useful when you need to for example create a very large space
	 *  but do not want to allocate several GBs for it right away just for the almost 100% part of only
	 *  countless bytes all zero, being written to HDD and taking possibly long time or to SSD contributing
	 *  significantly to the SSD write wearing, also making it much harder to copy and transfer.
	 *  <p>Even if you create a very large sparse file with very few actually written (spanning much less
	 *  than its logical size), when you copy it into the same or different space with such a supporting
	 *  filesystem, Windows just reads it just like a regular file and expands while writing the copy and
	 *  makes it a regular very large file. This method takes gigantic amounts of bytes and writes into a
	 *  sparse file, skipping the parts, even if those bytes are from a non-sparse file.
	 */
	public static void writeSparse(String into, java.io.InputStream is) throws Exception {
		
		final boolean debug_readBackAndCompareHash = true;
		final boolean debug_compareWholeFiles = false;
		
		java.nio.channels.SeekableByteChannel writeChannel;
		{
			java.util.EnumSet<java.nio.file.StandardOpenOption> params = java.util.EnumSet.of(
				java.nio.file.StandardOpenOption.CREATE_NEW, // Must be CREATE_NEW instead of CREATE!!
				java.nio.file.StandardOpenOption.WRITE,
				java.nio.file.StandardOpenOption.SPARSE // Just so that, when we skip forward in the file and write there,
				                                        // the between doesn't get automatically filled with 0es and is
				                                        // marked as sparse instead; we save disk space and disk write!!!
			);
			writeChannel = java.nio.file.Files.newByteChannel(
				java.nio.file.Paths.get(into), params
			);
		}
		
		final int bufferAndPartSize = 8*1024;
		final int partSize = 512;
		final int bufferSize = 512;
		
		Crypto.Hasher.Digest digester = Crypto.sha256.getDigester();
		
		list<Byte> debug_incoming;
		// If initializing and using its value is tied to the same final local (whose
		// value known at compile time; aka. not conditional)
		// variable then javac magically still can verify that the variable's value never
		// used unless it is initialized.
		if (debug_compareWholeFiles) debug_incoming = list.listWrapper.wrap(new java.util.ArrayList<>());
		
		java.util.function.Consumer<String> debug = (e) -> fn.print("[debug] "+e);
		
		boolean finished = false;
		Byte lastWritten = null;
		while (!finished) {
			byte[] buffer = new byte[bufferAndPartSize];
			final Integer sizeLastWritten;
			{
				int prim = is.read(buffer);
				sizeLastWritten = (prim == -1) ? null : prim;
			}
			if (sizeLastWritten != null) {
				if (sizeLastWritten < 1) throw new AssertionError();
				lastWritten = buffer[sizeLastWritten - 1];
			}
			boolean partIsSparse = true;
			
//			for (int i = 1; i <= buffer.length; i++) {
//				int byt = is.read();
//				if (byt == -1) {
//					finished = true;
//					break;
//				}
//				buffer[i-1] = fn.intByte(byt);
//				lastWritten = byt;
////				if (debug_compareFiles) incoming.add(fn.intByte(byt));
//				if (byt != 0) partIsEmpty = false;
//				lastLength++;
//			}
			
			if (sizeLastWritten != null && sizeLastWritten > buffer.length) throw new AssertionError();
			if (sizeLastWritten == null || sizeLastWritten < buffer.length)
				finished = true; // That was the last fill of the buffer
			
			if (sizeLastWritten != null) {
				{ // Check if the part is sparse
					for (int i = 1; i <= sizeLastWritten; i++) if (buffer[i-1] != 0) partIsSparse = false;
					if (debug_readBackAndCompareHash && debug_compareWholeFiles) {
						for (int i = 1; i <= sizeLastWritten; i++)
							debug_incoming.add(buffer[i-1]);
					}
				}
				
				if (debug_readBackAndCompareHash) { // Update the hash digester and the last byte
					if (sizeLastWritten > buffer.length) throw new AssertionError();
					if (sizeLastWritten == buffer.length) {
						digester.updateWith(buffer);
					}
					else {
						byte[] bufToUpdateHash = new byte[sizeLastWritten];
						fn.copy(buffer, 1, bufToUpdateHash, 1, 0);
						digester.updateWith(bufToUpdateHash);
					}
				}
				
				if (partIsSparse) { // Just skip instead of actually writing 0b0000...00
					writeChannel.position(writeChannel.position() + sizeLastWritten);
//					debug.accept("(l) "+bef+" -> "+aft+" (+"+(aft-bef)+")");
	//				debug.accept("Skipped "+lastLength+" bytes (saved as sparse part)");
				} else { // Actually write some sh*t
					if (sizeLastWritten == -1) throw new AssertionError("Not a quite unexpected assertionerror");
					java.nio.ByteBuffer nioBBuf = java.nio.ByteBuffer.wrap(buffer, 0, sizeLastWritten);
					if (nioBBuf.capacity() != sizeLastWritten) throw new AssertionError();
					writeChannel.write(nioBBuf);
	//				debug.accept("Written "+lastLength+" bytes");
//					debug.accept("(c) "+bef+" -> "+aft+" (+"+(aft-bef)+")");
				}
			}
		}
		{ // Rewrite the last byte (even if skipped to leave sparse) to set the EOF.
			// Until I discover a way to manually set the EOF explicitly, let's just
			// skip backwards by one byte and rewrite that byte's value so that the
			// trailing blank parts don't keep the logical size from growing.
			if (lastWritten == null) throw new AssertionError();
			writeChannel.position(writeChannel.position() - 1);
			{
				long b, a;
				b = writeChannel.position();
				writeChannel.write(java.nio.ByteBuffer.wrap(new byte[] {lastWritten}));
				a = writeChannel.position();
				if (a-b != 1) throw new AssertionError();
			}
		}
		writeChannel.close();
//		{
//			java.io.OutputStream os = new FileOutputStream("D:/ctr.txt");
//			for (String s: ctr) os.write(str.utf8(s+" "));
//			os.close();
//		}
		if (debug_compareWholeFiles) {
			
			byte[] income = list.byteArrayOf(debug_incoming);
			byte[] actuallyWritten = FileIO.read(into);
//			fn.print(income.length);
//			fn.print(actuallyWritten.length);
			
			if (!fn.equals(income, actuallyWritten)) {
				if ((income.length <= MB*200) && (actuallyWritten.length <= MB*200)) {
					FileIO.write("error_data_income.bin", income);
					FileIO.write("error_data_actually_written.bin", actuallyWritten);
					for (byte[] part: new byte[][] {income, actuallyWritten}) {
						byte[] lastPart = new byte[50];
						fn.copy(part, -lastPart.length, lastPart);
//						fn.print(a);
					}
				}
				throw new AssertionError();
			}
		}
		if (debug_readBackAndCompareHash) {
			byte[] hashIncome = digester.getResult();
			byte[] hashActuallyWritten = Crypto.sha256.hash(FileIO.getStreamFromFile(into));
			if (!fn.equals(hashIncome, hashActuallyWritten)) throw new AssertionError();
		}
	}
	
	
	
	
	private FileIO() {}
	
	
}






class FileIOException extends java.io.IOException {
	private static final long serialVersionUID = 1L;
//	private final String fileName;
//	public FileIOException(String fileName) {this.fileName = fileName;}
	public FileIOException(String fileName) {super("Error: The file \u201c" + fileName + "\u201d could not be found or accessed.");}
//	public String toString() {return "Error: The file \u201c" + fileName + "\u201d could not be found or accessed.";}
}
