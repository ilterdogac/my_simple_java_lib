import java.util.function.*;
import java.util.*;


public class Datetime implements Comparable<Datetime> {
	
	
	public static boolean warnDeprecated = true;
	/** Controls whether to accept the overflowing amounts of hour, minute, second and fractions in Time (not TimeAmount). */
	public static final boolean config_time_allowOverflowing = false;
	/** Controls whether to accept the overflowing amounts of hour, minute, second and fractions in TimeAmount. */
	public static final boolean config_timeamount_allowOverflowing = true;
	// TODO: Remove this: It really doesn't sense to let sec_frag to overflow into seconds without litting seconds or
	//  bigger to overflow into the biggers.
	public static final boolean config_allowSecFragToOverflowIfExceed1 = false;
	
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
	
	
	
	
	
	public Datetime(Date date, Time time) { // Generic constructor
		this.date = date;
		this.time = time;
	}
	
	public static Datetime now(int plusTimezoneH) {
		return now(plusTimezoneH, 0);
	}
	public static Datetime now(int plusTimezoneH, int plusTimezoneM) {
		long millisSinceEpoch = (new java.util.Date()).getTime();
		return new Datetime(millisSinceEpoch + plusTimezoneH*60*60*1000 + plusTimezoneM*60*1000);
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
		cal.add(java.util.Calendar.MILLISECOND, (int) (time.second_frag*1_000_000 + 0.5));
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
//			if (year >= ... || year < 0) throw new IllegalArgumentException();
			if (year < 0) throw new UnsupportedOperationException(
				"Negative years are not yet implemented (not sure how to display them properly in " +
					"dd-mm-yyyy format besides using “BC”)"
			);
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
						fn.Division.division_int_result res = fn.Division.div(d, monthDay(m, y));
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
						fn.Division.division_int_result res = fn.Division.div(d, monthDay(m, y));
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
	
	/*@Deprecated public static class Date_obsolete implements Comparable<Date_obsolete> {
		private static final String *//*acceptedDelimiter=".", *//*preferredDelimiter="-";
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
	}*/
	
// ---------------------------------------------------------------- ---- ----------------------------------------------------------------
	
	
	
	
// ---------------------------------------------------------------- Time ----------------------------------------------------------------
	
	// TODO: Bring the functionality to support timezones!
	public static class Time implements Comparable<Time> {
		
		public final byte hour, minute, second;
		// TODO: Make this null as this is public so users wouldn't need to predict that they need to deal with NaN
		//  as well (dealing with null is already enough)
		/** NaN if precise up to seconds */
		public final double second_frag; // in [0, 1)
		
		public Time(int hour, int minute, int second) {
			this(hour, minute, second, null);
		}
		
		
		
		/** Overflows the hours, minutes, second and second-fract values in a day, and returns how many days the equaling
		 *  value (which is shorter than a day) is less than the value the passed parameters ar modified into.
		 *  For example: If passed 23, 90, 0, 0.0; the params become 1, 30, 0, 0.0 and returns 1 (1d 1h 30m 0+0.0s).
		 *  
		 *  @param inout_secondFracts Optional and may be null. */
		public static int overUnderFlowHMS(
			fn.Pointer<Integer> inout_hours, fn.Pointer<Integer> inout_minutes, fn.Pointer<Integer> inout_seconds, fn.Pointer<Double> inout_secondFracts
		) {
			
			list<fn.Tuple2<fn.Union2<fn.Pointer<Integer>, fn.Pointer<Double>>, Integer>> iterable_LE = list.listWrapper.getArList();
			if (inout_secondFracts != null)
				iterable_LE.add(fn.tuple(fn.union(null, inout_secondFracts), 1));
			iterable_LE.add(fn.tuple(fn.union(inout_seconds, null), 60));
			iterable_LE.add(fn.tuple(fn.union(inout_minutes, null), 60));
			iterable_LE.add(fn.tuple(fn.union(inout_hours, null), 24));
			
			
			int lastOverflow = fn.overUnderflowValues(iterable_LE);
			
			return lastOverflow;
			
		}
		/** Overflows the hours, minutes, second and second-fract values in a day, and returns how many days the equaling
		 *  value (which is shorter than a day) is less than the value the passed parameters ar modified into.
		 *  For example: If passed 23, 90, 0, 0.0; the params become 1, 30, 0, 0.0 and returns 1 (1d 1h 30m 0+0.0s). */
		public static int overUnderFlowHMS(
			fn.Pointer<Integer> inout_hours, fn.Pointer<Integer> inout_minutes, fn.Pointer<Integer> inout_seconds
		) {
			return overUnderFlowHMS(inout_hours, inout_minutes, inout_seconds, null);
		}
		
		
		/** Normally does NOT accept the overflowing amounts of hour, minute, second and fractions.
		 *  @param sec_frag Optional and may be null. */
		public Time(int hour, int minute, int second, Double sec_frag) {
			final int hour_o = hour, minute_o = minute, second_o = second;
			final Double sec_frag_o = sec_frag;
			
			final int lastOverflow;
			
			if (config_time_allowOverflowing) { // Fix the overflowing amounts
				fn.Pointer<Integer>
					h = fn.wrap(hour),
					m = fn.wrap(minute),
					s = fn.wrap(second);
				fn.Pointer<Double> sf = sec_frag == null ? null : fn.wrap(sec_frag);
				lastOverflow = overUnderFlowHMS(h, m, s, sf);
				hour = h.value;
				minute = m.value;
				second = s.value;
				if (sf != null) sec_frag = sf.value; // Replaced with null if already is null
				
				try {
					// Now must be within the constraints!
					checkHMSConstraints(hour, minute, second, sec_frag);
				} catch (Exception | Error e) {
					throw new AssertionError(e);
				}
			} else {
				// Nothing is overflowing (except sec_frag, which sec will not overflow so can't be longer than
				// 24h or shorter than 0s)
				lastOverflow = 0;
				
				// If sec_frags is not null...
				if (sec_frag != null) {
					// Overflow sec_frags into seconds if configured so
					if (config_allowSecFragToOverflowIfExceed1) {
						fn.Pointer<Integer> s = fn.wrap(second);
						fn.Pointer<Double> sf = fn.wrap(sec_frag);
						int secondOverflow;
						{
							list<fn.Tuple2<fn.Union2<fn.Pointer<Integer>, fn.Pointer<Double>>, Integer>> iterable_LE = list.listWrapper.getArList();
							iterable_LE.add(fn.tuple(fn.union(null, sf), 1));
							iterable_LE.add(fn.tuple(fn.union(s, null), 60));
							// 00:00:(59 + 1.1) -> 00:00:60.1 -> Error! (not 00:01:00.1)
							secondOverflow = fn.overUnderflowValues(iterable_LE);
						}
						sec_frag = sf.value;
						// Un-overflow the second so if it has overflown then it becomes back exceeding its max
						second = s.value + 60*secondOverflow;
						
						// Seconds may be >=60 because of the overflowing sec_frag (but aren't overflown
						// into minutes as it is not config-set to be done so)
						// Therefore checkHMSConstraints(hour, minute, second, sec_frag) is done at the end
					}
				}
				
				checkHMSConstraints(hour, minute, second, sec_frag);
			}
			
			// Hours don't have anything to overflow into so don't let a too big value
			if (lastOverflow > 0)
				throw new IllegalArgumentException(
					str.format(
						"Value entered is too late to be a time moment in a day (>= 24h): %s:%s:%s",
						/*pad0leftBy2(*/hour_o/*)*/, /*pad0leftBy2(*/minute_o/*)*/, /*pad0leftBy2(*/second_o/*)*/
					)
				);
			if (lastOverflow < 0)
				throw new IllegalArgumentException(
					str.format(
						"Value entered is too early to be a time moment in a day (< 0 seconds): %s:%s:%s",
						/*pad0leftBy2(*/hour_o/*)*/, /*pad0leftBy2(*/minute_o/*)*/, /*pad0leftBy2(*/second_o/*)*/
					)
				);
			
			
			this.hour = fn.toByte(hour);
			this.minute = fn.toByte(minute);
			this.second = fn.toByte(second);
			this.second_frag = sec_frag != null ? sec_frag : Double.NaN;
		}
		
		
		
		
		/** Checks if all of the hour, minute and second values of a time value are within their limits
		 *  (<24, <60, <60, <1).
		 *  @param sec_frag Optional and may be null. */
		public static void checkHMSConstraints(int hour, int minute, int second, Double sec_frag) throws IllegalArgumentException {
			double tiny = 1.0 / (2 << 20);
			list<String> errors = new linklist<>();
			if (!(hour >= 0 && hour < 24)) errors.add("Hour value can't be out of 0–23: "+hour);
			if (!(minute >= 0 && minute < 60)) errors.add("Minute value can't be out of 0–59: "+minute);
			if (!(second >= 0 && second < 60)) errors.add("Second value can't be out of 0–59: "+second);
			if (sec_frag != null) {
				if (!(sec_frag >= 0-tiny && sec_frag < 1+tiny)) errors.add("Second fragment value can't be out of [0.0, 1.0): "+sec_frag);
			}
			if (!errors.isEmpty()) throw new IllegalArgumentException(
				"Input format is not valid:"+str.platformLineSeparator()+str.join(
					new linklist<>((s) -> " \u2022 "+s, errors), str.platformLineSeparator()
				)
			);
		}
		public final int compareTo(Time that) {
			if (this.hour < that.hour) return -1;
			if (this.hour > that.hour) return +1;
			if (this.minute < that.minute) return -1;
			if (this.minute > that.minute) return +1;
			if (this.second < that.second) return -1;
			if (this.second > that.second) return +1;
			Double sf1 = this.second_frag, sf2 = that.second_frag;
			if (sf1.isNaN()) sf1 = null;
			if (sf2.isNaN()) sf2 = null;
			if (sf1 == null && sf2 == null) return 0;
			if (sf1 == null) return ((Double) 0.0).compareTo(sf2);
			if (sf2 == null) return sf1.compareTo(0.0);
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
				return String.format(
					"%s"+delimiter+"%s"+delimiter+"%s",
					pad0leftBy2(hour),
					pad0leftBy2(minute),
					pad0leftBy2(second)
				);
			else
				return String.format(
					"%s"+delimiter+"%s"+delimiter+"%s",
					pad0leftBy2(hour),
					pad0leftBy2(minute),
					str.round(second+second_frag, 3, true)
				);
			
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
		
		// TODO: Maybe try passing the modified values from the least significant to the most
		//  significant (to a method returning 2 items) for whether they overflow and need to
		//  spill out to the greater or starving and need to take from the greater!!!
		
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
				fn.Division.division_int_result res = fn.Division.div(min, 60);
				min = res.rem;
				hour += res.quot;
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
		
		// FIXME
		public Time addSeconds(int secs, double sec_frag) {
//			if (secs < 0) throw new IllegalArgumentException();
			int hour = this.hour;
			int min = this.minute;
			int sec = this.second + secs;
			
			{
				fn.Division.division_int_result res = fn.Division.div(sec, 60);
				sec = res.rem;
				min += res.quot;
			} {
				fn.Division.division_int_result res = fn.Division.div(min, 60);
				min = res.rem;
				hour += res.quot;
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
			fn.Division.division_int_result divr = fn.Division.div(secs, 60*60*24);
			hms = (new Time(0, 0, 0)).addSeconds(divr.rem);
			days = divr.quot;
		}
		
		
		public TimeAmount(Time time) {
			this.days = 0;
			this.hms = time;
		}
		public TimeAmount(int days, Time time) {
			this.days = days;
			this.hms = time;
		}
		
		/** Normally accepts the overflowing amounts of hour, minute, second and fractions.
		 *  @param secFractions Optional and may be null. */
		public TimeAmount(int days, int hours, int minutes, int seconds, Double secFractions) {
			Time hms;
			int daysPlus;
			if (config_timeamount_allowOverflowing) {
				fn.Pointer<Integer>
					h = fn.wrap(hours),
					m = fn.wrap(minutes),
					s = fn.wrap(seconds);
				fn.Pointer<Double> sf = secFractions == null ? null : fn.wrap(secFractions);
				
				daysPlus = Time.overUnderFlowHMS(h, m, s, sf);
				
				if (days+daysPlus < 0) throw new IllegalArgumentException(
					str.format(
						"TimeAmount can't be negative: "+"%sd %sh %sm (%s + %s)s",
						days, hours, minutes, seconds, secFractions
					)
				);
				hours = h.value;
				minutes = m.value;
				seconds = s.value;
				if (sf != null) secFractions = sf.value;
			}
			hms = new Time(hours, minutes, seconds, secFractions);
			this.days = days + daysPlus;
			this.hms = hms;
		}
		
		/** Normally accepts the overflowing amounts of hour, minute, second and fractions. */
		public TimeAmount(int days, int hours, int minutes, int seconds) {
			this(days, hours, minutes, seconds, null);
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
				.addSeconds(that.hms.second)
				.addSeconds(0, that.hms.second_frag);
			return new TimeAmount(combined);
		}
		
		public int secs_int() {return hms.hour*60*60 + hms.minute*60 + hms.second;}
		public double secs() {return hms.hour*60*60 + hms.minute*60 + hms.second + hms.second_frag;}
		
		
		// TODO: Maybe make the outputs like “0 hour(s), 0 minute(s), 4 second(s)” into just “4 second(s)” instead???
		public String toString() {
			String res = String.format("%d hour(s), %d minute(s), %d second(s)", hms.hour, (int) hms.minute, (int) hms.second);
			if (days > 0) res = str.format("%d day(s), ", days) + res;
			return res;
		}
		
	}
	
// ---------------------------------------------------------------- ----------- ----------------------------------------------------------------
	
}
