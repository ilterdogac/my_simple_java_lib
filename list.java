import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Consumer;

/** A user-made list interface that provides some useful methods.
 *  
 *  <p>DISCLAIMER: USES MAINLY 1-BASED INDEXES AS OPPOSED TO WHAT THIS CLASS' DEVELOPER THINKS
 *  Y'ALL WOULD CRITISIZE HIM FOR NOT FOLLOWING: ARRAYS ALWAYS START FROM 0 AND PROGRAMMERS
 *  HAVE AN UNWRITTEN RULE THAT COUNTING THINGS IN CODE ALWAYS STARTS FROM 0. SO PLEASE
 *  KEEP YOUR WHINES TO YOURSELF.
 */
public interface list<Item> extends java.util.List<Item>, java.io.Serializable/*, Sortable<Item> */ {
	
//	public java.util.List<Item> getEncapsulated();
	
//	public final java.lang.reflect.Type type;
//	private list(boolean getType) {if (getType) type = (new list<>(false) {}).getClass().getGenericSuperclass(); else type = null; if (getType) System.err.println(type);}
	
//	public list() {/*this(true);*/ this(new java.util.LinkedList<Item>(), 0);}
//	private list(java.util.List<Item> wrapped, int dummy) {encapsulatedList = wrapped;}
	
	
	
	// Equivalent of one of the usages of the __init__ function of Python classes: void list.__init__(list newList)
	@Deprecated public default void __init__(list<Item> _new) {clear(); for (Item element: _new) add(element);}
	// TODO: Remove this because it looks kind of weird. You can't just “C++”-destruct (not “C++”-delete) a non-orphaned
	// Java object (without “C”-freeing) and “C++”-construct another one exactly right on its memory address. I don't
	// actually even know what the hell Python's would do.
	
//	/** Returns a new instance of the same type; that is, clones the object but without its elements.
//	 *  Supposed to be overridden by the implementing classes for the performance issues */
//	public default list<Item> newInstance() {
//		try {return this.getClass().getDeclaredConstructor().newInstance();}
//		catch (InstantiationException | IllegalAccessException | IllegalArgumentException
//		     | InvocationTargetException | NoSuchMethodException | SecurityException e) {
//			throw new Error(e);
//		}
//	}
	
	
	// FIXME: Add support to also try to use the private blank constructor.
	/** Returns a new instance of the same type; that is, clones the object but without its elements.
	 *  <strong>Warning: <code>list&lt;Item&gt; list.clone()</code> implementations generally depend
	 *  on the instance returned from <code>list&lt;Item&gt; list.newInstance()</code></strong> */
	public default list<Item> newInstance() {
		fn.log("Warning: Invoked the default implementation --- list<E> list.<E>newInstance()");
		Class<? extends list<Item>> cls = (Class<? extends list<Item>>) this.getClass();
		try {
			Constructor<? extends list<Item>> ctr = cls.getConstructor();
			return ctr.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			fn.log("Warning: Could not instantiate a new blank instance of "+this.getClass().getName()+" through the default implementation.");
			fn.log("Warning: "+str.getStackTraceText(e));
			return null;
		}
	}
	
	
	
	// Methods using 1-based index. Note that public Item rm(int) does not have the risk of removing the object equal with
	// the index anymore unlike public Item remove(int) and public Item remove(Item), when the type parameter Item is Integer.
	
	// To remind myself that I'm calling methods with 0-based indexes
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
//	@Deprecated public Item get(int i);
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
//	@Deprecated public Item set(int i, Item element);
	/** Warning - returns 0-based indexes!<p>{@inheritDoc} */
//	@Deprecated public int indexOf(Object o);
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
//	@Deprecated public void add(int index, Item element);
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} 
	 * @throws Exception */
//	@Deprecated public Item remove(int index);
	
	
	// ---------------------------------------- LEVEL1 INDEX BASED METHODS ----------------------------------------
	
	/*
        Layout example
                list
                        get     warn0Index(), return get0(i)
                        get0    <implemented by implementation>
                        g       return get0(normalize(i)-1)
                abstractList impl list
                        get     <inherit>
                        get0    <inherit>
                        g       <inherit>
                listWrapper ext abstractList
                        get     warn0Index(), return enclosed.get(i)
                        get0    if (enclosed instanceof ?list)
                                        return ((?list) enclosed).get0(i)
                                else
                                        return enclosed.get(i)
                        g       <inherit>
                linklist ext listWrapper
                        get     <inherit>
                        get0    <inherit>
                        g       <inherit>
	*/
	
	
//	public default boolean instanceOf_list() {
//		return true;
//	}
	
	// FIXME: A lot of abstract methods (i.e. get) now are not abstract and call someother methods (i.e. get0) which also call them back!
	// Any implementing classes that forget to override any of those methods have those methods as bombs ready to explode with a StackOverflowError!!!
	
	// TODO: Take the 0 based index method warnings from the class listwrapper to the interface list!!!
	
	/** Does what the original {@link java.util.List}&lt;Item&gt;.get(int) method normally does
	 *  and is to be used when the actual method is implemented so as to do something weird,
	 *  instead of interpreting the index argument (or preparing return value that is an index) as
	 *  0-based and normally doing what it should do, such as warning the user for (probably
	 *  mistakenly) using the 0-based index considering method. When the original
	 *  method is implemented so, this one should be overridden in a way that it implements
	 *  the overridden version of the original 0-based method. */
	public default Item get0(int index) {return get(index);}
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
	public default Item get(int index0) {return get0(index0);} 
	public default Item g(int index) {
		int size = size();
		checkForValidIndex(size, index, size, index);
		index = normalizeIndex(index, size());
		return get0(index-1);
	}
	
	
	/** Does what the original {@link java.util.List}&lt;Item&gt;.set(int, Item) method normally does
	 *  and is to be used when the actual method is implemented so as to do something weird,
	 *  instead of interpreting the index argument (or preparing return value that is an index) as
	 *  0-based and normally doing what it should do, such as warning the user for (probably
	 *  mistakenly) using the 0-based index considering method. When the original
	 *  method is implemented so, this one should be overridden in a way that it implements
	 *  the overridden version of the original 0-based method. */
	public default Item set0(int index, Item element) {return set(index, element);}
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
	public default Item set(int index0, Item element) {return set0(index0, element);} 
	public default Item s(int index, Item element) {
		int size = size();
		checkForValidIndex(size, index, size, index);
		index = normalizeIndex(index, size());
		return set0(index-1, element);
	}
	
	
	/** Does what the original {@link java.util.List}&lt;Item&gt;.add(int, Item) method normally does
	 *  and is to be used when the actual method is implemented so as to do something weird,
	 *  instead of interpreting the index argument (or preparing return value that is an index) as
	 *  0-based and normally doing what it should do, such as warning the user for (probably
	 *  mistakenly) using the 0-based index considering method. When the original
	 *  method is implemented so, this one should be overridden in a way that it implements
	 *  the overridden version of the original 0-based method. */
	public default void add0(int index, Item element) {add(index, element);}
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
	public default void add(int index, Item element) {add0(index, element);}
