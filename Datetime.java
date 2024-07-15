import java.util.function.*;
import java.util.*;


public class Datetime implements Comparable<Datetime> {
	
	
	public static boolean warnDeprecated = true;
	
	// The below few methods are deprecated fallback methods until I finally get my *ss up to be done with my classes for handling Datetime issues.
	// date -> DD-MM-yyyy HH.mm.ss
	// date -> DD-MM-yyyy HH.mm.ss
	//      -> 01-01-0000 00:00:00
	private static void depr() {
		if (warnDeprecated) fn.log("Warning: Used a deprecated method of Datetime that is for returning date/time representation");
	}
	@Deprecated public static String getDate(int d, int m, int y) {
		depr(); return pad0leftBy2(d)+"-"+pad0leftBy2(m)+"-"+pad0left(y, 4);
	}
	@Deprecated public static String getDate() {
		int[] values = epochMillisToDMYHMS(nowSinceEpoch())[1-1];
		depr(); return getDate(values[1-1], values[2-1], values[3-1]);
	}
	
	
	@Deprecated public static String getTime(int h, int m, int s) {
		depr(); String del = ":";
		return pad0leftBy2(h)+del+pad0leftBy2(m)+del+pad0leftBy2(s);
	}
	@Deprecated public static String getTimeFilename(int h, int m, int s) {
		depr(); String del = ".";
		return pad0leftBy2(h)+del+pad0leftBy2(m)+del+pad0leftBy2(s);
	}
	@Deprecated public static String getTime(long millisSinceEpoch) {
		depr();
		int[] values = epochMillisToDMYHMS(millisSinceEpoch)[2-1];
		int hour = values[1-1], minute = values[2-1], second = values[3-1];
		return getTime(hour, minute, second);
	}
	@Deprecated public static String getTimeFilename(long millisSinceEpoch) {
		depr();
		int[] values = epochMillisToDMYHMS(millisSinceEpoch)[2-1];
		int hour = values[1-1], minute = values[2-1], second = values[3-1];
		return getTimeFilename(hour, minute, second);
	}
	@Deprecated public static String getTime() {
		depr(); return getTime(nowSinceEpoch());
	}
	@Deprecated public static String getTimeFilename() {
		depr(); return getTimeFilename(nowSinceEpoch());
	}
	
	
	@Deprecated public static String getDateTime(int day, int month, int year, int hour, int minute, int second) {
		depr(); return getDate(day, month, year)+" "+getTime(hour, minute, second);
	}
	@Deprecated public static String getDateTimeFilename(int day, int month, int year, int hour, int minute, int second) {
		depr(); return getDate(day, month, year)+" "+getTimeFilename(hour, minute, second);
	}
	@Deprecated public static String getDateTime() {
		depr(); return getDateTime(nowSinceEpoch());
	}
	@Deprecated public static String getDateTimeFilename() {
		depr(); return getDateTimeFilename(nowSinceEpoch());
	}
	@Deprecated public static String getDateTime(long millisSinceEpoch) {
		depr(); return getDateTime(millisSinceEpoch, false);
	}
	@Deprecated public static String getDateTimeFilename(long millisSinceEpoch) {
		depr(); return getDateTime(millisSinceEpoch, true);
	}
	@Deprecated private static String getDateTime(long millisSinceEpoch, boolean filename) {
		depr();
		int[][] values = epochMillisToDMYHMS(millisSinceEpoch);
		int day  = values[1-1][1-1], month  = values[1-1][2-1], year   = values[1-1][3-1],
		    hour = values[2-1][1-1], minute = values[2-1][2-1], second = values[2-1][3-1];
		if (filename) return getDateTimeFilename(day, month, year, hour, minute, second);
		else return getDateTime(day, month, year, hour, minute, second);
	}
	
	
	private static int[][] epochMillisToDMYHMS(long millisSinceEpoch) {
		java.util.Date javaUtilDate = new java.util.Date(millisSinceEpoch);
		
		java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+0"));
		cal.setTimeInMillis(millisSinceEpoch);
		final int
			year = cal.get(Calendar.YEAR),
			month = cal.get(Calendar.MONTH) + 1, // From 0-based
			day = cal.get(Calendar.DAY_OF_MONTH),
			hour = cal.get(Calendar.HOUR_OF_DAY),
			minute = cal.get(Calendar.MINUTE),
			second = cal.get(Calendar.SECOND);
		
		return new int[][] {
			{day, month, year},
			{hour, minute, second}
		};
	}
	private static long nowSinceEpoch() {return System.currentTimeMillis();}
	
	
	
	
	
