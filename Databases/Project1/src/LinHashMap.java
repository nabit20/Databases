/************************************************************************************
 * @file LinHashMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Linear Hashing algorithm.
 * A hash table is created that is an array of buckets.
 */
public class LinHashMap <K, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, Map <K, V>
{
    /** The number of slots (for key-value pairs) per bucket.
     */
    private static final int SLOTS = 4;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket
    {
        int    nKeys;
        K []   key;
        V []   value;
        Bucket next;

        @SuppressWarnings("unchecked")
        Bucket (Bucket n)
        {
            nKeys = 0;
            key   = (K []) Array.newInstance (classK, SLOTS);
            value = (V []) Array.newInstance (classV, SLOTS);
            next  = n;
        } // constructor
    } // Bucket inner class

    /** The list of buckets making up the hash table.
     */
    private final List <Bucket> hTable;

    /** The modulus for low resolution hashing
     */
    private int mod1;

    /** The modulus for high resolution hashing
     */
    private int mod2;

    /** Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;

    /** The index of the next bucket to split.
     */
    private int split = 0;

    /********************************************************************************
     * Construct a hash table that uses Linear Hashing.
     * @param classK    the class for keys (K)
     * @param classV    the class for keys (V)
     * @param initSize  the initial number of home buckets (a power of 2, e.g., 4)
     */
    public LinHashMap (Class <K> _classK, Class <V> _classV)    // , int initSize)
    {
        classK = _classK;
        classV = _classV;
        hTable = new ArrayList <> ();
        mod1   = 4;                        // initSize;
        mod2   = 2 * mod1;
    } // constructor

    /*
     * Add buckets to the hash table
     */
    public void addBuckets(){
    	for(int i = 0; i< 10; i++){
    		Bucket b1 = null;
    		b1 = new Bucket(b1);
    		hTable.add(b1);
    	}
    }
    
    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> ();

        //  T O   B E   I M P L E M E N T E D
            
        return enSet;
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the hash table.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    public V get (Object key)
    {
        int i = h (key);
        V value = null;

        for(int h = 0; h < hTable.size(); h++){     	
	        for(int j = 0; j < hTable.get(h).key.length; j++)
	        {
	        	if(hTable.get(h).key[j] == key){
	        		value = hTable.get(h).value[j];
	        	}
	        }
        }
        //  T O   B E   I M P L E M E N T E D

        return value;
    } // get

    /********************************************************************************
     * Put the key-value pair in the hash table.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     */
    public V put (K key, V value)
    {
        int i = h (key);
        boolean inserted = false; //Determine whether to exit loop to add or not
        out.println ("LinearHashMap.put: key = " + key + ", h() = " + i + ", value = " + value);
        
        for(int j = 0; j < hTable.get(i).key.length; j++) // Case of inserting into index i
        {
        	if(hTable.get(i).key[j] == null && hTable.get(i).value[j] == null){
        		count++;
        		hTable.get(i).key[j] = key;  //adding key
        		hTable.get(i).value[j] = value; //adding value
        		inserted = true;
        		return null;
        	}
        }
        
        while(inserted == false){
        	int counter = 1; // Counter to increment the index
        	for(int j = 0; j < hTable.get(i+counter).key.length; j++){ //Case of index i filled, by open addressing we look for the next available index to insert
            	if(hTable.get(i+counter).key[j] == null && hTable.get(i+counter).value[j] == null){
            		count++;
            		hTable.get(i+counter).key[j] = key; 
            		hTable.get(i+counter).value[j] = value;
            		inserted = true;
            		return null;
            	}
        	}
        	if(i + counter - 1 == hTable.get(i = i - 1).key.length){
            	for(int j = 0; j < hTable.get(i = i - 1).key.length; j++){ //Case where the index is the last one, then we move to the first index to insert
                	if(hTable.get(i = i - 1).key[j] == null && hTable.get(i = i - 1).key[j] == null){
                		count++;
                		hTable.get(i = i - 1).key[j] = key; 
                		hTable.get(i = i - 1).value[j] = value;
                		inserted = true;
                		return null;
                	}
            	}
        	}
        	else{
        		counter++;
        	}//else
        }	
        //  T O   B E   I M P L E M E N T E D

        return null;
    } // put

    /********************************************************************************
     * Return the size (SLOTS * number of home buckets) of the hash table. 
     * @return  the size of the hash table
     */
    public int size ()
    {
        return SLOTS * (mod1 + split);
    } // size

    /********************************************************************************
     * Print the hash table.
     */
    private void print ()
    {
        out.println ("Hash Table (Linear Hashing)");
        out.println ("-------------------------------------------");

        for(int h = 0; h < hTable.size(); h++){ //Prints the hash table, everything below
        	out.println("Bucket" + h);
        	out.println();
	        for(int j = 0; j < hTable.get(h).key.length; j++)
	        {
	        	
	        	out.println("index " + j + ": " + hTable.get(h).value[j]);
	        	
	        }
	        out.println("-----------------------------------------------");
        }
        
        //  T O   B E   I M P L E M E N T E D

        out.println ("-------------------------------------------");
    } // print

    /********************************************************************************
     * Hash the key using the low resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h (Object key)
    {
        return key.hashCode () % mod1;
    } // h

    /********************************************************************************
     * Hash the key using the high resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h2 (Object key)
    {
        return key.hashCode () % mod2;
    } // h2

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
    	
        int totalKeys    = 30;
        boolean RANDOMLY = false;
        
        LinHashMap <Integer, Integer> ht = new LinHashMap <> (Integer.class, Integer.class);
        ht.addBuckets();
        if (args.length == 1) totalKeys = Integer.valueOf (args [0]);

        
        if (RANDOMLY) {
            Random rng = new Random ();
            for (int i = 1; i <= totalKeys; i += 2) ht.put (rng.nextInt (2 * totalKeys), i * i);
        } else {
            for (int i = 1; i <= totalKeys; i += 2) ht.put (i, i * i);
        } // if

        ht.print ();
        for (int i = 0; i <= totalKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.count / (double) totalKeys);
    } // main

} // LinHashMap class
