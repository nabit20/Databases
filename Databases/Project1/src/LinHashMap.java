
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

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> ();
		
        for(int i = 0; x < hTable.size(); i++)
		{
			Bucket b = hTable.get(i);
			for(int j = 0; j < b.nKeys; j++)
			{
                enSet.add(new AbstractMap.SimpleEntry<>(b.key[j], b.value[j]));
            }
		}
            
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
        
		if(i < split)
		{
            i = h2 (key);
		}
		
        Bucket b = hTable.get(i);
        
		if(b.nKeys == 0)
		{
            return null;
        }
        
		while(b != null)
		{
            for(int j=0; j<b.nKeys; j++)
			{
                if(key.equals(b.key[j]))
				{
                    return b.value[j];
				}
            }
            b = b.next;
		}

        return null;
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
        if(i < split)
		{
            i = h2 (key);
		}
		out.println ("LinearHashMap.put: key = " + key + ", h() = " + i + ", value = " + value);
		
        Bucket b = hTable.get(i);
        
		if(b.nKeys < SLOTS)
		{
            b.key[b.nKeys] = key;
            b.value[b.nKeys] = value;
            b.nKeys++;
        }else
		{
            hTable.add(new Bucket(null));
            
			while(b.next != null)
			{
                b = b.next;
            }
            
			if(b.nKeys < SLOTS)
			{
                b.key[b.nKeys] = key;
                b.value[b.nKeys] = value;
                b.nKeys++;
            }
			else
			{
                b.next = new Bucket(null);
                b = b.next;
                b.key[b.nKeys] = key;
                b.value[b.nKeys] = value;
                b.nKeys++;
            }
            
			int numKeys = 0;
            
			for(int j = 0; j < hTable.size();j++)
			{
                Bucket bkt = hTable.get(j);
                
				do
				{
                    numKeys = numKeys + bkt.nKeys;
                    bkt = bkt.next;
                }while(bkt !=null);
            }
            
			double a = ((double)numKeys)/(SLOTS * mod1);
            
			if(a >= 1)
			{
                Bucket b2 = new Bucket(null);
                Bucket b3 = new Bucket(null);
                b = hTable.get(split);
               
			   for(int k = 0; k < b.nKeys; k++)
			   {
                    int z = h2(b.key[k]);
                    
					if(z == split)
					{
                        if(b2.next == null)
						{
                            b2.next = new Bucket(null);
                            b2 = b2.next;
                        }
                        b2.key[b2.nKeys] = b.key[k];
                        b2.value[b2.nKeys] = b.value[k];
                        b2.nKeys++;
                    }
					else
					{
                        if(b3.next == null)
						{
                            b3.next = new Bucket(null);
                            b3 = b3.next;
                        }
                        b3.key[b3.nKeys] = b.key[k];
                        b3.value[b3.nKeys] = b.value[k];
                    }
                }
                if(split == mod1 - 1)
				{
                    mod1 = mod1 * 2;
                    mod2 = mod1 * 2;
                    split = 0;
                }
				else
				{
                    split++;
                }
            }   
		}

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

        for(int i=0; i<hTable.size();i++)
		{
            out.print(i + ":");
            Bucket tmp = hTable.get(i);
            boolean chain = false;
            if(tmp.next != null)
			{
                chain = true;
            }
			if(chain)
			{
                out.print("[ ");
                
				for(int j = 0; j < SLOTS; j++)
				{
                    out.print(tmp.key[j]);
                    if(SLOTS != j+1)
					{
                        out.print(", ");
					}
					else
					{
                        out.print(" ] --> ");
					}
                }
				
                out.print("[ ");
				
                for(int k = 0; k < SLOTS; k++)
				{
                    out.print(tmp.next.key[k]);
                    if(SLOTS != k+1)
					{
                        out.print(" ]");
					}
                }
            }
			else
			{
                out.print("[ ");
                for(int l = 0; l < SLOTS; l++){
                    out.print(tmp.key[l]);
                    if(SLOTS != l+1)
                        out.print(", ");
                }
                out.print(" ]");
            }
            out.println();
		}

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
