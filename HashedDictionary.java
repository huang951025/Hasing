/**
 * Class: CIS211 Data Structures
 * Author: Wei Wen Huang
 * Date: 12/2/2019
 * Describe: finish HashedDictionary. Complete the add and remove methods. 
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashedDictionary<K, V> implements DictionaryInterface<K, V> {
	// The dictionary
	private int numberOfEntries;
	private static final int DEFAULT_CAPACITY = 5; // must be prime 
	private static final int MAX_CAPACITY = 10000;
	
	// The hash table
	private TableEntry<K, V>[] hashTable;
	private int tableSize;
	@SuppressWarnings("unused")
	private static final int MAX_SIZE = 2 * MAX_CAPACITY;
	private boolean initialized = false;
	private static final double MAX_LOAD_FACTOR = 0.5;	// Fraction of hash table
														// that can be filled
	public HashedDictionary() 	{
		this(DEFAULT_CAPACITY); // Call next constructor 
	} // end default constructor

	public HashedDictionary(int initialCapacity) {
		checkCapacity(initialCapacity);
		numberOfEntries = 0; // Dictionary is empty
		
		// Set up hash table:
		// Initial size of hash table is same as initialCapacity if it is prime;
		// otherwise increase it until it is prime size
		int tableSize = getNextPrime(initialCapacity);
		checkSize(tableSize); // Check for max array size
		
		// The cast is safe because the new array contains null entries
		@SuppressWarnings("unchecked")
		TableEntry<K, V>[] temp = (TableEntry<K, V>[])new TableEntry[tableSize];
		hashTable = temp;
		initialized = true;
	} // end constructor
		
	private void checkCapacity(int capacity) {
		if (capacity > MAX_CAPACITY)
			throw new IllegalStateException("Attempt to create an array whose capacity exceeds allowed " +
					"maximum of " + MAX_CAPACITY);
	} // end checkCapacity
	
	private int getNextPrime(int anInteger) {
		if(anInteger <= 0) {
			throw new RuntimeException();
		}
		if (anInteger % 2 == 0) anInteger++;
		while (!isPrime(anInteger))
			anInteger += 2;
		
		return anInteger;
	}
	
	private boolean isPrime(int anInteger) {
		boolean found = false;
		int d = 2;
		
		while (!found && (d <= anInteger / 2)) {
			found = anInteger % d == 0;
			d++;
		}
		
		return !found;
	}
	
	private void checkSize(int tableSize) {
		if (tableSize > MAX_CAPACITY)
			throw new IllegalStateException("Attempt to create an array whose capacity exceeds allowed " +
					"maximum of " + MAX_CAPACITY);
	} // end checkSize

	public void display() {
		for (int index = 0; index < hashTable.length; index++) {
	   	if (hashTable[index] == null)
	   		System.out.println("null ");
	   	else if (hashTable[index].isRemoved())
	   		System.out.println("Removed ");
	   	else
	   		System.out.println(hashTable[index].getKey() + " " +
	   				hashTable[index].getValue());
		} // end for
	     System.out.println();
	} // end display
		
	public V add(K key, V value) {
		/* Enter your code here */
		V preValue;
		
		//check is hash table is full 
		if(isFull()) 
			enlargeHashTable();
		int index = getHashIndex(key);
		index = probe(index, key);
		
		assert (index >= 0) && (index < hashTable.length);
		if((hashTable[index] == null) || hashTable[index].isRemoved()) {
			
			hashTable[index] = new TableEntry<K,V>(key, value);
			numberOfEntries++;
			tableSize++;
			preValue = null;
		}else {
			preValue = hashTable[index].getValue();
			hashTable[index].setValue(value);
			}
		return preValue;
	} // end add

	@SuppressWarnings("unused")
	private boolean isHashTableTooFull() {
		boolean notFull = false;
		int index = 0;
		
		while (!notFull && index < hashTable.length) {
			notFull = hashTable[index] == null;
			index++;
		}
		
		return !notFull;
	}
	
	private int probe(int index, K key) {
		boolean found = false;
		int removedStateIndex = -1; // Index of first location in removed state
		
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn()) {
				if (key.equals(hashTable[index].getKey()))
					found = true;	// Key found
				else				// Follow probe sequence
					index = (index + 1) % hashTable.length; // Linear probing
			}
			else { // Skip entries that were removed
				// Save index of first location in removed state
				if (removedStateIndex == -1)
					removedStateIndex = index;
				index = (index + 1) % hashTable.length;		// Linear probing
			} // end if
		} // end while
		// Assertion: Either key or null is found at hashTable[index]
		if (found || (removedStateIndex == -1))
			return index; // Index of either key or null
		else
			return removedStateIndex; // Index of an available location
	} //end probe

	//Precondition: checkInitialization has been called
	private void enlargeHashTable() {
		TableEntry<K, V>[] oldTable = hashTable;
		int oldSize = hashTable.length;
		int newSize = getNextPrime(oldSize + oldSize);
		
		// The cast is safe because the new array contains null entries
		@SuppressWarnings("unchecked")
		TableEntry<K, V>[] temp = (TableEntry<K, V>[])new TableEntry[newSize];
		hashTable = temp;
		numberOfEntries = 0;	// Reset number of dictionary entries, since
								// it will be incremented by add during rehash
		// Rehash dictionary entries from old array to the new and bigger
		// array; skip both null locations and removed entries
		for (int index = 0; index < oldSize; index++) {
			if ((oldTable[index] != null) && oldTable[index].isIn())
				add(oldTable[index].getKey(), oldTable[index].getValue());
		} // end for
	} // end enlargeHashTable

	public V remove(K key) {
		/* Enter your code here */
		V removedValue = null;
		int index = getHashIndex(key);
		index = locate(index, key);
		if(index != -1) {
			removedValue = hashTable[index].getValue();
			hashTable[index].setToRemoved();
			numberOfEntries--;
					}
		return removedValue;
	} // end remove

	public V getValue(K key) {
		checkInitialization();
		V result = null;
	      
	    int index = getHashIndex(key);
	    index = locate(index, key);

	    if (index != -1)
	    	result = hashTable[index].getValue(); // key found; get value
	    // Else key not found; return null
	    
	    return result;
	} // end getValue

	private void checkInitialization() {
		if (!initialized)
			throw new SecurityException("Array object is not initialized properly.");
	}
	
	private int locate(int index, K key) {
		boolean found = false;
		
		int count = 0;
		while( !found && (hashTable[index] != null) && count < hashTable.length){
	    	if (hashTable[index].isIn() && key.equals(hashTable[index].getKey()))
	    		found = true;	// Key found
            else				// follow probe sequence
            	index = (index + 1) % hashTable.length; // Linear probing
	    	count++;
	    } // end while
	    // Assertion: Either key or null is found at hashTable[index]
	    int result = -1;
	    if (found)
	    	result = index;
	    
	    return result;
	} // end locate
		
	public boolean contains(K key) {
		boolean result = false;
		int index = getHashIndex(key);
		index = locate(index, key);
		if(index != -1)
			result = true;
		return result;
	} // end contains

	public boolean isEmpty() {
		return numberOfEntries == 0;
	} // end isEmpty

	public boolean isFull()
	{
		return numberOfEntries == hashTable.length;
	} // end isFull

	public int getSize() {
		return numberOfEntries;
	} // end getSize

	public final void clear() { 
		for (int index = 0; index < hashTable.length; index++)
			hashTable[index] = null;

		numberOfEntries = 0;
	    //locationsUsed = 0;
	} // end clear

	public Iterator<K> getKeyIterator() { 
		return new KeyIterator();
	} // end getKeyIterator
		
	public Iterator<V> getValueIterator() {	
		return new ValueIterator();
	} // end getValueIterator
	
	private int getHashIndex(K key) {
		int hashIndex = key.hashCode() % hashTable.length; 
			
		if (hashIndex < 0) {
			hashIndex = hashIndex + hashTable.length;
		} // end if

		return hashIndex;
	} // end getHashIndex
		
	//private boolean isHashTableTooFull()
	//{
	//	return locationsUsed > MAX_LOAD_FACTOR * hashTable.length;
	//} // end isHashTableTooFull

	private class KeyIterator implements Iterator<K> {
		private int currentIndex; // current position in hash table
		private int numberLeft;   // number of entries left in iteration

		private KeyIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		} // end default constructor
		  
		public boolean hasNext() {
			return numberLeft > 0; 
		} // end hasNext
		  
		public K next() {
			K result = null;
		    
			if (hasNext()) {
				// Skip table locations that do not contain a current entry
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved())
					currentIndex++;
		      
				result = hashTable[currentIndex].getKey();
				numberLeft--;
				currentIndex++;
			}
			else
				throw new NoSuchElementException();
		      
			return result;
		} // end next
		  
		public void remove() {
			throw new UnsupportedOperationException();
		} // end remove
	} // end KeyIterator
		
	private class ValueIterator implements Iterator<V> {
		private int currentIndex;
		private int numberLeft; 
			
		private ValueIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		} // end default constructor
			
		public boolean hasNext() {
			return numberLeft > 0; 
		} // end hasNext
			
		public V next() {
			V result = null;
				
			if (hasNext()) {
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved()) 
					currentIndex++;
					
				result = hashTable[currentIndex].getValue();
				numberLeft--;
				currentIndex++;
			}
			else
				throw new NoSuchElementException();

			return result;
		} // end next
			
		public void remove() {
			throw new UnsupportedOperationException();
		} // end remove
	} // end ValueIterator

	private static class TableEntry<S, T> {
		
		private S key;
		private T value;
		private boolean inTable;
		
		private TableEntry(S searchKey, T dataValue) {
			
			key = searchKey;
			value = dataValue;
			inTable = true;
		}// end constructor
		
		private boolean isIn() {
			return inTable;
		}//end isIn
		
		private boolean isRemoved() {
			return !inTable;
		}// end isRemoved
		
		private void setToIn() {
			inTable = true;
		}
		
		private void setToRemoved(){
			
			inTable = false;
		}	
		
		
		public S getKey()
        {
        	return key;
        }
        public T getValue()
        {
        	return value;
        }
        
        public void setInTable() {
        	this.inTable = true;
        }
        public void setValue(T entryValue) {
        	this.value = entryValue;
        }
		
		/* Enter your code here */
	} // end TableEntry
} // end HashedDictionary2
