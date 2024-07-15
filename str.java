import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

public class str {
	private str() {}
	
	public static class StrNative {
		private static boolean nativeLoaded = false, nativeLoadTried = false;
		private static byte nativeLoadStatus = 0; // 0: not tried, -1: failed, +1: successful
		
		/* Considering 3 different consoles: the Unix shell, the Windows console and console of the Eclipse on Windows;
		 *   -> Unix shell does not misbehave in printing Unicode text in Java, but the others require using the C-way (through a native JNI lib)
		 *   -> All of them suffer in doing the same in C, but we need the C-way only for Windows and Eclipse Windows consoles.
		 *   -> Unix shell and the Windows Eclipse console needs setlocale(LC_CTYPE, "")...
		 *   -> ...however, Windows console needs the not-so-nice-looking _setmode(_fileno(stdout), _O_U16TEXT), which ruins the others!
		 *   -> If the code prints a unicode text on Windows console without problem, it can't do that in the same way onto Windows CMD.
		 */
		
		private static native void setCConsoleLocale();
		private static native void setCConsoleLocaleForUnix();
		private static native void setCConsoleLocaleForWindows();
		private static native void resetCConsoleLocaleForWindows();
		/* Deprecated because we'll print the raw bytes (by public void java.io.OutputStream.write(byte[])) as UTF-8 encoding of
		 * the text we want to print and expect the console to use the exact same encoding so that the text would get displayed
		 * properly. */
		@Deprecated private static native void println(String text);
		private static native String getCmdLine0();
		private static native String[] getCmdArgs0();
		
	//	static {loadNative();}
		
		
	//	public static void print(String text) {if (nativeLoaded) println(text);}
		/** Writes the UTF-8 encoding result of the text without an extra newline. */
		public static void print(String text) {
			if (nativeLoaded)
				try {fn.outStream().write(text.getBytes("UTF-8"));}
				catch (IOException e) {throw new InternalError(e);}
			else if (!nativeLoadTried) {loadNative(); print(text);}
			else fn.outStream().print(text);
//			else {
//				java.io.PrintStream k = fn.outStream();
//				for (String ch: new list<>((i) -> str.sliceString(text, i, i), fn.range(text.length()))) {
//					fn.sleep(0.01);
//					k.print(ch);
//					k.flush();
//				}
//			}
		}
		// Specific for Windows because the Java Runtime Environment doesn't get the arguments from Windows consoles properly and alters the non-latin characters.
		public static String getCmdLine() {if (nativeLoaded) return getCmdLine0(); return null;}
		private static String[] getCmdArgs() {if (nativeLoaded) return getCmdArgs0(); return null;}
		
		public static boolean nativeLoaded() {return nativeLoaded;}
		
		public static void setConsoleLocale(boolean forUnix, boolean forWin) {
			if (!nativeLoaded) return;
			if (forUnix) setCConsoleLocaleForUnix();
			if (forWin) setCConsoleLocaleForWindows();
		}
		
		public static boolean loadNative() {
		//	if (nativeLoadTried) return nativeLoaded;
			if (nativeLoaded) return true;
			nativeLoaded = fn.loadLibrary("nativePrint");
			nativeLoadTried = true;
			// (Invoke setlocale/setmode)
			return nativeLoaded;
		}
	}
	
	/** Class with one string list for the characters of per one of the lower and upper case of an alphabet. */
	public static class Alphabet {
		public final Set<String> uppercase, lowercase;
		public Alphabet(Set<String> upper, Set<String> lower) {uppercase = upper; lowercase = lower;}
	}
	
	/** @Deprecated Because this method does not accept loner than 1-chacter units to be allowed for the
	 *  input string to consist only of to return true. It can't be used to test whether a string contains
	 *  only “12” or “89” (and not “18” or “29” or “111”). Also it accepts meaningful values, which are
	 *  prone for typos that can't be checked in the compile-time, for the parameters to behave differently. */
	@Deprecated public static boolean consistsOnlyOf(String text, String chars) {
		if (chars.equals("alphabet")) return consistsOnlyOf(text, "alphabet lowercase");
		if (chars.equals("digits")) return consistsOnlyOf(text, "0123456789");
		if (chars.equals("alphabet lowercase")) return consistsOnlyOf(text, __alphabet.g(1));
		if (chars.equals("alphabet uppercase")) return consistsOnlyOf(text, __alphabet.g(2));
	A:	for (int i: fn.range(text.length())) {
			for (int j: fn.range(chars.length()))
				if (slice(text, i, i).equals(slice(chars, j, j)))
					continue A;
			return false;
		}
		return true;
	}
	
	
	public static Alphabet alphabet() {return alphabet.get();}
	public static Alphabet trAlphabet() {return trAlphabet.get();}
	static {
		trAlphabet = () -> {
			list<Set<String>> val = new linklist<>();
			for (String s: new String[] {
				"abc\u00E7defg\u011Fh\u0131ijklmno\u00F6pqrs\u015Ftu\u00FCvwxyz",
				"ABC\u00C7DEFG\u011EHI\u0130JKLMNO\u00D6PQRS\u015ETU\u00DCVWXYZ"
			}) {
				// TOSO: Use splitIntoChars(String) instead
				list<String> currAlph = new linklist<>();
				for (int ind: fn.range(s.length())) currAlph.add(str.slice(s, ind, ind));
				val.add(new HashSet<String>(currAlph));
			}
			return new Alphabet(val.g(1), val.g(2));
		};
		
		alphabet = () -> {
			list<Set<String>> val = new linklist<>();
			for (String s: new String[] {
				"abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			}) {
				list<String> currAlph = new linklist<>();
				for (int ind: fn.range(s.length())) currAlph.add(str.slice(s, ind, ind));
				val.add(new HashSet<String>(currAlph));
			}
			return new Alphabet(val.g(1), val.g(2));
		};
	}
	
	private static final Supplier<Alphabet> trAlphabet;
	private static final Supplier<Alphabet> alphabet;
	
	
	public static String reduce(String text) {
		String out;
		// Separate the f**k out of the accent add-ons characters
		out = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKD);
		// Kick the f**k out of them
		out = out.replaceAll("\\p{M}", "");
		out = out.toLowerCase(Locale.ENGLISH);
		return out;
	}
	
	
	// TODO: When inserting the quick regex matching methods and classes, consider merging with the reduce method.
	
	/** Lowercases the string and reduces the not-ascii looking characters into their
	 *  corresponding-ish versions. When used with a string wrapper implementing its
	 *  hashCode and equals functions on top of this version of the string it holds, helps
	 *  treating a string like “AbcĞİ” same way as the one like “abCğı” by
	 *  reducing both of them into “abcgi”.
	 *  <p>Warning: The result of the results should not be used as a permanent value
	 *  and only as rather temporary values for comparison and hash values. “Aè” for example,
	 *  will not always be “a#” just because I did not implement a mapping as è -> e */
	// DONE: Make it like it discards and cleanup()'s all the diactricts and also handles characters with diactricts (like normal ğ, ş, ã etc.) vs the plain characters and diactrict add-on unicode characters (like “a” followed by an intermediate non-printable char and a ~)
	public static String reduce_old(String text) {
		java.util.Map<String, String> trmap = mapTRChars.get();
		java.util.Map<String, ?> asLetDec = asciiLetterAndDecimals.get();
		java.util.Map<String, ?> space = whiteSpaces.get();
		
		list<String> letters = new linklist<>((i) -> str.slice(text, i, i), fn.range(text.length()));
//		list<String> weird, replaceWith;
		{
			list<String> _letters = new linklist<>(letters);
			letters.clear();
			for (String letter: _letters) {
				String replacement = trmap.get(letter);
				if (replacement != null) letters.add(replacement);
				else if (asLetDec.containsKey(letter)) letters.add(letter);
				else if (space.containsKey(letter)) letters.add(letter);
				else letters.add("#");
			}
		}
		return str.join(letters, "").toLowerCase();
	}
	
