//import java.util.Collection;
import java.util.List;
//import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;


public class linklist<Item> extends list.listWrapper<Item> {
	private static final long serialVersionUID = -8977313416296681213L;
	
//	public linklist<Item> newInstance() {return new linklist<>();}
	private static class LinkedListWithPublicModCount<E> extends java.util.LinkedList<E> {
		public int getModCount() {return modCount;}
		public void setModCount(int val) {modCount = val;}
	}
	private LinkedListWithPublicModCount<Item> enclosed; // I'd initialize it right while returning an instance from getInitialEnclosed()
	                                                     // but returning from there does not mean that its caller is init(). But the below
	                                                     // method is expected to be called by init() and calls init(java.util.List<Item>).
	protected void init(java.util.List<Item> toEnclose) {
		if (toEnclose instanceof LinkedListWithPublicModCount)
			enclosed = (LinkedListWithPublicModCount<Item>) toEnclose;
		super.init(toEnclose);
	}
	
	public boolean add(Item elm) {return doKeepModCount(() -> super.add(elm));}
	public void a(int ind, Item elm) {doKeepModCount(() -> super.a(ind, elm));}
	public Item s(int ind, Item elm) {return doKeepModCount(() -> super.s(ind, elm));}
	public Item rm(int ind) {return doKeepModCount(() -> super.rm(ind));}
	public boolean remove(Object elm) {return doKeepModCount(() -> super.remove(elm));}
	public boolean rm(Object elm) {return doKeepModCount(() -> super.remove(elm));}
	// clear() doesn't need to keep the modCount
	
	
	public list<Item> sub(int begin, int end) {
		
		list<Item> subListToEnclose = super.sub(begin, end);
		
		// Creating a direct implementation of list and implementing a
		// massive number of methods to manipulate structurally mutating
		// ones to keep the modCount could be made as well.
		list<Item> returnValue = new listWrapper<Item>() { // Wrap in this in an instance of a class overriding some methods...
			
			private int expectedModCount = linklist.this.modCount(); // Cache the same value (that subList acts according to)
			                                                         // because we can't see its non-public or protected copy
			                                                         // of expectedModCount.
			
			private <E> E doSetAndRevertModCount(java.util.function.Supplier<E> action) {
				int last = linklist.this.modCount();
				linklist.this.modCount(expectedModCount); // Set the count of the list instance to what this sublist expects,
				expectedModCount++; // update our cache with the one (from the SubList class instance) that we can't access,
				E retVal = action.get(); // perform structurally mutation,
				linklist.this.modCount(last); // then revert the count back.
				return retVal;
			}
			private void doSetAndRevertModCount(Runnable action) {
				doSetAndRevertModCount(() -> {action.run(); return null;});
			}
			
			// ...those set the modCount as expectedModCount then revert back!
			public boolean add(Item elm) {return doSetAndRevertModCount(() -> super.add(elm));}
			public void a(int ind, Item elm) {doSetAndRevertModCount(() -> super.a(ind, elm));}
			public Item s(int ind, Item elm) {return doSetAndRevertModCount(() -> super.s(ind, elm));}
			public Item rm(int ind) {return doSetAndRevertModCount(() -> super.rm(ind));}
			public boolean remove(Object elm) {return doSetAndRevertModCount(() -> super.remove(elm));}
			public boolean rm(Object elm) {return doSetAndRevertModCount(() -> super.remove(elm));}
			
			public list.listWrapper<Item> newInstance() {
				return new linklist<>();
			}
			protected List<Item> getInitialEnclosed() {
				return subListToEnclose;
			}};
		return returnValue;
	}
	
	
//	public list<Item> sub(int begin, int end) {
//		{
//			listWrapper<Item> sub = (listWrapper<Item>) super.sub(begin, end);
//			// TODO: Verify the above line.
//		}
//		list<Item> sub = (list<Item>) super.sub(begin, end);
//		// TODO: Verify the above line.
//		
//		return new listWrapper<>() {
//			public list.listWrapper<Item> newInstance() {return sub.newInstance();}
//			protected List<Item> getInitialEnclosed() {return sub;}
//			public listWrapper<Item> clone() {return sub.clone();}
//		};
//	}
	