//	public default boolean add(Item element) {return getEncapsulated().add(element);}
	/** Adds the item into the specified position. When negative indexes used, the position
	 *  is perceived as what the negative index corresponds <strong>before</strong> adding
	 *  the item — that is, “-1” as the index does not cause the new item to be placed after
	 *  the last (to be in “-1”st order) but the last's place and shifts the last one. */
	public default void a(int index, Item element) {
		int size = size();
		checkForValidIndex(size+1, index, size, index);
		index = normalizeIndex(index, size);
		// Here, index is between acceptable bounds (both positive and big at most size+1)
		add0(index-1, element);
	}
	
	
	/** Does what the original {@link java.util.List}&lt;Item&gt;.remove(int) method normally does
	 *  and is to be used when the actual method is implemented so as to do something weird,
	 *  instead of interpreting the index argument (or preparing return value that is an index) as
	 *  0-based and normally doing what it should do, such as warning the user for (probably
	 *  mistakenly) using the 0-based index considering method. When the original
	 *  method is implemented so, this one should be overridden in a way that it implements
	 *  the overridden version of the original 0-based method. */
	public default Item remove0(int index) {return remove(index);}
	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
	public default Item remove(int index) {return remove0(index);}
	public default Item rm(int index) {
		int size = size();
		checkForValidIndex(size, index, size, index);
		index = normalizeIndex(index, size());
		return remove0(index-1);
	}
	
	@SuppressWarnings("unchecked")
	public default           int index(Object element                                               ) {return index(element, ((a, b) -> a.equals(b)/*(a==b)*/));}
//	public           int indexOf(Item element,            _2Item_boolean<Item> compareFunction) {for (int i: indexIterator()) if (compareFunction.evaluate(element, g(i))) return i; return 0;}
//	public <newItem> int index(newItem element, _1A1B_boolean<Item, newItem> compareFunction) {for (int i: indexInterval()) if (compareFunction.evaluate(g(i), element)) return i; return 0;}
	public default <newItem> int index(newItem element, _1A1B_boolean<Item, newItem> compareFunction) {
		int i = 0;
		for (Item each: this) {
			i++; if (compareFunction.apply(/*g(i)*/each, element)) return i;
		} return 0;
		/* Upon using a dict<String, javafx.scene.image.Image> (that holds a list) of 4003 elements (where the necessary ones are at the last
		 * and the first 4000 elements are like “a3”: null, “a4”: null, “a1800”: null) in the handle method of an 60FPS javafx.amination.AnimationTimer
		 * to get those (60 times every second) and seeing that the animation slogs hard, I changed the design so that it just returns the current index
		 * iterating over 1st to nth node once the match occurs (~n), instead of cycling through the linked nodes from the root to the n'th
		 * node for each index i such that i iterates from 1 to n to find a match (~(1/2)n^2). (finding an ith element of a linked list requires
		 * iterating on the nodes by keeping jumping to the node at the address written in the field “next” of the current one for i times (~i))*/
	}
//	/** Does what the original {@link java.util.List}&lt;Item&gt;.remove(int) method normally does
//	 *  and is to be used when the actual method is implemented so as to do something weird,
//	 *  instead of interpreting the index argument (or preparing return value that is an index) as
//	 *  0-based and normally doing what it should do, such as warning the user for (probably
//	 *  mistakenly) using the 0-based index considering method. When the original
//	 *  method is implemented so, this one should be overridden in a way that it implements
//	 *  the overridden version of the original 0-based method. */
//	public default int indexOf0(Object element) {return indexOf0(element);}
//	/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
//	public default int indexOf(Object element) {return indexOf(element);}
	public default int indexOf(Object element) {return index(element) - 1;}
	
	
	// ---------------------------------------- -------------------------- ----------------------------------------
	
	
	
	
	
	
	
	
	public default           boolean contains(Object element                                               ) {return index(element) != 0;}
//	public           boolean contains(Item element,            _2Item_boolean<Item> compareFunction) {return indexOf(element, compareFunction)!=0;}
	public default <newItem> boolean contains(newItem element, _1A1B_boolean<Item, newItem> compareFunction) {return index(element, compareFunction) != 0;/*for (Item each: this) if (compareFunction.evaluate(each, element)) return true; return false;*/}
	
//	public void convertToArrayList() {encapsulatedList = new java.util.ArrayList<>(encapsulatedList);}
//	public void convertToLinkedList() {encapsulatedList = new java.util.LinkedList<>(encapsulatedList);}
	
//	// Converts the negative index into backwards equivalent, or throws an exception if the result would still be negative.
//	@Deprecated private int convert(int index) { // -3 (10) -> 8; -12 (10) -> <!>
//		int size = size();
//		if (Math.abs(index) > size) throwIndexOutOfBoundsException(index);
//		return convertIgnoreBounds(index, size);
//	}
	
//	// Converts the negative index into backwards equivalent.
//	@Deprecated private int convertIgnoreBounds(int index, int size) { // -3 (10) -> 8; -12 (10) -> -1
//		// I did not want to make it calculate the size again, although the size is actually an integer changing in structural modifications and present; it is recalculated when the LinkedList is deserialized from bytes.
//		if (index < 0) return size + 1 + index;
//		if (index == 0) throwIndexOutOfBoundsException(index); // Thrown only when index == 0 this time.
//		return index;
//	}
	
//	public /*Item[] */Object[] toArray() {return getEncapsulated().toArray();}
	
	/* It is goddamn not legal to declare it to return an array of the type that can store Item (H for Item
	 * extends H) by defining a type variable for the method as “<H super Item>”. “super” is allowed only
	 * for wildcard ones of lower bounds like “returnType method(type1<? super type2> param1) {...}”; not
	 * defined ones.
	 */
	/** Warning: Maybe use the built-in <i><code>toArray((length) -> new ItemType[length])</code></i> from Collection?? */
	@SuppressWarnings("unchecked")
	public default /* <H super Item> H[]*/ Item[] getArray(Class<Item> type) {
		Item[] array = (Item[]) java.lang.reflect.Array.newInstance(type, size()); // Yes, you see correct; java.lang.reflect!
		forEach(new java.util.function.Consumer<Item>() {
			private int i = 0;
			public void accept(Item each) {array[i++] = each;}
		});
		return array;
	}
	
	
	
	// TODO: Find a way to properly publish the useful pieces from those comments on somewhere else and then remove that f.ing garbage!!
	