	public static boolean compareReduced(String str1, String str2) {
		fn.noNull(str1, str2);
		return reduce(str1).equals(reduce(str2));
	}
	public static int hashCodeReduced(String str1) {return reduce(str1).hashCode();}
	
	
	// Without this, the str.reduce() would have to treat the TR-specific characters as unknown characters.
	private static final Supplier<java.util.Map<String, String>> mapTRChars = fn.cachedReference(() -> {
		java.util.Map<String, String> map = new java.util.HashMap<>();
		// TR non-ASCII looking chars
//			{
//				java.util.Map<String, ?> lower = new java.util.HashMap<>(), upper = new java.util.HashMap<>();
//				Alphabet t = str.trAlphabet(), e = str.alphabet();
//				for (String ch: t.lowercase) lower.put(ch, null);
//				for (String ch: t.uppercase) upper.put(ch, null);
//				for (String ch: e.lowercase) lower.remove(ch);
//				for (String ch: e.uppercase) upper.remove(ch);
//			}
		list<String> weird, mapInto;
		weird = new linklist<>("Ğ", "ğ", "Ç", "ç", "İ", "ı", "Ş", "ş", "Ü", "ü", "Ö", "ö");
		mapInto = new linklist<>("G", "g", "C", "c", "I", "i", "S", "s", "U", "u", "O", "o");
		java.util.Iterator<String> itr1 = weird.iterator(), itr2 = mapInto.iterator();
		while (itr1.hasNext()) map.put(itr1.next(), itr2.next());
		return map;
	});
	
	private static final Supplier<java.util.Map<String, ?>> asciiLetterAndDecimals = fn.cachedReference(() -> {
		Function<String, list<String>> split = (a) -> {
			list<String> b = new linklist<>();
			for (int i: fn.range(a.length())) b.add(str.slice(a, i, i));
			return b;
		};
		java.util.Map<String, ?> set = new java.util.HashMap<>();
		for (String ch: alphabet().lowercase) set.put(ch, null);
		for (String ch: alphabet().uppercase) set.put(ch, null);
		for (int i: fn.range(0, 9)) set.put(""+i, null);
		for (String ch: split.apply(".-,:;*?\\/{}[]()<>|!'^+%&~$#_")) set.put(ch, null);
		return set;
	});
	private static final Supplier<java.util.Map<String, ?>>
		whiteSpaces = fn.cachedReference(() -> {
			java.util.Map<String, ?> set = new java.util.HashMap<>();
			for (String ch: new linklist<>(" ", "\n", "\r")) set.put(ch, null);
			return set;
		});
	
	
	
	@Deprecated public static list<String> splitIntoChars(String whole) {
		return split(whole);
	}
	/** Splits the string into the list of strings as individual characters */
	public static list<String> split(String whole) {
		list<String> b = new linklist<>();
		for (int i: fn.range(whole.length())) b.add(str.slice(whole, i, i));
		return b;
	}
	
	@Deprecated private static list<String> __alphabet = new linklist<>(
		"abc\u00E7defg\u011Fh\u0131ijklmno\u00F6pqrs\u015Ftu\u00FCvwxyz",
		"ABC\u00C7DEFG\u011EHI\u0130JKLMNO\u00D6PQRS\u015ETU\u00DCVWXYZ"
	);
	/**
	 * @Deprecated Look for Java's String library and usage of locales instead.
	 * FUN FACT:
	 *     If you have ever thought about the transform i-I and transforms i-İ, ı-I; the
	 *     Oracle's javadoc page about the String.toLowerCase(java.util.Locale), from countless
	 *     languages in the World, just picks Turkish as the example (and mentions those
	 *     characters) lol :D
	 *     
	 *     https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/lang/String.html#toLowerCase(java.util.Locale)
	 */
	@Deprecated public static String changeCase(String source, boolean toLower, boolean isTurkish) {
		list<String> lower, upper;
		list<String> lst;
		String result = "";
		lower = new linklist<>();
		upper = new linklist<>();
		{
			list<list<String>> lists;
			String str;
			lists = new linklist<>();
			lists.add(lower); lists.add(upper);
			
			// TODO: Change that sh.t -- it uses that old asf case!
			for (int i: fn.range(2)) {
				lst = lists.g(i);
				str = __alphabet.g(i);
				for (int j: fn.range(str.length()))
					lst.add(slice(str, j, j));
			}
		}
		
		String currentChar;
		String currentAlphabet = __alphabet.g((!toLower)?1:2);
		String newAlphabet     = __alphabet.g(( toLower)?1:2);
		if (!isTurkish) {
			if (toLower) {for (int i: fn.range(source.length())) if (slice(source, i, i).equals("\u0130"))
				throw new Error("Error: The string supposed not to be Turkish contains \u201c\u0130\u201d.");
			} else       {for (int j: fn.range(source.length())) if (slice(source, j, j).equals("\u0131"))
				throw new Error("Error: The string supposed not to be Turkish contains \u201c\u0131\u201d.");
			} newAlphabet = slice(newAlphabet, 1, toLower?10:11) + slice(newAlphabet, toLower?12:11, toLower?12:11) + slice(newAlphabet, toLower?12:13, -1);
		}
	A:	for (int i: fn.range(source.length())) {
			currentChar = slice(source, i, i);
			for (int j: fn.range(currentAlphabet.length())) {
				if (currentChar.equals(slice(currentAlphabet, j, j))) {
					result = result + slice(newAlphabet, j, j);
					continue A;
				}
			}
			result = result + currentChar;
		}
//		fn.print("from: " + currentAlphabet);
//		fn.print("to: " + newAlphabet);
		return result;
		
	}
	
	private static String toString(char[] array) {
		String pfx = "(char[]) ";
		String add = "";
		for (int i: fn.range(array.length)) add += array[i-1];
		return pfx + str.escapeString(add);
	}
	private static String toString(byte[] array) {
//		String pfx = "(" + fn.getClassName(array) + ") ";
		Function<Byte, String> toHex = (value) -> {
			String result = Integer.toString(0xff & (int) value, 16);
			if (result.length() == 1) result = "0" + result;
			return result;
		};
//		return "[" + join(new linklist<>((i) -> toHex.apply(array[i-1]) + "", array.length), ", ") + "]";
		return "byte[] {" + join(new linklist<>((i) -> toHex.apply(array[i-1]), array.length), " ") + "}";
	}
	
	public static String toString(Object obj) {
		if (obj == null) return "(null)";
		if (obj instanceof char[]) return toString((char[]) obj);
		if (obj instanceof byte[]) return toString((byte[]) obj);
		if (obj.getClass().isArray()) {
			
			Function<Object, list<?>> toList = (array) -> {
				Class<?> type = array.getClass();
				if (!type.isArray()) return null;
				type = type.getComponentType();
				if (!type.isPrimitive())        return     new linklist<>((Object[])    array);
				if (type.equals(int.class))     return linklist.fromArray((int[])       array);
				if (type.equals(byte.class))    return linklist.fromArray((byte[])      array);
				if (type.equals(boolean.class)) return linklist.fromArray((boolean[])   array);
				if (type.equals(long.class))    return linklist.fromArray((long[])      array);
				if (type.equals(double.class))  return linklist.fromArray((double[])    array);
				list<?> output = null;
				       if (array instanceof short[]) {
					list<Short>     ls = new linklist<>(); output = ls; short[] arr = (short[]) array;
					for (int i: fn.range(arr.length)) ls.add((((short[]) array)[i-1]));
				} else if (array instanceof float[]) {
					list<Float>     ls = new linklist<>(); output = ls; float[] arr = (float[]) array;
					for (int i: fn.range(arr.length)) ls.add((((float[]) array)[i-1]));
				} else if (array instanceof char[])  {
					list<Character> ls = new linklist<>(); output = ls; char[] arr = (char[]) array;
					for (int i: fn.range(arr.length)) ls.add((((char[])  array)[i-1]));
				} return output;
			};
			
			return toString(toList.apply(obj));
		}
		return obj.toString();
	}
	
	/** @deprecated Obtain a byte[] version of the number through unsigned/signed little/big
	 *  endian and use str.binary() for operations like these. */
	@Deprecated public static String toUnsignedBinaryString(int num) {return Integer.toUnsignedString(num, 2);}
	/** @deprecated Obtain a byte[] version of the number through unsigned/signed little/big
	 *  endian and use str.binary() for operations like these. */
	@Deprecated public static String toUnsignedBinaryString(byte num) {throw new UnsupportedOperationException();}
	
	public static String multiply(String strn, int itrNum) {return multiply(strn, itrNum, "");}
	public static String multiply(String strn, int itrNum, String delimiter) {
		// 1   2 3 4 5
		
		// itrNum = 5	1 2 3 4  -  5
		// itrNum = 4	1 2 3  -  4
		// itrNum = 3	1 2  -  3
		// itrNum = 2	1  -  2
		// itrNum = 1	.  -  1
		// itrNum = 0	.  -  .
		
		StringBuilder sb = new StringBuilder();
		String _new = delimiter+strn;
		for (int i: fn.range(1, (itrNum >= 1)?1:0, 1))
			sb.append(strn);
		for (int i: fn.range(2, itrNum, 1))
			sb.append(_new);
		return sb.toString();
	}
	