	protected <E> E doKeepModCount(java.util.function.Supplier<E> modCountIncrementingAction) {
		int last = modCount();
		E retVal = modCountIncrementingAction.get();
		modCount(last);
		return retVal;
	}
	protected void doKeepModCount(Runnable modCountIncrementingAction) {doKeepModCount(() -> {modCountIncrementingAction.run(); return null;});}
	
	
	/** Warning: Using those and manipulating the modcount to structurally modify the list with different iterators and
	 *  backed sublists concurrently (even with just one thread at a time) is UNDEFINED just like in here:
	 *  
	 *  <p> https://youtu.be/8FOrfPpnhFI?t=28 (I was still laughing, even while writing the last word of the
	 *      last paragraph :D)
	 *  
	 *  <p>For the cause, just think about that you take sublists of [1, 2, 3, 4, 5, 6] as [1, 2] and [3, 4] then modify
	 *  the main list by adding 23 just after 2 (resulting into [1, 2, 23, 3, 4, 5, 6]). Which sublist(s) will mirror
	 *  the change? [1, 2] (to [1, 2, 23])? [3, 4] (to [23, 3, 4])? Or BOTH??! Even if such a scene comes with a
	 *  consistent result (like that doesn't depend on sth. like one of the sublists beginning's being closer to
	 *  the first or last node), there is not any guarantee that it changes across different JVM implementations.
	 */
	protected int modCount() {if (enclosed == null) return -1; return enclosed.getModCount();}
	protected void modCount(int newValue) {if (enclosed == null) return; enclosed.setModCount(newValue);}
	
	public linklist<Item> newInstance() {return new linklist<>();}
	
	
	
//	protected void init() { // Override so that the call to this initializes the wrapped list variable with a linked-list
//		init(new java.util.LinkedList<>());
//	}
	
	// Override so that the initializer inits the wrapped list variable with a linked-list
	protected java.util.List<Item> getInitialEnclosed() {return new LinkedListWithPublicModCount<>();}
	
	public linklist() {
		super();
	}
	@SafeVarargs // Varargs is safe
	public/* <newItem extends Item>*/ linklist(/*new*/Item... eachItem) {super(eachItem);}
	
	public/* <newItem extends Item>*/ linklist(int quantity) {super(quantity);}
	@SuppressWarnings("unused")
	public/* <newItem extends Item>*/ linklist(int quantity, /*new*/Item defaultItem) {super(quantity, defaultItem);}
	public linklist(Iterable<Item> collection) {super(collection);}
	
	public <newItem> linklist(Function<newItem, Item> expr, Iterable<newItem>     collection, Function<newItem, Boolean>     cond) {
		super(expr, collection, cond);
	}
	public <newItem> linklist(Function<newItem, Item> expr, Iterable<newItem>     collection                                     ) {
		super(expr, collection      );
	}
	public/* <newItem>*/ linklist(                          Iterable</*new*/Item> collection, Function</*new*/Item, Boolean> cond) {
		super(      collection, cond);
	}
	
	public linklist(Consumer<list<Item>> initializer) {super(initializer);}
	
	public linklist(Function<Integer, Item> expr,            int end               ) {super(expr,        end           );}
	public linklist(Function<Integer, Item> expr, int start, int end               ) {super(expr, start, end           );}
	public linklist(Function<Integer, Item> expr, int start, int end, int increment) {super(expr, start, end, increment);}
	
	public linklist(Function<Integer, Item> expr,            int end,                Function<Integer, Boolean> cond) {super(expr,        end,            cond);}
	public linklist(Function<Integer, Item> expr, int start, int end,                Function<Integer, Boolean> cond) {super(expr, start, end,            cond);}
	public linklist(Function<Integer, Item> expr, int start, int end, int increment, Function<Integer, Boolean> cond) {super(expr, start, end, increment, cond);}
	
	
	
	public linklist<Item> clone() {return (linklist<Item>) super.clone();}
	
	
	
	public static <H> list<H>       fromArray(H[]       arr) {return new linklist<>(arr);}
	public static     list<Byte>    fromArray(byte[]    arr) {list<Byte>    ls = new linklist<>(); for (byte    elm: arr) ls.add(elm); return ls;}
	public static     list<Integer> fromArray(int[]     arr) {list<Integer> ls = new linklist<>(); for (int     elm: arr) ls.add(elm); return ls;}
	public static     list<Boolean> fromArray(boolean[] arr) {list<Boolean> ls = new linklist<>(); for (boolean elm: arr) ls.add(elm); return ls;}
	public static     list<Long>    fromArray(long[]    arr) {list<Long>    ls = new linklist<>(); for (long    elm: arr) ls.add(elm); return ls;}
	public static     list<Double>  fromArray(double[]  arr) {list<Double>  ls = new linklist<>(); for (double  elm: arr) ls.add(elm); return ls;}

}