	private static class Division {
		
		public static class division_int_result {
			public final int div, rem;
			public division_int_result(int d, int r) {
				div = d;
				rem = r;
			}
			public String toString() {return div+" ("+rem+")";}
		}
		private static class division_fp_result {
			public final int div;
			public final double rem;
			public division_fp_result(int d, double r) {
				div = d;
				rem = r;
			}
			public String toString() {return div+" ("+rem+")";}
		}
		
		
		public static division_int_result div(int num, int by) {
			return div2(num, by);
		}
		// The by param will be like 24, 60, 12 etc. and not a fp num unlike num that
		// may be (3 + 0.38) seconds, hence the parameter by will not need to be a fp.
		public static division_fp_result div(double num, int by) {
			return div2(num, by);
		}
		
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 ->  0, -1 (not -1, 4)
		// -6, 5 -> -1, -1 (not -2, 4)
		public static division_int_result div1(int num, int by) {
			if (by < 0) throw new IllegalArgumentException("by < 0");
			int div = num / by;
			int rem = num % by;
			fn.assertAnyway(div*by + rem == num, div+"*"+by+" + "+rem+" != "+num);
			return new division_int_result(div, rem);
		}
		public static division_fp_result div1(double num, int by) {
			if (by < 0) throw new IllegalArgumentException("by < 0");
			int div = ((int) num) / by;
			double rem = num % by;
			
			fn.assertAnyway(Math.abs((div*by + rem) - num) < 0x1p-30 , div+"*"+by+" + "+rem+" != "+num);
			return new division_fp_result(div, rem);
		}
		
		//  1, 5 ->  0, 1
		//  7, 5 ->  1, 2
		// -1, 5 -> -1, 4 (not 0, -1)
		// -6, 5 -> -2, 4 (not -1, -1)
		public static division_int_result div2(int num, int by) {
			if (by < 0) throw new IllegalArgumentException("by < 0");
			int div = num / by;
			if (num < 0) div += -1;
			int rem = fn.modulo(num, by);
			fn.assertAnyway(div*by + rem == num, div+"*"+by+" + "+rem+" != "+num);
			return new division_int_result(div, rem);
		}
		public static division_fp_result div2(double num, int by) {
			if (by < 0) throw new IllegalArgumentException("by < 0");
			int div = ((int) num) / by;
			double rem = fn.modulo(num, by);
			
			fn.assertAnyway(Math.abs((div*by + rem) - num) < 0x1p-30 , div+"*"+by+" + "+rem+" != "+num);
			return new division_fp_result(div, rem);
		}
		
	}
	
	
	
//	public Datetime(Datetime clone) { // Copy constructor
//		this(clone.date, clone.time);
//	}
	
	public Datetime(Date date, Time time) { // Generic constructor
		this.date = date;
		this.time = time;
	}
	
	public static Datetime now(int plusTimezoneH) {
		return now(plusTimezoneH, 0);
	}
	public static Datetime now(int plusTimezoneH, int plusTimezoneM) {
		return new Datetime((new java.util.Date()).getTime() + plusTimezoneH*60*60*1000 + plusTimezoneM*60*1000);
	}
	
	public Datetime(long millisSinceEpoch) {
		int[][] values = epochMillisToDMYHMS(millisSinceEpoch);
		int day  = values[1-1][1-1], month  = values[1-1][2-1], year   = values[1-1][3-1],
		    hour = values[2-1][1-1], minute = values[2-1][2-1], second = values[2-1][3-1];
		this.date = new Date(day, month, year);
		this.time = new Time(hour, minute, second);
	}
	
	public long toMillisSinceEpoch() {
		// TODO: Implement a method to calculate the millis since epoch instead of depending such weird APIs!!
		java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+0"));
		cal.set(date.year, date.month-1, date.day, time.hour, time.minute, time.second);
		cal.add(java.util.Calendar.MILLISECOND, (int) (time.second_frag * 1_000_000));
		return cal.getTimeInMillis();
	}
	