	public static final String lineSeparatorRegex = "((\n)|(\r\n))+";
	
	public static String escapeString(String str) {
		String result, nextChar;
		if (str==null) return "<null>";
		result = "";
		result += "\u201c";
		for (int i: fn.range(1, str.length(), 1)) {
			nextChar = slice(str, i, i);
			if (	   nextChar.equals("\u201c")
				|| nextChar.equals("\u201d")
				|| nextChar.equals("\"")
				|| nextChar.equals("\'")
				|| nextChar.equals("\\")
			) result += "\\" + nextChar;
			else if (nextChar.equals("\n")) result += "\\n";
			else if (nextChar.equals("\r")) result += "\\r";
			else if (nextChar.equals("\t")) result += "\\t";
			else result += nextChar;
		} result += "\u201d";
		return result;
	}
	
	
	
	/* ------------------------------------------------ Thread/stack/throwable methods ------------------------------------------------ */
	// TODO: Do not publicly open the methods returning or taking an internal-ish type (java.lang.StackTraceElement)!
	
	public static String getStackTraceText(StackTraceElement[] array) {
		Throwable th = new Throwable();
		th.setStackTrace(array);
		String stTracTextWithHead = getStackTraceText(th); // that includes an unnecessary line like java.lang.Throwable out of an array of StackTraceElements
		list<String> lines = new linklist<>();
		{
			list<String> lines1 = str.split(stTracTextWithHead, "(\\r)?\\n", true);
			if (lines1.isEmpty()) return null;
			java.util.Iterator<String> itr = lines1.iterator();
			itr.next(); // Skip the heading line
			while (itr.hasNext()) lines.add("  at " + str.slice(itr.next(), 5, -1)); // Slice off “<tab>at ” in each line but add “at” back differently
		}
		return str.join(lines, "\n");
	}
	
	public static String getStackTraceText(/*of this thread*/) {
		return getStackTraceText(fn.getStackTrace());
	}
	
	@SuppressWarnings("resource")
	public static String getStackTraceText(Throwable th) {
		String[] trace = {""};
		th.printStackTrace(new java.io.PrintWriter(new java.io.Writer() {
			public void write(char[] cbuf, int off, int len) {
				if (len+off > cbuf.length) {
					if (fn.isAssertOn()) fn.printe(String.format(
						"[Debug] Error: buf=%s, off=%s, len=%s",
					str.toString(cbuf), ""+off, ""+len));
					return;
				}
				char[] str = new char[len];
				for (int i: fn.range(1, len, 1)) str[i-1] = cbuf[off+i-1];
				trace[0] = trace[0] + new String(str);
			}
			public void flush() {}
			public void close() {}
		}));
		return trace[0];
	}
	/** @deprecated Getting another thread's stack trace does not quite much make sense. */
	@Deprecated private static String getStackTraceText(Thread th) {
		return getStackTraceText(fn.getStackTrace(th));
	}
	/* ------------------------------------------------ ------------------------------ ------------------------------------------------ */
	
	
	private static Supplier<java.util.Base64.Encoder> base64encoder = fn.cachedReference(() -> java.util.Base64.getEncoder());
	private static Supplier<java.util.Base64.Decoder> base64decoder = fn.cachedReference(() -> java.util.Base64.getDecoder());
	
	public static byte[] base64(String text) {return fromBase64(text);}
	public static String base64(byte[] data) {return toBase64(data);}
	public static byte[] fromBase64(String text) {
		return base64decoder.get().decode(text);
	}
	public static String toBase64(byte[] data) {
		return base64encoder.get().encodeToString(data);
	}
	
	public static byte[] hex(String input) {return fromHex(input);}
	public static String hex(byte[] input) {return toHex(input);}
	public static byte[] fromHex(String input) {
		int len = input.length();
		if (len % 2 != 0) throw new IllegalArgumentException("Input length is not an even number. Each byte spans 2 characters in hexadecimal representation.");
		int qty = len/2;
		byte[] output = new byte[qty];
		for (int i: fn.range(1, qty, 1))
			// Not calling fn.toByte because it unnecessarily checks the 0-255 boundaries.
			output[i-1] = (byte) (int) Integer.valueOf(str.slice(input, 2*(i-1)+1, 2*(i-1)+2), 16);
		return output;
	}
	public static String toHex(byte[] input) {
		StringBuilder result = new StringBuilder();
		for (byte each: input) {
			String _byte = Integer.toString(fn.unsignedExtendToInt(each), 16);
			result.append(((_byte.length() != 1) ? "" : "0") + _byte);
		}
		return result.toString();
	}
	
	public static byte[] binary(String input) {return fromBinary(input);}
	public static String binary(byte[] input) {return toBinary(input);}
	public static byte[] fromBinary(String input) {
		int len = input.length();
		if (len % 8 != 0) throw new IllegalArgumentException(
			"Input length is not an integer multiple of 8. Each byte spans 8 characters in binary representation."
		);
		byte[] output = new byte[len/8];
		for (int i: fn.range(1, len, 1)) {
			boolean b;
			String chr = str.slice(input, i, i);
			if (chr.equals("0")) b = false;
			else if (chr.equals("1")) b = true;
			else throw new IllegalArgumentException("Given string to convert from binary contains something else than 0s and 1s");
			fn.putBool(output, i, b);
		}
		return output;
	}
	public static String toBinary(byte[] input) {
		StringBuilder result = new StringBuilder();
		for (byte each: input) for (int i: fn.range(8)) result.append(fn.getBool(each, i) ? "1":"0");
		return result.toString();
	}
	
