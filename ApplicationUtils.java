import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.*;


/** General purposed methods and types for the graded works of mostly
 *  Hacettepe <strong>BBM1XX</strong> and <strong>BBM2XX</strong> lab courses. */
public class ApplicationUtils {
	
	private ApplicationUtils() {}
	
	
	// “n1 s”    -> [[“n1”], [“s”]]
	// “n1 n2 s” -> [[“n1”, “n2”], [“s”]]
	// “n1”      -> [[“n1”], []]
	public static fn.Tuple2<list<String>, list<String>> parseName(String fullName, String splitPattern) {
		list<String> names = str.split(fullName, splitPattern);
		list<String> fnames = new linklist<>(), lnames = new linklist<>();
		switch (names.size()) {
			case 0:
				break;
			case 1:
				fnames.add(names.g(1));
				break;
			default:
				names.indexInterval( 1, -2).forEach((each) -> {fnames.add(names.g(each));});
				names.indexInterval(-1, -1).forEach((each) -> {lnames.add(names.g(each));});
				break;
		}
		return fn.tuple(fnames, lnames);
	}
	
	
	public static <E> Set<E> cloneIntoSet(Iterable<E> input) {
		Set<E> output = new HashSet<>();
		for (E e: input) output.add(e);
		return output;
	}
	
	public static <K, V> Map<K, V> cloneIntoHMap(Map<K, V> input) {
		Map<K, V> output = new HashMap<>();
		for (K k: input.keySet()) output.put(k, input.get(k));
		return output;
	}
	
	public static <K, V> list<fn.Tuple2<K, V>> unwindAndSort(Map<K, V> map, Comparator<fn.Tuple2<K, V>> sortBy) {
		list<fn.Tuple2<K, V>> ls = list.listWrapper.getArList();
		for (K key: map.keySet()) ls.add(fn.tuple(key, map.get(key)));
		if (sortBy != null) ls.sort(sortBy);
		return ls;
	}
	
	public static <E> list<E> cloneIntoLinkList(Iterable<E> input) {
		list<E> output = new linklist<>();
		for (E e: input) output.add(e);
		return output;
	}
	public static <E> list<E> cloneIntoArList(Iterable<E> input) {
		list<E> output = getArList();
		for (E e: input) output.add(e);
		return output;
	}
	public static <E> list<E> getArList() {
		return list.listWrapper.wrap(new ArrayList<>());
	}
	
	
	
	
	
	public static final String causeStart = " \u2022 ";
	public static String getLineOmitMessageBegin(String line) {
		return "Error: The line \u201c"+line+"\u201d has been omitted because of the following:";
	}
	public static String getLineOmitMessage(String line, list<String> causes) {
		return str.join((new linklist<>(getLineOmitMessageBegin(line))).concat(causes), "\n"+causeStart) + "\n";
	}
	public static String getInvalidLineMessageBegin(String line) {
		return "Error: The line \u201c"+line+"\u201d is invalid because of the following:";
	}
	public static String getInvalidLineMessage(String line, list<String> causes) {
		return str.join((new linklist<>(getInvalidLineMessageBegin(line))).concat(causes), "\n"+causeStart) + "\n";
	}
	
	
	// TODO: Move this back into str maybe
	public static String ordinalOf(int num) {
		String s1 = "st", s2 = "nd", s3 = "rd", s0 = "th";
		if (num < 0) {
			fn.log("[Warning] str.getOrdinal(int) called with negative number: "+num);
			num = -num;
		}
		if ((num % 100) / 10 == 1) return s0;
		else {
			switch (num % 10) {
				case 1: return s1;
				case 2: return s2;
				case 3: return s3;
				default: return s0;
			}
		}
	}
	
	
	/** An interface to implement some methods to handle given strings as lines.
	 *  This is meant to wrap an actual PrintStream or something else (like
	 *  file writer) so that the formatted text might be printed as raw in the cases
	 *  the wrapped object does not support ASCII color codes unlike
	 *  console outputs. */
	public static interface PrintStream {
//		@Deprecated public void print(String text);
//		@Deprecated public void print(str.TextStyle.FormattedText text);
		public void println(String line);
		public default void println() {println("");}
		public void println(str.TextStyle.FormattedText line);
		public default Iterable<String> flush() {return null;}
	}
	
	/** Stores all of the strings (and the raw values of the formatted texts)
	 *  passed as a parameter to its print methods inside a list ready to be
	 *  flushed out */
	public static class PrintOutStream implements PrintStream {
		private list<String> data;
		
		public PrintOutStream() {data = new linklist<>(); reset();}
		// Synchronize the instance methods on the object itself and the static methods on the class (OutputStream.class) object.
		public synchronized void reset() {data = new linklist<>();}
		/** Note: This clears the stored data. */
		public synchronized Iterable<String> flush() {list<String> last = data; reset(); return last;}
		public synchronized String toString() {return "(StringBuilder) "+str.join(data, "\n");}
		public synchronized void println(String str) {data.add(str);}
//		public synchronized void print(String text) {}
//		public synchronized void print(str.TextStyle.FormattedText text) {print(text.toString());}
		public synchronized void println(str.TextStyle.FormattedText line) {println(line.toString());}
	}
	
	
	public static class PrintStreamWrapper implements PrintStream {
		private final java.io.PrintStream enclosed;
		public PrintStreamWrapper(java.io.PrintStream stream) {enclosed = stream;}
//		public void print(String text) {enclosed.print(text);}
//		public void print(str.TextStyle.FormattedText text) {text.printTo(enclosed);}
		public void println(String line) {enclosed.println(line);}
		public void println(str.TextStyle.FormattedText line) {line.printlnTo(enclosed);}
	}
	
	
}