//	/** @deprecated Because it will fail if the type parameter is an interface that the most specific class that all of the
//	 *  items are of does not implement, it will cause a {@link ClassCastException}.<p>
//	 *  For example, let class A do NOT implement the interface K, classes B and C extend A but implement K, the type parameter
//	 *  item be K and the elements of the list be of type B and C. Calling this method would cause the the ClassCastExcaption
//	 *  because the array of determined type (class A) can't be casted to array of K as A does not implement K unlike B and C
//	 *  that extend A do. The ClassCastException occurs right at the call to the function (just as if you were trying to cast it
//	 *  after the function returns as a raw type) so the function has nothing to do with that exception and can't handle it
//	 *  (otherwise it could even find the class by its name from the exception message!) because generic classes can never be
//	 *  aware of what the hell their type parameters are at the runtime -- type parameters don't even exist at the runtime as
//	 *  they are just for the compile-time type safety.<p>
//	 *  Also this method has a worst-case time complexity of almost O(n^2) which is terrible for such a purpose!*/
//	public Item[] getArray() {return getArray(determineMemberType(this));}
	
	
//	// Maybe I'm not able to instantiate an array of type variable (like “Item”), but only through non-psychopathic ways.
//	/** <Strong>Warning:</Strong> If the generic type is a damn <em>interface</em> (instead of <em>class</em>) that the smallest enclosing class of all non-null elements does not implement, the {@link #ClassCastException} gets thrown, right at where it is called (hence impossible to catch here).*/
//	@SuppressWarnings("unchecked")
//	public static <H> Class<H> determineMemberType(Iterable<H> collection) {
//		/* An array of type variable (like Item) as well as of a generic class (like java.util.List<String>) can't be created as well as the objects of type variables themselves can't be instantiated.
//		 * All of these examples are illegal:
//			 - Item anItem = new Item();
//			 - class A extends Item {...}
//			 - Item[] array = new Item[size()];
//			 - Item[] array = java.lang.reflect.Array.newInstance(Item.class, size());
//			 - like java.util.List<String>[] array = new like java.util.List<String>[] */
//		/* Accessing the class object (of type Class<?>) of a type variable gives an error - as the method/class shouldn't
//		 * even be aware what its type variables actually are because variable types don't exist at runtime; the type erasure assumes them all as Object by default.
//		 * So we have to estimate the smallest superclass of all the elements (which can't be any larger than the type variable Item) BY CHECKING THEM MANUALLY.
//		 * Note that an String[] is an Object[] as well (unlike generics), but an Object[] has no chance to be String[]
//		 * as well if it has been created as new Object[...] {...}, even if all the elements it has are Strings.
//		 * Example: new list<Throable>(new IOException(), new NullPointerException(), new RuntimeException()) -> (Throwable[]) new Exception[] {...} (not new Throwable[] {...}) */
//		
//		// But before that, I'd like to introduce more fantastic way, which would determine the exact class that the type variable regardless of the nonexistence of non-null elements, IF IT F.ING WORKED AS I EXPECTED!
//		// ClassCastException during the typecast of an Object into Type DOES NOT occur at the line I do the cast, just as the stack trace shows.
//		/*if (true) {
//			Class<?> finalType = Object.class;
//			Item[] array = null;
//			try {@SuppressWarnings("unused") Item foo = (Item) new Object();} catch (ClassCastException e) {
//				try {finalType = Class.forName(str.split(e.getMessage(), " ").g(8));} // “... cannot be cast to <className>” -> “<className>”
//				catch (ClassNotFoundException _e) {e.printStackTrace(); return null;}
//			}
//			array = (Item[]) java.lang.reflect.Array.newInstance(finalType, size());
//			for (int i: indexInterval()) array[i-1] = g(i);
//			return array;
//		}*/
//		
//		list<Class<?>> types = new list<>(), temp = new list<>();
//		list<Integer> removal = new list<>();
////		Class<?> finalType/* = Object.class*/;
//		for (Class<?> eachType: new list<>((each) -> each.getClass(), collection, (each) -> each != null))
//			if (!types.contains(eachType)) types.add(eachType);
//		// If there is no non-null object, it is not possible to determine the type of the array which is guaranteed to be no larger than Item.
//		// To avoid ClassCastException, we return null instead.
//		if (types.size() == 0) return null;
////		if (types.size() == 0) return toArray();
//		
//		while (true) { // Get the smallest enclosing class.
//			removal.clear();
//			/* Add the index of (and remove based on the index every time the while loop iterates)
//			 * any type whose nearest superclass is not in the list... */
//		I:	for (int i: types.indexInterval()) {
//			J:	for (int j: types.indexInterval(i+1, -1)) {
//					if (removal.contains(i)) continue I; if (removal.contains(j)) continue J;
//					if (types.g(i).isAssignableFrom(types.g(j))) {removal.add(j); continue J;} // i type is assignable from j type; j objects can be assigned into of i type variable because class i is same as or a superclass of j.
//					if (types.g(j).isAssignableFrom(types.g(i))) {removal.add(i); continue I;}
//				}
//			}
//			temp.__init__(types); types.clear();
//			for (int i: temp.indexInterval()) if (!removal.contains(i)) types.add(temp.g(i));
//			fn.assertAnyway(types.size() != 0);
//			if (types.size() == 1) break;
//			{
//				Class<?> current;
//				for (Class<?> type: temp) { // ...and add the nerest superclass of the remaining.
//					current = type.getSuperclass();
//					if (!types.contains(current)) types.add(current);
//				}
//			}
//		}
//		fn.assertAnyway(types.size() == 1);
//		return (Class<H>) types.g(1);
//	}
	
	
	
	// (5, +4) -> 4
	// (5, +5) -> 5
	// (5, -1) -> 5
	// (5, -2) -> 4
	// (5, -5) -> 1
	// (5, -6) -> <“Size: 5, Index: -6”>
	// (5,  0) -> <“Size: 5, Index: 0”> / <“Given index parameter is 0”>
	// Warning: Does not check anything other than index's being 0, (including size's being negative)!
	public static int normalizeIndex(int index, int size) { // -3 (10) -> 8; -5 (100) -> 96; -12 (10) -> <-1>; -11 (10) -> <0>
		if (index < 0) return size + 1 + index;
		if (index == 0)
			throw new IndexOutOfBoundsException("Given index parameter is 0"); // Thrown only when index == 0.
//		throwIndexOutOfBoundsException(index, size); // Thrown only when index == 0.
		return index;
	}
	@Deprecated public static int convertIndex(int index, int size) { // -3 (10) -> 8; -5 (100) -> 96; -12 (10) -> <-1>; -11 (10) -> <0>
		fn.log("Warning: Called an old method (convertIndex)\n"+str.getStackTraceText());
		return normalizeIndex(index, size);
	}
	/** Translates an index interval by converting negative indexes into backwards equivalent; returns the values
	  * when appropriate, returns null if the interval is empty, or throws an exception if any of the bounds is zero.
	  * Clamps the indexes beyond the edges to the edges. Just returns null instead of clamping if both of the
	  * indexes are beyond the same edge. */
	public static int[] normalizeIntervalAndClampToBounds(int from, int to, int size, boolean ascending) {
//		if (from == to) return new int[] {from, from};
		from = normalizeIndex(from, size);
		to   = normalizeIndex(to,   size);
		if ( ascending && (from > to)) return null;
		if (!ascending && (from < to)) return null;
		if (from > size && to > size)  return null;
		if (from <    1 && to <    1)  return null;
		
		if (to   > size) to   = size; // 5, ( 3,  6)             -> (3, 5)
		else if (to   <    1) to   =    1; // 5, ( 4, -7) -> ( 4, -4) -> (1, 4)
		if (from > size) from = size; // 5, ( 7,  2)             -> (5, 2)
		else if (from <    1) from =    1; // 5, (-7,  4) -> (-2,  4) -> (1, 4)
		return new int[] {from, to};
	}
	@Deprecated public static int[] convertIntervalAndClampToBounds(int from, int to, int size, boolean ascending) {
		fn.log("Warning: Called an old method (convertIntervalAndClampToBounds)\n"+str.getStackTraceText());
		return normalizeIntervalAndClampToBounds(from, to, size, ascending);
	}
	public static void checkForValidIndex(int sizeToCheck, int indexToCheck, int sizeToShow, int indexToShow) {
		fn.assertAnyway(sizeToCheck >= 0 && sizeToShow >= 0, "Size is negative!");
		if (indexToCheck == 0)
			throw new IndexOutOfBoundsException("Given index parameter is 0");
		if ((indexToCheck > sizeToCheck) || (indexToCheck < -sizeToCheck))
			throw new IndexOutOfBoundsException("Index: " + indexToShow + ", Size: " + sizeToShow);
	}
	
//	public static final boolean prevent0methods = false;
//	// Throws the relevant error only if the assertions are enabled and the variable above is true.
//	public static void throw0basedError(String methodName) {
//		String msg= "The 0-index-based method \u201c"+methodName+"\u201d of class list has been invoked.";
//		if (fn.isAssertOn() && prevent0methods) throw new Error(msg);
//		else fn.log("Warning: "+msg);
//	}
	