	// Hex and base64 are the string results of byte arrays but utf-8 looks more
	// like a transition between a string and byte array, thus the name is
	// encode/decode rather than from/to
	public static byte[] utf8(String text) {return utf8encode(text);}
	public static String utf8(byte[] data) {return utf8decode(data);}
	// TODO: Maybe rename as encodeUTF8 / decodeUTF8 ?????
	// TODO: Maybe rename as fromUTF8 (byte[] -> string) / toUTF8 (string -> byte[]) ????????
	public static byte[] utf8encode(String text) {
		try {return text.getBytes("UTF-8");}
		catch (UnsupportedEncodingException e) {
			throw new InternalError(e);
		}
	}
	public static String utf8decode(byte[] data) {
		try {return new String(data, "UTF-8");}
		catch (UnsupportedEncodingException e) {
			throw new InternalError(e);
		}
	}
	public static byte[] ascii(String text) {return ASCIIencode(text);}
	public static String ascii(byte[] data) {return ASCIIdecode(data);}
	public static byte[] ASCIIencode(String text) {
		try {return text.getBytes("ASCII");}
		catch (UnsupportedEncodingException e) {
			throw new InternalError(e);
		}
	}
	public static String ASCIIdecode(byte[] data) {
		try {return new String(data, "ASCII");}
		catch (UnsupportedEncodingException e) {
			throw new InternalError(e);
		}
	}
	
	
	// ------------------------------------------------ Number parsing methods ------------------------------------------------
	private static class NumParsingException extends NumberFormatException { // ( <-- IllegalArgumentException <-- RuntimeException)
		private java.util.Map<String, Boolean> attributes = null;
		public NumParsingException() {super();}
		public NumParsingException(String message) {super(message);}
		public NumParsingException(String message, Iterable<String> attribues) {
			this(message);
			attributes = new java.util.HashMap<>();
			for (String key: attribues)
				this.attributes.put(key, true);
		}
		public boolean getAttribute(String key) {
			if (attributes == null) return false;
			Boolean value = attributes.get(key);
			if (value == null) return false;
			return value;
		}
//		public NumFmtException(Throwable cause) {super(cause);}
//		public NumFmtException(String message, Throwable cause) {super(message, cause);}
	}
	public static long strToLong(String number) { // “255” -> 255, “0xff” -> 255
		final String originalNumber = number;
		int base = 10;
		Supplier<IllegalArgumentException> malformedException; {
			String num = number;
			malformedException = () -> new IllegalArgumentException(
				"\u201c"+originalNumber+"\u201d is not a valid number representation"
			);
		}
		if (str.slice(number, 1, 1).equals("0")) {
				String sym = str.slice(number, 2, 2);
				     if (sym.equals("x")) base = 16;
				else if (sym.equals("o")) base = 8;
				else if (sym.equals("b")) base = 2;
				
				if ((base != 10) && !(number.length() > 2))
					throw malformedException.get();
		}
		
		if (base != 10)
			number = str.slice(number, 3, -1);
		
		long val;
		try {val = Long.parseLong(number, base);} // Is a valid integer?
		catch (NumberFormatException e1) { // Too big, non-integer rational or not even a number
			try { // Is just too big?
				new java.math.BigInteger(number, base);
			} catch (NumberFormatException e2) {
				try { // Is even a (decimal) number?
					if (base != 10) throw new NumberFormatException();
					Double.valueOf(number);
				} catch (NumberFormatException e3) {
					throw new NumParsingException("\u201c"+originalNumber+"\u201d is neither an integer nor a decimal number");
				}
				throw new NumParsingException("\u201c"+originalNumber+"\u201d is not an integer");
			}
			throw new NumParsingException("\u201c"+originalNumber+"\u201d is too big to fit into 64-bit integer", new linklist<>("too big"));
		}
		return val;
	}
	public static int strToInt(String number) {
		boolean tooBig = false;
		int val = -1; boolean valInit = false;
		long lval;
		try {
			lval = strToLong(number);
			val = (int) lval; valInit = true;
			if (lval != val) tooBig = true;
		}
		catch (NumParsingException e) {
			if (e.getAttribute("too big")) tooBig = true;
			else throw e;
		}
		if (tooBig) throw new NumParsingException(
			"\u201c"+number+"\u201d is too big to fit into 32 bit integer", new linklist<>("too big")
		);
		
		if (!valInit) throw new AssertionError();
		return val;
	}
	public static long decimalToLong(String decimal) { // “255” -> 255
		long val;
		try {val = Long.parseLong(decimal, 10);} // Is a valid integer?
		catch (NumberFormatException e1) { // Too big, non-integer rational or not even a number
			try { // Is just too big?
				new java.math.BigInteger(decimal, 10);
			} catch (NumberFormatException e2) {
				try { // Is even a decimal number?
					Double.valueOf(decimal);
				} catch (NumberFormatException e3) {
					// No it is not even decimal number
					throw new NumParsingException("\u201c"+decimal+"\u201d is not a decimal number");
				}
				// Yes it is number
				throw new NumParsingException("\u201c"+decimal+"\u201d is not an integer");
			}
			// Yes, just too big
			throw new NumParsingException("\u201c"+decimal+"\u201d is too big to fit into 64 bit integer", new linklist<>("too big"));
		}
		return val;
	}
	public static int decimalToInt(String decimal) {
		boolean tooBig = false;
		int val = -1; boolean valInit = false;
		long lval;
		try {
			lval = decimalToLong(decimal);
			val = (int) lval; valInit = true;
			if (lval != val) tooBig = true; // Too big for int
		}
		catch (NumParsingException e) { // Comes from to-long parser
			if (e.getAttribute("too big")) // Too big for even long!
				tooBig = true;
			else throw e; // Is just invalid
		}
		if (tooBig) throw new NumParsingException(
			"\u201c"+decimal+"\u201d is too big to fit into 32 bit integer", new linklist<>("too big")
		);
		
		if (!valInit) throw new AssertionError();
		return val;
	}
	// ------------------------------------------------ ---------------------- ------------------------------------------------
	
	
	
	
	/*public static String round(double num, int afterFp) {
		String _str;
		double remainder = Math.pow(10, -afterFp);
		if (afterFp<=0) afterFp = -1;
		
		num += remainder/2 + 1e-12;
		num += - (num % remainder);
		
		_str = ""+num;
		{
			int digitsAfterFP = 0;
			for (int i: fn.range(_str.length()))
				if (str.sliceString(_str, -i, -i).equals("."))
					digitsAfterFP = i-1;
			return str.sliceString(_str, 1, Math.min(-1, afterFp-digitsAfterFP-1));
		}
	}*/
	
	/** Used for pretty-printing floating-point values.
	 */
	@SuppressWarnings("unused")
	public static String round(double num, int afterFp) {
		if (afterFp < 0) throw new IllegalArgumentException("afterFp < 0");
		String formatTemplate = afterFp > 0 ? "0." : "0";
		for (int i: fn.range(afterFp)) formatTemplate = formatTemplate + "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat(formatTemplate);
		df.setRoundingMode(java.math.RoundingMode.HALF_UP);
		return df.format(num);
	}
	
	@Deprecated public static String sliceString(String source, int start, int end) {
		fn.log("Warning: Used a deprecated method in class str");
		return slice(source, start, end);
	}
	public static String slice(String source, int start, int end) {
		/*int len = source.length();
		if (start == 0 || end == 0) return "";
		if (Math.abs(start) > len || Math.abs(end) > len) return "";
		if (start <= -1) return sliceString(source, len+start+1, end);
		if (end <= -1)   return sliceString(source, start, len+end+1);
		if (start > end) return sliceString(source, 0, 0);
		return source.substring(start-1, end-1 + 1);*/
//		if (start == 0 || end == 0) return "";
		
		// 1234567890
		//          
		int[] interval = list.normalizeIntervalAndClampToBounds(start, end, source.length(), true);
		if (interval == null) return "";
		start = interval[1-1]; end = interval[2-1];
		return source.substring(start-1, end-1+1);
		
//		return joinStrList(
//			(new list<>((i) -> source.substring(i, i+1), 0, source.length()-1, 1)).slice(start, end),
//			""
//		);
	}
	
	
	@Deprecated public static boolean equal(String a, String b) {return a.equals(b);}
	
	
	@Deprecated public static boolean containsAt(String substr, int index, String mainstr) {
		int slen = substr.length();
		return
			(! (index+slen-1 > mainstr.length()) ) &&
			substr.equals(str.slice(mainstr, index, index+slen-1));
		/*if (index+substr.length()-1 > mainstr.length()) return false;
		if (substr.equals(str.sliceString(mainstr, index, index+substr.length()-1))) return true;
		return false;*/
	}
	
		public static boolean startsWith(String text, String prefix, int offset) {
			if (offset < 0) throw new IllegalArgumentException("Given offset amount is negative, which does not quite make sense");
			if (!(text.length() >= prefix.length() + offset)) return false;
			return text.startsWith(prefix, offset);
		}
		public static boolean endsWith(String text, String suffix, int offset) {
			if (offset < 0) throw new IllegalArgumentException("Given offset amount is negative, which does not quite make sense");
			if (!(text.length() >= suffix.length() + offset)) return false;
			return text.startsWith(suffix, text.length() - suffix.length() - offset);
		}
		public static boolean startsWith(String text, String prefix) {
			return startsWith(text, prefix, 0);
		}
		public static boolean endsWith(String text, String suffix) {
			return endsWith(text, suffix, 0);
		}
		
		
	//	************************************ (garbage??) ************************************
			// TODO: Index (at least 1) or offset (at least 0)????????
			// TODO: Maybe (!) use your implementation that slices and compares (though might use additional unnecessary memory with very large main string)
			// TODO: Check length differences etc. as well as just the f.ing index!
			// TODO: startsWith and endsWith with index/offset OR startsOrEndsWith with only index (negative to check for ends and positive to for starts)??????
			//            -> i.e. containsAt style (but negative means count the end of the substring and not start as the index, or count the start but count
			//               from the ending of the MAIN string???????????)
			// Hence those below are f.ing PRIVATE till u get ur ass to decide on all those!!!
			private static boolean _startsWith(String text, String prefix, int index) {
			//	if (index == 0) throw new IllegalArgumentException("Given index is 0");
				if (index <= 0) throw new IllegalArgumentException("Given index is not positive");
			//	if (index < 0) index = -index;
			//	if (index < 0) throw new IndexOutOfBoundsException("Index: "+index+", Size: "+text.length());
			//	if (index < 0) return text.startsWith(prefix_suffix, index-1);
			//	else return text.endsWith(prefix_suffix, index-1);
				return text.startsWith(prefix, index-1);
			}
			private static boolean _endsWith(String text, String suffix, int index) {
				if (index <= 0) throw new IllegalArgumentException("Given index is not positive");
				return text.startsWith(suffix, text.length() - suffix.length() - index + 1);
			}
			private static boolean _startsWith(String text, String prefix) {
				return _startsWith(text, prefix, 1);
			}
			private static boolean _endsWith(String text, String suffix) {
				return _endsWith(text, suffix, 1);
			}
	//	************************************ (garbage??) ************************************
	
	
	
