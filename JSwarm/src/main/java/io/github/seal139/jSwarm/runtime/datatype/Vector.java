package io.github.seal139.jSwarm.runtime.datatype;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provide as huge data chunk for parallel process. The purpose of this class it to provides
 * a way to synchronize data from / to device memory for further processing. Data stored here
 * is stored in native side and unmanaged. However, automatic garbage collection should be work as usual
 * by performing memory deletion upon GC call.
 * 
 * <br/>
 * 
 * As information, Java memory and native memory are two separated things. this will be a problem because
 * the device memory can only work with native memory, not Java memory and can cause data duplication that
 * can easily exhaust memory. As workaround, right after status is {@link CollectionStatus SYNCHRONIZED},
 * we will move data from native into Java. Rendering this vector is in read only state  
 * 
 * 
 * @param <T>
 */
public abstract class Vector<T extends Number> implements List<T> {
	
	protected enum DataType {
		/**
		 * Half precision 8 bit floating point. Currently not supported
		 */
		HALF,
		
		/**
		 * Single precision 16 bit floating point.
		 */
		FLOAT,
		
		/**
		 * Double precision 32 bit floating point.
		 */
		DOUBLE,
		
		/**
		 * 8 bit signed integer. Currently not supported
		 */
		BYTE,
		
		/**
		 * 16 bit signed integer.
		 */
		SHORT,
		
		/**
		 * 32 bit signed integer.
		 */
		INTEGER,
		
		/**
		 * 64 bit signed integer.
		 */
		LONG
	}
	
	public enum CollectionStatus {
		/**
		 * Memory space initialized in native-host side
		 */
		INITIALIZED,
		
		/**
		 * Memory space deallocated in native-host side
		 */
		DESTROYED,
		
		/**
		 * Data is synchronized from / to device. Further data modification is not allowed.
		 */
		SYNCHRONIZED
	}
	
	public enum Purpose {
		
		/**
		 * Indicates this data is used as input
		 */
		PARAM_ARGS,
		
		/**
		 * Indicates this data is used as output 
		 */
		PARAM_RETURN
	}
	
	protected static native long init    (int type);	// malloc
	protected static native void destroy (long handle);	// delete
	
	protected native void insert    (long handle, int type, T[] buffer, int size); // ptr add
	protected native T    update    (long handle, int type, int index, T value);   // ptr set
	protected native void persist   (long handle);                                 // finalize data and sync to device memory
	protected native T    fetch     (long handle, int type, int index);            // get
	protected native T[]  fetchBulk (long handle, int type, int index, int count); // get All

	
	protected final long hwnd;
	protected final T[] buffer;
	protected final int maxBuffer;
	protected final Purpose purpose;
	protected final int type;
	
	protected int totalSize = 0;
	protected int bufferIdx = 0;
	
	@SuppressWarnings("unchecked")
	protected Vector(Purpose purpose, int bufferSize, DataType type) {
		this.maxBuffer = bufferSize;
		this.purpose   = purpose;
		this.type      = type.ordinal();
		this.hwnd      = init(this.type);
		this.buffer    = (T[]) new Object[bufferSize];
	}
	
	@Override
	public boolean add(T e) {
		totalSize +=1;
		buffer[bufferIdx++] = e;

		if(bufferIdx >= maxBuffer) {
			insert(hwnd, type, buffer, bufferIdx);
			bufferIdx = 0;
		}
		
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		c.forEach(v -> add(v));
		
		return true;
	}

	@Override
	public int size() {
		return totalSize;
	}

	@Override
	public boolean isEmpty() {
		return 0 == size();
	}
	
	@Override
	public Iterator<T> iterator() {
		
		return new Iterator<T>() {
			int offset     = 0;
			int index      = 0;
			int localIndex = 0;
			
			private T[] ctr;
			
			private boolean fetch() {
				if(offset >= totalSize) {
					return false;
				}
				
				int count = totalSize - offset;
				if(count > maxBuffer) {
					count = maxBuffer;
				}
				
				ctr = fetchBulk(hwnd, type, offset, maxBuffer);
				offset += count;
				
				return true;
			}

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public T next() {
				if (!hasNext()) {
					throw new IllegalStateException("No more elements");
	            }
				
				if(localIndex >= ctr.length) {
					fetch();
				}
				 
				index++;
				return ctr[localIndex++];
			}
		};
	}

	@Override
	public void clear() {
		destroy(hwnd);
	}

	@Override
	public T get(int index) {
		return fetch(hwnd, type, index);
	}

	@Override
	public T set(int index, T element) {
		return update(hwnd, type, index, element);
	}

	public void persist() {
		persist(hwnd);
	}
	
	// Not supported
	
	@Override
	public Object[] toArray() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public <K> K[] toArray(K[] a) {
		throw new RuntimeException("Not supported");
	}
	
	@Override
	@Deprecated
	public void add(int index, T element) {
		throw new RuntimeException("Not supported");
	}
	
	@Override
	@Deprecated
	public T remove(int index) {
		throw new RuntimeException("Insert / update only");
	}

	@Override
	@Deprecated
	public int indexOf(Object o) {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public int lastIndexOf(Object o) {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public ListIterator<T> listIterator() {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public ListIterator<T> listIterator(int index) {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public List<T> subList(int fromIndex, int toIndex) {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("Insert / update only");
	}
	
	@Override
	@Deprecated
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("Insert / update only");
	}

	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new RuntimeException("Not supported");	
	}

	@Override
	@Deprecated
	public boolean contains(Object o) {
		throw new RuntimeException("Insert / update only");
	}
	
	@Override
	@Deprecated
	public boolean remove(Object o) {
		throw new RuntimeException("Insert / update only");
	}
	
	@Override
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Insert / update only");
	}
	
}