	public static String pad0leftBy2(int numAsDecimal) {return pad0left(numAsDecimal, 2);}
	public static String pad0left(int numAsDecimal, int maxPadAmt) {
		if (numAsDecimal < 0) throw new IllegalArgumentException("Negative number in pad0left");
		int len = (""+numAsDecimal).length();
		if (len > maxPadAmt) fn.log("Number whose decimal form is longer than maxPadAmt in pad0left"); // Year should be able to exceed 9999 so don't throw exception.
		return str.multiply("0", maxPadAmt - len) + numAsDecimal;
	}
	
	public final int compareTo(Datetime that) {
		int diff;
		diff = this.date.compareTo(that.date);
		if (diff != 0) return diff;
		diff = this.time.compareTo(that.time);
		if (diff != 0) return diff;
		return 0;
	}
	
	protected boolean acceptAsEqual(Datetime that) {
		return this.compareTo(that) == 0;
	}
	public final boolean equals(Datetime that) {return this.acceptAsEqual(that) && that.acceptAsEqual(this);}
	public final boolean equals(Object that) {return (that instanceof Datetime) && equals((Datetime) that);}
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (int value: new int[] {date.hashCode(), time.hashCode()})
			result = result*prime + value;
		return result;
	}
	
	
	
	
// ---------------------------------------------------------------- Fields ----------------------------------------------------------------
	
	public final Date date;
	public final Time time;
	public String toString() {return date.toString()+" "+time.toString();}
	public String toStringFileName() {return date.toString()+" "+time.toStringFileName();}
	
// ---------------------------------------------------------------- ------ ----------------------------------------------------------------
	
	
	