	/** Class of match results of a String with a regex pattern.
	 *  Gives the strings that match into the groups of the pattern by the group number or group names.
	 *  The Java regex API doesn't seem to provide a way to clone the match result with a state independent
	 *  from the original one or to get all of the group names. This class can't be used with finding the
	 *  multiple substrings rather than matching the whole given string (maybe ...)
	 */
	// obj.get("part1") -> matcher.group("part1"), obj.get(2) -> matcher.group(2)
	// TODO: Consider removing this (at expense of making any code calling this
	// dependent on a class that you don't own) and using a MatchResult
	public static class Match {
//		private final MatchResult matchResult;
		private final Matcher matcher;
		
//		private Match(MatchResult result) {enclosedMatcher = result;}
		private Match(Matcher matcher) {
			this.matcher = matcher;
//			matchResult = matcher.toMatchResult();
		}
		
		
		/** Checks all of the substrings that are in the regex string against itself to see which
		 *  ones are named group names, by examining the matcher derived from it that matches with a string
		 *  (anythingThatMatches) that the regex can be found in; and returns all of those groups.
		 *  
		 *  <p>This method's runtime time complexity is quadratic by the length of the regex pattern.
		 *  However, it could be greatly reduced by only checking named groups from the strings
		 *  that match with  as substring of the regex pattern; it will match
		 *  all named group names including the ones inside a pair of literal evaluation blocks
		 *  ("\Q" and "\E") and test only those against whether they are actually groups. Note the
		 *  missing closing parentheses and the pattern inside the named group as if it matches
		 *  the whole group then it misses the nested groups and hence their names!
		 */
		public static java.util.Set<String> getAllNamedGroups(String regexPattern) {
			final list<String> possibleGpNames;
			if (true) {
				String regGpNamePattern = "\\(\\?\\<(?<nameOfTheNamedGroup>.+?)\\>";
				possibleGpNames = new linklist<>();
				Matcher mat = Pattern.compile(regGpNamePattern).matcher(regexPattern);
				while (mat.find()) possibleGpNames.add(mat.group("nameOfTheNamedGroup"));
			}
			java.util.Set<String> groups = new java.util.HashSet<>();
			Pattern pat = Pattern.compile("(()|("+regexPattern+"))");
			Matcher mat = pat.matcher("");
			if (!mat.matches()) throw new Error();
			int len = regexPattern.length();
			
	//			for (String groupTest: possibleGpNames) {
			for (String possibleGpName: new linklist<String>((self) -> {
				for (int s: fn.range(1, len)) for (int e: fn.range(s, len)) self.add(str.slice(regexPattern, s, e));
			})) {
				try {
					if (mat.group(possibleGpName) != null)
						throw new AssertionError("Empty string can\'t match with a named group of a regex like \u201c()|(<another pattern>)\u201d!");
				} catch (IllegalArgumentException ex) {
					String expected = "No group with name <"+possibleGpName+">";
					if (!ex.getMessage().equals(expected))
						throw new AssertionError(
							"Regex matcher IllegalArgumentException detail message does not match with \u201c"+expected+"\u201d (\u201c"+ex.getMessage()+"\u201d)"
						);
					continue;
					
				}
				groups.add(possibleGpName);
			}
			
			return groups;
		}
		
		public static java.util.Set<String> getAllNamedGroups(Pattern regex) {return getAllNamedGroups(regex.pattern());}
		
		public java.util.Set<String> getAllNamedGroups() {
			return getAllNamedGroups(matcher.pattern().pattern());
		}
		
		public String get() {return matcher.group();}
		public String get(String groupName) {return matcher.group();}
		public String get(int groupInd) {return matcher.group(groupInd);}
		public int[] getIndex() {return new int[] {matcher.start() + 1, matcher.end()};}
		public int[] getIndex(String groupName) {return new int[] {matcher.start(groupName) + 1, matcher.end(groupName)};}
		public int[] getIndex(int groupID) {return new int[] {matcher.start(groupID) + 1, matcher.end(groupID)};}
	//	public String getGroup() {}
	//	public String group() {}
		
		public static Match getMatch(String text, Pattern regex) {
			Matcher mt = regex.matcher(text);
			boolean matches = mt.matches();
			if (!matches) return null; // Or throw some exception?
			return new Match(mt);
		}
		
		public static Match getMatch(String text, String regex) {
			return getMatch(text, Pattern.compile(regex));
		}
		
		public static interface MatchGetter {
			public String get(int groupInd);
			public String get(String groupName);
		}
		
		public static String replaceAll(String text, String matching, Function<String, Function<MatchGetter, String>> as) {
			Pattern pat = Pattern.compile(matching);
			Matcher mat = pat.matcher(text);
//			mat.replaceAll((result) -> { // NOT AVAILABLE UNDER JAVA 8!!!!!
//				return null;
//			}); // FIXME
			return null; // FIXME
		}
		
		// We can't return a set/list/array/iterable or an iterator of match objects because we
		// can't derive multiple java.util.regex.Matcher objects from a single one that do not
		// affect each other (are independent from each other). The Matcher class just presents
		// an asMatchResult methud but it is insufficient: MatchResult object can't return the
		// value or indexes of the NAMED groups!!
		// But what if we use the same, then if we try to use the prev one (that has the same
		// matcher that is ready for an existing next one), it just recreates its matcher out
		// of scratch???
		// Or what if we just create another matcher for each element from the iterator and set its
		// position into some manual value and make it match the first part after that index??
		// 
		// HINT (HANTHINTHUNT HINT): matcher.find(startIndexOfUpnextMatchPart_1based_inclusive - 1);
		// Index to find the next from should be the 1 more of the ending index of the last part.
		// In “abc12345678def”, this way, “[0-9]{3}” finds “123” then “456”; not “345” or “567”.
		// 
		/*public static java.util.Iterator<Match> getMatches(String text, java.util.regex.Pattern regex) {
			java.util.regex.Matcher mt = regex.matcher(text);
			
			return new java.util.Iterator<Match>() {
				public boolean hasNext() {
					// TODO Auto-generated method stub
					return false;
				}
				public str.Match next() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}*/
		
//		public static java.util.Iterator<Match> getMatches(String text, String regex) {
//			return getMatches(text, java.util.regex.Pattern.compile(regex));
//		}
	}
	
	
	