//	public static final Consumer<String> throw0basedError = (methodName) -> {
//		if (fn.isAssertOn() && prevent0methods) throw new Error(
//			"The 0-index-based method \u201c"+methodName+"\u201d of class list has been invoked."
//		);
//	};
	
	public default java.util.ListIterator<Item> iterator() {return listIterator();}
	
//	/** Removes the specified amount of the elements from the end of the list and returns
//	 *  the list of those elements */
//	public default list<Item> trimEnd(int by) {
//		
//	}
//	/** Removes the specified amount of the elements from the beginning of the list and returns
//	 *  the list of those elements */
//	public default list<Item> trimBegin(int by) {
//		
//	}
	
	/* Just returns of a new list consisting only of particular some objects from this list, not an extension backed by this one
	 * (therefore not like subList(from, to) but like new list<Item>(subList(from, to)), clone().subList(from, to) or
	 * ((java.util.LinkedList<Item>) subList(from, to)).clone().
	 * Equivalent of “someList[from:to]” in Python except here indexes start at 1. */
//	public list<Item> slice(int from, int to)         {return new list<>((index) -> g(index), indexInterval(from, to    ));}
	public default list<Item> slice(int from, int to) {
//		if (true) return new list<>((index) -> g(index), indexInterval(from, to, 1));
		java.util.ListIterator<Item> itr = iterator();
		Iterable<Integer> interval = indexInterval(from, to);
		
		int[] bounds = list.normalizeIntervalAndClampToBounds(from, to, size(), true);
		if (bounds == null) return newInstance();
		from = bounds[0]; to = bounds[1];
		if (from >= 2 && true) for (int i: indexInterval(1, from - 1))
			itr.next();
		list<Item> _new = this.newInstance(); // clone() is based on this!!! This must not return an instance from a
		                                      // type that is incompatible with clone()'s signature!!
		                                      // That is, when clone() is overridden into a narrower return type (abstract
		                                      // or not), this must be overridden accordingly too (abstract or not).
		for (int i: indexInterval(from, to))
			_new.add(itr.next());
		return _new;
	}
	
	/** Returns a sublist bi-directionally backed by the main list - that is, changes (including structural ones) made to any one
	 *  between the main list and the sublist are reflected to the other. Note: Dependent on <code>list&lt;Item&gt;
	 *  list.newInstance()</code> whenever the specified indexes lead to a slice with negative length, otherwise returns <code>
	 *  listWrapper</code>. If indexes leading a 0-length slice (such as from 9 to 8) are passed, it returns an empty list
	 *  that adding items result in adding to the original one. */
	@SuppressWarnings("serial")
	public default list<Item> sub(int from, int to) {
		// FIXME: Make the method so that it either gives error or returns a list that adding and removing items
		//        definitely affect the list it is backed! Not an unrelated new f.ing blank list!!!
		int size = size();
		int[] bounds = list.normalizeIntervalAndClampToBounds(from, to, size, true);
		if (bounds == null) {
			final int f, t;
			f = list.normalizeIndex(from, size);
			t = list.normalizeIndex(to, size);
			if (f == t) {
				bounds = new int[] {f, t};
			}
		}
		if (bounds == null) return this.newInstance(); // TODO: Throw sth. or return null instead!
		
		// FIXME!!!
		if (false) {
			{
				String a = null;
				for (int ind: new int[] {from, to})
					try {checkForValidIndex(size(), ind, size(), ind);}
					catch (IndexOutOfBoundsException e) {
						
						a.toString();
						if (a == null) a = e.getMessage();
						else {
							a = a+", ";
							a = a + e.getMessage();
						}
					}
			}
			bounds = new int[] {normalizeIndex(from, size()), normalizeIndex(to, size())};
			
			if (bounds == null) {
				return this.newInstance(); // TODO: Throw sth. or return null instead!
			}
		}
		
		from = bounds[0]; to = bounds[1];
		java.util.List<Item> enc = subList(from-1, to+1-1); // Do not simply return this, because this is a “java.util.List”; not “list”!
		list<Item> self = this;
		
		class impl extends listWrapper<Item> { // list$1impl
			// TODO: Verify this method
			public listWrapper<Item> newInstance() {return listWrapper.wrap(self.newInstance());}
			// F.ing interestingly, “list.this” does not evaluate to the list instance that here is a local class in one of its methods!
			// It is most probably because that although non-static nested (inner) classes have
			// the enclosing instance as a parameter (first param.) of each of their constructors,
			// Local classes just have each object they reference (as successive parameters), not
			// the object that they are defined in an instance method of it.
			// Btw you can extend a non-static inner class outside of where it is defined; you just
			// invoke the super constructor inside its constructors as like enclosingInstance.super();
			
//			public listWrapper<Item> clone() {return new impl();}
			// Why not just use the same reference instead?
			public listWrapper<Item> clone() {throw new UnsupportedOperationException(new CloneNotSupportedException(
				  "Cloning a sublist instead of getting a copy of slice from begin to end (1, -1) does not give "
				+ "any benefit over simply using the same reference to this object. Since the clone would affect "
				+ "its parent and the parent would affect both children (the clone and original) upon calling"
				+ "mutating methods like set, remove and add."
			));}
			
//			protected void init() {init(enc);}
			protected java.util.List<Item> getInitialEnclosed() {return enc;}
			// Init itself with the generated sublist that depends on (is backed by) the main one
		}
		
		
		return new impl();
	}
	
	public default list<Item> slice(int from, int to, int by) {
		if (by == 1) return slice(from, to);
		list<Item> _new = this.newInstance();
		for (int i: indexInterval(from, to, by))
			_new.add(this.g(i));
		return _new;
	}
	