// ---------------------------------------------------------------- Date ----------------------------------------------------------------
	
	/** Warning: Does not have a time zone feature. */
	public static class Date implements Comparable<Date> {
		public final int day, month, year;
		
		public Date(int day, int month, int year) {
//			if (year >= 24 || year < 0) throw new IllegalArgumentException();
			if (month > 12 || month < 1) throw new IllegalArgumentException(toString(day, month, year)+" is not a valid date.");
			if (day > 31 || month < 1) throw new IllegalArgumentException();
			this.year = year;
			this.month = month;
			this.day = day;
		}
		
		public static String pad0leftBy2(int numAsDecimal) {return pad0left(numAsDecimal, 2);}
		public static String pad0left(int numAsDecimal, int maxPadAmt) {
			if (numAsDecimal < 0) throw new IllegalArgumentException("Negative number in pad0left");
			int len = (""+numAsDecimal).length();
			if (len > maxPadAmt) fn.log("Number whose decimal form is longer than maxPadAmt in pad0left"); // Year should be able to exceed 9999 so don't throw exception.
			return str.multiply("0", maxPadAmt - len) + numAsDecimal;
		}
		
		public static String toString(int day, int month, int year) {
			return pad0leftBy2(day)+"-"+pad0leftBy2(month)+"-"+pad0left(year, 4);
		}
		
		public String toString() {
			return toString(day, month, year);
		}
		
		public Date addMonths(int months) {
			if (months < 0) throw new IllegalArgumentException();
			int d = this.day;
			int m = this.month;
			int y = this.year;
			m += months;
			y += m / 12;
			m += m % 12;
			return new Date(d, m, y);
		}
		private static final Supplier<Map<Integer, Integer>> monthDays = fn.cachedReference(() -> {
			Map<Integer, Integer> map = new HashMap<>();
			for (int i: fn.range(1, 7, 2)) map.put(i, 31);
			for (int i: fn.range(2, 6, 2)) map.put(i, 30);
			for (int i: fn.range(8, 12, 2)) map.put(i, 31);
			for (int i: fn.range(9, 11, 2)) map.put(i, 30);
			map.remove(2); // Sometimes 29 otherwise 28
			return map;
		});
		public static int monthDay(int month) {
			if (month == 2) return -1;
			else return monthDays.get().get(month);
		}
		public static int monthDay(int month, int year) {
			if (month == 2) return isLeap(year) ? 29 : 28;
			else return monthDay(month);
		}
		public static boolean isLeap(int year) {
			boolean m4 = (year % 4 == 0), m100 = (year % 100 == 0), m400 = (year % 400 == 0);
			return m4 && (!m100 || m400);
//			if (m4) {if (m100) {if (m400) return true; else return false;} else return true;} else return false;
//			return m4 ? (m100 ? (m400 ? true : false) : true) : false;
		}
		
		// FIXME!!!
		//  TimeAmount
		//     vvv
		public int subtractDate(Date that) {
			int d = day, m = month, y = year; // To make 0
			
			if (this.year == that.year) y = 0;
			int diff = 0;
			if (this.compareTo(that) < 0) throw new IllegalArgumentException(); // Negative time amount
			
			if (true) throw new UnsupportedOperationException(); // FIXME!!!!!!!!
			
			// FIXME: This should take like O(log(n)) times and not n or roughly n/30 times!!!
		A:	{
				for (int i: fn.range(500000)) {
					if (d > monthDay(m, y)) {
						Division.division_int_result res = Division.div(d, monthDay(m, y));
					}
					if (false) break A;
				}
				throw new Error("NOOOOOOOO!!!!! Too many iterations in one of Datetime.Date methods!!!");
			}
			
//			return new TimeAmount(diff, null);
			return diff;
		}
		
		public Date addDays(int days) {
			int d = this.day;
			int m = this.month;
			int y = this.year;
			
			
			d += days;
			
			if (true) throw new UnsupportedOperationException();
			
			// FIXME: This should take like O(log(n)) times and not n or roughly n/30 times!!!
		A:	{
				for (int i: fn.range(500000)) {
					if (d > 1000) {
						Division.division_int_result res = Division.div(d, monthDay(m, y));
					}
					if (false) break A;
				}
				throw new Error("NOOOOOOOO!!!!! Too many iterations in one of Datetime.Date methods!!!");
			}
			
			return new Date(d, m, y);
		}
		
		
		public int compareTo(Date that) {
			if (this.year > that.year) return 1;
			if (this.year < that.year) return -1;
			if (this.month > that.month) return 1;
			if (this.month < that.month) return -1;
			
			// DAMN THIS COSTED ME LIKE 10 POINTS IN BBM104 2023 FALL PA1 TO LEAVE THE LINES COMPARING THE DAY AT THE FIRST!!!
			if (this.day > that.day) return 1;
			if (this.day < that.day) return -1;
			return 0;
		}
	}
	
	
	public static class Date_obsolete implements Comparable<Date_obsolete> {
		private static final String /*acceptedDelimiter=".", */preferredDelimiter="-";
		private static final Supplier<list<String>> months = fn.cachedReference(() -> new linklist<>(
			"January", "February", "March", "April", "May", "June", "July", "August", "Setember", "October", "November", "December"
		));
		private static final Supplier<list<Integer>> monthsHaving31days = fn.cachedReference(() -> new linklist<>(1, 3, 5, 7, 8, 10, 12));
		public final int day, month, year;
//		public int day() {return day;}
//		public int month() {return month;}
//		public int year() {return year;}
		
		
		public static void validateInput(int day, int month, int year, Supplier<String> rawInput) {
			if (day<=0 || month<=0 || year<=0) throw new IllegalArgumentException("Date values must be positive: "+rawInput.get());
			if (day > 31) throw new IllegalArgumentException("Day value can\'t be greater than 31: "+rawInput.get());
			if (month > 12) throw new IllegalArgumentException("Month value can\'t be greater than 12: "+rawInput.get());
			if (day == 31) if (!monthsHaving31days.get().contains(month))
				throw new IllegalArgumentException(months.get().g(month)+" contains less than 31 days: "+rawInput.get());
			if (month == 2) {
				if (day > 29) throw new IllegalArgumentException(months.get().g(month)+" contains less than 30 days: "+rawInput.get());
				if (day == 29) if (!( (year%4==0 && year%100!=0) || year%400==0))
					throw new IllegalArgumentException(months.get().g(month)+" of "+year+" does not contain "+day+" days: "+rawInput.get());
			}
		}
		
		public Date_obsolete(int day, int month, int year) {
			validateInput(day, month, year, () -> String.format("(%s)"+preferredDelimiter+"(%s)"+preferredDelimiter+"(%s)", day, month, year));
			this.day = day;
			this.month = month;
			this.year = year;
		}
		
		@Deprecated public Date_obsolete(String input) {
			list<String> values = str.split(input, preferredDelimiter);
			if (values.size() != 3) throw new IllegalArgumentException("Date input must consist of 3 values separated with \u201c"+preferredDelimiter+"\u201d: \u201c"+input+"\u201d");
			{
				int day, month, year;
				try {
					day   = Integer.valueOf(values.g(1));
					month = Integer.valueOf(values.g(2));
					year  = Integer.valueOf(values.g(3));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("At least one of the values of a date input is not valid: "+"\u201c"+input+"\u201d", e);
				}
				this.day   = day;
				this.month = month;
				this.year  = year;
			}
		}
		
		public String toString() {return pad0leftBy2(day) + preferredDelimiter + pad0leftBy2(month) + preferredDelimiter + pad0left(year, 4);}
		
		public final int compareTo(Date_obsolete that) {
			if (this.year < that.year) return -1;
			if (this.year > that.year) return +1;
			if (this.month < that.month) return -1;
			if (this.month > that.month) return +1;
			if (this.day < that.day) return -1;
			if (this.day > that.day) return +1;
			return 0;
		}
		
		protected boolean acceptAsEqual(Date_obsolete that) {
			return this.compareTo(that) == 0;
		}
		public final boolean equals(Date_obsolete that) {return this.acceptAsEqual(that) && that.acceptAsEqual(this);}
		public final boolean equals(Object that) {return (that instanceof Date_obsolete) && equals((Date_obsolete) that);}
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			for (int value: new int[] {day, month, year})
				result = result*prime + value;
			return result;
		}
		
		@Deprecated public static class DateRange {
			private final Date_obsolete start, end;
			public DateRange(Date_obsolete start, Date_obsolete end) {
				this.start = start;
				this.end = end;
				if (start.compareTo(end) == +1) throw new IllegalArgumentException(this+" specifies a negative amount of time.");
			}
			public DateRange(String start, String end) {
				this(new Date_obsolete(start), new Date_obsolete(end));
			}
			public boolean contains(Date_obsolete date) {return date.compareTo(start)>=0 && date.compareTo(end)<=0;}
			public boolean contains(DateRange that) {return contains(that.start) && contains(that.end);}
			public boolean overlaps(DateRange that) {
				return
					this.contains(that.start) || this.contains(that.end) ||
					that.contains(this.start) || that.contains(this.end);
			}
			public Date_obsolete getFrom() {return start;}
			public Date_obsolete getTo() {return end;}
			public String toString() {return start + " \u2013 " + end;}
		}
	}
	