	@Deprecated public static String joinStrList(Iterable<String> list, String delimiter) {
		fn.log("Warning: Used a deprecated method in class str");
		return join(list, delimiter);
	}
	public static String join(Iterable<String> list, String delimiter) {
		StringBuilder sb = new StringBuilder();
		java.util.Iterator<String> itr = list.iterator();
		if (itr.hasNext()) sb.append(itr.next());
		while (itr.hasNext()) sb.append(delimiter+itr.next());
		return sb.toString();
	}
	
	
	/** <p>
	 *  Warning: Do not structurally modify the returned value!
	 *  <p> Slices off the trailing empty strings from the beginning and the end.
	 */
	public static list<String> split(String input, String regExp) {
		return split(input, Pattern.compile(regExp));
	}
	/**  Warning: Do not structurally modify the returned value!
	 */
	public static list<String> split(String input, String regExp, boolean sliceOffHeadingAndTrailing) {
		return split(input, Pattern.compile(regExp), sliceOffHeadingAndTrailing);
	}
	/** <p> Warning: Do not structurally modify the returned value!
	 * Slices off the trailing empty strings from the beginning and the end.
	 */
	public static list<String> split(String input, Pattern regExp) {
		return split(input, regExp, true);
	}
	public static list<String> split(String input, Pattern regExp, boolean sliceOffHeadingAndTrailing) {
		String[] array = regExp.split(input, -1); // 0: discard all TRAILING empty strings, <0: do not discard any empty strings
		list<String> output;
		if (sliceOffHeadingAndTrailing) output = new linklist<>(array);
		else output = list.listWrapper.asList(array);
//		output = list.listWrapper.asList(regexSplitted); // This makes in not possible to modify the list below
//		output = new list.listWrapper<String>(regexSplitted) {
//			public list.listWrapper<String> newInstance() {return clone();}
//			protected List<String> getInitialEnclosed() {return java.util.Arrays.asList(regexSplitted);}
//		};
		
		// Use structurally modifiable list as long as the below is going to work!!!
		if (sliceOffHeadingAndTrailing) {
			for (int i: new int[] {1, -1})
				while (!output.isEmpty() && output.g(i).length() == 0)
					output.rm(i);
			// Convert back to non structurally-modifiable array list (just like the
			// other case wehre the boolean param is false) because the method should
			// return a value from the same type, consistently, against different cases.
			output = list.listWrapper.asList(output.getArray(String.class));
		}
		return output;
	}
	
	
//	
//	
//	/** @deprecated Because I've explored the useful {@link java.util.regex.Pattern} (much better) class in the JRE that can split Strings. */
//	@Deprecated private static class stringSplitter {
//		
//		
//		/*private static class resourceContainer {
////			public String input, current;
////			public list<String> result;
////			public int lensrc;
//			public int startInd, currentInd; // Only these variables will get assigned values inside lambda expressions, others can stay as local variable.
////			public boolean appendPending;
//		}*/
//		
//		
//		public static interface void_void {public void act();}
////		public static interface resCont_void {public void act(resourceContainer container);}
//		public static interface int_int {public int calc(int n);}
//		public static interface int_void {public void act(int n);}
//		public static interface _2int_void {public void calc(int m, int n);}
////		public static interface _2str1int_bool {public void calc(String source, String substr, int start);}
////		public static interface _1resCont2str1int_bool {public void calc(resourceContainer container, Stirng source, String substr, int start);}
//		
//		/** @deprecated Because a method in the {@link String} class provides the same function (yet better) by taking a
//		 * <strong>regexp</strong> to split exactly where the matches occur at. */
//		@Deprecated private static list<String> splitString(String source, list<String> delimiters, boolean sweepConsecutively) {
//			list<String> result;
//			int lensrc;
//			
//			void_void/* alloc,*/ append;
//			void_void takeStart;
//			int_void stepCurrent, trim;
//			int_int lenOfDelmWhichMatches;
//			
//			result = new linklist<>();
//			int[] startInd = new int[] {1};
//			int[] currentInd = new int[] {1};
//			
//			
//			
//			append = () -> {
//				result.add(str.slice(source, startInd[0], currentInd[0]-1));
//			};
//			stepCurrent = (increment) -> {currentInd[0] += increment;};
//			takeStart = () -> {startInd[0] = currentInd[0];};
//			trim = (index) -> {
//				if (result.size() < 1) return;
//				if (index == 1 || index == -1)
//					if (result.g(index).equals(""))
//						result.rm(index);
//			};
//			
////------------------------------------------------------
//			
//			int matchingDelmLen;
//			lensrc = source.length();
//			lenOfDelmWhichMatches = (int start) -> {
////				for (int each: delimiters.indexIterator())
////					if (fn.equal(delimiters.g(each), start, source))
////						return delimiters.g(each).length();
//				for (String delimiter: delimiters)
//					if (containsAt(delimiter, start, source))
//						return delimiter.length();
//				return 0;
//			};
//			for (String each: delimiters) if (each.length() == 0) return new linklist<>(1, source);
//			
//			while (currentInd[0] <= lensrc) {
//				matchingDelmLen = lenOfDelmWhichMatches.calc(currentInd[0]);
//				if (matchingDelmLen != 0) {
//					append.act();
//					stepCurrent.act(matchingDelmLen);
//					takeStart.act();
//				} else stepCurrent.act(1);
//			}
//			
//			append.act();
//			trim.act(1);
//			trim.act(-1);
//			
//			if (sweepConsecutively) result.__init__(new linklist<>(result, (word) -> !word.equals("")));
//			return result;
//			
//			
////------------------------------------------------------
//			
////			if (!multipleDelimiters) {
////				String delimiter = delimiters.get(1);
////				int lendelm = delimiter.length();
////				if (lendelm == 0) return new list<>(1, source);
////				while (currentInd[0] <= lensrc) {
//////					if (!u.appendPending) alloc.act();
////					if (fn.equal(delimiter, currentInd[0], source)) {
////						append.act();
////						stepCurrent.act(lendelm);
////						takeStart.act();
//////						alloc.act();
////					} else stepCurrent.act(1);
////				}
//////				fn.print(u. currentInd, " ");
//////				u.currentInd = u.lensrc+1;
//////				fn.print(u.currentInd);
////				
////			}
////			
////			else {
////				int_int lenOfDelmWhichMatches = (int start) -> {
////					for (int each: delimiters.indexIterator())
////						if (fn.equal(delimiters.get(each), start, source))
////							return delimiters.get(each).length();
////					return 0;
////				};
////				int matchingDelmLen;
////			A:	while (true) {
////				B:	while (currentInd[0] <= lensrc) {
////						matchingDelmLen = lenOfDelmWhichMatches.calc(currentInd[0]);
////						if (matchingDelmLen != 0) stepCurrent.act(matchingDelmLen); // Until matches stop coming or the string ends
////						else break;
////					}
////					if (! (currentInd[0] <= lensrc)) break;
////					
////					takeStart.act();
////				B:	while (currentInd[0] <= lensrc) {
////						if (lenOfDelmWhichMatches.calc(currentInd[0]) == 0) stepCurrent.act(1); // Until a match is found or the string ends
////						else break;
////					}
////					if (currentInd[0] >= 1) append.act();
////					if (! (currentInd[0] <= lensrc)) break;
////				}
////			}
//			
//			
//			
//			
//			
//			
////			return result;
//			
//		}
//		
//		
//		
//	}
	
	
	
	
	public static class TextStyle {
		
		
		/** Gets an empty formatted text that other formatted texts, single strings and single format entries can be concatenated to. */
		public static FormattedText newStr() {
			return new FormattedText();
		}
		
//		@Deprecated public static FormattedText render(Object... args) {
//			list<strStyleUnion> _args = new linklist<>();
//			for (Object arg: args) {
//				if (arg instanceof String)
//					_args.add(new strStyleUnion((String) arg));
//				else if (arg instanceof Style)
//					_args.add(new strStyleUnion((Style) arg));
//				else if (arg == null)
//					throw new NullPointerException();
//				else throw new IllegalArgumentException(
//					"All elements of the array passed to this method should be either String or Style"
//				);
//			}
//			return new FormattedText(_args);
//		}
		
		
		public static class FormattedText {
			private final basicList<strStyleUnion> attributes;
			
			public FormattedText() {
				attributes = new basicList<strStyleUnion>() {
					public strStyleUnion get(int i) {
						throw new IndexOutOfBoundsException();
					}
					public int size() {return 0;}
				};
			}
			
			public FormattedText(Iterable<strStyleUnion> entries) {
				list<strStyleUnion> entryList = new linklist<>(entries);
				strStyleUnion[] arr = new strStyleUnion[entryList.size()];
				
				attributes = new basicList<str.TextStyle.strStyleUnion>() {
					public strStyleUnion get(int i) {return arr[i-1];}
					public int size() {return arr.length;}
				};
				
				{int i = 0; for (strStyleUnion entry: entries) {
					arr[(++i)-1] = entry;
				}}
			}
			
//			/** Gets an empty formatted text. */
//			public static FormattedText getEmpty() {
//				return new FormattedText();
//			}
			
			public FormattedText(basicList<strStyleUnion> _list) {
				attributes = _list;
			}
			
			public basicList<strStyleUnion> getAttributes() {
				return attributes;
			}
			
			/** Returns the {@link FormattedText} that is the concatenated the given String entry to the current instance */
			public FormattedText text(String text) {
				return concat(new FormattedText(
					java.util.Arrays.asList(
						new strStyleUnion(text)
					)
				));
			}
			/** Returns the {@link FormattedText} that is the concatenated the given Style entry to the current instance */
			public FormattedText style(Style style) {
				return concat(new FormattedText(
					java.util.Arrays.asList(
						new strStyleUnion(style)
					)
				));
			}
			
//			/*private*/ FormattedText concat(FormattedText suffix) {
			public FormattedText concat(FormattedText suffix) {
				FormattedText prefix = this;
				
				basicList<strStyleUnion> attrList = new basicList<strStyleUnion>() {
					private int prefixSize = -1, suffixSize = -1;
					// Remember (cache) the this's size in the new object, to avoid calculating again and again by calling the prefix's and suffix's size() s and summing them
					private int prefixSize() {
						if (prefixSize != -1) return prefixSize;
//						else return prefixSize = FormattedText.this.attributes.size();
						else return prefixSize = prefix.attributes.size();
					}
					private int suffixSize() {
						if (suffixSize != -1) return suffixSize;
//						else return suffixSize = that.attributes.size();
						else return suffixSize = suffix.attributes.size();
					}
					
					public strStyleUnion get(int ind) {
						if (ind > prefixSize())
//							return that.attributes.get(ind - sizeOfEnclosed);
							return suffix.attributes.get(ind - prefixSize());
						else
//							return FormattedText.this.attributes.get(ind);
							return prefix.attributes.get(ind);
					}
					
					public int size() {
//						return FormattedText.this.attributes.size() + that.attributes.size();
						return prefixSize() + suffixSize();
					}
				};
				
				// Return a new FormattedText object such that whose basicList attributes gets
				// that's entries for a given index pointing to the last few entries and the
				// encapsulated's (prefix's) for the other indexes, and gets the
				// sum of prefix's and suffix's sizes when size() is invoked.
				return new FormattedText(attrList);
			}
			
