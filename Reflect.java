import java.io.IOException;
//import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Supplier;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.function.Function;

public class Reflect {
	
	private Reflect() {}
	
	// TODO: Place the following to appropriate location
	/* 
	 * Let me tell you how i feel,
	 * I never felt a love so real.
	 * Like an island from a cloud,
	 * you'd picked me somehow.
	 * 
	 * I was yours and you were mine,
	 * in another place and time.
	 * Like 2 islands on the sea,
	 * separated endlessly...
	 * 
	 * ...
	 * 
	 * Like 2 islands,
	 * like 2 islands,
	 * like 2 islands,
	 * separated endlessly... 💗😭
	 * 
	 *  — QED, in “Islands”, DJ Tinman & HhH remix (Natural HhHienspiration),
	 *    https://www.youtube.com/watch?v=E92IVbOPewk&t=1m24s
	 *    (please ignore the background video which ruins the f**k out
	 *    of the ambiance)
	 *    WARNING: An awful scene will welcome you right away at that timestamp
	 *    when you got to that link!
	 * 
	 * These are the lines that remind me of a situation where 2 very large
	 * sets of classes under 2 different classloaders (neither of which is parent
	 * of other), kind of 2 islands each in another place and time than other
	 * and on the sea, sandboxed and separated endlessly 🥹
	 * 
	 * Lastly, when you create 2 classloaders capable of finding the same set of
	 * the class bytecodes, that becomes like the parallel universes! 🫠  
	 */
	
	// FIXME: Add support for modules!!!
	/*  Here are something about Java modules with my words, from what I've learned.
	 *  Modules add additional encapsulation in the code and make it easy for the
	 *  code to be written as separate pieces that are less dependent on each other
	 *  (decreasing coupling) and as cumulated small pieces each containing classes
	 *  that are dependent on many of them (increasing cohesion). Modules can define
	 *  additional restrictions on top of the access modifiers by keeping their “public”
	 *  classes from other modules.
	 *  
	 *  A classloader can have many modules but a module can have only one classloader.
	 *  And multiple modules have same or different named classes being in same
	 *  packages (and therefore completely unaware of each other just like 2 possibly same
	 *  classes in different classloaders which none of them is parent of other), but apparently
	 *  this is OK only in the compile-time, as the JVM in the runtime throws
	 *  LayerInstantiationException and says that the same package (even if they do not have
	 *  any same class) is in 2 modules.
	 *  
	 *  In stacktraces, compile-time and runtime errors, packages belonging to named
	 *  modules can be seen as moduleName/fullClassName such as the following:
	 *       - “java.base/java.lang.Object”
	 *       - “java.base/jdk.internal.misc.Unsafe”
	 *  latter of which you can't access (despite being public) just because of the
	 *  module encapsulations; that is that the module “java.base” does not
	 *  'exports' the package “jdk.internal.misc” to any of your modules --- so you
	 *  can't get Unsafe, because it is unsafe to get Unsafe xD just as public static
	 *  sun.misc.Unsafe sun.misc.unsafe.getUnsafe() says!
	 *  
	 *  Although Eclipse (don't know about IntelliJ Idea) defines every module as a
	 *  projects that you can add other modules as dependencies (just like you do to use
	 *  JavaFX) and you can't define multiple modules within a single projects, you can
	 *  compile all of them with javac at once by placing all sources of every module
	 *  into one of the sibling (having same direct parents) folders that has the same
	 *  name of the module, putting the relevant module-info.java right under the folder
	 *  of every module and giving all source files' (including module-infos') paths and
	 *  specifying the path of the parent file containing all the module folders as
	 *  “--module-source-path”. In Eclipse, I think the multiple modules can be handled
	 *  to be in a single project by linking all the source folders and omitting (except)
	 *  the module-info files into project hence breaking and ignoring the module
	 *  encapsulation to get the IDE accept the code, whereas the folder of the modules
	 *  can be compiled with javac occasionally to often to make sure the code written in
	 *  Eclipse did not actually break the module encapsulation.
	 *  
	 */
	