// ---------------------------------------------------------------- ---- ----------------------------------------------------------------
	
	
	
	
// ---------------------------------------------------------------- Time ----------------------------------------------------------------
	
	public static class Time implements Comparable<Time> {
		public final byte hour, minute, second;
		public final double second_frag; // in [0, 1)
		
		public Time(int hour, int minute, int second) {
			this(hour, minute, second, Double.NaN);
		}
		
		public Time(int hour, int minute, int second, double sec_frag) {
			list<String> errors = new linklist<>();
			if (!(hour >= 0 && hour <= 23)) errors.add("Hour value can\'t be out of 0\u201323");
			if (!(minute >= 0 && minute <= 59)) errors.add("Minute value can\'t be out of 0\u201359");
			if (!(second >= 0 && second <= 59)) errors.add("Second value can\'t be out of 0\u201359");
			if (!errors.isEmpty()) throw new IllegalArgumentException(
				"Input format is not valid:\n"+str.join(new linklist<>((s) -> " \u2022 "+s, errors), "\n")
			);
			
			byte sec = (byte) second;
			if (!Double.isNaN(sec_frag)) {
				sec = (byte) (int) (sec + sec_frag);
				sec_frag = sec_frag % 1;
			}
			
			this.hour = (byte) hour;
			this.minute = (byte) minute;
			this.second = sec;
			this.second_frag = sec_frag % 1;
		}
		
		public final int compareTo(Time that) {
			if (this.hour < that.hour) return -1;
			if (this.hour > that.hour) return +1;
			if (this.minute < that.minute) return -1;
			if (this.minute > that.minute) return +1;
			if (this.second < that.second) return -1;
			if (this.second > that.second) return +1;
			if (this.second_frag < that.second_frag) return -1;
			if (this.second_frag > that.second_frag) return +1;
			return 0;
		}
		
		protected boolean acceptAsEqual(Time that) {
			return this.compareTo(that) == 0;
		}
		public final boolean equals(Time that) {return this.acceptAsEqual(that) && that.acceptAsEqual(this);}
		public final boolean equals(Object that) {return (that instanceof Time) && equals((Time) that);}
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			for (int value: new byte[] {hour, minute, second})
				result = result*prime + value;
			return result;
		}
		
		
		public static String pad0leftBy2(int numAsDecimal) {return pad0left(numAsDecimal, 2);}
		public static String pad0left(int numAsDecimal, int maxPadAmt) {
			if (numAsDecimal < 0) throw new IllegalArgumentException("Negative number in pad0left");
			int len = (""+numAsDecimal).length();
			if (len > maxPadAmt) fn.log("Number whose decimal form is longer than maxPadAmt in pad0left"); // Year should be able to exceed 9999 so don't throw exception.
			return str.multiply("0", maxPadAmt - len) + numAsDecimal;
		}
		
		public String toString() {
			return toString(":");
		}
		public String toStringFileName() {
			return toString(".");
		}
		public String toString(String delimiter) {
			if (Double.isNaN(second_frag))
				return String.format("%s"+delimiter+"%s"+delimiter+"%s", pad0leftBy2(hour), pad0leftBy2(minute), pad0leftBy2(second));
			else
				// TODO: Fix this part!
				return String.format("%s"+delimiter+"%s"+delimiter+"%s", pad0leftBy2(hour), pad0leftBy2(minute), pad0leftBy2(second));

		}
		