//	public default list<Item> clone() {
//		list<Item> copy = newInstance();
//		for (Item each: this) copy.add(each);
//		return copy;
//	}
	
	
	public default list<Item> clone() {return slice(1, -1);}
	
	
	/*public list<Item> slice(int from, int to) {
		list<Item> subList = new list<Item>();
		int size = size();
		     if (from >  size) from =  size; // 5, ( 6,  3)             -> (5, 3)
		else if (from < -size) from = -size; // 5, (-6,  3) -> (-5,  3) -> (1, 3)
		     if (to   >  size) to   =  size; // 5, ( 3,  6)             -> (3, 5)
		else if (to   < -size) to   = -size; // 5, ( 3, -6) -> ( 3, -5) -> (3, 1)
		for (int i: fn.range(convert(from), convert(to)*//*, 1*//*)) // Allowing reverse-ordered sub list; [1, 2, 3, 4, 5].split(3, 1) -> [3, 2, 1]
			subList.add(get(i));
		return subList;
	}*/
	public default list<Item> concat(@SuppressWarnings("unchecked") Item... toAdd) {
		list<Item> _new = clone();
		for (Item each: toAdd) _new.add(each);
		return _new;
	}
	public default <newItem extends Item> list<Item> concat(Iterable<newItem> that) {
		list<Item> _new = clone();
		that.forEach((each) -> {_new.add(each);});
		return _new;
	}
	
	public default Iterable<Integer>/*fn.range*/ indexInterval() {return fn.range(1, size(), +1);}
	/**Increment is always 1 regardless of whether to - from is positive.*/
	public default Iterable<Integer>/*fn.range*/ indexInterval(int from, int to) {return indexInterval(from, to, 1);}
	public default Iterable<Integer>/*fn.range*/ indexInterval(int from, int to, int by) {
		int[] bounds = normalizeIntervalAndClampToBounds(from, to, size(), by >= 0);
		if (bounds == null) return fn.range(0);
		return fn.range(bounds[0], bounds[1], by);
	}
	
	
	// list<Integer> -> int[]
	public static byte[]    byteArrayOf(java.util.List<Byte>     input) {return (byte[])    getPrimitiveArray(byte.class,    input.size(), input);}
	public static int[]     intArrayOf(java.util.List<Integer>   input) {return (int[])     getPrimitiveArray(int.class,     input.size(), input);}
	public static boolean[] boolArrayOf(java.util.List<Boolean>  input) {return (boolean[]) getPrimitiveArray(boolean.class, input.size(), input);}
	public static long[]    longArrayOf(java.util.List<Long>     input) {return (long[])    getPrimitiveArray(long.class,    input.size(), input);}
	public static double[]  doubleArrayOf(java.util.List<Double> input) {return (double[])  getPrimitiveArray(double.class,  input.size(), input);}
	
	public static <E> void reverseList(java.util.List<E> ls) {
		list<E> temp = new linklist<>(ls);
		ls.clear();
		for (int i: fn.range(temp.size()))
			ls.add(temp.rm(-1));
	}
	
	public static <E> list<E> cloneIntoArList(Iterable<E> from) {
		list<E> to = list.listWrapper.getArList();
		for (E e: from) to.add(e);
		return to;
	}
	
	
	
	// Instantiates a primitive array with given size and fills with the given values.
	// Both int.class and Integer.class are Class<Integer> despite being not equal objects.
	@SuppressWarnings("unchecked")
	public static <E> Object getPrimitiveArray(Class<E> compType, int len, Iterable<E> collection) {
		// This part does not seem to compile with the implementation of the JDK on dev.cs server; although it does on my machine with compilance level set to 1.8
		if (compType == null)
			return null;
		Object output;
		java.util.Iterator<E> itr = collection.iterator();
		try {
				       if (compType.equals(byte.class)) {byte[] array = new byte[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (byte)(Byte) itr.next();
				} else if (compType.equals(int.class)) {int[] array = new int[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (int)(Integer) itr.next();
				} else if (compType.equals(long.class)) {long[] array = new long[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (long)(Long) itr.next();
				} else if (compType.equals(boolean.class)) {boolean[] array = new boolean[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (boolean)(Boolean) itr.next();
				} else if (compType.equals(double.class)) {double[] array = new double[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (double)(Double) itr.next();
				} else if (compType.equals(float.class)) {float[] array = new float[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (float)(Float) itr.next();
				} else if (compType.equals(short.class)) {short[] array = new short[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (short)(Byte) itr.next();
				} else if (compType.equals(char.class)) {char[] array = new char[len];
					output = array; for (int i: fn.range(1, len, 1)) array[i-1] = (char)(Character) itr.next();
				} else output = null;
			}/* catch (NullPointerException e) {return null;}*/ catch (java.util.NoSuchElementException e) {throw e;}
		return output;
	}
	
	
	
	@Deprecated public static void throwIndexOutOfBoundsException(int index, int size) {
		String num, ord;
		num = ((Integer)index).toString();
		ord = "th";
		if (!((num.length() >= 2) && str.slice(num, -2, -2).equals("1")) ) {
			if (str.slice(num, -1, -1).equals("1")) ord = "st";
			if (str.slice(num, -1, -1).equals("2")) ord = "nd";
			if (str.slice(num, -1, -1).equals("3")) ord = "rd";
		}
		throw new IndexOutOfBoundsException("Trying to point at " + ((size>=0)?("the " + (index+ord)):"an") + " element from a list/array of " + ((size==0)?"no":size) + " element" + ((size==1)?"":"s"));
	}
	
	
	
	@Deprecated public static interface _2Item_boolean<E> {boolean apply(E a, E b);}
	            public static interface _1A1B_boolean<A, B> {boolean apply(A a, B b);}
//	public static interface _1A1B_boolean_1<A, B> {boolean evaluate(A a, B b);}
//	public static interface _1A1B_boolean_2<A, B> {boolean evaluate(A a, B b);}	
	@Deprecated public static interface Item_boolean<E> {boolean evaluate(E item);}
	@Deprecated public static interface typeA_typeB<inputType, outputType> {outputType evaluate(inputType object);}
	@Deprecated public static interface void_typeA<outputType> {outputType evaluate();}
	
	
	
	
	
	
//	------------------------------------------------------------------------------------------------
	
	// TODO: Move that away!
	@SuppressWarnings({"unused", "unchecked"})
	public static void genericDemo() {
		list<Integer> a1 = new linklist<Integer>(1, 2, 3, 4, 5, 6, 7);
		list<Integer> b1 = new linklist<>(1, 2, 3, 4, 5, 6, 7);
		list<?>       a2 = new linklist<Integer>(1, 2, 3, 4, 5, 6, 7);
		list<?>       b2 = new linklist<>(1, 2, 3, 4, 5, 6, 7);
		
		a1.g(1); // Returns Integer
		b1.g(1); // Returns Integer
		a2.g(1); // Returns Object
		b2.g(1); // Returns Object
		
		a1.add(8);
		b1.add(8);
//		a2.add(8); // Does not even accept an int/Integer
//		b2.add(8); // Same as above
		
		
		list<Integer> c1;
		list<?>       c2;
		c1 = a1; c1 = b1;
//		c1 = a2; c1 = b2; // list<?> seems to have to be casted into list<Integer>.
		c1 = (list<Integer>) a2; c1 = (list<Integer>) b2;
		c2 = a1; c2 = a2;
		
		
		list<Object>  x1;
		list<Integer> x2;
		new linklist<>(1, 2, 3, 4, 5); // This returns a list<Integer>, and new ... 3, 4, “5”) returns a list<Object>.
		/* 
		 * Unlike generics, an Integer (not int) array (Integer[]) is also an Object array (Object[]) and therefore doing these two do not lead to a compile-time error, but throw an ArrayStoreException:
		 * Integer[] a = new Integer[5]; Object[] b = a; b[0] = “foo”; // (or simply)
		 * ((Object[]) new Integer[5])[0] = “foo”;
		 * But with generics, this kind of issue is compile-time checked; a list<Integer> is not a list<Object>.
		 */
		
		x1 = new linklist<>(1, 2, 3, 4, 5); // Even though these two should return the same thing; since type variables are handled in a process of the compilation called type-erasure, type variables get inferred differently and this one gets assigned a list<Object>...
		x2 = new linklist<>(1, 2, 3, 4, 5); // ...whereas this one gets assigned a list<Integer>.
		// And these both are illegal!
//		x1 = x2;
//		x2 = x1;
		x1 = new linklist<>();
		x2 = new linklist<>();
		// Below are still illegal; x2 keeps being able to get list<Integer> and unable to get such list<T> that T does not subclass Integer, and x1 keeps getting list<Object>.
//		x1 = x2;
//		x2 = x1;
		
		list<String> sl = new linklist<>();
		list<Integer> il = (list<Integer>) (Object) sl; // Passes the type-check with just a warning
		sl.add("1");
		il.add(2);
//		int i = il.g(1);
		/* This statement just above causes a ClassCastException that String
		 * can't be casted into Integer which seems to be thrown right at this
		 * statement, not in anywhere of the implementation of class list;
		 * therefore not possible to catch inside the method Item list.g(int).
		 * Because there is an implicit typecasting which occurs at the runtime,
		 * just like this one does:
		 * int i = (Integer) (Object) sl.g(1) // Because they're Object after the erasure
		 * */
		
		
		
		fn.print("Done \"genericDemo()\".");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("serial")
	/** A list class that provides some useful methods and Python's list comprehension - like constructors */
	public static abstract class abstractList<Item> implements list<Item> {
		
		public abstractList() {}
		
		
		public int hashCode() {
			int hashCode = 0;
			for (Item e: this) hashCode = 31*hashCode + e.hashCode();
			return hashCode;
		}
		public boolean equals(list<?> that) {
			if (this.size() != that.size()) return false;
			java.util.Iterator<?> itr1 = this.iterator();
			java.util.Iterator<?> itr2 = that.iterator();
			while (itr1.hasNext() && itr2.hasNext()) if (!fn.equals(itr1.next(), itr2.next())) return false;
			return true;
		}
		public boolean equals(Object that) {return (that instanceof list) && equals((list<?>) that);}
		
		
		
		// Implementing the 0-based index methods to use the original java.util.List 0-based index methods.
		// aaaaaaaaaaaaaaaaaaaaa
		public Item get0(int i) {log0BasedIndexMethodInvoke("get"); return list.super.get0(i);}
		public Item set0(int i, Item element) {log0BasedIndexMethodInvoke("set"); return list.super.set0(i, element);}
		public void add0(int i, Item element) {log0BasedIndexMethodInvoke("add"); list.super.add0(i, element);}
		public Item remove0(int i) {log0BasedIndexMethodInvoke("remove"); return list.super.remove0(i);}
		public int indexOf0(Object element) {log0BasedIndexMethodInvoke("indexOf"); return list.super.indexOf(element);}
		
		private void log0BasedIndexMethodInvoke(String methodName) {
			fn.log("Warning: (maybe mistakenly) invoked a method ("+methodName+") that uses 0-based index, of list ("+this.getClass()+").");
			fn.log("Warning: "+fn.getStackTrace());
		}
		
		
		@SuppressWarnings("static-method")
		protected int modCount() {return -1;}
		protected void modCount(int newValue) {}
		protected void modCountIncrement() {modCount(modCount() + 1);}
		protected void modCountDecrement() {modCount(modCount() - 1);}
		
		public abstract abstractList<Item> newInstance();
		
		/*Varargs is safe: */@SafeVarargs
		// Keep in mind that an array of a type variable/parameter can't be created (“new Item[n];” in this case) and an array of a generic type (like “new LinkedList<String>[n];”) can't be created.
			/* This method can be called with an array of that type (probably only if “Item” were a real type) or with an arbitrary number of items of that type;
			 * therefore the following commented-out method is invalid with this one and vice versa!
			 * Because a method taking some variables in a repeated permutation of types and a variable number of variables of a type can be called
			 * with the same permutation of types and with just an array of that variadic parameter variable type.
			 * foo(int, boolean, String...) can be called with (int, boolean, String[]). */
		public/* <newItem extends Item>*/ abstractList(/*new*/Item... eachItem) {
			this();
			for (int i: fn.range(eachItem.length)) add(eachItem[i-1]);
		}
	//	public list(Item[] eachItem) {} // But this one can't be called with any number of “Item” variables but can be only with an array of Item. foo(int, boolean, String[]) can NOT be called with (int, boolean, String...) (like “(int, boolean, String, String, String)”).
		
		public/* <newItem extends Item>*/ abstractList(int quantity) {this(quantity, null);}
		@SuppressWarnings("unused")
		public/* <newItem extends Item>*/ abstractList(int quantity, /*new*/Item defaultItem) {this(); for (int i: fn.range(quantity)) add(defaultItem);}
		public abstractList(Iterable<Item> collection) {this((item) -> item, collection);}
		
		// Creates a list consisting of the ones among the items in the collection (Iterable collection) those evaluate true through the condition (Item_boolean cond).
		// Equivalent of the Python list comprehension [expr.evaluate(item) for item in collection if cond.evaluate(item)].
		public <newItem> abstractList(Function<newItem, Item> expr, Iterable<newItem>     collection, Function<newItem, Boolean>     cond) {
			this(); for (newItem item: collection) if (cond.apply(item)) add(expr.apply(item));
		}
		// Equivalent of the Python list comprehension [expr.evaluate(num) for num in range(rangeParameters)].
		public <newItem> abstractList(Function<newItem, Item> expr, Iterable<newItem>     collection                                     ) {
			this(expr, collection, (a) -> true);
		}
		// Equivalent of the Python list comprehension [item for item in collection if cond.evaluate(item)].
		public/* <newItem>*/ abstractList(                          Iterable</*new*/Item> collection, Function</*new*/Item, Boolean> cond) {
			this((a) -> a, collection, cond);
		}
		
		@Deprecated public abstractList(Supplier<list<Item>> constructor) {this(constructor.get());}
		
		/** Takes a java.util.Consumer object to give the reference of <strong>this</strong> list and
		 *  allows an user-specified operation to be executed on the list, then returns the constructed list.
		 *  Example:<p>new list<>((ls) -> {<p>for (int i = 1; i<=50; i++) ls.add((int) Math.pow(i, 2));<p>}); */
		public abstractList(Consumer<list<Item>> initializer) {this(); initializer.accept(this);}
		
		/*public list(typeA_typeB<Integer, Item> expr, Integer... rangeParameters) { // Type argument infering when I pass “new list<>((a) -> a*a, 1, 10)” as just an Object (not list<Integer>, list<Number> etc.) (directly to fn.print(Object)) literally f.ks up if I use the vararg as “int...” instead of “Integer...”.
			this((n) -> expr.evaluate(n), fn.range(rangeParameters), (n) -> true); // However, int... / int[] does not, of course, accept and Integer[], therefore I'm better off to split this into 3 different methods.
		}*/
		
		// Compilation step “type-erasure” decides what Item should be, according to the expr, while inferring type arguments of such an statement like new list<>((n) -> ..., a, b, c).
		// For example, if expr = (a) -> ""+a, it turns out to be list<String>, or list<Long> if it is (a) -> a.longValue().
		// Equivalent of the Python list comprehension [expr.evaluate(num) for num in range(rangeParameters)].
		public abstractList(Function<Integer, Item> expr,            int end               ) {this((n) -> expr.apply(n), fn.range(       end           )/*, (n) -> true*/);}
		public abstractList(Function<Integer, Item> expr, int start, int end               ) {this((n) -> expr.apply(n), fn.range(start, end           )/*, (n) -> true*/);}
		public abstractList(Function<Integer, Item> expr, int start, int end, int increment) {this((n) -> expr.apply(n), fn.range(start, end, increment)/*, (n) -> true*/);}
		
		// Equivalent of the Python list comprehension [expr.evaluate(num) for num in range(rangeParameters) if cond.evaluate(num)].
		public abstractList(Function<Integer, Item> expr,            int end,                Function<Integer, Boolean> cond) {this((n) -> expr.apply(n), fn.range(       end           ), cond);}
		public abstractList(Function<Integer, Item> expr, int start, int end,                Function<Integer, Boolean> cond) {this((n) -> expr.apply(n), fn.range(start, end           ), cond);}
		public abstractList(Function<Integer, Item> expr, int start, int end, int increment, Function<Integer, Boolean> cond) {this((n) -> expr.apply(n), fn.range(start, end, increment), cond);}
		
		
		public abstractList<Item> clone() {return (abstractList<Item>) list.super.clone();}
		
		
		public String toString() {
			String result = "[";
			int size = size();
			
	//		for (int i: fn.range(size)) {
			int i = 0;
			for (Object item: this) {
				i++;
				if (item instanceof String) result += str.escapeString((String)item);
				else result += str.toString(item);
				if (i <= size-1) result += ", ";
			} result += "]";
			return result;
		}
	}
	
	
	
	
	
	
	
	
	/** Provides an implementation by wrapping another {@link java.util.List} object */
	public static abstract class listWrapper<Item> extends abstractList<Item> {
		private static final long serialVersionUID = -8977313416296681213L;
//		private <E> E initEnclosed(E anyObj) {
//			init();
//			return anyObj;
//		}
		
		// Item[] -> list<Item>
		// TODO: Move out of the ListWrapper class
		public static <Item> listWrapper<Item> asList(Item[] array) {return wrap(java.util.Arrays.asList(array));}
		
		// TODO: Move out of the ListWrapper class
		// The shortcut until I put the class arlist right next ot linklist.
		public static <Item> listWrapper<Item> getArList() {return wrap(new java.util.ArrayList<>());}
		
		/** Provides a quick way to wrap the list into a wrapper without having to extend the class with implementing its 2 methods.
		 *  @param getNew The supplier that public list.listWrapper list.listWrapper.getNew() returns its value.
		 */
		public static <Item> listWrapper<Item> wrap(java.util.List<Item> enclosed, Supplier<listWrapper<Item>> getNew) {
//		public static <Item> listWrapper<Item> wrap(java.util.List<Item> enclosed, Function<listWrapper<Item>, listWrapper<Item>> getNew) {
			ListWrapperImpl<Item> returnObj = new ListWrapperImpl<Item>(enclosed) {
				public listWrapper<Item> newInstance() {
					return getNew.get();
				}
			};
			return returnObj;
		}
		
		// TODO: Try to hide this class inside one of the below or above methods!!!
		@SuppressWarnings("serial")
		private static abstract class ListWrapperImpl<E> extends listWrapper<E> {
			public ListWrapperImpl(java.util.List<E> enc) {
				init(enc);
			}
//			protected void init() {} // Constructor should just call what the hell this would call
			protected java.util.List<E> getInitialEnclosed() { // Constructor already initializes so this won't be called
				throw new Error();
			}
		}
		
		/** Provides a quick way to wrap the list into a wrapper without having to extend the class with implementing its 2 methods */
		public static <Item> listWrapper<Item> wrap(java.util.List<Item> listToEnclose) {
			listWrapper<Item>[] returnObject = new listWrapper[] {null};
			
			Supplier<list.listWrapper<Item>> toGetNewEmpty = () -> { // Supplier whose get() is called whenever the newInstance() is called.
				java.util.List<Item> enc; // Enclosed list from the wrapper object. Its class will be used to instantiate
				                          // a new blank one so that we can wrap it and return when newInstance() is called.
				
				// Uncommenting here causes compile-time error.
				//                              vvvvvv
				Class<? extends java.util.List/*<Item>*/> cls;
				// Normally even that the method public final java.lang.Class<?> java.lang.Object.getClass() returns
				// with a generic type as the “?” matching that object's class is a violation to the logic of the generics itself.
				
				if (listToEnclose instanceof list<?>) {
					enc = ((list<Item>) listToEnclose).newInstance(); // Just use my public default list<Item> list.newInstance() method if the wrapped one is a “list” instance.
				} else { // Otherwise get its class and create a new instance reflectively.
					enc = listToEnclose; // Get the internal wrapped java.util.List (the one that we have used to wrap)...
					cls = enc.getClass(); // ...so that we can use its class...
					try {
						java.lang.reflect.Constructor<? extends java.util.List> con;
							try {
								con = cls.getConstructor();
							} catch (NoSuchMethodException e) {
								fn.log("Warning: newInstance called on a listWrapper that wraps a list object whose class does not have a no-arg constructor ACCESSIBLE FROM HERE (public, package etc.).");
								fn.log(str.getStackTraceText());
								con = cls.getDeclaredConstructor();
								con.setAccessible(true);
							}
							enc = con.newInstance(); // ...to get a new instance of that item using that class; and wrap it into a new listWrapper instance.
					}
					catch (
						InstantiationException   | IllegalAccessException |
						IllegalArgumentException | InvocationTargetException |
						NoSuchMethodException    | SecurityException e
				//	) {throw new RuntimeException(e);/*return null;*/}
					) {
						fn.log("Warning: newInstance called on a listWrapper that wraps a list object whose class does not have a no-arg constructor.");
						return new linklist<>();
//						return null;
					}
				}
				
				return wrap(enc);
//				return new impl<>(enc) {
//					public list.listWrapper<Item> newInstance() {
//						return wrap(enclosed);
//					}
//				};
			};
			
			returnObject[0] = wrap(listToEnclose, toGetNewEmpty);
			
			return returnObject[0];
		}
		
		
		public listWrapper<Item> clone() {return (listWrapper<Item>) super.clone();}
		public abstract listWrapper<Item> newInstance(); // clone() can safely typecast from super.clone()'s
		                                                 // abstractList into listWrapper because this returns
		                                                 // listWrapper too.
		
		private java.util.List<Item> enclosed;
		
		protected java.util.List<Item> enclosed() { // Although any other constructor calls only add(Item), the only one
			                                      // of them that takes a consumer can call any other implicitly and
			                                      // therefore I can't leave that nullcheck to add(Item). Whenever a
			                                      // method invokes enclosed(), this should check.
			// TODO: Fix that because every single time a method is called, generally that check occurs.
//			if (enclosed == null) init();
			if (enclosed == null) init(getInitialEnclosed());
//			if (enclosed == null) enclosed = getInitialEnclosed();
			return enclosed;
		}
		
//		/** Initializes the encapsulated list instance variable <code>private java.util.list&lt;Item&gt;
//		 *  enclosed</code> by calling <code>protected void init(java.util.List&lt;Item&gt;)</code>
//		 *  with the value returned from <code>java.util.List&lt;Item&gt; getInitialEnclosed()</code> */
//		private void init() {init(getInitialEnclosed());}
		// No need for this.
		
		/** Initializes the encapsulated list instance variable <code>private java.util.list&lt;Item&gt;
		 *  enclosed</code> by calling <code>protected void init(java.util.List&lt;Item&gt;)</code> */
		protected final void init() {init(getInitialEnclosed());}
		// Actually no need for this too.
		
		/** Returns the <code>java.util.List&lt;Item&gt;</code> object to be used to initialize the
		 *  <code>java.util.List&lt;Item&gt;</code> variable “enclosed”. */
		protected abstract java.util.List<Item> getInitialEnclosed();
		
		/** Initializes the enclosed <code>java.util.List&lt;Item&gt;</code> variable with the passed parameter */
		protected /*final*/ void init(java.util.List<Item> enclosed) {
			if (this.enclosed != null) {
				if (fn.isAssertOn()) throw new AssertionError(
					"Called listWrapper<Item>.init(java.util.List<Item>) the second time! (The variable for the wrapped object has already been initialized)"
				);
				return;
			}
			if (enclosed == null) throw new IllegalArgumentException(new NullPointerException("The parameter \"enclosed\" can\'t be null"));
			if (enclosed == this) throw new IllegalArgumentException("The parameter \"enclosed\" can\'t be the same object as this list");
			this.enclosed = enclosed;
		}
		
		public listWrapper() {super();}
		
		// Varargs is safe
		@SafeVarargs
		public/* <newItem extends Item>*/ listWrapper(/*new*/Item... eachItem) {super(eachItem);}
		
		public/* <newItem extends Item>*/ listWrapper(int quantity) {super(quantity);}
		@SuppressWarnings("unused")
		public/* <newItem extends Item>*/ listWrapper(int quantity, /*new*/Item defaultItem) {super(quantity, defaultItem);}
		public listWrapper(Iterable<Item> collection) {super(collection);}
		
		public <newItem> listWrapper(Function<newItem, Item> expr, Iterable<newItem>     collection, Function<newItem, Boolean>     cond) {
			super(expr, collection, cond);
		}
		public <newItem> listWrapper(Function<newItem, Item> expr, Iterable<newItem>     collection                                     ) {
			super(expr, collection      );
		}
		public/* <newItem>*/ listWrapper(                          Iterable</*new*/Item> collection, Function</*new*/Item, Boolean> cond) {
			super(      collection, cond);
		}
		
		public listWrapper(Consumer<list<Item>> initializer) {super(initializer);}
		
		public listWrapper(Function<Integer, Item> expr,            int end               ) {super(expr,        end           );}
		public listWrapper(Function<Integer, Item> expr, int start, int end               ) {super(expr, start, end           );}
		public listWrapper(Function<Integer, Item> expr, int start, int end, int increment) {super(expr, start, end, increment);}
		
		public listWrapper(Function<Integer, Item> expr,            int end,                Function<Integer, Boolean> cond) {super(expr,        end,            cond);}
		public listWrapper(Function<Integer, Item> expr, int start, int end,                Function<Integer, Boolean> cond) {super(expr, start, end,            cond);}
		public listWrapper(Function<Integer, Item> expr, int start, int end, int increment, Function<Integer, Boolean> cond) {super(expr, start, end, increment, cond);}
		
		
		// Pipe the original java.util.List methods to the actual enclosed object
		public int size() {return enclosed().size();}
		public boolean isEmpty() {return enclosed().isEmpty();}
		public boolean add(Item e) {return enclosed().add(e);}
		public Item get(int i) {return get0(i);}
		public Item set(int i, Item element) {return set0(i, element);}
		public int indexOf(Object o) {return indexOf0(o);}
		public void add(int index, Item element) {add0(index, element);}
		public Item remove(int index) {return remove0(index);}
		
		public Item get0(int i) {return enclosed().get(i);}
		public Item set0(int i, Item element) {return enclosed().set(i, element);}
		public int indexOf0(Object o) {return enclosed().indexOf(o);}
		public void add0(int index, Item element) {enclosed().add(index, element);}
		public Item remove0(int index) {return enclosed().remove(index);}
		
		public <T> T[] toArray(T[] a) {return enclosed().toArray(a);}
		public boolean remove(Object elm) {return enclosed().remove(elm);}
		public boolean containsAll(Collection<?> c) {return enclosed().contains(c);}
		public boolean addAll(Collection<? extends Item> c) {return enclosed().addAll(c);}
		public boolean addAll(int index, Collection<? extends Item> c) {return addAll(index, c);}
		public boolean removeAll(Collection<?> c) {return enclosed().removeAll(c);}
		public boolean retainAll(Collection<?> c) {return enclosed().retainAll(c);}
		public void clear() {enclosed().clear();}
		public int lastIndexOf(Object o) {return enclosed().lastIndexOf(o);}
		public java.util.ListIterator<Item> listIterator() {return enclosed().listIterator();}
		/** Warning - accepts 0-based indexes!<p>{@inheritDoc} */
		public java.util.ListIterator<Item> listIterator(int index) {return enclosed().listIterator(index);}
		
		/** Warning - accepts 0-based indexes! Also, returns an instance as a “java.util.List”; not “list”. <p>{@inheritDoc} */
		public java.util.List<Item> subList(int fromIndex, int toIndex) {
			return enclosed().subList(fromIndex, toIndex);
		}
		
		
		public Object[] toArray() {return enclosed().toArray();}
		
		// No such methods like below in java.util.Arrays!
		// int[] -> list<Integer>
//		public static listWrapper<Integer> asList(int[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Long> asList(long[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Boolean> asList(boolean[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Double> asList(double[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Float> asList(float[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Short> asList(short[] array) {return wrap(java.util.Arrays.asList(array));}
//		public static listWrapper<Character> asList(char[] array) {return wrap(java.util.Arrays.asList(array));}
		
		/** Not yet in release state! */
		// TODO: Initialize and release!!!
//		private static class ArrayAsList {
//			
//			private static UnsupportedOperationException unsupportedExc() {return new UnsupportedOperationException();}
//			
//			private static interface ThrowsExceptionWhenStructurallyModified<Item> extends list<Item> {
//				public default boolean add(Item e) {throw unsupportedExc();}
//				public default boolean remove(Object o) {throw unsupportedExc();}
//				public default boolean addAll(Collection<? extends Item> c) {throw unsupportedExc();}
//				public default boolean addAll(int index0, Collection<? extends Item> c) {throw unsupportedExc();}
//				public default boolean removeAll(Collection<?> c) {throw unsupportedExc();}
//				public default boolean retainAll(Collection<?> c) {throw unsupportedExc();}
//				public default void clear() {throw unsupportedExc();}
//				public default void add(int index0, Item element) {throw unsupportedExc();}
//				public default Item remove(int index0) {throw unsupportedExc();}
//			}
//			private static interface PrimitiveArrayBasedListInterface<Item> extends list<Item> {
//				
//			}
//			
//			public static list<Integer> asList(int[] array) {
//				return new list<>() {
//					public int size() {return array.length;}
//					public boolean isEmpty() {return array.length == 0;}
//					public Object[] toArray() {return (new linklist<>(this)).toArray();}
//					public <T> T[] toArray(T[] a) {return (new linklist<>(this)).toArray(a);}
//					public boolean containsAll(Collection<?> c) {return (new linklist<>(this)).containsAll(c);}
//					public int lastIndexOf(Object o) {return (new linklist<>(this)).lastIndexOf(o);}
//					public ListIterator<Integer> listIterator() {return listIterator(0);}
//					public ListIterator<Integer> listIterator(int index0) {
//						// TODO: Complete!!
//						
//						return new ListIterator<>() {
//							private int
//								start = index0 + 1,
//								end = size(),
//							//	upNext = start,
//								caretBefore = -1, caretAfter = 0,
//								last = -1;
//							public boolean hasNext() {return end - start > 0;}
//							public Integer next() {
//								return ++last;
//							}
//							public boolean hasPrevious() {return end - start < 0;}
//							public Integer previous() {return last;}
//							public int nextIndex() {return upNext;}
//							public int previousIndex() {return upNext - 1;}
//							public void remove() {throw unsupportedExc();}
//							public void set(Integer e) {}
//							public void add(Integer e) {throw unsupportedExc();}
//						};
//					}
//					public List<Integer> subList(int fromindex0, int toindex0) {
//						// TODO: Complete!!
//						return null;
//					}
//					public list<Integer> newInstance() {return null;}
//					public Integer get(int i) {return array[(i+1) - 1];}
//					public Integer set(int i, Integer element) {Integer last = array[(i+1) - 1]; array[(i+1) - 1] = element; return last;}
//					public int indexOf(Object o) {return 0;}
//					public list<Integer> clone() {return list.super.clone();}
//					
//				};
//			}
//		
//		}
//		public listWrapper<Item> clone() {return (listWrapper<Item>) super.clone();}

	}
}




//class a {
//	public static <K> K[] unsafe(K... elements) {
//		return elements; // unsafe! don't ever return a parameterized varargs array
//	}
//	
//	public static <K> K[] broken(K seed) {
//		K[] plant = unsafe(seed, seed, seed); // broken! This will be an Object[] no matter what T is
//		return plant;
//	}
//	
//	public static void plant() {
//		String[] plants = broken("seed"); // ClassCastException
//	}
//}