	public static final ClassLoader applicationClassLoader = ClassLoader.getSystemClassLoader();
	
//	private static java.util.function.Supplier<CustomClassLoader> existingCustomCL = null;
	
//	public static Class<?> defineClass(byte[] data/*, String className*/) {
//		return _getCustomClassLoader().invokeDefineClass(null, data);
//	}
	
//	private static CustomClassLoader getCustomClassLoader0(Function<String, byte[]> bytecodeGetter) {
//		// Null at the beginning because a fn.WeakReference object spans a size of at least a few pointers.
//		if (existingCustomCL == null)
//			existingCustomCL = fn.reference(
//				() -> new CustomClassLoader(ClassLoader.getSystemClassLoader(), bytecodeGetter)
//			);
//		return existingCustomCL.get();
//	}
	/** Initializes a new class loader if the existing one is garbage-collected.
	  * If there is any strong reference to any object, weak references to it won't be cleared by the GC. */
//	public static ClassLoader getCustomClassLoader(Function<String, byte[]> bytecodeGetter) {
//		return getCustomClassLoader0(bytecodeGetter);
//	}
	
	
//	public static ClassLoader getBootstrapCL() {return ClassLoader.getBootstrapClassLoader();} // = null
//	public static ClassLoader getExtensionCL() {return ClassLoader.getExtensionClassLoader();}
	public static ClassLoader getAppCL() {return ClassLoader.getSystemClassLoader();}
	
	
	// TODO: Maybe bother to test those????
	public static byte[] getBytecode(String classBinaryName) {
		return getBytecode(fn.getSecurityContext()[4-1].getClassLoader(), classBinaryName);
	}
	@SuppressWarnings("resource")
	public static byte[] getBytecode(ClassLoader loader, String classBinaryName) {
		String path = str.join(str.split(classBinaryName, "\\."), "/") + ".class";
		java.io.InputStream is = loader.getResourceAsStream(path);
		if (is == null) return null;
		try {return ByteIO.readAllBytes(is);}
		catch (IOException e) {throw new RuntimeException(e);}
	}
	
	
	public static String getProtectionDomainClassLocation(Class<?> theClass) {
		URL url = theClass.getProtectionDomain().getCodeSource().getLocation();
		try {
			return (new java.io.File(url.toURI())).getPath();
		}
		catch (URISyntaxException e) {throw new Error(e);}
//		return (new java.io.File()).getAbsolutePath();
	}
	
	
	
//	public static Object deserialize(java.io.InputStream stream) {return deserialize(stream, Object.class);}
	/** Returns an object constructed from the input stream. You give the input stream to it to read;
	 *  not get returned an output stream to write by yourself. */
	public static Object deserialize(java.io.InputStream stream) {
		java.io.ObjectInputStream ois;
		Object obj = null;
		try {
			ois = new java.io.ObjectInputStream(stream);
			obj = ois.readObject(); ois.close();
		} catch (IOException e) {}
		catch (ClassNotFoundException e) {e.printStackTrace();}
		return obj;
	}
	
	/** Returns an input stream for you to get the data from the object; not gets an output stream
	 *  passed for itself to fill it with the data. The data the returned input stream is ready to
	 *  provide does not depend on the object anymore after the calling this. */
	public static java.io.InputStream serialize(Object obj) {
		// TODO: Use your implementation of byte output and input streams instead!
		java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
		try {
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
			oos.writeObject(obj); oos.close();
		}
	//	catch (java.io.NotSerializableException e) {throw e;}
		catch (IOException e) {return null;}
		return new java.io.ByteArrayInputStream(os.toByteArray());
	}
	
	
	
	public static abstract class CustomClassLoader extends ClassLoader {
		
		private final String name;
		private final int id;
		public String getName() {return name;}
		// Overrides only on Java 9+ as no such function exists in java.lang.ClassLoader before that
		
