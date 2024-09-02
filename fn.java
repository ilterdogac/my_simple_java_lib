// package ...;
// TODO: Remanme the package as “util”, “misc” or “miscutils”
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import static *.*.[*.[*. ...]]{method|"*"};
// import static java.lang.Math.*;


// TODO: Remanme the class as “misc”
public class fn {
	// Some crazy not so necessary sh*t
//	static {
//		Class<?> shadowed = null;
//		try {shadowed = fn.class.getClassLoader().getParent().loadClass(fn.class.getName());}
//		catch (ClassNotFoundException e) {}
//		if (shadowed != null) {
//			throw new IllegalStateException("The class "+fn.class.getName()+" has already been defined in a parent classloader");
//		}
//	}
	
	
	// ------------------------------------------------- Natives ------------------------------------------------
	private static boolean nativeLoaded = false;
	private static boolean nativeLoadTried = false;
	
	public static boolean nativeLoaded() {return nativeLoaded;}
	public static boolean nativeLoadTried() {return nativeLoadTried;}
	
	static {
		// Do it on-demand instead
//		loadNatives();
	}
	
	/** Attempts to load the necessary native libraries to the loader classloader, for the native methods to be
	 *  linked with. Returns the success state.
	 *
	 *  Note: 2 classloader cannot have the same (at the same location) native libraries loaded to themselves.
	 *  A parent classloader should load such a library for them and let the sub classloader use its class'
	 *  native methods.
	 **/
	public static boolean loadNatives() {
		if (nativeLoadTried) return nativeLoaded; // Already done
		boolean loaded = true;
		loaded = loaded & fn.loadLibrary("ProcessCMDLine");
		nativeLoaded = loaded;
		nativeLoadTried = true;
		return nativeLoaded;
	}
	
	
	public static native String getCommandLineW();
	public static native String getCommandArgsW();
	// ----------------------------------------------------------------------------------------------------------
	
	
	
	// -------------------------------------------- Process commandline --------------------------------------------
	// Specific for Windows because the Java Runtime Environment doesn't get the arguments from Windows consoles
	// properly and all the non-latin characters get broken.
	public static String getCmdLine() {
		if (!nativeLoadTried) loadNatives();
		if (nativeLoaded) return getCmdLine0();
		else return null;
	}
	private static String[] getCmdArgs() {
		if (!nativeLoadTried) loadNatives();
		if (nativeLoaded) return getCmdArgs0();
		else return null;
	}
	
	private static native String getCmdLine0();
	private static native String[] getCmdArgs0();
	
	// -------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	static {
		
		// TODO: Add a security manager that denies deleting (or maybe also overwriting)
		// any file, so that any script that I write does not delete sh*t.
		