			public byte[] getUTF8Bytes() {
				list<Byte> bytes = new linklist<>();
				java.io.PrintStream out;
				{
					java.io.OutputStream os = new java.io.OutputStream() {
						public void write(int b) {
							if (b != -1) bytes.add((byte) b);
						}
					};
					out = new java.io.PrintStream(os);
				}
				printTo(out);
				out.close();
				return list.byteArrayOf(bytes);
			}
			
			public String toString() {
				String[] output = {""};
				boolean plain = true;
				Consumer<String[]> appendCodes = (codes) -> {
						output[0] = output[0] + String.format("<esc>[%sm",
						str.join(linklist.fromArray(codes), ";")
					);
				};
				for (strStyleUnion part: attributes)
					if (part.isString) output[0] = output[0] + part.text;
					else {
						plain = false;
						appendCodes.accept(part.format.getCodes());
					}
				if (!plain) appendCodes.accept(new String[] {"0"});
				return output[0];
			}
			public String toPlainString() {
				String plain = "";
				for (strStyleUnion part: attributes)
					if (part.isString) plain = plain + part.text;
				return plain;
			}
			
			
			/** Prints the formatted text into the passed {@link java.io.PrintStream PrintStream} parameter */
			public void printlnTo(java.io.PrintStream out) {printTo(out); out.println();}
			/** Prints the formatted text into the passed {@link java.io.PrintStream PrintStream} parameter */
			public void printTo(java.io.PrintStream out) {
				int count = 0, len = attributes.size();
				String end = "m";
				for (strStyleUnion entry: attributes) {
					count++;
					if (!entry.isString) {
						out.write(esc);
						out.print("[" + str.join(java.util.Arrays.asList(entry.format.getCodes()), ";"));
						out.print(end);
					} else {
						out.print(entry.text);
					}
					if (count == len) {
						out.write(esc);
						out.print("[0"+end);
					}
				}
			}
			
			
			public static interface basicList<E> extends Iterable<E> {
				public E get(int ind);
				public int size();
				public default java.util.Iterator<E> iterator() {
					java.util.Iterator<Integer> intItr = fn.range(1, size(), 1).iterator();
					return new java.util.Iterator<E>() {
						public boolean hasNext() {return intItr.hasNext();}
						public E next() {return get(intItr.next());}
					};
				}
			}
			
			
		}
		
		public static class strStyleUnion {
			public final String text;
			public final Style format;
			public final boolean isString;
			
			public strStyleUnion(String text) {
				this.text = text;
				this.format = null;
				this.isString = true;
			}
			public strStyleUnion(Style format) {
				this.text = null;
				this.format = format;
				this.isString = false;
			}
		}
		
		private static final byte esc = (byte) 0x1b;
		
//		public static String format(String input, Style... colorEntries) {
//			String prefix = "";
//			for (Style entry: colorEntries) prefix = prefix + entry;
//			return prefix + input + constant.RESET;
//		}
		
		// TODO: Make so that the returned color has a public Style FrontColor.asBackgroundColor() that returns the style for coloring the background with the same color.
		// Not used customcolor because some consoles (like PuTTY) do not support it yet.
		// If yours supports at least xterm256 (8bit) colors let's just be thankful
		// instead of expecting the 24-bit (16-million colors) support
		public static ForegroundColor forRGB(double r, double g, double b) {return xterm256color.forRGB(r, g, b);}
		public static ForegroundColor forRGB(int r, int g, int b) {return xterm256color.forRGB(r, g, b);}
//		private static final boolean e = fn.getRandom(1, 2) == 1;
//		public static Style forRGB(int r, int g, int b) {
//			if (e)
//				return xterm256color.forRGB(r, g, b);
//			else return CustomColor.forRGB(r, g, b);
//		}
		
		public static interface ForegroundColor extends Style {
			public default Style asBackgroundColor() {
				return new Style() {
					public String[] getCodes() {
						String[] codes = ForegroundColor.this.getCodes();
						codes[1-1] = "48";
						return codes;
					}
				};
			}
		}
		
		public static interface Style {
			public String[] getCodes(); // [“38”, “5”, “221”] -- colorize foreground with xterm 221; [“35”] -- colorize background with ASCII 5
//			public default String format(String input) {return TextStyle.format(input, this);}
		}
		
		public static enum constant implements Style {
			
			RESET("0"),
			BOLD("1"), ITALIC("3"), UNDERLINED("4"), INVERSED("7"),
			
			WHITE("37"), BLACK("30"),
			RED("31"), GREEN("32"), BLUE("34"),
			YELLOW("33"), CYAN("36"), MAGENTA("35"), PURPLE("35"),
			
			WHITE_BG("47"), BLACK_BG("40"),
			RED_BG("41"), GREEN_BG("42"), BLUE_BG("44"),
			YELLOW_BG("43"), CYAN_BG("46"), MAGENTA_BG("45"), PURPLE_BG("45"),
			
			L_WHITE("97"), L_BLACK("90"),
			L_RED("91"), L_GREEN("92"), L_BLUE("94"),
			L_YELLOW("93"), L_CYAN("96"), L_MAGENTA("95"), L_PURPLE("95");
			
			
			private final String code;
			private constant(String code) {this.code = code;}
			public String[] getCodes() {return new String[] {code};}
			
//			public String toString() {return formatIntoSpecifier(code);}
		}
		
		public static Style forASCII(int val) {
			if (val>=0 && val<=255) return new xterm256color(val);
			throw new IllegalArgumentException(""+val);
		}
		
		
		private static class CustomColor implements Style, ForegroundColor {
			public final int r, g, b;
			public CustomColor(int r, int g, int b) {
				for (int i: new int[] {r, g, b}) {if (! (i>=0 && i<=255) ) throw new IllegalArgumentException(
					"Passed argument is out of range. The method \u201cforRGB\u201d takes 3 integers ranging in 0-255 or 3 fp rationals ranging in 0-1."
				);}
				this.r = r; this.g = g; this.b = b;
			}
			public static CustomColor forRGB(double r, double g, double b) {int u = 255; return forRGB((int) (u*r), (int) (u*g), (int) (u*b));}
			public static CustomColor forRGB(int r, int g, int b) {return new CustomColor(r, g, b);}
			
			public String[] getCodes() {
				return new String[] {
					"38", "2",
					Integer.toString(r, 10),
					Integer.toString(g, 10),
					Integer.toString(b, 10)
				};
			}
		}
		
		
		private static class xterm256color implements Style, ForegroundColor {
			
			private final String code;
//			private static java.lang.ref.Reference<list<byte[]>> _256constants = new java.lang.ref.WeakReference<>(null);
			public static final Supplier<list<byte[]>>
				_256constants = fn.cachedReference(xterm256color::generate256constants);
			
			private xterm256color(int code) {this.code = Integer.toString(code, 10);}
			
			public String[] getCodes() {return new String[] {"38", "5", code};}
			
			private static Supplier<java.util.Map<triplet, Integer>>
				cachedTriplets = fn.cachedReference(() -> new java.util.WeakHashMap<>()); // i.e.: {[af, 87, ff]: 141, [df, ff, df]: 194}
			// Both the keys/values and the map itself will be swept by GC.
			
			private static final class triplet {
				final byte r, g, b;
				public triplet(byte r1, byte g1, byte b1) {r=r1; g=g1; b=b1;}
				public boolean equals(triplet that) {return this.r==that.r && this.g==that.g && this.b==that.b;}
				public boolean equals(Object that) {return (that instanceof triplet) && equals((triplet) that);}
				public int hashCode() {
					int result = 0;
					for (int i: new int[]{r, g, b}) result = 31*result + i;
					return result;
				}
			}
			