//		public Time add(Time that) {
//			return this.addHours(that.hour).addMinutes(that.minute).addSeconds(that.second + that.second_frag);
//		}
//		public Time add(Time that) {
//			return add(that.hour, that.minute, that.second, that.second_frag);
//		}
//		public Time add(int hours, int minutes, int seconds, double second_frag) {
//			int hour = this.hour + hours;
//			int min = this.minute + minutes;
//			int sec = this.second + seconds;
//			double secf = this.second_frag + second_frag;
//			
//			if (!Double.isNaN(secf)) {
//				sec += (int) secf;
//				secf = secf % 1;
//			}
//			
//			min += sec / 60;
//			sec = sec % 60;
//			
//			hour += min / 60;
//			min = min % 60;
//			
//			hour = hour % 24;
//			return new Time(hour, min, sec, secf);
//		}
//		
		public static boolean checkHour(int h) {return h >= 0 && h < 24;}
//		public static boolean checkMin(int m) {return m >= 0 && m < 60;}
//		public static boolean checkSecond(int s) {return s >= 0 && s < 60;}
//		public static boolean checkSecondFrag(double sf) {
//			return (Math.abs(sf - 0) < 0x1p-30) || (Math.abs(sf - 1) < 0x1p-30) || (sf >= 0 && sf < 1);
//		}
		
		// TODO: Maybe try passing the modified values from the least significant to the most
		// significant (to a method returning 2 items) for whether they overflow and need to
		// spill out to the greater or starving and need to take from the greater!!!
		
		public Time addHours(int hours) {
//			if (hours < 0) throw new IllegalArgumentException();
			int hour = this.hour + hours;
			if (!checkHour(hour)) return null;
			return new Time(hour, minute, second);
		}
		
		public Time addMinutes(int mins) {
//			if (mins < 0) throw new IllegalArgumentException();
			int hour = this.hour;
			int min = this.minute + mins;
			{
				Division.division_int_result res = Division.div(min, 60);
				min = res.rem;
				hour += res.div;
			}
			if (!checkHour(hour)) return null;
			return new Time(hour, min, second);
		}
		
		public Time addSeconds(double secs) {
			return addSeconds((int) secs, secs % 1);
		}
		public Time addSeconds(int secs) {
			return addSeconds(secs, 0);
		}
		
		public Time addSeconds(int secs, double sec_frag) {
//			if (secs < 0) throw new IllegalArgumentException();
			int hour = this.hour;
			int min = this.minute;
			int sec = this.second + secs;
			
			{
				Division.division_int_result res = Division.div(sec, 60);
				sec = res.rem;
				min += res.div;
			} {
				Division.division_int_result res = Division.div(min, 60);
				min = res.rem;
				hour += res.div;
			}
			
			if (!checkHour(hour)) return null;
			return new Time(hour, min, sec);
		}
		
	}
	