	//	log(fn::print); // Set so that the messages that shouldn't always be
	//	                // directly printed to STDOUT get normally printed to STDOUT.
	//	// /* Or this: */ log((line) -> {fn.print(line);});
		setLog((String line) -> {
			String warnPfx = "warning";
		A:	{
			
				for (String a: new String[] {warnPfx, "["+warnPfx+"]"})
				for (String b: new String[] {a, a+":"}) {
					if (str.slice(line, 1, b.length()).toLowerCase().equals(b)) {
						flog(str.TextStyle.newStr().style(str.TextStyle.forRGB(230, 230, 40)).text(line));
						break A;
					}
				}
				fn.print(line);
			}
		});
		setFLog((line) -> {fn.print(line);});
	}
	
	// This class is not meant for instantiation
	private fn() {}
	
	
	/* ------------------------------------------------ Basic IO operations ------------------------------------------------ */
	public static class ProcessConsoleIO {
		private ProcessConsoleIO() {}
		
		/* Considering 3 different consoles: the Unix shell, the Windows console and console of the Eclipse on Windows;
		 *   -> Unix shell does not misbehave in printing Unicode text in Java, but the others require using the C-way (through a native JNI lib)
		 *   -> All of them suffer in doing the same in C, but we need the C-way only for Windows and Eclipse Windows consoles.
		 *   -> Unix shell and the Windows Eclipse console needs setlocale(LC_CTYPE, "")...
		 *   -> ...however, Windows console needs the not-so-nice-looking _setmode(_fileno(stdout), _O_U16TEXT), which ruins the others!
		 *   -> If the code prints a unicode text on Windows console without problem, it can't do that in the same way onto Windows CMD.
		 *
		 * 04-08-2024: Consoles have a codepage and processes like Java can access to them and encode their strings
		 * to print accordingly. If it is 65001 then it means the console will be using UTF-8, and therefore processes
		 * (at least Java) see that and their print functions accordingly encode the variables.
		 */
		
		
		private static boolean console65001Done = false;
		private static boolean console65001Tried = false;
		
		
		/** Writes the UTF-8 encoding result of the text without any extra newline. */
		public static void print(java.io.PrintStream into, String text) {
			if (!console65001Tried) {
				// FIXME: --- how to determine the OS???
				changeCHCPTo65001(false, true);
			}
			if (console65001Done) {
				// We're changing the CHCP after the Java process started; PrintStream.write will still use the
				// initial one --- so we can't use that and have to directly write it as bytes instead.
				try {into.write(str.utf8(text));}
				catch (IOException e) {throw new InternalError(e);}
			} else {
				into.print(text);
			}
		}
		
		
		/** Writes the UTF-8 encoding result of a newline, regarding the OS' line separator. */
		public static void println(java.io.PrintStream into) {
			print(into, str.platformLineSeparator());
		}
		
		
		
		public static void changeCHCPTo65001(boolean forUnix, boolean forWin) {
			console65001Tried = true;
			try {
				if (forWin) WindowsChangeCHCPTo65001();
				console65001Done = true;
			} catch (Error e) {
				System.err.println("Warning: Couldn't set the console codepage: "+e.getMessage());
				System.err.println(str.getStackTraceText(e));
			}
		}
		
		public static void WindowsChangeCHCPTo65001() {
			windowsSetCHCP(65001);
		}
		
		public static int windowsGetCHCP() {
			var outs = execReturnRedirectInput(new String[] {"cmd.exe", "/c", "chcp"});
			return getValueFromWindowsCHCPCommand(outs);
		}
		
		/** Changes the codepage/encoding of the console host process that provides this process' CLI on Windows
		 * systems. Works by spawning a new CMD process that runs chcp something, but has its input redirected to
		 * the parent process therefore its codepage changes too.
		 * <p>Warning: On IDEs (such as IntelliJ IDEA), does not seem to change the codepage; but they are already
		 * intelligent enough to properly use UTF-8 anyway.
		 * <p>Returns normally if the CHCP command succeeds. */
		public static void windowsSetCHCP(int to) throws RuntimeException {
			var outs = execReturnRedirectInput(new String[] {"cmd.exe", "/c", "chcp", ""+to});
			int value = getValueFromWindowsCHCPCommand(outs);
			if (value != to) throw new RuntimeException("CHCP didn'tf set the codepage to the specified value");
		}
		
		private static fn.Tuple3<Integer, String, String> execReturnRedirectInput(String[] args) {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
			final Process p;
			final int retVal;
			try {
				p = pb.start();
				retVal = p.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
			
			final byte[] output, error;
			try {
				output = p.getInputStream().readAllBytes();
				error = p.getErrorStream().readAllBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return fn.tuple(retVal, str.utf8(output), str.utf8(error));
		}
		
		
		private static int getValueFromWindowsCHCPCommand(fn.Tuple3<Integer, String, String> outputs) {
			String outputText = outputs.val2;
			String errorText = outputs.val3;
			
			Pattern pat = Pattern.compile("(?:\\r?\\n)*Active code page: ([0-9]+)(?:\\r?\\n)*");
			Matcher m = pat.matcher(outputText);
			int val;
			A:	{
				if (outputs.val1 != 0) {
					throw new RuntimeException("CHCP didn\'t exit with 0 and returned: "+outputText+errorText);
				}
				if (m.matches()) {
					try {
						val = str.decimalToInt(m.group(1));
						break A;
					} catch (NumberFormatException e) {}
				}
				throw new RuntimeException("CHCP returned an unexpected value: "+outputText);
			}
			return val;
		}
	}
	
	@SuppressWarnings("resource") // Because I of course won't close the InputStream System.in (by closing any buffered wrapper of it).
	public static String getLine() {
		if (inputGetter == null) inputGetter = cachedReference(
			() -> new java.io.BufferedReader(new java.io.InputStreamReader(System.in))
		);
		try {return inputGetter.get().readLine();}
	//	catch (java.io.IOException e) {e.printStackTrace(); return null;}
		catch (java.io.IOException e) {throw new RuntimeException(e);}
	}
	
	private static Supplier<java.io.BufferedReader> inputGetter = null;
	
	public static java.io.PrintStream outStream() {return outputStream.get();}
	public static void outStream(Supplier<java.io.PrintStream> set) {outputStream = set;}
	private static Supplier<java.io.PrintStream> outputStream = () -> System.out; // Set after the static initializer above executes
	
	// TODO: Maybe one day, rename “print”s into “println”s?
	public static void print(Object obj) {print(obj, true);}
	public static void print(Object obj, boolean newline) {
		if (obj instanceof str.TextStyle.FormattedText) {
			str.TextStyle.FormattedText text = (str.TextStyle.FormattedText) obj;
			if (newline) text.printlnTo(outStream());
			else text.printTo(outStream());
		}
		else print(outStream(), str.toString(obj), newline);
	}
	
	private static boolean warnedCROnce = false;
	
	private static void print(PrintStream into, String strn, boolean newline) {
		if (fn.isAssertOn()) {
			if (!warnedCROnce) {
				java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\r(?!\\n)");
				java.util.regex.Matcher m = p.matcher(strn);
				if (m.find())
					fn.log("[Warning] A string passed to fn.print contains a CR character. "
					     + "Could you have splitted sth. with \u201c\\\\n\u201d instead "
					     + "of \u201c(\\\\r)?\\n\u201d?"
					);
			}
		}
		if (newline)
			ProcessConsoleIO.print(into, strn + str.platformLineSeparator());
		else
			ProcessConsoleIO.print(into, strn);
		into.flush();
	}
	
	public static void printe(Object obj) {
		print(System.err, str.toString(obj), true);
	}
	public static void printe(Object obj, String end) {
		print(System.err, str.toString(obj) + end, false);
	}
	public static void print(Object obj, String end) {print(str.toString(obj) + end, false);}
	public static void print() {print("");}
	public static void print(Object obj, int itr) {
		if (itr >= 0) for (@SuppressWarnings("unused") int i: range(itr)) print(obj);
	}
	public static void print(Object obj, int itr, String delimiter) {
		for (@SuppressWarnings("unused") int i: range(itr - 1)) {
			print(obj, false);
			print(delimiter, false);
		}
		print(obj, true);
	}
	/* ------------------------------------------------ ------------------- ------------------------------------------------ */
	
	
	
	// Almost equals
	// MAYBE: Fix it so that it compares also with the respect of magnitudes:
	// i.e. 1 over trillion and twice of that might not be quite equal even though
	// they are much closer to 0 than 1 over just a billion.
	public static boolean equals(double a, double b) {
		return equals(a, b, 0x1p-30); // Differ less than by 1 in ~billion
	}
	public static boolean equals(double a, double b, double by) {
		return Math.abs(a - b) < by;
	}
	
	// An array .equals(another with the exact same list of objects/values) is false!!
	
	public static boolean equals(byte[] a, byte[] b) {
		return java.util.Arrays.equals(a, b);
	}
	public static boolean equals(int[] a, int[] b) {
		return java.util.Arrays.equals(a, b);
	}
	public static boolean equals(boolean[] a, boolean[] b) {
		return java.util.Arrays.equals(a, b);
	}
	public static boolean equals(long[] a, long[] b) {
		return java.util.Arrays.equals(a, b);
	}
	public static <E> boolean equals(E[] a, E[] b) {
		return java.util.Arrays.equals(a, b);
	}
	// I may need this method not to rely on the IDE's warning on
	// comparing unrelated objects.
	public static <Type> boolean equals(Type a, Type b) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		if (b == null) return false; // Selfnote: If a.equals(null) is true (for non-null a) then this method still returns false!
		return a.equals(b);
	}
	
	
	
	/** Logs a message to somewhere that is not necessarily always STDOUT or STDERR (although to STDOUT
	 *  by default) and can be redirected to write the messages it is called with into somewhere else (i.e.
	 *  the STDOUT will be used by another program and is not expected to include additional messages) */
	public static void log(String line) {if (logText != null) logText.accept(line);}
	public static void log(Object obj) {log(""+obj);}
	public static void flog(str.TextStyle.FormattedText line) {if (logFText != null) logFText.accept(line);}
	private static Consumer<str.TextStyle.FormattedText> logFText;
	private static Consumer<String> logText;
	/** Updates the callback function for void fn.log(String) */
	public static void setLog(Consumer<String> callback) {
		logText = callback;
	}
	/** Updates the formatted text taking callback function for void fn.log(str.TextStyle.FormattedText) */
	public static void setFLog(Consumer<str.TextStyle.FormattedText> callback) {logFText = callback;}
	
	
	
	
	
	
	// TODO: Create a class named “Math” for those!
	/* ------------------------------------------------ Math operations ------------------------------------------------ */
	public static int remainder(int num, int by) {return num % by;}
	/** Euclidian modulo. Not remainder. */
	public static int modulo(int num, int by) {
		if (by == 0) throw new ArithmeticException("/ by 0");
		if (by == -1) return 0; // This test is needed to prevent undefined behavior of "INT_MIN % -1"
		int m = num % by;
		if (m < 0) {
			// m += (b < 0) ? -b : b; // avoid this form: it is undefined behavior when b == INT_MIN
			m = (by < 0) ? (m - by) : (m + by);
		}
		return m;
	}
	/** Euclidian modulo. Not remainder. */
	public static double modulo(double num, int by) {
		if (by == 0) throw new ArithmeticException("/ by 0");
		if (by == -1) return 0; // This test is needed to prevent undefined behavior of "INT_MIN % -1"
		double m = num % by;
		if (m < 0) {
			// m += (b < 0) ? -b : b; // avoid this form: it is undefined behavior when b == INT_MIN
			m = (by < 0) ? (m - by) : (m + by);
		}
		return m;
	}
	/** Euclidian modulo. Not remainder. */
	public static double modulo(double num, double by) {
		if (by == 0) throw new ArithmeticException("/ by 0");
//		if (b == -1) return 0; // This test is needed to prevent undefined behavior of "INT_MIN % -1"
		double m = num % by;
		if (m < 0) {
			// m += (b < 0) ? -b : b; // avoid this form: it is undefined behavior when b == INT_MIN
			m = (by < 0) ? (m - by) : (m + by);
		}
		return m;
	}
	
	/** Rounds a fp down (not towards 0 but absolutely downwards regardless of its sign and regardless of the
	 *  behavior of typecasting a negative fp into an int (in terms of rounding absolutely down or closer to 0).
	 *  <p> • 3.001 –> 3
	 *  <p> • 3.000 –> 3
	 *  <p> • 2.999 –> 2
	 *  <p> • -2.999 –> -3
	 *  <p> • -3.000 –> -3
	 *  <p> • -3.001 –> -4
	 *  */
	public static int floor(double n) {
		final boolean simpleOneLiner = false;
		
		final int result;
		if (n >= 0) {
			result = (int) n;
		} else {
			if (simpleOneLiner) {
				return ((int) (n + (1 + ((int) (-n))))) - (1 + ((int) (-n)));
			} else {
				// Shift the negative number to make it positive, apply int downcast and then
				// shift it back by the same amount.
				
				// Don't rely on negative numbers' integer downcast behavior (use positive instead)
				final int nDistance0ABitSmallerEq = (int) (-n); // TODO: Or call roundDown again
				fn.assertAnyway(nDistance0ABitSmallerEq <= Math.abs(n));
				final int arbitraryAtLeastOne = 100;
				fn.assertAnyway(arbitraryAtLeastOne >= 1);
				final int nDistance0Larger = nDistance0ABitSmallerEq + arbitraryAtLeastOne;
				fn.assertAnyway(nDistance0Larger > Math.abs(n));
				fn.assertAnyway(Math.abs(n) >= nDistance0ABitSmallerEq);
				final double shifted = n + nDistance0Larger;
				fn.assertAnyway(shifted >= 0);
				final int roundedDown = (int) shifted; // TODO: Or call roundDown again
				final int roundedDownShiftedBack = roundedDown - nDistance0Larger;
				result = roundedDownShiftedBack;
			}
		}
		fn.assertAnyway(result <= n);
		return result;
	}
	
	
	private static boolean isGreaterThan(int a, int than, boolean atLeast) {
		if (atLeast) return a >= than;
		else return a > than;
	}
	public static boolean isMoreThanHalf(int num, int over, boolean atLeast) {
		if (num < 0) throw new IllegalArgumentException();
		if (over <= 0) throw new IllegalArgumentException();
		if (over % 2 == 0) return isGreaterThan(num, (over/2), atLeast);
		else return isGreaterThan(num, (over/2), false);
	}
	
	public static long gcd(long a, long b) {
		if (a == 0 && b == 0) throw new IllegalArgumentException();
		if (a < 0) throw new IllegalArgumentException();
		if (b < 0) throw new IllegalArgumentException();
		while (a != 0 && b != 0) {
			if (a > b) a = a % b;
			else b = b % a;
		}
		return Math.max(a, b);
	}
	
	public static boolean areCoprimes(long a, long b) {
		return gcd(a, b) == 1;
	}
	
	public static class Primes {
		
		/** From <a href="https://stackoverflow.com/a/62150343">this</a> page
		 * (except the Dynamic Programming Cache part) */
		public static boolean isPrime(PrimeCache cache, long n) {
			final long veryLarge = pow(10, 10);
			final boolean isPrime;
		A:	{
				if (n <= 1) {isPrime = false; break A;}
				if (n <= 3) {isPrime = true; break A;}
				if (n % 2 == 0) {isPrime = false; break A;}
				if (n % 3 == 0) {isPrime = false; break A;}
				if (false) { // TRICKY/HACKY SOLUTION!!!
					if (n > veryLarge) {
						if (isDefinitelyNotPrime_fermat(n)) {isPrime = false; break A;}
//						else {isPrime = true; break A;} // Definitely??????
					}
				}
				/*if (cache != null) for (long somePrime: cache.nearestUpIncl.values()) {
					if (n % somePrime == 0) {
						isPrime = false;
						break A;
					}
				}*/
				for (long i = 5; i*i <= n; i += 6) {
					if (n % i == 0) {
						isPrime = false;
						break A;
					}
					if (n % (i + 2) == 0) {
						isPrime = false;
						break A;
					}
				}
				isPrime = true;
			}
			
			/*if (isPrime) if (cache != null) cache.nearestUpIncl.put(n, n);*/
			return isPrime;
		}
		
		@Deprecated private static boolean isDefinitelyNotPrime_fermat(long n) {
			for (int a = 2; a <= 31; a++) {
				boolean coprimeAN = gcd(a, n) == 1;
				if (!coprimeAN) continue;
				boolean maybe_prime = powMod(a, n-1, n) == 1;
				if (maybe_prime) return false;
			}
			return true;
		}
		
		public static class PrimeCache {
			// ...
		}
		
	}
	
	// TODO: Tested only a little!
	/** Returns (base^exponent) % modulus in much shorter way than actually multiplying and returning them
	 *  <p>From <a href="https://www.geeksforgeeks.org/modular-exponentiation-power-in-modular-arithmetic">this</a> page.
	 */
	public static long powMod(long base, long exponent, long modulus) {
		if (base < 0) throw new IllegalArgumentException();
		if (modulus < 0) throw new IllegalArgumentException();
		if (modulus == 0) throw new ArithmeticException("/ by 0");
		if (exponent <= 0) throw new IllegalArgumentException();
		long res = 1; // Initialize result
		
		base = base % modulus; // Update base if it is more than or equal to mod
		
		if (base == 0) return 0; // In case base is divisible by mod;
			
		while (exponent > 0) {
			// If exp is odd, multiply base with result
			if ((exponent & 1) != 0) res = (res * base) % modulus;
			
			// exp must be even now
			exponent = exponent >> 1; // exp = exp/2
			base = (base * base) % modulus;
		}
		return res;
	}
	
	
	
	
	
	// TODO: Support division and remainder/modulus by negative number (Update methods to handle that: 2 (int, int),
	//  2 (double, int))
	// TODO: Support division and remainder/modulus by float (Add methods: 2 (double, double))
	public static class Division {
		
		/** If true then remainder, otherwise the modulus is applied in default methods. */
		public static final boolean config_remaindersMayBeNegative = false;
		
		public static class division_int_result {
			public final int quot, rem;
			public division_int_result(int quotient, int remainder) {
				quot = quotient;
				rem = remainder;
			}
			public String toString() {return quot +" ("+rem+")";}
		}
		public static class division_fp_result {
			public final int quot;
			public final double rem;
			public division_fp_result(int quotient, double remainder) {
				quot = quotient;
				rem = remainder;
			}
			public String toString() {return quot +" ("+rem+")";}
		}
		
		
		/** Divides an int by an int in a way that results an int quotient and an int remainder or modulo. */
		public static division_int_result div(int num, int by) {
			if (config_remaindersMayBeNegative)
				return div_remainder(num, by);
			else
				return div_modulo(num, by);
		}
		// The by param will be like 24, 60, 12 etc. and not a fp num unlike num that
		// may be (3 + 0.38) seconds, hence the parameter by will not need to be a fp.
		/** Divides an int by an int in a way that results an int quotient and a fp remainder or modulo. */
		public static division_fp_result div(double num, int by) {
			if (config_remaindersMayBeNegative)
				return div_remainder(num, by);
			else
				return div_modulo(num, by);
		}
		
		// Remainders may be negative
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 ->  0, -1 (not -1, 4)
		// -6, 5 -> -1, -1 (not -2, 4)
		/** Divides an int by an int in a way that results an int quotient and an int <b><i>remainder</i></b> and
		 *  that the remainder (or modulo) may be negative. */
		public static division_int_result div_remainder(int num, int by) {
			if (by <= 0) throw new IllegalArgumentException("÷ by <= 0");
			int div = num / by;
			int rem = num % by;
			fn.assertAnyway(div*by + rem == num, div+"*"+by+" + "+rem+" != "+num);
			return new division_int_result(div, rem);
		}
		// Remainders may be negative
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 ->  0, -1 (not -1, 4)
		// -6, 5 -> -1, -1 (not -2, 4)
		/** Divides a fp by an int in a way that results an int quotient and a fp <b><i>remainder</i></b> and that
		 *  the remainder (or modulo) may be negative. */
		public static division_fp_result div_remainder(double num, int by) {
			if (by <= 0) throw new IllegalArgumentException("÷ by <= 0");
			int div = ((int) num) / by;
			double rem = num % by;
			
			fn.assertAnyway(Math.abs((div*by + rem) - num) < 0x1p-30 , div+"*"+by+" + "+rem+" != "+num);
			return new division_fp_result(div, rem);
		}
		
		
		// Remainders can't be negative
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 -> -1, 4 (not 0, -1)
		// -6, 5 -> -2, 4 (not -1, -1)
		/** Divides an int by an int in a way that results an int quotient and an int <b><i>modulo</i></b> and that
		 *  the modulo (or remainder) can't be negative. */
		public static division_int_result div_modulo(int num, int by) {
			if (by <= 0) throw new IllegalArgumentException("÷ by <= 0");
			int div;
			if (num < 0) {
				div = ((num+1) / by);
				div += -1;
			}
			else {
				div = num / by;
			}
			int rem = fn.modulo(num, by);
			
			
			fn.assertAnyway(div*by + rem == num, div+"*"+by+" + "+rem+" != "+num);
			fn.assertAnyway(rem >= 0, "rem < 0 in modulus with by > 0");
			return new division_int_result(div, rem);
		}
		
		
		// Remainders can't be negative
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 -> -1, 4 (not 0, -1)
		// -6, 5 -> -2, 4 (not -1, -1)
		/** Divides a fp by an int in a way that results an int quotient and a fp <b><i>modulo</i></b> and that the
		 *  modulo (or remainder) can't be negative. */
		public static division_fp_result div_modulo(double num, int by) {
			if (by <= 0) throw new IllegalArgumentException("÷ by <= 0");
			int div;
			if (num < 0) {
				div = (floor(num+1)) / by;
				div += -1;
			}
			else {
				div = ((int) num) / by;
			}
			double rem = fn.modulo(num, by);
			
			fn.assertAnyway(Math.abs((div*by + rem) - num) < 0x1p-30 , div+"*"+by+" + "+rem+" != "+num);
			fn.assertAnyway(rem >= 0, "rem < 0 in modulus with by > 0");
			return new division_fp_result(div, rem);
		}
		
		private static void test() {
			fn.print(div_modulo(239, 24)); // 9 (23)
			fn.print(div_modulo(240, 24)); // 10 (0)
			fn.print(div_modulo(241, 24)); // 10 (1)
			fn.print(div_modulo(-239, 24)); // -10 (1)
			fn.print(div_modulo(-240, 24)); // -10 (0)
			fn.print(div_modulo(-241, 24)); // -11 (23)
			fn.print();
			fn.print(div_modulo(239.0, 24)); // 9 (23)
			fn.print(div_modulo(240.0, 24)); // 10 (0)
			fn.print(div_modulo(241.0, 24)); // 10 (1)
			fn.print(div_modulo(-239.0, 24)); // -10 (1)
			fn.print(div_modulo(-239.5, 24)); // -10 (0.5)
			fn.print(div_modulo(-240.0, 24)); // -10 (0)
			fn.print(div_modulo(-240.5, 24)); // -11 (23.5)
			fn.print(div_modulo(-240.2, 24)); // -11 (23.8)
			fn.print(div_modulo(-240.05, 24)); // -11 (23.95)
			fn.print(div_modulo(-240.005, 24)); // -11 (23.995)
			fn.print(div_modulo(-240.001, 24)); // -11 (23.999)
			fn.print(div_modulo(-240.5, 24)); // -11 (23.5)
			fn.print(div_modulo(-241.0, 24)); // -11 (23)
		}
		
	}
	
	
	/** You pass a few values alongside with their capacities and this method overflows/underflows whichever
	 *  exceeds its capacity or starves below 0 to the one larger (such as an amount of hours, minutes and seconds).
	 *  Note: Also handles if one value is so large that it exceeds the larger subsequent value
	 *  even after overflowing/underflowing to/from it; such as 6000 seconds is 100 minutes and finally is 1 hour
	 *  and 40 minutes (overflows seconds into hours too).
	 *
	 *  @param valueAndMaxPairs_littleEndian_inout Values overflow/underflow into/from the subsequents (itr.next()).
	 *  The wrapper/pointers are mutated to reflect the changes.
	 *
	 *  @return The overflow/underflow from the largest unit. */
	public static int overUnderflowValues(
		Iterable<fn.Tuple2<fn.Union2<fn.Pointer<Integer>, fn.Pointer<Double>>, Integer>> valueAndMaxPairs_littleEndian_inout
	) {
		int carry = 0;
		for (var currentUnit: valueAndMaxPairs_littleEndian_inout) {
			final var current = currentUnit.val1;
			final int max = currentUnit.val2;
			if (current.getType() == 1) { // Int
				var res = fn.Division.div(current.get1().value + carry, max);
				carry = res.quot;
				current.get1().value = res.rem;
			} else if (current.getType() == 2) { // Float
				var res = fn.Division.div(current.get2().value + carry, max);
				carry = res.quot;
				current.get2().value = res.rem;
			}
			else throw new Error();
			
		}
		
		return carry;
		
	}
	
	
	/* ------------------------------------------------ --------------- ------------------------------------------------ */
	
	
	
	
	
	
	public static class Random {
		/** Returns true with a chance by <strong><em>by/over</em></strong> */
		public static boolean randomBool(java.util.Random generator, int by, int over) {
			if (!(over > 0 && by >= 0 && by <= over)) throw new IllegalArgumentException();
			return random(generator, 1, over) <= by;
		}
		/** Returns a fp number in [0, 1) */
		public static double random(java.util.Random generator) {return generator.nextDouble();}
		public static int random(java.util.Random generator, int from, int to) {return from + generator.nextInt(to-from+1);}
		
		/** Returns true with a chance by <strong><em>by/over</em></strong> */
		public static boolean randomBool(int by, int over) {return randomBool(getExistingGenerator(), by, over);}
		/** Returns a fp number in [0, 1) */
		public static double random() {return random(getExistingGenerator());}
		public static int random(int from, int to) {return random(getExistingGenerator(), from, to);}
		
		private static java.util.Random getExistingGenerator() {return GeneratorHolder.generator.get();}
		
		// The purpose of holding it as a static member in a class is to avoid unnecessary runtime memory footprint growth.
		// I think, even an empty/null weak reference object spans at least just a few bytes.
		private static class GeneratorHolder {
			public static final Supplier<java.util.Random>
				generator = fn.cachedReference(() -> {return new java.util.Random();});
			// Initializes one with a seed number generated from system clock, exactly after the first random number call and the any first such call after the instance is cleared by GC.
		}
	}
	
	
	
	
	/* ------------------------------------------------ Thread/stack/throwable methods ------------------------------------------------ */
	private static class fnThread extends Thread {
//		// TODO: Find a better commandline property for that than “debugthreads” (“-Ddebugthreads=true”)!!!
//		public static final boolean debugCallStack = System.getProperty("debugthreads").equals("true");
		public fnThread() {super();}
		public fnThread(String name) {super(name);}
		public fnThread(Runnable action) {super(action);}
		public fnThread(Runnable action, String name) {super(action, name);}
		public void start() {
			super.start();
			if (false) // To detect right where the f*ck they deadlock on
				(new Thread(() -> {
					while (true) {
						try {sleep(1000);}
						catch (InterruptedException e) {throw new RuntimeException(e);}
						synchronized (System.out) {
							System.out.println("-------------");
							System.out.println(getName());
							for (StackTraceElement ste: this.getStackTrace())
								System.out.println(ste);
						}
					}
				})).start();
		}
	}
	// Maybe to handle creation of new threads, because the installed SecurityManagers can restrict instantiation of a Thread object.
	public static Thread newThread(Runnable action, boolean isDaemon) {fnThread th = new fn.fnThread(action); th.setDaemon(isDaemon); return th;}
	public static Thread newThread(Runnable action) {return new fnThread(action);}
	
	
	// Selfnote: There is Map<Thread, StackTraceElement[]> Thread.getAllStackTraces() so that you can see
	//           JVM threads like Signal Dispatcher, Common Cleaner, Reference Handler and Finalizer 💀 
	
	// TODO: Do not publicly open the methods returning or taking an internal-ish type (java.lang.StackTraceElement)!
	
	
	public static StackTraceElement[] getStackTrace(/*of this thread*/) {
		int numExcessStackTraces = 0; // TODO: Adjust this later
		StackTraceElement[]
			origTrace = (new Throwable()).getStackTrace(), // Creates some unnecessary frames that are in the methods of this class...
			trace = new StackTraceElement[origTrace.length - numExcessStackTraces];
		fn.copy(origTrace, numExcessStackTraces+1, trace); // ...so slice them out.
		return trace;
	}
	
	/** Note: This method does not need to even exist. */
	@Deprecated private static StackTraceElement[] getStackTrace(Throwable th) {return th.getStackTrace();}
	
	
	/** Note: This method does not need to even exist.
	  * @deprecated Getting another thread's stack trace does not quite much make sense. */
	@Deprecated public static StackTraceElement[] getStackTrace(Thread th) {return th.getStackTrace();}
	
	
	/*  When jEdit really often froze to death right when I dragged an item into one of its window in Windows,
	 *  leaving the sprite of the file icon that the cursor brought also frozen and Explorer unable to drag
	 *  something else until I terminated jEdit and not Explorer, I used this method to find out, when this
	 *  happened, that, while the other threads were waiting at methods named like park or Object.wait,
	 *  one was waiting exactly at the native method
	 *  sun.awt.windows.WToolkitThreadBlockedHandler.startSecondaryEventLoop() I then Google'd that stack
	 *  trace line and immediately found results about a java bug (affecting 8u281, 11.0.10, 15.0.2 and 16)
	 *  causing that drag and drop freezes the Java app 🥺 
	 *  I decided to use my old JRE/JDK (13) with that which didn't have that bug.
	 */
	public static void startTracingAllThreads(double perSecond, java.util.function.Consumer<String> printTo) {
		fn.newThread(() -> {
			while (true) {
				fn.sleep(perSecond);
				list<String> sink = new linklist<>();
				java.util.Map<Thread, StackTraceElement[]> ths = Thread.getAllStackTraces();
				sink.add("----------------------------------------------------------------");
				int qty = ths.size(), c = 0;
				for (Thread t: ths.keySet()) {
					c++;
					sink.add(""+t+":");
					sink.add(""+str.getStackTraceText(ths.get(t)));
					if (c < qty) sink.add("");
				}
				sink.add("----------------------------------------------------------------");
				printTo.accept(str.join(sink, "\n"));
			}
		}, true).start();
	}
	public static void startTracingAllThreads(double perSecond) {
		startTracingAllThreads(perSecond, (e) -> fn.log(e));
	}
	public static void startTracingThread(Thread theThread, double perSecond, java.util.function.Consumer<String> printTo) {
		fn.newThread(() -> {
			while (true) {
				fn.sleep(perSecond);
				list<String> sink = new linklist<>();
				StackTraceElement[] stack = theThread.getStackTrace();
				sink.add("----------------------------------------------------------------");
				sink.add(""+str.getStackTraceText(stack));
				sink.add("----------------------------------------------------------------");
				printTo.accept(str.join(sink, "\n"));
			}
		}, true).start();
	}
	public static void startTracingThread(double perSecond, java.util.function.Consumer<String> printTo) {
		startTracingThread(Thread.currentThread(), perSecond, printTo);
	}
	public static void startTracingThread(double perSecond) {
		startTracingThread(perSecond, (e) -> fn.log(e));
	}
	
	/* The purpose of the non-void return type is making them an expression; hence be
	 * able to be used in the ternary expressions to make it an error when necessary.
	 * Example: (condition ? a:(fn.throwObj(...)?b:c)) for b and c are suitable arbitrary values.
	 * If the condition evaluates false, the method is called to choose between b and c therefore
	 * a throwable is thrown instead of the expression evaluating to a 
	 * This can also be used directly as the other option of ternary by specifying the return type:
	 * (condition ? a : fn.<...>throwObj(...))
	 * Or leaving the inferring to the compiler: (condition ? a : fn.throwObj(...)). Compiler
	 * understands by inferring, even which kind of throwable will be thrown and the following
	 * example requires the caller method to surround with try-catch or declare thrown to handle
	 * the IOException, not Exception or Throwable: fn.throwObj(new IOException())
	 */
//	public static <E> E throwObj(Throwable th) throws Throwable {throw th;}
//	public static <E> E throwObj(Exception exc) throws Exception {throw exc;}
	public static <E, F extends Throwable> E throwObj(F th) throws F {throw th;}
//	public static <E> E throwObj(Error err) {throw err;}
//	public static <E> E throwObj(RuntimeException re) {throw re;}
//	public static <E> E throwErr() {return throwObj(new Error());}
	/* ------------------------------------------------ ------------------------------ ------------------------------------------------ */
	
	
	
	
	
	
	public static void sleep(double seconds) {sleep(seconds, null);}
	public static void sleep(double seconds, Runnable onInterrupted) {
		try {fnThread.sleep((long) (1000*seconds));}
		catch (InterruptedException e) {if (onInterrupted != null) onInterrupted.run();}
	}
//	@Deprecated public static void sleep(long milliseconds) {sleep(milliseconds, null);}
//	@Deprecated public static void sleep(long milliseconds, Runnable onInterrupted) {
//		try {Thread.sleep(milliseconds);}
//		catch (InterruptedException e) {if (onInterrupted != null) onInterrupted.run();}
//	}
	/* No need to specify a runnable to run only when the thread calling this
	 * (hence the wait method of that object) gets interrupted, because getting
	 * interrupted is to only way to exit an indefinite thread suspension (via
	 * void Object.wait()). Runnable can be placed just after the method call. */
	public static void sleep() {
		try {Object lock = new Object(); synchronized (lock) {lock.wait();}}
		catch (InterruptedException e) {}
	}
	
	
	
	/* ------------------------------------------------ Assert-related methods ------------------------------------------------ */
	/** Returns whether the VM parameter that enables the keyword 'assert' to throw {@link AssertionError} is present. */
//	public static boolean isAssertOn() {try {assert false; return false;} catch (AssertionError e) {return true;}}
	public static boolean isAssertOn() {
		boolean[] a = {false};
		assert mk1AndRet1(a); // No assertion error is thrown in any case but that line is executed if asserts are on.
		return a[0];
	}
	
	// Computed just once, ready to use many times
	public static final boolean isAssertOn = fn.isAssertOn();
	
	// I can't move this method into isAssertOn, because that'd be a local class (anon, not anon or
	// just lambda; doesn't matter) make JVM instantiate an object just to execute this method,
	// because local or non-static classes can't have static field, method or inner classes.
	private static boolean mk1AndRet1(boolean[] a) {a[0] = true; return true;}
	
	/** If used with <code>assert fn.act(() -> {&#47;*some code*&#47;});</code>, performs the action and returns <code>true</code> only if the VM parameter
	  * that enables the keyword 'assert' to throw {@link AssertionError} is present.
	  * <p>This method is to be used in conjunction with an assert statement. This way, the JVM (most probably) will opt the method call out of the code in
	  * the memory (in one of the stages of copying the bytecode in the memory, JIT compiling with light compiler into platform-specific machine code and
	  * recompiling with heavy one into optimized code) whenever the assertion enabling switch is not present. Checking if assertions are enabled each time
	  * by doing 'assert false' and catching the assertion error, instead of putting a boolean expression making the operation as a statement in front of
	  * 'assert' to let the JVM opt it out when assertions are disabled, adds a runtime cost.
	  * <p>Warning: Do not use this (or put such an expression in front of an assert directive) with an expression that changes the flow or the state of
	  * your program, because this creates a need of testing your code separately also for the case that assertions are not enabled. This is meant to be
	  * used for i.e. logging the assertion failure in a more meaningful way. */
	public static boolean act(Runnable action) {action.run(); return true;}
	
	/** Same thing that applies to <code>fn.act(Runnable)</code> applies to this one. Throws AssertionError if the inside is false.
	 *  */
	public static boolean act(Supplier<Boolean> action) {return action.get();}
	
	/** Performs an assertion regardless of whether “ea” is present as a VM argument for the current Java process */
	public static void assertAnyway(boolean expression) {if (!expression) throw new AssertionError();}
	/** Performs an assertion regardless of whether “ea” is present as a VM argument for the current Java process */
	public static void assertAnyway(Supplier<Boolean> expression) {if (!expression.get()) throw new AssertionError();}
	/** Performs an assertion regardless of whether “ea” is present as a VM argument for the current Java process */
	public static void assertAnyway(boolean expression, String message) {if (!expression) throw new AssertionError(message);}
	/** Performs an assertion regardless of whether “ea” is present as a VM argument for the current Java process */
	public static void assertAnyway(Supplier<Boolean> expression, String message) {if (!expression.get()) throw new AssertionError(message);}
	
	/** Performs a print operation only if the VM parameter that enables the keyword 'assert' to throw {@link AssertionError}. */
	@Deprecated public static void printIfAssertModeOn(String strn) {doIfAssertModeOn(() -> {fn.print(strn);});}
	
	/** Performs the operation only if the VM parameter that enables the keyword 'assert' to throw {@link AssertionError} is present. */
	@Deprecated public static void doIfAssertModeOn(Runnable action) {if (isAssertOn()) action.run();}
	
	/** Performs the operation only if the VM parameter that enables the keyword 'assert' to throw {@link AssertionError} is <strong>not</strong> present. */
	@Deprecated public static void doIfAssertModeOff(Runnable action) {if (!isAssertOn()) action.run();}
	
	/** Performs the operation only if the VM parameter that enables the keyword 'assert' to throw {@link AssertionError} is present. */
	@Deprecated public static <Type> void doIfAssertModeOn(Consumer<Type> consumer, Type input) {
		try {assert false;} catch (AssertionError e) {consumer.accept(input);}
	}
	/* ------------------------------------------------ ---------------------- ------------------------------------------------ */
	
	// “ABC” for “libABC.so” and “ABC.dll”
	public static boolean loadLibrary(String libname) {
		
		// Try to find from the directories in the OS environment variable “PATH”
		try {
			System.loadLibrary(libname);
			fn.log("(loaded a native library from some kind of a library path)");
			return true;
		}
		catch (SecurityException | UnsatisfiedLinkError | NullPointerException e) {
//			fn.log(e.getMessage());
//			fn.log(str.getStackTraceText(e));
		}
		
		// Try to find from the directory where the class file of this source file is in
		String path;
		
		try {
			Class<?> cls = fn.class;
			path = (new java.io.File(
				fn.class.getResource(cls.getSimpleName()+".class").toURI()
			)).getCanonicalFile().getParent();
		} catch (IllegalArgumentException | URISyntaxException | IOException e1) {path = "";}
		for (String arg: new String[] {path+"\\"+libname+".dll", path+"/"+"lib"+libname+".so"})
			try {
				System.load(arg);
				fn.log("(loaded a native library from the dir containing the class)");
				return true;
			}
			catch (IllegalArgumentException | SecurityException | UnsatisfiedLinkError | NullPointerException e) {
//				fn.log(e.getMessage());
//				fn.log(str.getStackTraceText(e));
			}
		return false;
	}
	
	
	
	/** Provides more functionality than the standard Java “synchronized” interface/sytax. */
	public static java.util.concurrent.locks.Lock createRntLock() {return new java.util.concurrent.locks.ReentrantLock();}
	
	
	
	/** <strong>Warning:</strong> Does not always determine the name of the class file in where the class exists.*/
	public static java.io.File getDirectory(Class<?> cls) {
		try {return (new java.io.File(cls.getResource(cls.getSimpleName()+".class").toURI())).getCanonicalFile().getParentFile();}
		catch (IOException | URISyntaxException e) {return null;}
	}
	
	
	
	/* ------------------------------------------------ Range operations ------------------------------------------------ */
	public static class range implements java.lang.Iterable<Integer> {
		private int start, end, increment;
		private void init(int... params) {
			int start, end;
			int increment;
			switch (params.length) {
				case 0: {
					init(1, 2, -1); // You can't reach 1 from 2 by -1; therefore it is equivalent to an empty range.
					break;
				}
				case 1: {
					end = params[0];
					     if (end >= +1) init( 1, end);
					else if (end <= -1) init(-1, end);
					else                init();
					break;
				}
				case 2: {
					start = params[0]; end = params[1];
					if (start <= end) init(start, end,  1);
				//	if (start  > end) init(start, end, -1);
					if (start  > end) init(start, end, +1); // Even if start is greater, keep the increment positive.
					break;
				}
				case 3: {
					start = params[0]; end = params[1]; increment = params[2];
					this.start     = start;
					this.end       = end;
					this.increment = increment;
				break;
				}
			}
		}
		public range() {init();}
		public range(int to) {init(to);}
		public range(int from, int to) {
//			if (to < from) fn.log("Warning: Constructed a fn.range object with a \u201cto\u201d parameter smaller than the \u201cfrom\u201d parameter without specifying also a \u201cby\u201d parameter: "+from+" --> "+to);
			init(from, to);
		}
		public range(int from, int to, int by) {init(from, to, by);}
		
		public int getStart() {return start;}
		public int getEnd() {return end;}
		public int getIncrement() {return increment;}
		
		public sequenceIterator iterator() {return new sequenceIterator(this);}
		public String toString() {return "Range object "+start+" -> "+end+" by "+increment+" ("+super.toString()+") ";}
		
		private static class sequenceIterator implements java.util.Iterator<Integer> {
			private range isIteratorOf;
			private int start, end, increment;
			private int next;
			public sequenceIterator(range instance) {
				isIteratorOf = instance;
				start        = instance.start;
				end          = instance.end;
				increment    = instance.increment;
				next         = start;
//				first        = true;
			}
//			private boolean first;
			
			public boolean hasNext() {
				if ((increment  > 0) && (start <= next) && (next <= end)) return true;
				if ((increment  < 0) && (start >= next) && (next >= end)) return true;
				if ((increment == 0) && (start == next) && (next == end)) return true;
				return false;
			}
		/*	
			// TODO: EITHER GO WITH C++-STYLE ITERATOR IMPLEMENTATION AND VERIFY THAT THIS DOES NOT CAUSE A PROBLEM OR GO WITH JAVA-STYLE IMPLEMENTATION AND REMOVE THIS!!
			private void step() {
				if (hasNext()) {
					next += increment;
					if (increment == 0) next++;
				}
				throw new Error("Error: Iterator of the range object from " + start + " to " + end + " by " + increment + ", whose next() is called, has finished.");
			}
			public Integer next() {
				if (first) first = false;
				else       step();
				return next;
			}
		*/	
			public Integer next() {
				int current = next;
				if (hasNext()) {
					next += increment;
					if (increment == 0) next++;
					return current;
				}
				throw new java.util.NoSuchElementException(
					"Iterator of the range object from "+start+" to "+end+" by "+increment+" whose next() is called has already finished."
				);
			}
			public String toString() {return (hasNext()?("Iterator just before "+next):"Finished iterator")+" of \u201c"+isIteratorOf.toString()+"\u201d"+" ("+super.toString()+") ";}
		}
	}
	public static range range(int to) {return new range(to);}
	public static range range(int from, int to) {return new range(from, to);}
	// TODO: Consider making range from big to small (2 params) still incrementing by positive 1 and hence just an empty range.
	public static range range(int from, int to, int by) {return new range(from, to, by);}
	
	/* ------------------------------------------------ ---------------- ------------------------------------------------ */
	
	
	
	
	
	
	public static double pow(double base, double exponent) {return java.lang.Math.pow(base, exponent);}
	/** Deals with integers, therefore does not have the risk of reducing the number because of the
	 *  floating point precision error like in the example that could be as following:
	 *  (int) Math.pow(7, 2) = 7<sup>2</sup> -> (int) ~48.9999999998475 -> 48 */
	public static long pow(long base, int exponent) {
		if (base == 0 && exponent == 0) // 0^0 = 0^(1-1) = 0^1 / 0^1 = 0/0
			throw new ArithmeticException("0^0 -- 0 divided by 0");
		if (base == 1) return 1;
		if (base == -1) return (exponent % 2 == 0)?(1):(-1);
		if (exponent < 0) throw new IllegalArgumentException(
			"An integer other than 1 and -1 raised to the power of a negative integer is not an integer."
		);
		long result = 1;
		if (exponent <= 3)
			for (int time = 1; time <= exponent; time++) // Not using my range impl. otherwise it'd slow down a bit
				result = result*base;
		else { // eg.: (4, 25) -> (4, 12)*(4, 12)*4
			long segment = pow(base, exponent/2);
			return segment*segment*((exponent % 2 == 0) ? 1 : base);
			// Multiplying by itself because calling itself as pow(segment, 2) instead might be a bit costly.
		}
		return result;
	}
	
	
	
	
	/* ------------------------------------------------ basic set operations ------------------------------------------------ */
	// TODO
//	public static <Key, Value> Map<Key, Value> union(Map<Key, Value> a, Map<Key, Value> b) {}
//	public static <Key, Value> Map<Key, Value> intersection(Map<Key, Value> a, Map<Key, Value> b) {}
//	public static <Key, Value> Map<Key, Value> subtract(Map<Key, Value> from, Map<Key, Value> thisMap) {}
//	public static <Key, Value> Map<Key, Value> doSetOperations(
//		Map<Key, Value> a, Map<Key, Value> b,
//		Map<Key, Value> out_union,
//		Map<Key, Value> out_intersection,
//		Map<Key, Value> out_AMinusB
//	) {}
	
	public static <Key, Value> Value getOrCreate(java.util.Map<Key, Value> from, Key key, Supplier<Value> putIfAbsent) {
		return getOrCreate(from, key, putIfAbsent, null);
	}
	
	// TODO: Maybe change that to take Function<Value, Value> instead of Function<Key, Value> so that it can
	//  update the existing value if the parameter is not null.
	public static <Key, Value> Value getOrCreate(java.util.Map<Key, Value> from, Key key, Supplier<Value> putIfAbsent, Function<Value, Value> updateIfExist) {
		boolean absent = !from.containsKey(key);
		Value v;
		if (absent) {
			v = putIfAbsent.get();
			Object old = from.put(key, v);
			fn.assertAnyway(old == null);
		} else { // Exists
			v = from.get(key);
			if (updateIfExist != null) { // Update existing
				Value oldV1 = v;
				v = updateIfExist.apply(v);
				Value oldV2 = from.put(key, v);
				fn.assertAnyway(oldV1 == oldV2);
			}
		}
		return v;
	}
	
	public static <E> void shuffle(list<E> arrayList) {
		for (int i: fn.range(arrayList.size())) {
			// Swap every ith element with the god-knows-which'th element!
			E a = arrayList.g(i);
			E b = arrayList.g(i);
			int random = fn.Random.random(1, arrayList.size());
			arrayList.s(i, b);
			arrayList.s(random, a);
		}
	}
	
	
	
	
	// TODO: MAYBE HAVE A MASK ON WHICH ONES WILL BE SELECTED AND WHICH WILL BE NOT, AND AT EACH VALUE, DETERMINE IT
	//  THROUGH RANDOMBOOL (target_left/items_left)
	public static <E> list<E> pickRandomNElements(list<E> from, int qty) {
		// TODO: Maybe try to make use of PriorityQueues
		// We can't just select&pop a random element from a arraylist or linkedlist as each selection will cost us O(n + log(n)) which is just O(n)!
		list<E> out = list.listWrapper.getArList();
		java.util.Set<Integer> indexes = new java.util.HashSet<>();
		list<Integer> indexes_sorted = new linklist<>();
		final int len = from.size();
		
		if (qty > len) throw new IllegalArgumentException();
		int debug_itrC = 0;
		if (qty < from.size()/2) { // Select none then select some
			while (indexes.size() < qty) {
				debug_itrC++;
				int rnNum = fn.Random.random(1, len);
				boolean newAdded = indexes.add(fn.Random.random(1, len));
				if (newAdded) indexes_sorted.add(rnNum);
			}
			
		// TODO: The below line probably just does not cause any performance improvement!!!!
		} else { // Select all indexes then deselect just some
			java.util.Set<Integer> indexesNotToAdd = new java.util.HashSet<>();
			while (indexes.size() < len-qty) {
				indexesNotToAdd.add(fn.Random.random(1, len));
			}
			while (indexes.size() < qty) {
				debug_itrC++;
				int rn = fn.Random.random(1, len);
				if (indexesNotToAdd.contains(rn)) {}
				else {
					indexes.add(rn);
					indexes_sorted.add(rn);
				}
			}
		}
		
		if (indexes.size() != qty) throw new AssertionError();
		if (indexes_sorted.size() != qty) throw new AssertionError();
		
		for (int ind: indexes_sorted) out.add(from.g(ind));
		
		if (out.size() != qty) throw new AssertionError("out.size() != qty: "+out.size()+"/"+qty);
		return out;
	}
	
	/* ------------------------------------------------ -------------------- ------------------------------------------------ */
	
	
	/* ------------------------------------------------ Just some immutable tuple types with variable types ------------------------------------------------ */
	private static class Tuples {
		@SafeVarargs
		public static <E> String toString(E... values) {
			String output = "";
			output = output+"(";
			int c = 0;
			for (E value: values) {
				c++;
				output = output+str.toString(value);
				if (c <= values.length - 1) output = output += ", ";
			}
			output = output+")";
			return output;
		}
		@SafeVarargs
		public static <E> int hashCode(E... objects) {
			int c = 0;
			for (E obj: objects) c = 31*c + obj.hashCode();
			return c;
		}
		public static boolean equals(Object o1, Object o2) {
			if (o1 == o2) return true;
			if ((o1 == null) && (o2 == null)) return true;
			if (o1 == null) throw new AssertionError(); // To convince Eclipse from complaining that it maybe null w.o. changing coding assist config
			return o1.equals(o2);
		}
	}
	
	public static class Tuple2<T1, T2> {
		public final T1 val1;
		public final T2 val2;
		public Tuple2(T1 item1, T2 item2) {
			val1 = item1;
			val2 = item2;
		}
		public int hashCode() {return Tuples.hashCode(val1, val2);}
		public boolean equals(Tuple2<?, ?> that) {
			if (!Tuples.equals(this.val1, that.val1)) return false;
			if (!Tuples.equals(this.val2, that.val2)) return false;
			return true;
		}
		public boolean equals(Object that) {
			return (that instanceof Tuple2) && equals((Tuple2<?, ?>) that);
		}
		public Tuple2<T1, T2> clone() {return new Tuple2<>(val1, val2);}
		public String toString() {return Tuples.toString(val1, val2);}
	}
	
	public static class Tuple3<T1, T2, T3> {
		public final T1 val1;
		public final T2 val2;
		public final T3 val3;
		public Tuple3(T1 item1, T2 item2, T3 item3) {
			val1 = item1;
			val2 = item2;
			val3 = item3;
		}
		public int hashCode() {return Tuples.hashCode(val1, val2, val3);}
		public boolean equals(Tuple3<?, ?, ?> that) {
			if (!Tuples.equals(this.val1, that.val1)) return false;
			if (!Tuples.equals(this.val2, that.val2)) return false;
			if (!Tuples.equals(this.val3, that.val3)) return false;
			return true;
		}
		public boolean equals(Object that) {
			return (that instanceof Tuple3) && equals((Tuple3<?, ?, ?>) that);
		}
		public Tuple3<T1, T2, T3> clone() {return new Tuple3<>(val1, val2, val3);}
		public String toString() {return Tuples.toString(val1, val2, val3);}
	}
	public static class Tuple4<T1, T2, T3, T4> {
		public final T1 val1;
		public final T2 val2;
		public final T3 val3;
		public final T4 val4;
		public Tuple4(T1 item1, T2 item2, T3 item3, T4 item4) {
			val1 = item1;
			val2 = item2;
			val3 = item3;
			val4 = item4;
		}
		public int hashCode() {return Tuples.hashCode(val1, val2, val3, val4);}
		public boolean equals(Tuple4<?, ?, ?, ?> that) {
			if (!Tuples.equals(this.val1, that.val1)) return false;
			if (!Tuples.equals(this.val2, that.val2)) return false;
			if (!Tuples.equals(this.val3, that.val3)) return false;
			if (!Tuples.equals(this.val4, that.val4)) return false;
			return true;
		}
		public boolean equals(Object that) {
			return (that instanceof Tuple4) && equals((Tuple4<?, ?, ?, ?>) that);
		}
		public Tuple4<T1, T2, T3, T4> clone() {return new Tuple4<>(val1, val2, val3, val4);}
		public String toString() {return Tuples.toString(val1, val2, val3, val4);}
	}
	public static class Tuple5<T1, T2, T3, T4, T5> {
		public final T1 val1;
		public final T2 val2;
		public final T3 val3;
		public final T4 val4;
		public final T5 val5;
		public Tuple5(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
			val1 = item1;
			val2 = item2;
			val3 = item3;
			val4 = item4;
			val5 = item5;
		}
		public int hashCode() {return Tuples.hashCode(val1, val2, val3, val4, val5);}
		public boolean equals(Tuple5<?, ?, ?, ?, ?> that) {
			if (!Tuples.equals(this.val1, that.val1)) return false;
			if (!Tuples.equals(this.val2, that.val2)) return false;
			if (!Tuples.equals(this.val3, that.val3)) return false;
			if (!Tuples.equals(this.val4, that.val4)) return false;
			if (!Tuples.equals(this.val5, that.val5)) return false;
			return true;
		}
		public boolean equals(Object that) {
			return (that instanceof Tuple5) && equals((Tuple5<?, ?, ?, ?, ?>) that);
		}
		public Tuple5<T1, T2, T3, T4, T5> clone() {return new Tuple5<>(val1, val2, val3, val4, val5);}
		public String toString() {return Tuples.toString(val1, val2, val3, val4, val5);}
	}
	
	// Handy shortcuts to avoid typing like “new fn.Tuple3<>()”
	public static <T1, T2> Tuple2<T1, T2> tuple(T1 item1, T2 item2) {
		return new Tuple2<>(item1, item2);
	}
	public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(T1 item1, T2 item2, T3 item3) {
		return new Tuple3<>(item1, item2, item3);
	}
	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> tuple(T1 item1, T2 item2, T3 item3, T4 item4) {
		return new Tuple4<>(item1, item2, item3, item4);
	}
	public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>
	tuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
		return new Tuple5<>(item1, item2, item3, item4, item5);
	}
	