			private static list<byte[]> generate256constants() {
				/*Function<String, byte[]> hex2bytes = (input) -> {
					int qty;
					{int len = input.length();
					if (len % 2 != 0) throw new IllegalArgumentException(
						"Length of the given hex as string is not an integer times of 2."
					);
					qty = len/2;}
					byte[] result = new byte[qty];
					(new list<>((n) -> {
						int ind = 2*n - 1;
						return Integer.valueOf(str.sliceString(input, ind, ind+1), 16);
					}, qty)).forEach(
						new Consumer<>() {private int i = 0;
							public void accept(Integer _byte) {result[i++] = (byte) (int) _byte;}
						}
					);
					return result;
				};*/
				
				list<byte[]> result = new linklist<>();
				  { // Add the first 14 colors, a white and a black
					byte[] first16 = str.fromBase64("AAAAgAAAAIAAgIAAAACAgACAAICAwMDAgICA/wAAAP8A//8AAAD//wD/AP//////");
					for (int i: fn.range(16)) {
						byte[] color = new byte[3];
						for (int j: fn.range(3))
							color[j - 1] = first16[3*(i-1)+j - 1];
						result.add(color);
					}
				} { // Add 216 colors
					list<Byte> c = new linklist<>((i) -> (byte) (0x5f + i*0x28), 0, 4); c.a(1, (byte)0);
					for (byte r: c) for (byte g: c) for (byte b: c) result.add(new byte[] {r, g, b});
				} { // Add the end-grayscale
					for (int[] state: new linklist<>(new int[] {0x08, 0x0a, 8}, new int[] {0x60, 0x06, 1}, new int[] {0x76, 0x0a, 12})) {
						int start = state[0], by = state[1], upperBound = state[2];
						for (int i: fn.range(0, upperBound)) {
							byte[] current = new byte[3]; for (int j: fn.range(3)) current[j-1] = (byte) (start + i*by);
							result.add(current);
						}
					}
				}
				if (true)
				{ // Discard 2-16 back!
					java.util.ListIterator<byte[]> itr = result.listIterator();
					for (int i: fn.range(1, 1)) itr.next();
					for (int i: fn.range(2, 16)) {
						itr.set(null);
					}
				}
				else {
					byte[] first = result.g(1);
					result = (new linklist<byte[]>(16-1, null)).concat(result.slice(17, -1));
					result.a(1, first);
					if (result.size() != 256) throw new Error(""+result.size());
				}
				return result;
			}
			
			
			public static xterm256color forRGB(double r, double g, double b) {int u = 255; return forRGB((int) (u*r), (int) (u*g), (int) (u*b));}
			/** Returns a piece of text like "\u001b[38;5;0" that ANSI consoles generally understand as
			 * some text with a special escape character to change the text color, with a proper number
			 * instead of 0, to represent the closest possible xterm256 color constant to the given values. */
			/* TODO: Make it so that called value triplets get either in a weak HashMap to their XTerm256
			 * numbers so that the every entry of the map is cleared whenever the GC hits it (i.e. not ones
			 * used too often and became in long generation or eden space in the heap); or a HashMap that
			 * clears one per insertion after the ~10th. */
			public static xterm256color forRGB(int r, int g, int b) {
				Function<int[][], Double> calcDiff = (values) -> {
					// [[r1, r2], [g1, b2], [b1, b2]] -> ...
					int sum = 0; for (int[] pair: values) sum += (int) fn.pow(pair[1] - pair[0], 2);
					return Math.pow(((double)sum)/(values.length/* - 1*/), 0.5);
				};
				
				for (int i: new int[] {r, g, b}) {if (! (i>=0 && i<=255) ) throw new IllegalArgumentException(
					"Passed argument is out of range. The method \u201cforRGB\u201d takes 3 integers ranging in 0-255 or 3 fp rationals ranging in 0-1."
				);}
				
				java.util.Map<triplet, Integer> cached = cachedTriplets.get();
				assert cached != null;
				Integer val = cached.get(new triplet((byte)r, (byte)g, (byte)b));
				if (val != null) return new xterm256color(val);
				
				double minDiff = Double.NaN;
				int minind = 0;
				int[] args = new int[] {r, g, b};
				
				byte[] existingColor = null;
				{int ind = 0; for (byte[] eachColor: _256constants.get()) {
					ind++;
					if (eachColor == null) continue;
					int[][] arg = new int[3][2]; // [[r1, r2], [g1, g2], [b1, b2]]
					for (int i: fn.range(3)) {
						arg[i-1][0] = 0xff & eachColor[i-1];
						arg[i-1][1] = args[i-1];
					}
					double diff = calcDiff.apply(arg);
					if (Double.isNaN(minDiff) || diff < minDiff) {
						minDiff = diff;
						minind = ind;
						existingColor = eachColor;
					}
				}}
				assert existingColor != null;
				cached.put(new triplet(
					existingColor[1-1],
					existingColor[2-1],
					existingColor[3-1]
				), minind - 1);
				cached.put(new triplet(
					(byte)r, (byte)g, (byte)b
				), minind - 1);
				
				return new xterm256color(minind - 1);
			}
			
		}
		
	}

	
	
	
	
	
	
	
	
}


// -------------------------------------- UNUSED STUFF ------------------------------------------------
//	
//	
//	
//		private static list<String> splitString(boolean multipleDelimiters, String source, list<String> delimiters) {
//			list<String> result;
//			int lensrc;
//			
//			void_void/* alloc,*/ append;
//			void_void takeStart;
//			int_void stepCurrent, trim;
//			
//			resourceContainer u = new resourceContainer();
//			result = new list<>();
//			u.startInd = 1;
//			u.currentInd = 1;
////			u.appendPending = true;
////			u.current = "";
//			
//			
//			append = () -> {
//				result.add(str.sliceString(source, u.startInd, u.currentInd-1));
////				u.appendPending = false;
//			};
////			alloc = () -> {
////				u.result.add("");
////				u.appendPending = true;
////			}; // I translated this method from my C implementation of it and there does not seem a need for using this inner function, because I just don't need to recall operations including manual memory allocation.
//			stepCurrent = (increment) -> {u.currentInd += increment;};
//			takeStart = () -> {u.startInd = u.currentInd;};
//			trim = (index) -> {
//				if (result.size() < 1) return;
//				if (index == 1 || index == -1)
//					if (result.get(index).equals(""))
//						result.remove(index);
//			};
//			
//			
//			lensrc = source.length();
//			
//			if (!multipleDelimiters) {
//				String delimiter = delimiters.get(1);
//				int lendelm = delimiter.length();
//				if (lendelm == 0) return new list<>(1, source);
//				while (u.currentInd <= lensrc) {
////					if (!u.appendPending) alloc.act();
//					if (fn.equal(delimiter, u.currentInd, source)) {
//						append.act();
//						stepCurrent.act(lendelm);
//						takeStart.act();
////						alloc.act();
//					} else stepCurrent.act(1);
//				}
////				fn.print(u. currentInd, " ");
////				u.currentInd = u.lensrc+1;
////				fn.print(u.currentInd);
//				append.act();
//				trim.act(1);
//				trim.act(-1);
//			}
//			
//			else {
//				int_int lenOfDelmWhichMatches = (int start) -> {
//					for (int each: delimiters.indexIterator())
//						if (fn.equal(delimiters.get(each), start, source))
//							return delimiters.get(each).length();
//					return 0;
//				};
//				int matchingDelmLen;
//			A:	while (true) {
//				B:	while (u.currentInd <= lensrc) {
//						matchingDelmLen = lenOfDelmWhichMatches.calc(u.currentInd);
//						if (matchingDelmLen != 0) stepCurrent.act(matchingDelmLen); // Until matches stop coming or the string ends
//						else break;
//					}
//					if (! (u.currentInd <= lensrc)) break;
//					
//					takeStart.act();
//				B:	while (u.currentInd <= lensrc) {
//						if (lenOfDelmWhichMatches.calc(u.currentInd) == 0) stepCurrent.act(1); // Until a match is found or the string ends
//						else break;
//					}
//					if (u.currentInd >= 1) append.act();
//					if (! (u.currentInd <= lensrc)) break;
//				}
//			}
//			
//			return result;
//		}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	