		private static String getName(ClassLoader cl) {
			  try {
				return (String) ClassLoader.class.getMethod("getName").invoke(cl);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				return null;
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
		
////		public java.util.Map<String, byte[]> bytecodesToDefine = new java.util.HashMap<>();
//		private Function<String, byte[]> bytecodeGetter/* = (binaryName) -> {
//			return bytecodesToDefine.remove(binaryName);
//		}*/;
		
		/** Fetches and returns the bytecode for a class to be defined by this classloader. */
		protected abstract byte[] getByteCode(String binaryName);
		
		private java.util.Map<String, Class<?>> cachedFromParent = new java.util.HashMap<>();
		/* Since this loader already holds the parent and hence its classes, holding
		 * some of their classes also with that map does not leak memory.
		 * 
		 * I really hope that holding instances of classes does not prevent
		 * their bytecode data (not its state i.e. the static fields) from
		 * cleared from JVM's cache (and left to loadClass(String) or
		 * findClass(String) to re-read it from the same source
		 * upon calling them). Like, values of the static fields never vanish
		 * away, right? */
		
		private final java.util.Map<String, Object> loadedByThis = new java.util.HashMap<>();
		
		
		public CustomClassLoader(String name, ClassLoader parent/*, Function<String, byte[]> bytecodeGetter*/) {
			super(/*name, */parent); this.name = name;
			// Not calling the constructor taking String since the name is not supported prior to Java 9
//			this.bytecodeGetter = bytecodeGetter;
			id = -1;
		}
		public CustomClassLoader(ClassLoader parent/*, Function<String, byte[]> bytecodeGetter*/) {
			this(createName(), parent/*, bytecodeGetter*/);
		}
		
		private static final java.util.Map<Integer, Object>
			occupiedNumbers = new java.util.HashMap<>(); // Holds numbers for existing classloaders to properly name the new ones created without a name.
		private static String createName() { // Occupies a new number in the static map and returns a name generated from that
			String suffix = "CustomClassLoader #";
			synchronized (occupiedNumbers) {
				int[] nums = list.intArrayOf(new linklist<>(occupiedNumbers.keySet()));
				java.util.Arrays.sort(nums); // TODO: Sort yourself you stupid lazy*ss! Haven't you taken BBM204??
				int itr = 0;
				int val;
				for (int num: nums) { // [1, 2, 3, 5, 6, 9,...]
					itr++; //             1, 2, 3, 4,...
					if (itr != num)
						return suffix + itr;
				}
				return suffix + (itr+1);
			}
		}
		
		
		/* In the delegation model, if a classloader hasn't loaded a class (if has it just returns it from
		 * findLoadedClass(String)) it just doesn't greedily try to find its bytecode from somewhere, but
		 * first delegates the request to its parent and expects the java.lang.Class object from it and
		 * recursively the delegate reaches to the (primorder) Bootstrap ClassLoader (until any of the
		 * classLoaders on the way has already found and defined it from its bytecode therefore just returns
		 * it), then it tries to find it; if it can't, its child tries to find and finally the first
		 * ClassLoader (farthest from the BootStrap on the delegation chain) tries to find by its custom way
		 * (maybe download from the network, generate from somewhere or just load from a file just like the
		 * java.net.URLClassLoader “System ClassLoader”).
		 * 
		 * Here however, if this hasn't loaded the class, this tries to find and define it first, so it can load a
		 * class that has the same name with another one in its direct or one of indirect parents and can SHADOW THAT CLASS.
		 * In the normal delegation, that occurs only if one of the (in)direct children of [a classloader that CAN
		 * find it] has already found and defined it; it just returns it from findLoadedClass(String) even before
		 * forwarding the delegate to its parent.
		 * 
		 * Now you know that instances of classloader load a “Main” class by trying to redefine it even if their
		 * parents has already looaded one with the same binary name.
		 * 
		 * 
		 * But, what happens if I instantiate one (B), over a parent (let's say A) that can and has loaded class
		 * “Main” with a very useful method/field for one of my general-purpose classes like “fn” (functions) and
		 * let B load an altered “Main” that is for some kind of a sub-application and not even aware of what my
		 * usual “Main” looks like? When a method of a class on B call a method of “fn” and it calls that useful method
		 * on “Main”, will B directly try to get its own “Main” for “A”'s “fn” while fn normally would expect such “Main”
		 * like the one loaded on A? Or load right what “fn”'s method expects? It will act resembling C++'s logic of
		 * calling the version of overridden virtual method that DEPENDS on constructor/destructor of which the
		 * hell class' of the object the virtual method is invoked is currently the last frame of the callstack of
		 * the thread that invoked the method; that is, call to the virtual method “getArea” by the destructor of
		 * Shape class (or by any method being called by that destructor) while destructing a Rectangle object
		 * results in calling its Shape version, not the version of the class the object is an instance of
		 * (Rectangle). In that way; when you suppose that java.lang.String (from the bootstrap ClassLoader)
		 * referred to a class named “foo.bar.A”, defining the same class (named as “foo.bar.A”) on your classloader
		 * definitely would not cause your using of java.lang.String from that classloader to refer to (and use) your
		 * version of “foo.bar.A” instead of what it normally expects. However, what if it was so just because the
		 * things have come up that way while designing the Java runtime and has not had that emphasis on making it
		 * that way it has been made? Why not try to actually ensure this by, maybe trying to map a Classloader to
		 * the current thread when entering and removing it while exitting some method or something like that?
		 * 
		 */
		protected synchronized Class<?> loadClass(String binaryName, boolean resolve) throws ClassNotFoundException {
			String loaderName = getName1();
			String cName = simplifyName(binaryName);
			debug("loadClass of "+loaderName+" will load "+cName);
//			final boolean alreadyLoadedByThis = loadedByThis.containsKey(binaryName);
			Class<?> cls = findLoadedClass(binaryName);
			
			if (cls != null) {
				debug(cName + " was returned from "+loaderName+" because it has been defined on the same classloader");
				return cls; // Return simply if this has already defined it (and still has it cached in the memory).
			}
			
			cls = cachedFromParent.get(binaryName); // Just don't f.ing try to load (i.e. such a class like LinkedList) again each damn time it is requested
			if (cls != null) {
				debug(cName + " is not loaded on "+loaderName+" but loaded on and is got from the parent by using the cache here");
				return cls;
			}
			// ==================================== Reverse of the 'delegation' model ====================================
			// Otherwise try to find before leaving to the parent and delegating the request
			try {cls = findClass(binaryName);}
			catch (ClassNotFoundException e) {/*cls = null;*/} // Already null
			
			if (cls != null) return cls;
			
			debug("loadClass of "+loaderName+" is delegating the request to the parent classloader "+str.escapeString(getName(getParent()))+"...");
			cls = getParent().loadClass(binaryName); // If this classloader can't find and define then just delegate the request to the parent
			
			if (cls != null) {
				cachedFromParent.put(binaryName, cls); // Cache the class so this loader does not try to load again or wait for the delegate to return
				return cls;
			}
			// ==================================== --------------------------------- ====================================
			
			// If it can't give that class either, throw that exception.
			throw new ClassNotFoundException();
		}
		
		/** Invokes “defineClass” with the bytecode data */
		protected synchronized Class<?> findClass(String binaryName) throws ClassNotFoundException {
			final boolean alreadyHadLoadedByThis = loadedByThis.containsKey(binaryName);
			if (alreadyHadLoadedByThis) {
				// Loaded but the bytecode content is cleared from the cache (only field values are kept),
				// therefore the loader needs to re-read from the same source and pass to “defineclass” again)
				// Maybe the java.lang.Class instance, upon finding out that the bytecode it has just has been
				// cleared and only the class values (all first initialized at the class initialization) left,
				// can invoke it's classloader's right this method to make it re-find its source and re-read
				// to re-cache the bytecode.
				debug(binaryName+" had been loaded by "+getName1()+" but was cleared from the bytecode cache; therefore will be re-read from the same source");
			}
			else
				debug("findClass of " + getName1() + " will find and define " + simplifyName(binaryName));
			
			byte[] code = getByteCode(binaryName);
			if (code != null) {
				if (alreadyHadLoadedByThis) debug(getName1()+" is recaching "+simplifyName(binaryName)+" from a byte array...");
				else debug(getName1()+" is defining "+simplifyName(binaryName)+" from a byte array...");
				Class<?> cls = defineClass(binaryName, code);
				loadedByThis.put(binaryName, null);
				return cls;
			}
			debug("findClass of " + getName1() + " couldn\'t find " + simplifyName(binaryName));
	//		return null;
			throw new ClassNotFoundException();
		}
		
		// TODO: Maybe make protected?
		public synchronized Class<?> defineClass(String expectedBinaryName, byte[] bytecode) {
			Class<?> defined = super.defineClass(expectedBinaryName, bytecode, 0, bytecode.length);
			String name = defined.getName();
			if (cachedFromParent.containsKey(name)) {
				debug(name+" had already been loaded by the parent but a call to defineClass(String, byte[]) on "+getName1()+" shadowed it and cleared from "+getName1()+"\'s list of classes that have been cached from the parent.");
				cachedFromParent.remove(name);
				loadedByThis.put(name, null);
			}
			return defined;
		}
		
		// TODO: Maybe make protected?
		public synchronized Class<?> defineClass(byte[] bytecode) {
			return defineClass(null, bytecode);
		}
		
		
		
		public static String simplifyName(String binaryName) {
			if (binaryName == null) return "";
			list<String> names;
			String last;
			names = str.split(binaryName, "\\."); // “\\.” -> regex(“\\.”) -> regex(\.) -> <regex matching “.”>
			if (!(names.size() > 0)) return "<a class>";
			last = names.g(-1);
			/*Don't further simplify by chopping off the enclosing class names from the binary name */
//			names = str.splitString(last, "\\$");
//			if (names.size() > 0) last = names.g(-1);
//			else return "<a class>";
			return "the class " + last;
		}
		
		public static void debug(String text) {
			fn.flog(str.TextStyle.newStr().style(str.TextStyle.forRGB(60, 60, 60)).text("[Debug] " + text));
		//	fn.print(str.TextStyle.newStr().style(str.TextStyle.forRGB(60, 60, 60)).text("[Debug] " + text));
		}
		
		// Returns something like “a CustomClassLoader”
		protected String getName1() {
			String name = getName();
			if (name == null) return "a "+getClass().getSimpleName();
//			if (name == null) return "a classloader";
//			if (name == null) return "a CustomClassLoader";
			return /*"the classloader " + */str.escapeString(name);
		}
		
		protected void finalize() {
			synchronized (occupiedNumbers) {
				occupiedNumbers.remove(id);
			}
			final String name = getName1();
			fn.newThread(() -> {
				fn.sleep(0.01);
//				String name = getName();
//				if (name != null) name = str.escapeString(name);
				synchronized (fn.outStream()) {
					debug("GC has just collected ClassLoader "+name+" along with its classes");
//					debug("GC just collected "+((name!=null)?"the":"a")+" classloader" + ((name!=null)?(" "+name):""));
				}
			}).start();
			
		}
	}
	
	
	
	
	// ---------------------------------------------- Experimental stuff ----------------------------------------------
	/** Acts almost just like {@link java.util.function.Supplier&lt;T&gt;}
	 *  fn.reference(java.util.function.Supplier&lt;T&gt;), but holds a separate ClassLoader
	 *  to have the class that regenerates the method; in case the JVM's runtime Class objects
	 *  are ever something whose bytecodes are actually never cleared (unlike the native codes
	 *  JIT compiled from them) until their ClassLoader is garbage-collected along with all of
	 *  its classes and all instances of them. Because this returns a reference holding its class
	 *  in a separate ClassLoader, the whole method body to generate does not stay at the memory
	 *  until the ClassLoader whose class' method called this method (likely the JVM's built-in
	 *  Application ClassLoader) gets garbage-collected.
	 *  
	 *  @Deprecated
	 *  Because this method relies on the accessibility of the class of the passed object
	 *  over different ClassLoaders, and the existence of that class' valid no-arg constructor. Even a
	 *  f.ing lambda will cause an error/exception because lambdas do not have reflectively visible
	 *  constructors at the runtime.
	 *  */
	@Deprecated public static <T> Supplier<T> reference(Supplier<T> generator) {
		Class<? extends Supplier> c = generator.getClass();
		String binaryName = c.getName();
		
		ClassLoader cl = new Reflect.CustomClassLoader(Reflect.class.getClassLoader()) {
			protected byte[] getByteCode(String bn) {
				if (bn.equals(binaryName)) {
					String addr = str.join(str.split(bn, "\\."), "/")+".class";
					try {return ByteIO.readAllBytes(getParent().getResourceAsStream(addr));}
					catch (IOException e) {throw new RuntimeException(e);}
				}
				return null;
			}
		};
		
		java.lang.reflect.Constructor<? extends Supplier> con;
		try {
			con = (java.lang.reflect.Constructor<Supplier<T>>) cl.loadClass(binaryName).getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		con.setAccessible(true);
		
		/* FIXME: The object “generator” does not have to have a valid no-arg constructor
		 * that is accessible over a different classloader! We're here, but that object from
		 * a class redefined on “cl” is from a different classloader. We can be unable to
		 * instantiate it from here */
		try {
			generator = (Supplier) (con.newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return fn.cachedReference(generator);
	}
	// ----------------------------------------------------------------------------------------------------------------

	
	
	
	
	
}