//	public static <T1, T2> Tuple2<T1, T2> tup(T1 item1, T2 item2) {
//		return tuple(item1, item2);
//	}
//	public static <T1, T2, T3> Tuple3<T1, T2, T3> tup(T1 item1, T2 item2, T3 item3) {
//		return tuple(item1, item2, item3);
//	}
//	public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> tup(T1 item1, T2 item2, T3 item3, T4 item4) {
//		return tuple(item1, item2, item3, item4);
//	}
//	public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5>
//	tup(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
//		return tuple(item1, item2, item3, item4, item5);
//	}
	/* ---------------------------------------- -------------------------------------------------------------------------- ---------------------------------------- */
	
	
	
	
	
	/* ------------------------------ Immutable orderless group types (set with known size) with repeatable elements of variable type ------------------------------ */
	private static class Groups {
		@SafeVarargs
		public static <E> String toString(E... values) {
			String output = "";
			output = output+"(";
			int c = 0;
			list<E> values_sorted = list.listWrapper.getArList();
			for (E val: values) values_sorted.add(val);
			values_sorted.sort((a, b) -> a.toString().compareTo(b.toString()));
			for (E value: values_sorted) {
				c++;
				output = output+str.toString(value);
				if (c <= values_sorted.size() - 1) output = output += ", ";
			}
			output = output+")";
			return output;
		}
		
		@SafeVarargs
		public static <E> int hashCode(E... objects) {
			list<Integer> hcodes = list.listWrapper.getArList();
			for (E obj: objects) hcodes.add(obj.hashCode());
			hcodes.sort((a, b) -> a.compareTo(b)); // Sort the list of hashcodes
			int c = 0;
			for (int hcode: hcodes) c = 31*c + hcode;
			return c;
		}
		
		// FIXME: Split this method into 2 versions: Ono copying into new set and other returning a view AS SET!
		@SafeVarargs
		public static <E> java.util.Set<E> toSet(E... elms) {
			java.util.Set<E> set = new java.util.HashSet<>();
			for (E elm: elms) set.add(elm);
			return set; // Warning: May return fewer elements than expected since all elements must be unique in sets
		}
		@SafeVarargs
		public static <E> java.util.Iterator<E> iterator(E... elms) {
			return toSet(elms).iterator(); // Warning: May return fewer elements than expected since all elements must be unique in sets
		}
		
		public static boolean equals(Object o1, Object o2) {
			if (o1 == o2) return true;
			if ((o1 == null) && (o2 == null)) return true;
			if (o1 == null) throw new AssertionError(); // To convince Eclipse from complaining that it maybe null w.o. changing coding assist config
			return o1.equals(o2);
		}
		@Deprecated // Make one with 2 lists instead then sort them according to the hashcodes!
		public static <E> boolean equals(java.util.Set<E> o1, java.util.Set<E> o2) {
			return o1.equals(o2);
			// Warning: I bet that Java uses the elements' hashcodes to maintain a consistent
			// sorting out of the orderlessness of the sets therefore even if 2 sets have same
			// qty of all-equal elements (like {a, b, c} and {b, c, a}) if 2 equal elements in
			// different sets have the different hashcodes those sets will not be seen as equals.
			
		}
	}

	public static class Group2<T> {
		public final T val1;
		public final T val2;
		public Group2(T item1, T item2) {
			val1 = item1;
			val2 = item2;
		}
		public int hashCode() {return Groups.hashCode(val1, val2);}
		public boolean equals(Group2<?> that) {
			// TODO: I bet that something like [a, a, b] will be equal to [a, b, b] which they
			// shouldn't!
			return Groups.equals(
				Groups.toSet(this.val1, this.val2),
				Groups.toSet(that.val1, that.val2)
			);
		}
		public boolean equals(Object that) {
			return (that instanceof Group2) && equals((Group2<?>) that);
		}
		public Group2<T> clone() {return new Group2<>(val1, val2);}
		public String toString() {return Groups.toString(val1, val2);}
	}
	
	public static class Group3<T> {
		public final T val1;
		public final T val2;
		public final T val3;
		public Group3(T item1, T item2, T item3) {
			val1 = item1;
			val2 = item2;
			val3 = item3;
		}
		public int hashCode() {return Groups.hashCode(val1, val2, val3);}
		public boolean equals(Group3<?> that) {
			return Groups.equals(
				Groups.toSet(this.val1, this.val2, this.val3),
				Groups.toSet(that.val1, that.val2, that.val3)
			);
		}
		public boolean equals(Object that) {
			return (that instanceof Group3) && equals((Group3<?>) that);
		}
		public Group3<T> clone() {return new Group3<>(val1, val2, val3);}
		public String toString() {return Groups.toString(val1, val2, val3);}
	}
	
	
	// Handy shortcuts to avoid typing like “new fn.Group3<>()”
	public static <T> Group2<T> group(T item1, T item2) {
		return new Group2<>(item1, item2);
	}
	public static <T> Group3<T> group(T item1, T item2, T item3) {
		return new Group3<>(item1, item2, item3);
	}
	
	