// ---------------------------------------------------------------- ---- ----------------------------------------------------------------
	
	
	
// ---------------------------------------------------------------- Time Amount ----------------------------------------------------------------
	
	public static class TimeAmount implements Comparable<TimeAmount> {
		public final int/* years, months,*/ days;
		public final Time hms;
		
//		public TimeAmount(double secs) {
//			
//		}
		
		public TimeAmount(int secs) {
			Datetime.Division.division_int_result divr = Division.div(secs, 60*60*24);
			hms = (new Time(0, 0, 0)).addSeconds(divr.rem);
			days = divr.div;
		}
		
		public TimeAmount(Time time) {
			this(0, time);
		}
		public TimeAmount(int days, Time time) {
			this.days = days;
			this.hms = time;
		}
		
		public int compareTo(TimeAmount that) {
			int a;
			a = ((Integer) this.days).compareTo(that.days);
			if (a != 0) return a;
			a = this.hms.compareTo(that.hms);
			if (a != 0) return a;
			return 0;
		}
		public boolean equals(TimeAmount that) {
//			return this.days == that.days && this.hms.equals(that.hms);
			return this.compareTo(that) == 0;
		}
		public boolean equals(Object that) {
			return that instanceof TimeAmount && equals((TimeAmount) that);
		}
		
		public TimeAmount add(TimeAmount that) {
			// FIXME: Handle the days and the overflow of the time value!!!!
			Time combined;
			combined = this.hms
				.addHours(that.hms.hour)
				.addMinutes(that.hms.minute)
				.addSeconds(that.hms.second + that.hms.second_frag);
			return new TimeAmount(combined);
		}
		
		public int secs() {return hms.hour*60*60 + hms.minute*60 + hms.second;}
		public double secs1() {return hms.hour*60*60 + hms.minute*60 + hms.second + hms.second_frag;}
		
		public String toString() {
			return String.format("%d hours, %d minutes, %d seconds", hms.hour, ""+hms.minute, ""+hms.second);
		}
		
	}
//	public static class TimeAmount_obsolete implements Comparable<TimeAmount_obsolete> {
//		
//		public final int hour;
//		public final int min;
//		public final int sec;
//		public final double sec_frag;
//		
//		
//		public TimeAmount_obsolete(int hour, int min, double sec) {
//			
////			{
////				int c;
////				c = (int) (sec / 60);
////				sec = sec % 60;
////				
////				min = c + min;
////				c = min / 60;
////				min = min % 60;
////				
////				hour = c + hour;
////			}
//			
//			this.hour = hour;
//			this.min = min;
//			this.sec = (int) sec;
//			this.sec_frag = sec % 1;
//		}
//		
//		public int compareTo(TimeAmount_obsolete that) {
//			if (this.hour > that.hour) return +1;
//			if (this.hour < that.hour) return -1;
//			if (this.min > that.min) return +1;
//			if (this.min < that.min) return -1;
//			if (this.sec > that.sec) return +1;
//			if (this.sec < that.sec) return -1;
//			return 0;
//		}
//		
//		public TimeAmount_obsolete add(TimeAmount_obsolete that) {
//			int h, m; double s;
//			
//			s = sec + that.sec;
//			m = min + that.min;
//			h = hour + that.hour;
//			
//			return new TimeAmount_obsolete(h, m, s);
//		}
//		
//		public TimeAmount_obsolete(Double secs) {
//			TimeAmount_obsolete t = new TimeAmount_obsolete(0, 0, secs);
//			this.hour = t.hour;
//			this.min = t.min;
//			this.sec = t.sec;
//		}
//		
//		public double secs() {return hour*60*60 + min*60 + sec;}
//		
//		public String toString() {
//			return String.format("%s hours, %s minutes, %s seconds", ""+hour, ""+min, ""+sec);
//		//	return String.format("%s:%s:%s", expand2(hour), expand2(minute), expand2(second));
//		}
//	}
	
// ---------------------------------------------------------------- ----------- ----------------------------------------------------------------
	
}
