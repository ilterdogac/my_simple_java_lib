import java.util.*;
import java.io.*;

/** The STDIN functions for handling the operations like
 *  scanning/getting tokens and numbers from the STDIN.
 */
public class STDINUtils {
	
	public static final String inputLineSeparator = "((\\r)?(\\n))+";
	
	/** Quick when it comes to extremely large lines (very big part of input with no match for regex like ((\r)?(\n))+). */
	public static final String inputLineSeparator_DAMN_QUICK;
	
	public static final String lineArgSeparator = "(\\t| )+";
	private static final int config_newLineType = 1; // LF: 1, CRLF: 2
	public static final String newLine;
	static {
		if (config_newLineType == 1) {
			inputLineSeparator_DAMN_QUICK = "\\n";
			newLine = "\n";
		}
		else if (config_newLineType == 2) {
			inputLineSeparator_DAMN_QUICK = "\\r\\n";
			newLine = "\r\n";
		}
		else throw new IllegalStateException("New line type (LF/CRLF) static field is not set properly");
	}
	
	
	
	@SuppressWarnings("resource")
	public static java.util.Iterator<String> getLineReader(java.io.InputStream inputStream, String lineDelimiter) throws IOException {
		String encoding = "UTF-8";
		
		java.util.Scanner sc = new java.util.Scanner(inputStream, encoding);
		sc.useDelimiter(lineDelimiter);
		
		return new java.util.Iterator<>() {
			private java.util.Scanner enclosed = sc; // Actually implements Iterator<String> but we need to make sure it will finally be closed.
//			private String upNext = null;
//			private byte hasNext = -1;
			public boolean hasNext() {
				return enclosed.hasNext();
			}
			
			public String next() {
				if (!hasNext()) throw new java.util.NoSuchElementException();
				return enclosed.next();
			}
//			public String next() {
//				if (!hasNext()) throw new java.util.NoSuchElementException();
//				String current = upNext;
//				step();
//				return current;
//			}
//			private void step() {
//				if (enclosed.hasNext()) {
//					upNext = enclosed.next();
//					hasNext = 1;
//				} else {
//					hasNext = 0;
//					enclosed.close();
//				}
//			}
		};
	}
	
	
	
	
	
	public static Iterator<List<String>> consumeParseBySpace(Iterator<String> lines) {
		return consumeParseBy(lines, "( )+");
	}
	public static Iterator<List<String>> consumeParseByTab(Iterator<String> lines) {
		return consumeParseBy(lines, "(\\t)+");
	}
	public static Iterator<List<String>> consumeParseBySpaceTab(Iterator<String> lines) {
		return consumeParseBy(lines, "( |\\t)+");
	}
	public static Iterator<List<String>> consumeParseBy(Iterator<String> lines, String regex) {
		if (lines == null) return null; // TODO: Only keep for BBM104 2023 Fall PA3.
		return new Iterator<List<String>>() {
			public boolean hasNext() {
				return lines.hasNext();
			}
			public List<String> next() {
				return str.split(lines.next(), regex, true);
			}
		};
	}
	
	
	
	
	
	public static Iterator<List<String>> consumeParseSTDIN() {
		return consumeParseInput(System.in, inputLineSeparator, lineArgSeparator);
	}
	public static Iterator<List<String>> consumeParseSTDIN_QUICK() {
		return consumeParseInput(System.in, inputLineSeparator_DAMN_QUICK, lineArgSeparator);
	}
	public static Iterator<List<String>> consumeParseInput(InputStream is) {
		return consumeParseInput(is, inputLineSeparator, lineArgSeparator);
	}
	
	// TODO: An unexpected slowdown may occur because of an instance's being called
	// to do an operation (instead of directly).
	@SuppressWarnings("resource")
	public static <E> Iterator<E> consumeParseInput_Generic_VERY_VERY_QUICK(InputStream is, java.util.function.Function<Scanner, E> toGetNext) {
		Scanner inputGetter = new java.util.Scanner(is, "UTF-8");
		return new Iterator<>() {
			public boolean hasNext() {return inputGetter.hasNextInt();}
			public E next() {
				E value;
				try {value = toGetNext.apply(inputGetter);}
				catch (NoSuchElementException e) {throw e;}
				Objects.requireNonNull(value);
				return value;
			}
		};
	}
	public static Iterator<Integer> consumeParseInput_Int32vals_VERY_VERY_QUICK(InputStream is) {
		return consumeParseInput_Generic_VERY_VERY_QUICK(is, (sc) -> sc.nextInt());
	}
	public static Iterator<Long> consumeParseInput_Int64vals_VERY_VERY_QUICK(InputStream is) {
		return consumeParseInput_Generic_VERY_VERY_QUICK(is, (sc) -> sc.nextLong());
	}
	
	public static Iterator<List<String>> consumeParseInput(InputStream is, String patternInputToLines, String patternLineToArgs) {
		Iterator<String> inputGetter = consumeInput(is, patternInputToLines);
		
		return new Iterator<>() {
			public boolean hasNext() {return inputGetter.hasNext();}
			public List<String> next() {
				String line;
				try {line = inputGetter.next();}
				catch (NoSuchElementException e) {throw e;}
				Objects.requireNonNull(line);
				return str.split(line, patternLineToArgs);
			}
		};
	}

	public static Iterator<String> consumeInput(InputStream is, String patternInputToLines) {
		Scanner inputGetter = new java.util.Scanner(is, "UTF-8");
		inputGetter.useDelimiter(patternInputToLines);
		
		return inputGetter;
	}
	
	
	
}