//	public static <T> Group2<T> group2(java.util.Set<T> _2elms) {
//		return new Group2<>();
//	}
//	public static <T> Group3<T> group3(java.util.Set<T> _2elms) {
//		return new Group3<>();
//	}
	/* ------------------------------ ----------------------------------------------------------------------------------------------- ------------------------------ */
	
	
	
	
	/* ------------------------------------------------ Unions to store one of the multiple types at a time ------------------------------------------------ */
	
	// I didn't store the value into just one field as Object because I somehow
	// felt bad for type-casting an object from Object to a specific type and
	// depending on a ClassCastException's being thrown or not.
	// That's why I seem to be doing both spaghetti code and be wasting
	// memory in the code with fields like val2, val3, val4 etc.
	
	// FIXME: Shouldn't the Unions be immutable??? Remove the setters and add factory methods like with1, with2 etc.
	//  and maybe also remove the chance of being not set to any value --- it is not a pointer to be able to be null!!
	//  Why not store the union inside a fn.Pointer to emulate mutability???
	
	private static abstract class Union {
		public Union() {
			
		}
		
		public abstract Object get();
		
		/** Returns 0 if the union is not set any value (kind of null). */
		public abstract int getType();
		
		protected void checkTypeGet(int assumed) {
			if (getType() != 0 && getType() != assumed)
				throw new IllegalStateException("Tried to get the type-"+getType()+" current value from the union as type-"+assumed);
		}
		
		public final int hashCode() {
			return get().hashCode();
		}
		public final boolean equals(Object that) {
			if (!(that instanceof Union)) return false;
			return equals((Union) that);
		}
		public final boolean equals(Union that) {
			return this.getType() == that.getType() && fn.equals(this.get(), that.get());
		}
		
		public String toString() {return "union {"+str.toString(get())+"}";}
	}
	
	public static class Union2<T1, T2> extends Union {
		private int type = 0;
		private T1 val1 = null;
		private T2 val2 = null;
		
		public Union2() {super();}
		public Union2(T1 t1, T2 t2) {
			int cNoNull = 0;
			for (Object o: new Object[] {t1, t2})
				if (o != null) cNoNull++;
			if (!(cNoNull <= 1))
				throw new IllegalArgumentException("At most one of the parameters could be non-null");
			     if (t1 != null) set1(t1);
			else if (t2 != null) set2(t2);
		}
		
		public int getType() {return type;}
		
		public Object reset() {
			if (type == 0) return null;
			final Object old = get();
			if (type == 1) {val1 = null;}
			else if (type == 2) {val2 = null;}
			type = 0;
			return old;
		}
		
		public Object get() {
			if (type == 0) return null;
			final Object old;
			if (type == 1) {old = val1;}
			else if (type == 2) {old = val2;}
			else throw new Error();
			return old;
		}
		
		public T1 set1(T1 val) {T1 old = val1; reset(); val1 = val; type = 1; return old;}
		public T2 set2(T2 val) {T2 old = val2; reset(); val2 = val; type = 2; return old;}
		
		public T1 get1() {checkTypeGet(1); return val1;}
		public T2 get2() {checkTypeGet(2); return val2;}
		
		
	}
	
	public static class Union3<T1, T2, T3> extends Union {
		private T1 val1 = null;
		private T2 val2 = null;
		private T3 val3 = null;
		private int type = 0;
		
		public Union3() {}
		public Union3(T1 t1, T2 t2, T3 t3) {
			int cNoNull = 0;
			for (Object o: new Object[] {t1, t2, t3})
				if (o != null) cNoNull++;
			if (!(cNoNull <= 1))
				throw new IllegalArgumentException("At most one of the parameters could be non-null");
			     if (t1 != null) set1(t1);
			else if (t2 != null) set2(t2);
			else if (t3 != null) set3(t3);
		}
		
		public int getType() {return type;}
		
		public Object reset() {
			if (type == 0) return null;
			final Object old = get();
			if (type == 1) {val1 = null;}
			else if (type == 2) {val2 = null;}
			else if (type == 3) {val3 = null;}
			type = 0;
			return old;
		}
		
		public Object get() {
			if (type == 0) return null;
			final Object old;
			if (type == 1) {old = val1;}
			else if (type == 2) {old = val2;}
			else if (type == 3) {old = val3;}
			else throw new Error();
			return old;
		}
		
		public T1 set1(T1 val) {T1 old = val1; reset(); val1 = val; type = 1; return old;}
		public T2 set2(T2 val) {T2 old = val2; reset(); val2 = val; type = 2; return old;}
		public T3 set3(T3 val) {T3 old = val3; reset(); val3 = val; type = 3; return old;}
		
		public T1 get1() {checkTypeGet(1); return val1;}
		public T2 get2() {checkTypeGet(2); return val2;}
		public T3 get3() {checkTypeGet(3); return val3;}
	}
	
	public static class Union4<T1, T2, T3, T4> extends Union {
		private T1 val1 = null;
		private T2 val2 = null;
		private T3 val3 = null;
		private T4 val4 = null;
		private int type = 0;
		
		public Union4() {}
		public Union4(T1 t1, T2 t2, T3 t3, T4 t4) {
			int cNoNull = 0;
			for (Object o: new Object[] {t1, t2, t3, t4})
				if (o != null) cNoNull++;
			if (!(cNoNull <= 1))
				throw new IllegalArgumentException("At most one of the parameters could be non-null");
			     if (t1 != null) set1(t1);
			else if (t2 != null) set2(t2);
			else if (t3 != null) set3(t3);
			else if (t4 != null) set4(t4);
		}
		
		public int getType() {return type;}
		
		public Object reset() {
			if (type == 0) return null;
			final Object old = get();
			if (type == 1) {val1 = null;}
			else if (type == 2) {val2 = null;}
			else if (type == 3) {val3 = null;}
			else if (type == 4) {val4 = null;}
			type = 0;
			return old;
		}
		
		public Object get() {
			if (type == 0) return null;
			final Object old;
			if (type == 1) {old = val1;}
			else if (type == 2) {old = val2;}
			else if (type == 3) {old = val3;}
			else if (type == 4) {old = val4;}
			else throw new Error();
			return old;
		}
		
		public T1 set1(T1 val) {T1 old = val1; reset(); val1 = val; type = 1; return old;}
		public T2 set2(T2 val) {T2 old = val2; reset(); val2 = val; type = 2; return old;}
		public T3 set3(T3 val) {T3 old = val3; reset(); val3 = val; type = 3; return old;}
		public T4 set4(T4 val) {T4 old = val4; reset(); val4 = val; type = 4; return old;}
		
		public T1 get1() {checkTypeGet(1); return val1;}
		public T2 get2() {checkTypeGet(2); return val2;}
		public T3 get3() {checkTypeGet(3); return val3;}
		public T4 get4() {checkTypeGet(4); return val4;}
	}
	
	public static class Union5<T1, T2, T3, T4, T5> extends Union {
		private T1 val1 = null;
		private T2 val2 = null;
		private T3 val3 = null;
		private T4 val4 = null;
		private T5 val5 = null;
		private int type = 0;
		
		public Union5() {}
		public Union5(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
			int cNoNull = 0;
			for (Object o: new Object[] {t1, t2, t3, t4, t5})
				if (o != null) cNoNull++;
			if (!(cNoNull <= 1))
				throw new IllegalArgumentException("At most one of the parameters could be non-null");
			     if (t1 != null) set1(t1);
			else if (t2 != null) set2(t2);
			else if (t3 != null) set3(t3);
			else if (t4 != null) set4(t4);
			else if (t5 != null) set5(t5);
		}
		
		public int getType() {return type;}
		
		public Object reset() {
			if (type == 0) return null;
			// In case anyone criticizes for not, I know how to use switch but just don't quite want to use it.
			final Object old = get();
			if (type == 1) {val1 = null;}
			else if (type == 2) {val2 = null;}
			else if (type == 3) {val3 = null;}
			else if (type == 4) {val4 = null;}
			else if (type == 5) {val5 = null;}
			type = 0;
			return old;
		}
		
		public Object get() {
			if (type == 0) return null;
			final Object old;
			if (type == 1) {old = val1;}
			else if (type == 2) {old = val2;}
			else if (type == 3) {old = val3;}
			else if (type == 4) {old = val4;}
			else if (type == 5) {old = val5;}
			else throw new Error();
			return old;
		}
		
		public T1 set1(T1 val) {T1 old = val1; reset(); val1 = val; type = 1; return old;}
		public T2 set2(T2 val) {T2 old = val2; reset(); val2 = val; type = 2; return old;}
		public T3 set3(T3 val) {T3 old = val3; reset(); val3 = val; type = 3; return old;}
		public T4 set4(T4 val) {T4 old = val4; reset(); val4 = val; type = 4; return old;}
		public T5 set5(T5 val) {T5 old = val5; reset(); val5 = val; type = 5; return old;}
		
		public T1 get1() {checkTypeGet(1); return val1;}
		public T2 get2() {checkTypeGet(2); return val2;}
		public T3 get3() {checkTypeGet(3); return val3;}
		public T4 get4() {checkTypeGet(4); return val4;}
		public T5 get5() {checkTypeGet(5); return val5;}
	}
	
	public static <T1, T2> Union2<T1, T2>
		union(T1 t1, T2 t2) {return new Union2<>(t1, t2);}
	public static <T1, T2, T3> Union3<T1, T2, T3>
		union(T1 t1, T2 t2, T3 t3) {return new Union3<>(t1, t2, t3);}
	public static <T1, T2, T3, T4> Union4<T1, T2, T3, T4>
		union(T1 t1, T2 t2, T3 t3, T4 t4) {return new Union4<>(t1, t2, t3, t4);}
	public static <T1, T2, T3, T4, T5> Union5<T1, T2, T3, T4, T5>
		union(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {return new Union5<>(t1, t2, t3, t4, t5);}
	
	/* ------------------------------------------------ --------------------------------------------------- ------------------------------------------------ */
	
	
	
	/* ------------------------------------------------ Pointer/Wrapper ------------------------------------------------ */
	
//	public static interface AbstractWrapper<E> extends Supplier<E>, Consumer<E>/*, Function<E, E>*/ {
//		public E get();
//		public default void set(E newValue) {accept(newValue);}
//		public default void accept(E newValue) {accept(newValue);}
//		// Here I tried to make the user need to implement at least only
//		// from of the below 2, however it will be like a bomb ready to
//		// throw a stackOverFlow at an unexpected place if the user forgets
//		// to implement any because Java will not force them to implement
//		// at least one because they both are implemented by default
//		// with each other!!!
//		// I hope I can find a meaningful solution for that one day!
//	}
	
	/** Handy shortcut in place of “new fn.Wrapper<>()” */
	public static <E> Pointer<E> wrap() {return new Pointer<>();}
	/** Handy shortcut in place of “new fn.Wrapper<>()” */
	public static <E> Pointer<E> wrap(E value) {return new Pointer<>(value);}
//	public static intWrapper wrap(int value) {return new intWrapper(value);}
	public static <E> Pointer<E> concurrentWrap() {throw new UnsupportedOperationException();}
	public static <E> Pointer<E> concurrentWrap(E value) {throw new UnsupportedOperationException();}
//	public static <E> Pointer<E> ptr() {return new Pointer<>();}
//	public static <E> Pointer<E> ptr(E value) {return new Pointer<>(value);}
//	public static <E> Pointer<E> conptr() {throw new UnsupportedOperationException();}
//	public static <E> Pointer<E> conptr(E value) {throw new UnsupportedOperationException();}
//	public static <E> Wrapper<E> wp() {return new Wrapper<>();}
//	public static <E> Wrapper<E> wp(E value) {return new Wrapper<>(value);}
//	public static <E> Wrapper<E> conwp() {throw new UnsupportedOperationException();}
//	public static <E> Wrapper<E> conwp(E value) {throw new UnsupportedOperationException();}
	
	/** Class of mutable object instances to mutate their wrapped/encapsulated value.
	 *  Especially useful in Java's local classes' methods' referring to non
	 *  effectively-final local variables to make values into references and
	 *  defining methods with output parameters like done with pointers to fill
	 *  inside the methods with the output value in C/C++, also helpful when you
	 *  can't create single element array (to be parameter value to be filled with
	 *  output) with type parameters and when an array as parameter is not that
	 *  helpful for the user to understand that it is supposed to be a
	 *  single-element array as a wrapper.
	 *  <p> Note that it implements {@link java.util.function.Consumer}; type that
	 *  takes a value and returns void.
	 */
	public static class Pointer<E> implements Supplier<E>, Consumer<E>/*, Function<E, E>*/ {
		public E value;
		public Pointer(E value) {this.value = value;}
		public Pointer() {this(null);}
		public E get() {return value;} // Supplier
		public void set(E newValue) {value = newValue;}
		public void accept(E newValue) {set(newValue);} // Consumer
		public String toString() {return "("+value+")";}
//		public E apply(E newValue) { // Function (Supplier+Consumer)
//			E oldValue = get();
//			set(newValue);
//			return oldValue;
//		}
	}
	/** Class of mutable object instances to mutate their wrapped/encapsulated
	 *  <code>int</code> value.
	 *  Especially useful in Java's local classes' methods' referring to non
	 *  effectively-final local variables to make values into references. */
	public static class intWrapper implements Supplier<Integer>, Consumer<Integer>/*, Function<Integer, Integer>*/ {
		public int value;
		public intWrapper(int value) {this.value = value;}
		public Integer get() {return value;}
		public void set(Integer newValue) {value = newValue;}
		public void accept(Integer newValue) {set(newValue);}
		public String toString() {return "("+value+")";}
//		public Integer apply(Integer newValue) { // Function (Supplier+Consumer)
//			Integer oldValue = get();
//			set(newValue);
//			return oldValue;
//		}
	}
	
//	/** Just like {@link fn.Wrapper}, but supports racing threads and avoids
//	 *  race condition by making use of volatility and synchronizing */
//	public static class ConcurrentWrapper<E> implements Supplier<E>, Consumer<E>/*, Function<E, E>*/ {
//		public volatile E value; // Volatile is only needed alas this is public (hence can be changed from outside)
//		public ConcurrentWrapper(E value) {this.value = value;}
//		public ConcurrentWrapper() {this(null);}
//		public E get() {synchronized (this) {return value;}}
//		public void set(E newValue) {synchronized (this) {value = newValue;}}
//		public void accept(E newValue) {set(newValue);}
//		public String toString() {synchronized (this) {return "("+value+")";}}
////		public E apply(E newValue) {synchronized (this) {
////			// Getting the old and setting the new is in a single synchronized block
////			E oldValue = get();
////			set(newValue);
////			return oldValue;
////		}}
//	}
	
	/** Handy shortcut for <code>E Objects.&lt;E&gt;requireNonNull(E)</code>. */
	public static <E> E noNull(E o) {
		return java.util.Objects.requireNonNull(o);
	}
	/** Handy shortcut for <code>E Objects.&lt;E&gt;requireNonNull(E)</code>. */
	public static <E> void noNull(@SuppressWarnings("unchecked") E... all) {
		for (E each: all) noNull(each);
	}
	
	/** Just returns a weak reference such whose public T get() returns the referent or null depending on whether the GC has cleared it. */
	public static <T> java.lang.ref.WeakReference<T> weakRef(T referent) {return new java.lang.ref.WeakReference<>(referent);}
	
	// Deprecated because of the choice of the name.
	@Deprecated public static <T> Supplier<T> reference(Supplier<T> supplier) {
		fn.log("Warning: Used the deprecated method fn.reference at\n"+str.getStackTraceText());
		return new fn.RegenerativeWeakReference<>(supplier);
	}
	
	// TODO: Sure to use that name???
	public static <T> Supplier<T> cachedReference(Supplier<T> supplier) {
		return new fn.RegenerativeWeakReference<>(supplier);
	}
	
	/** (Re-)generates the object by the supplier given and stores it as a weak reference to it
	 *  (if the existing reference gives null because of the garbage collector), then returns it. */
	private static class RegenerativeWeakReference<Item> implements Supplier<Item> {
		
		private volatile java.lang.ref.Reference<Item> cache;
		private final Supplier<Item> supplier;
		public RegenerativeWeakReference(Supplier<Item> supplier) {
			this.supplier = supplier;
			this.cache = new java.lang.ref.WeakReference<>(null);
		}
		
		public Item get() {
			Item item = cache.get();
			if (item == null) {
				item = supplier.get();
				cache = new java.lang.ref.WeakReference<>(item);
			}
			return item;
		}
		
	}
	
	
	
	/** Returns the native class path string of the VM that is from the OS environment variable, specified in the commandline
	 *  with --class-path or from the classpath entry of the jar that is run with -jar argument (using the *.jar's classpath
	 *  and main class to run the main method) */
	// Reading the process' OS environment variable is not acceptable since
	// it gives the correct result only if the classpath is inherited from
	// the OS' environment variable.
	public static String getClasspathString() {
		return System.getProperty("java.class.path");
	}
	/** Returns the class path of the VM that is from the OS environment variable, specified in the commandline
	*  with --class-path or from the classpath entry of the jar that is run with -jar argument (using the *.jar's classpath
	*  and main class to run the main method) */
	public static list<String> getClasspath() {
		return str.split(getClasspathString(), java.util.regex.Pattern.quote(System.getProperty("path.separator")), true);
	}
	
	/* ------------------------------------------------ --------------- ------------------------------------------------ */
	
	
	
	
	/* ------------------------------------------------ hashing ------------------------------------------------ */
	
	// Moved to Crypto.java
	
	/* ------------------------------------------------ ------- ------------------------------------------------ */
	
	
	
	
	
	/* ------------------------------------------------ number <-> byte array operations ------------------------------------------------ */
	
	/* 
	 * byte (as should be): 1, int: 4, long: 8 bytes.
	 * Those are already defined in Java Language Specification (requiring all JREs/JDKs follow to
	 * declare themselves as JRE/JDK), but JUST IN CASE.
	 * 
	 * Since there is no such restriction about the sizes of numeric types in C/C++, meaning that
	 * compilers can compile code into binaries allocating varying sizes for them and still can be
	 * counted as C/C++ compiler (to be compatible to the native CPU's registers and architecture
	 * and avoid unnecessary performance reduction); we have types like uint8_t, int32_t and int64_t
	 * as well, also like int_least32_t and uint_fast64_t. See https://en.cppreference.com/w/cpp/language/types
	 * for how the sizes of C/C++ numeric types like int, long long, unsigned short etc. differ.
	 */
	private static final boolean intHasExpectedSize, longHasExpectedSize; // 4, 8
	private static final boolean doubleHasExpectedSize; // 8
//	private static final byte byteBuffersUseBigEndianForIntAndLong; // -1: undefined, 0/1: false/true
	
	
	static {label: { // At the initialization of fn that occurs at the first usage of the class, warn about those!
		
		int intBytes = Integer.BYTES, longBytes = Long.BYTES;
		int doubleBytes = Double.BYTES;
		intHasExpectedSize = (intBytes == 4);
		longHasExpectedSize = (longBytes == 8);
		doubleHasExpectedSize = (doubleBytes == 8);
		if (!(intHasExpectedSize && longHasExpectedSize && doubleHasExpectedSize)) {
			// Due to the position of this static initializer, the fields for fn.log are ready at this point
			if (!intHasExpectedSize) fn.log("WARNING: INT IS "+intBytes+" BYTES INSTEAD OF 4!!!");
			if (!longHasExpectedSize) fn.log("WARNING: LONG IS "+longBytes+" BYTES INSTEAD OF 8!!!");
			if (!doubleHasExpectedSize) fn.log("WARNING: DOUBLE IS "+longBytes+" BYTES INSTEAD OF 8!!!");
		// (after initializing the size status)
		}
		
		
		final boolean myImplWorksCorrectly;
		
		
		if (intHasExpectedSize && longHasExpectedSize) { // Verify that the java.nio.ByteBuffer uses signed big endian
			
			java.util.function.Function<byte[][], Boolean> comp = (_2byteArr) -> {
				if (_2byteArr.length != 2) throw new Error();
				byte[] a = _2byteArr[1-1], b = _2byteArr[2-1];
				if (a.length != b.length) throw new Error();
				for (int i: fn.range(a.length))
					if (a[i-1] != b[i-1]) return false;
				return true;
			};
			
			class tupi {
				public final int value;
				public final byte[] data;
				public tupi(int v, byte[] d) {value = v; data = d;}
				public String toString() {return ""+value;}
			}
			class tupl {
				public final long value;
				public final byte[] data;
				public tupl(long v, byte[] d) {value = v; data = d;}
				public String toString() {return ""+value;}
			}
			
			java.util.Base64.Decoder b64d_dec = java.util.Base64.getDecoder();
			java.util.function.Function<String, byte[]> b64d = (text) -> b64d_dec.decode(text);
			// Do not use str.fromBase64 here!
			// Otherwise when initializing str requires initialization of fn and these run, the cached reference fields
			// of str will still be null and will fail when str.fromBase64 is used in the initialization of fn started
			// because of a point in the initializer of str before assigning those fields.
			
		A:	{
				for (tupi tuple: new tupi[] {
					new tupi(0, b64d.apply("AAAAAA==")),
					new tupi(1, b64d.apply("AAAAAQ==")),
					new tupi(-1, b64d.apply("/////w==")),
					new tupi(32768, b64d.apply("AACAAA==")),
					new tupi(123456, b64d.apply("AAHiQA==")),
					new tupi(-123456, b64d.apply("//4dwA=="))
				}) {
					if (!comp.apply(new byte[][] {tuple.data,    fromSBEInt(tuple.value)})) {
						myImplWorksCorrectly = false; break A;
					}
					if (!(                        tuple.value == toSBEInt(  tuple.data ) )) {
						myImplWorksCorrectly = false; break A;
					}
				}
				for (tupl tuple: new tupl[] {
					new tupl(0, b64d.apply("AAAAAAAAAAA=")),
					new tupl(1, b64d.apply("AAAAAAAAAAE=")),
					new tupl(-1, b64d.apply("//////////8=")),
					new tupl(32768, b64d.apply("AAAAAAAAgAA=")),
					new tupl(123456, b64d.apply("AAAAAAAB4kA=")),
					new tupl(-123456, b64d.apply("///////+HcA="))
				}) {
					if (!comp.apply(new byte[][] {tuple.data,    fromSBELong(tuple.value)})) {
						myImplWorksCorrectly = false; break A;
					}
					if (!(                        tuple.value == toSBELong(  tuple.data ) )) {
						myImplWorksCorrectly = false; break A;
					}
				}
				myImplWorksCorrectly = true;
			}
			
			if (!myImplWorksCorrectly) throw new AssertionError();
		} else {
			
		}
	}}
	
	// TODO: Implement your own method and get rid of this!
	private final static String
		byteBufferEndianErrorMsg = "Since java.nio.ByteBuffer does not use signed big endian for int and long "
		                         + "in this runtime, converting between int/long and byte[] is not possible.";
	
	
	
	public static byte[] fromSBEInt(int num) {
		if (!intHasExpectedSize) throw new InternalError("int does not have 4 bytes in this JVM");
		byte[] retVal = new byte[4];
		for (int i = 0; i <= 3; i++) retVal[i] = (byte) (num >>> (8*(3-i)));
		return retVal;
	}
	
	public static int toSBEInt(byte[] num) {
		if (!intHasExpectedSize) throw new InternalError("int does not have 4 bytes in this JVM");
		if (num.length != 4) throw new IllegalArgumentException("Given byte array is not sized as int's size");
		int retVal = 0;
		for (int i = 0; i <= 2; i++) {retVal += (num[i] & 0x000000ff); retVal <<= 8;}
		for (int i = 3; i <= 3; i++) {retVal += (num[i] & 0x000000ff);              }
//		fn.printe("");
//		retVal = 0;
//		fn.printe(retVal);
//		retVal += num[0];
//		fn.printe(retVal);
//		retVal <<= 8;
//		fn.printe(retVal);
//		retVal += num[1];
//		fn.printe(retVal);
//		retVal <<= 8;
//		fn.printe(retVal);
//		retVal += num[2];
//		fn.printe(retVal);
//		retVal <<= 8;
//		fn.printe(retVal);
//		retVal += num[3];
//		fn.printe(retVal);
//		fn.printe("");
		return retVal;
	}
	
	public static byte[] fromSBELong(long num) {
		if (!longHasExpectedSize) throw new InternalError("long does not have 8 bytes in this JVM");
		byte[] retVal = new byte[8];
		for (int i = 0; i <= 7; i++) retVal[i] = (byte) (num >>> (8*(7-i)));
		return retVal;
	}
	
	public static long toSBELong(byte[] num) {
		if (!longHasExpectedSize) throw new InternalError("long does not have 8 bytes in this JVM");
		if (num.length != 8) throw new IllegalArgumentException("Given byte array is not sized as long's size");
		long retVal = 0;
		for (int i = 0; i <= 6; i++) {retVal += (num[i] & 0x000000ff); retVal <<= 8;}
		for (int i = 7; i <= 7; i++) {retVal += (num[i] & 0x000000ff);              }
		return retVal;
	}
	
	
	public static byte[] fromIEEE754DoubleFP(double num) {
		if (!doubleHasExpectedSize) throw new InternalError("double does not have 8 bytes");
		long bits = Double.doubleToRawLongBits(num);
		return fromSBELong(bits);
	}
	public static double toIEEE754DoubleFP(byte[] num) {
		if (!doubleHasExpectedSize) throw new InternalError("double does not have 8 bytes");
		if (num.length != 8) throw new IllegalArgumentException("Given byte array is not sized as double's size");
		long bits = toSBELong(num);
		return Double.longBitsToDouble(bits);
	}
	
	/* ------------------------------------------------ -------------------------------- ------------------------------------------------ */
	
	
	
	
	/* ------------------------------------------------ number <-> byte operations ------------------------------------------------ */
	
	/** Returns the integer value (of the byte) (in [0, 255]) which is equal to the byte when the byte is treated as an
	 * <b>unsigned</b> 8-bit integer. Thait is, <i><b>unsigned</b> extend</i>s the byte. **/
	// TODO: A better name???!!:
	//     “toInt”, “toUInt”, “uExtend”, “uExtToInt”, “byteUToInt”
	//     “uExtByte”, “uExtendByte”, “uExtendToInt”, “unsignedExtendByte”
	public static int unsignedExtendToInt(byte val) {
		return val & 0x000000ff;
	}
	/** Returns the byte value (of the integer in [0, 255]) which is equal to the integer when treated as an
	 * <b>unsigned</b> 8-bit integer. That is, takes the last 8 bits (slices off the rest) of the integer. **/
	public static byte toByte(int val) {
		if (!(val >= 0 && val <= 255)) throw new IllegalArgumentException("Expected in [0, 255], got "+val);
		return (byte) val;
	}
	// TODO: Should be public but not quite sure it is appropriate. Rename before making public.
	/** Returns the byte value (of the integer in [0, 255]) which is equal to the integer when treated as an
	 * <b>unsigned</b> 8-bit integer. That is, takes the last 8 bits (slices off the rest) of the integer.
	 * <p>Skips checking for the 0-255 boundaries for the performance! **/
	private static byte toByte0(int val) {
		return (byte) val;
	}
	// TODO: Also better name for these 2??...
	public static byte intByte(int intVal) {return toByte(intVal);}
	public static int intByte(byte byteVal) {return unsignedExtendToInt(byteVal);}
	// TODO: ...like these 2??
	public static byte uintByte(int intVal) {return toByte(intVal);}
	public static int uintByte(byte byteVal) {return unsignedExtendToInt(byteVal);}
	
	/* ------------------------------------------------ -------------------------- ------------------------------------------------ */
	
	
	
	
	/* ------------------------------------------------ bitwise (byte <-> bit/boolean) operations ------------------------------------------------ */
	
	// The private methods below do not check for bounds given that they are already checked by one of their (in)direct callers that is public
	
	/** Returns the boolean (1-bit) value of the specified index in the given byte.
	 *  <p> Expects 1-based index, and this index is big-endian (1 corresponds the most significant bit of 0bXXXXXXXX).
	 *  <p> Of course does not allocate a Boolean (a few bytes in total of spanning a pointer and
	 *  a padded-to-8-bit 1-bit bool value) list of the 8 bits of the byte just for a
	 *  stupid bitwise operation! */
	public static boolean getBool(byte number, int index) {
		if (!(index >= 1 && index <= 8)) throw new IndexOutOfBoundsException(index+" is out of [1, 8]");
		return getBool0(number, index);
	}
	// TODO: TEST!!
	private static boolean getBool0(byte number, int index) {
		int intersect;
		if (isAssertOn) {
			intersect = fn.unsignedExtendToInt(number) & fn.unsignedExtendToInt((byte) (1 << (8 - index)));
			if (!(intersect >= 0)) throw new AssertionError();
			return intersect > 0;
		} else {
			intersect = number & ((byte) (1 << (8 - index)));
			return intersect != 0;
		}
	}
	/** Sets the boolean (1-bit) value of the specified index in the given byte and returns
	 *  the new boolean.
	 *  <p> Expects 1-based index, and this index is big-endian (1 corresponds the most significant bit of 0bXXXXXXXX).
	 *  <p> Of course does not allocate a Boolean (a few bytes in total of spanning a pointer and
	 *  a padded-to-8-bit 1 bit bool value) list of the 8 bits of the byte just for a
	 *  stupid bitwise operation! */
	public static byte putBool(byte number, int index, boolean value) {
		if (!(index >= 1 && index <= 8)) throw new IndexOutOfBoundsException(index+" is out of [1, 8]");
		return putBool0(number, index, value);
	}
	private static byte putBool0(byte number, int index, boolean value) {
		byte mask = 1; // 0b00000001
		mask = (byte) (mask << (8 - index));
		if (!value) mask = (byte) ~mask;
		if (value) number = (byte) (number | mask);
		else number = (byte) (number & mask);
		return number;
	}
	
	
	public static boolean getBool(byte[] memblock, int byteIndex, int bitIndex) {
		if (!(byteIndex >= 1 && byteIndex <= memblock.length)) throw new IndexOutOfBoundsException(byteIndex+" is out of [1, "+memblock.length+"]");
		if (!(bitIndex >= 1 && bitIndex <= 8)) throw new IndexOutOfBoundsException(bitIndex+" is out of [1, 8]");
		return getBool0(memblock, byteIndex, bitIndex);
	}
	private static boolean getBool0(byte[] memblock, int byteIndex, int bitIndex) {
		return getBool0(memblock[byteIndex-1], bitIndex);
	}
	public static void putBool(byte[] memblock, int byteIndex, int bitIndex, boolean value) {
		if (!(byteIndex >= 1 && byteIndex <= memblock.length)) throw new IndexOutOfBoundsException(byteIndex+" is out of [1, "+memblock.length+"]");
		if (!(bitIndex >= 1 && bitIndex <= 8)) throw new IndexOutOfBoundsException(bitIndex+" is out of [1, 8]");
		putBool0(memblock, byteIndex, bitIndex, value);
	}
	private static void putBool0(byte[] memblock, int byteIndex, int bitIndex, boolean value) {
		memblock[byteIndex-1] = putBool0(memblock[byteIndex-1], bitIndex, value);
	}
	
	
	public static boolean getBool(byte[] memblock, int index) {
		if (!(index >= 1 && index <= memblock.length*8)) throw new IndexOutOfBoundsException(index+" is out of [1, "+memblock.length*8+"]");
		return getBool0(memblock, index);
	}
	private static boolean getBool0(byte[] memblock, int index) {
		return getBool0(memblock, (index-1)/8 + 1, (index-1) % 8 + 1);
	}
	public static void putBool(byte[] memblock, int index, boolean value) {
		if (!(index >= 1 && index <= memblock.length*8)) throw new IndexOutOfBoundsException(index+" is out of [1, "+memblock.length*8+"]");
		putBool0(memblock, index, value);
	}
	private static void putBool0(byte[] memblock, int index, boolean value) {
		putBool0(memblock, (index-1)/8 + 1, (index-1) % 8 + 1, value);
	}
	
	
	// TODO: Large byte array taking more than additional 30% of its size in the actual memory?
	// Why not implement a wrapper for bytebuffer or something similar (or maybe JNI-based
	// one), such as:
	//	public static abstract class memblock {
	//		public abstract void putByte(int index, byte value);
	//		public abstract byte getByte(int index);
	//		public abstract long length();
	//	}
	// ...instead of byte arrays?
	
	/* ------------------------------------------------ ----------------------------------------- ------------------------------------------------ */
	
	
	
	/* ------------------------------------------------ primitive/object array slice ------------------------------------------------ */
		// TODO
	/* ------------------------------------------------ ---------------------------- ------------------------------------------------ */
	
	
	/* ------------------------------------------------ primitive/object array copy ------------------------------------------------ */
	
//	public static void copy(int[] src, int srcStart, int srcEnd, int[] dest, int destStart) {
//		copy(src, srcStart, dest, destStart, (srcEnd-srcStart));
//	}
	// MAYBE: Implement your method
	// “Maybe” instead of TODO because the JVM's method is supposed to be significantly
	// faster than manually assigning from one array to another
	
	// Although there are many array-related methods in java.util.Arrays, java.lang.System
	// features a single such method (taking arrays as objects then runtime-checking),
	// arraycopy, that is different and is not among those many methods in the Arrays class.
	// It looks important and much more fundamental than those in java.util.Arrays; as cool as
	// the System class itself where it is defined in 🤩🥹 
	// Here I'm wrapping (and ready to implement my own way too) it.
	
	// Accepts 1 based indexes and also negative indexes.
	
	// TODO: Maybe rename from “fn.copy” into “fn.arraycopy” or “fn.arrayCopy”
	
	// -------------------------------- byte arrays --------------------------------
	// Copy length elements starting from srcStart-th element from src into dest starting from destStart-th element.
	public static void copy(byte[] src, int srcStart, byte[] dest, int destStart, int length) {
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	// Copy all elements of src into dest starting from destStart-th element.
	public static void copy(byte[] src,/* srcStart = 1,*/ byte[] dest, int destStart /*length = src.length*/) {
		int length = src.length;
		int srcStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	// Copy dest.length elements starting from srcStart-th element from src into dest.
	public static void copy(byte[] src, int srcStart, byte[] dest /*, destStart = 1, length = src.length*/) {
		int length = dest.length;
		int destStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	// The 0-named private methods do not accept negative indexes
	private static void copy0(byte[] src, byte[] dest, int[] indexes, int length) {
		int srcStart = indexes[1-1], destStart = indexes[2-1];
		tryCopyThwAssert(src, srcStart-1, dest, destStart-1, length);
	}
	
	// -------------------------------- Object arrays --------------------------------
	public static <E> void copy(E[] src, int srcStart, E[] dest, int destStart, int length) {
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static <E> void copy(E[] src,/* srcStart = 1,*/ E[] dest, int destStart /*length = src.length*/) {
		int length = src.length;
		int srcStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static <E> void copy(E[] src, int srcStart, E[] dest /*, destStart = 1, length = src.length*/) {
		int length = dest.length;
		int destStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	private static <E> void copy0(E[] src, E[] dest, int[] indexes, int length) {
		int srcStart = indexes[1-1], destStart = indexes[2-1];
		tryCopyThwAssert(src, srcStart-1, dest, destStart-1, length);
	}
	
	// -------------------------------- bool arrays --------------------------------
	public static void copy(boolean[] src, int srcStart, boolean[] dest, int destStart, int length) {
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(boolean[] src,/* srcStart = 1,*/ boolean[] dest, int destStart /*length = src.length*/) {
		int length = src.length;
		int srcStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(boolean[] src, int srcStart, boolean[] dest /*, destStart = 1, length = src.length*/) {
		int length = dest.length;
		int destStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	private static void copy0(boolean[] src, boolean[] dest, int[] indexes, int length) {
		int srcStart = indexes[1-1], destStart = indexes[2-1];
		tryCopyThwAssert(src, srcStart-1, dest, destStart-1, length);
	}
	
	// -------------------------------- int arrays --------------------------------
	public static void copy(int[] src, int srcStart, int[] dest, int destStart, int length) {
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(int[] src,/* srcStart = 1,*/ int[] dest, int destStart /*length = src.length*/) {
		int length = src.length;
		int srcStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(int[] src, int srcStart, int[] dest /*, destStart = 1, length = src.length*/) {
		int length = dest.length;
		int destStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	private static void copy0(int[] src, int[] dest, int[] indexes, int length) {
		int srcStart = indexes[1-1], destStart = indexes[2-1];
		tryCopyThwAssert(src, srcStart-1, dest, destStart-1, length);
	}
	
	// -------------------------------- long arrays --------------------------------
	public static void copy(long[] src, int srcStart, long[] dest, int destStart, int length) {
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(long[] src,/* srcStart = 1,*/ long[] dest, int destStart /*length = src.length*/) {
		int length = src.length;
		int srcStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	public static void copy(long[] src, int srcStart, long[] dest /*, destStart = 1, length = src.length*/) {
		int length = dest.length;
		int destStart = 1;
		int[] indexes = checkArraycopyParams_retNormInd(src.length, srcStart, dest.length, destStart, length);
		copy0(src, dest, indexes, length);
	}
	private static void copy0(long[] src, long[] dest, int[] indexes, int length) {
		int srcStart = indexes[1-1], destStart = indexes[2-1];
		tryCopyThwAssert(src, srcStart-1, dest, destStart-1, length);
	}
	
	private static void tryCopyThwAssert(Object src, int srcStart, Object dest, int destStart, int length) {
		try {System.arraycopy(src, srcStart, dest, destStart, length);}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new AssertionError("Malformed input check was caught by System.arraycopy(...) instead of fn.copy(...)!", e);
		}
	}
	
	// Check arraycopy parameters and return normalized indexes
	private static int[] checkArraycopyParams_retNormInd(int srclen, int srcstart, int destlen, int deststart, int qtyElems) {
		java.util.function.Function<Integer, String> ordinalOf = (num) -> {
			String s1 = "st", s2 = "nd", s3 = "rd", s0 = "th";
			if (num < 0) throw new Error();
			if ((num % 100) / 10 == 1) return s0;
			else {
				switch (num % 10) {
					case 1: return s1;
					case 2: return s2;
					case 3: return s3;
					default: return s0;
				}
			}
		};
		if (qtyElems == 0) {
			// It should be OK to write just nothing into an array of 0 elements starting from any index.
			// It should be also OK to write just nothing into an array of i.e. 5 elements starting from index 10..
			throw new Error("// FIXME!");
		}
		try {
//			list.normalizeIntervalAndClampToBounds(srcstart, srclen, qtyElems, true);
//			list.normalizeIntervalAndClampToBounds(deststart, destlen, qtyElems, true);
			list.checkForValidIndex(srclen, srcstart, srclen, srcstart);
			list.checkForValidIndex(destlen, deststart, destlen, deststart);
		} catch (IndexOutOfBoundsException e) {
//			fn.print(srclen, " ");
//			fn.print(srcstart, " ");
//			fn.print(destlen, " ");
//			fn.print(deststart);
			throw new ArrayIndexOutOfBoundsException(e.getMessage());
		}
		if (qtyElems < 0) throw new IllegalArgumentException("Negative qty of elements for arraycopy: "+qtyElems);
		srcstart = list.normalizeIndex(srcstart, srclen);
		deststart = list.normalizeIndex(deststart, destlen);
		if (!(srcstart+qtyElems-1 <= srclen && deststart+qtyElems-1 <= destlen))
			throw new ArrayIndexOutOfBoundsException(
				"Quantity of elements to be copied from the start index of one of the arrays exceeds its size: "
				+qtyElems+" elements from array of "+srclen+" elements (from "+srcstart+ordinalOf.apply(srcstart)+") "
				+"into array of "+destlen+" elements (from "+deststart+ordinalOf.apply(deststart)+")"
			);
		return new int[] {srcstart, deststart};
	}
	
	
	// -------------------------------- simple clone methods --------------------------------
	// TODO: Sure to keep those though? Even to change the implementation of them instead of
	// changing every single occurrence of someArray.clone(), in case a JVM might come up
	// and do not override the protected methods with public ones?
	public static int[] clone(int[] array) {return array.clone();}
	public static boolean[] clone(boolean[] array) {return array.clone();}
	public static byte[] clone(byte[] array) {return array.clone();}
	public static long[] clone(long[] array) {return array.clone();}
	public static <E> E[] clone(E[] array) {return array.clone();}
	/* ------------------------------------------------ --------------------------- ------------------------------------------------ */
	
	
	
	/** Class for using byte values to represent 2 values of boolean and an uninitialized value. 0 and 1 are
	  * false and true; whereas -1 means uninitialized, undefined or null. */
	public static class bools {
		private bools() {}
		public static void assertNoNull(byte val) {if (!noNull(val)) throw new AssertionError();}
		public static boolean from(byte val) {if (val == 0) return false; else if (val == 1) return true; else throw new NullPointerException("Can\'t convert "+val+" to boolean.");}
		public static byte to(boolean val) {return val ? ((byte)1) : ((byte)0);}
		public static boolean convert(byte val) {return from(val);}
		public static byte convert(boolean val) {return to(val);}
		public static boolean isNull(byte val) {return !noNull(val);}
		public static boolean noNull(byte val) {if (val == 0 || val == 1) return true; else if (val == -1) return false; else throw new IllegalStateException();}
	}
	
	
	
	
	// TODO: Move these into an appropriate class instead of fn!!
	private static native boolean windowsExplorerOpenFolderAndSelectItem(byte[] utf8EncodeOfPath, byte[] utf8EncodeOfFilename);
	public static boolean windowsExplorerOpenFolder(String folderToOpen) {
		return windowsExplorerOpenFolderAndSelectItem(str.utf8encode(folderToOpen), null);
	}
	public static boolean windowsExplorerOpenFolderAndSelectItem(String folderToOpen, String filenameToSelect) {
		if (filenameToSelect == null)
			return windowsExplorerOpenFolder(folderToOpen);
		else
			return windowsExplorerOpenFolderAndSelectItem(
				str.utf8encode(folderToOpen), str.utf8encode(filenameToSelect)
			);
	}
	
	
	
	/* This is not meant for a regular use. This is to prevent a java process
	 * that runs BeanShell macro or just a dynamically loaded class in one of
	 * its main threads, and that I don't want to terminate and respawn, from
	 * falling into an endless stupid while loop that I forgot to make sure that
	 * it eventually breaks.
	 * I created it for the BeanShell scripts I made to run on jEdit.
	 * I added it here just to copy from here when I ever need it again.
	 * Running such scripts in a separate thread that is subject to
	 * forcefully stopping after a minute or so would be a good idea too. */
	private static class SafeWhile {
		public static class LoopControl extends Throwable {
			public final Object matcher;
			public LoopControl(Object uniqueMatcher) {super(); matcher = uniqueMatcher;}
		}
		public static class LoopBreak extends LoopControl {public LoopBreak(Object uniqueMatcher) {super(uniqueMatcher);}}
		public static class LoopContinue extends LoopControl {public LoopContinue(Object uniqueMatcher) {super(uniqueMatcher);}}
		public static interface LoopController {
			public default void stop() throws LoopControl {throw stopper();}
			public default void skip() throws LoopControl {throw skipper();};
			public LoopControl stopper();
			public LoopControl skipper();
		}
		public static interface ConsumerAction {public void accept(LoopController st) throws LoopControl;}
		public static void loop(int maxIterations, ConsumerAction action) throws fn.SafeWhile.LoopBreak {
			int c = 0;
			Object uniqueMatcher = new Object();
			LoopController lc = new LoopController() {
				public LoopControl stopper() {return new LoopBreak(uniqueMatcher);}
				public LoopControl skipper() {return new LoopContinue(uniqueMatcher);}
			};
			try {while (true) {
				if (++c > maxIterations) throw new Error("AAAAAAAAAAAAAAAAAAA!!!!!!!!!!! WHILE LOOP DOESN\'T END!!!????????");
				try {action.accept(lc);}
				catch (LoopContinue th) {if (th.matcher != uniqueMatcher) throw th;}
			}} catch (LoopBreak th) {if (th.matcher != uniqueMatcher) throw th;}
			// If the unique object from the throwable in the consumer action does not match with our
			// object, then it is from another loopcontroller; that is, the loop controller of an
			// enclosing scope. Consider the following: {
//				int[] count = {0};
//				int[] i = {1}, j = {1};
//				fn.SafeWhile.loop(1000, (lcA) -> {
//					// do stuff A
//					fn.SafeWhile.loop(1000, (lcB) -> {
//						// do stuff B
//						if (cond) lcA.stop(); // Break from A. Since this loop has different unique object to be used with
//						                      // loop stop and loop skip throwables than the outer loop has, this loop does
//						                      // not absorb the throwable that is thrown from outer loop's loopcontroller (lcA)
//						                      // and instead throws it back out of its consumer action to let the outer catch.
//					});
//					// do stuff C
//				});
//			}
			catch (LoopControl th) {throw new RuntimeException(th);}
		}
		
//			public static void loop(ConsumerAction action) { // Does not stop after a number of iterations
//				Stopper st = () -> {throw new LoopBreak();};
//				try {while (true) {
//					action.accept(st);
//				}} catch (LoopBreak s) {}
//			}
		
		
	}
	
	
	// TODO: This is not the current (in terms of the one installed to the process) SecureityManager.
	// FIXME: Are these actually necessary?
	private static class SecMgr extends SecurityManager {
		public Class<?>[] getClassContext() {return super.getClassContext();}
	}
	private static final Supplier<SecMgr> currentSecurityManager = cachedReference(() ->  new SecMgr());
	public static java.lang.SecurityManager getSecurityManager() {return currentSecurityManager.get();}
	public static Class<?>[] getSecurityContext() {return currentSecurityManager.get().getClassContext();}
	
	
	
	public static enum KnownOS {
		Windows(0), Linux(1);
		// “Linux Is Not UniX” –> LINUX
		private KnownOS(int type) {this.typecode = type;}
		
		private final int typecode;
		public int typecode() {return typecode;}
		
		private static final java.util.Map<Integer, KnownOS> byTypecode = new java.util.HashMap<>();
		
		static {
			for (KnownOS each: KnownOS.values()) {
				byTypecode.put(each.typecode, each);
			}
		}
		
		public static KnownOS getCurrent() {return fn.getOS();}
	}
	
	
	/** Returns whether the underlying environment is Windows or a distribution in the
	  * Linux family (including unix) and null otherwise. */
	public static KnownOS getOS() {
		String key = System.getProperty("os.name");
		if (java.util.regex.Pattern.compile("windows").matcher(key).find()) return KnownOS.Windows;
		if (java.util.regex.Pattern.compile("unix").matcher(key).find()) return KnownOS.Linux;
		if (java.util.regex.Pattern.compile("linux").matcher(key).find()) return KnownOS.Linux;
		fn.log("Warning: getOS returned null because OS name is "+key);
		return null;
	}
	
	
	/** For testing the runtime time performances of implementations of time-critical tasks, such as:
	  * <li>Signed big-endian converting an int to array
	  * <li>Unsigned extending a byte to int */
	public static class Stopwatch {
		private int state = 0; // 0 -> 1 -> 2
		private long start = -1, end = -1;
		public Stopwatch() {}
		public void start() {
			if (state != 0) throw new IllegalStateException();
			start = System.nanoTime();
			state = 1;
		}
		public long stop() {
			if (state != 1) throw new IllegalStateException();
			end = System.nanoTime();
			state = 2;
			return end - start;
		}
		public long ns() {
			if (state != 2) throw new IllegalStateException();
			return end - start;
		}
		public double microseconds() {return ns() / ((double) fn.pow(10, 3));}
		public double milliseconds() {return ns() / ((double) fn.pow(10, 6));}
		public double seconds() {return ns() / ((double) fn.pow(10, 9));}
	}
	
	
	
	
	
	
	
	
	/**
	 * @deprecated This class is deprecated because of the non-necessity and
	 *             that it couldn't be used unless outside the default package
	 *             (this one) it is moved into class <code>fn</code> as a
	 *             <strong>public</strong> static inner class, or into a new
	 *             file named with a same name it has (in order to be able to
	 *             be made <strong>public</strong>)
	 *             <p>Use instead:<ul><li><code>
	 *             (new Thread(() -> {fn.sleep(delay);<pre>    // (action)</pre>
	 *             })).start();</code><pre></pre><li><code>(new Thread(()
	 *             -> {fn.sleep(delay); while (true) {synchronized (...) {if
	 *             (!keep) break;}<pre>    // (action)</pre>
	 *             fn.sleep(period);}})).start();</code></ul>
	 */
	@Deprecated private static class ScheduledTask {/*
	...
	 */}
	
}
