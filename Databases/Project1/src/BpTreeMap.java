
/************************************************************************************
 * @file BpTreeMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import internal.database.BpTree;
import internal.database.BpTree.Node;
import internal.database.BpTree.Pair;

import static java.lang.System.out;

/************************************************************************************
 * This class provides B+Tree maps.  B+Trees are used as multi-level index structures
 * that provide efficient access for both point queries and range queries.
 * All keys will be at the leaf level with leaf nodes linked by references.
 * Internal nodes will contain divider keys such that divKey corresponds to the
 * largest key in its left subtree.
 */
public class BpTreeMap <K extends Comparable <K>, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, SortedMap <K, V>
{
    /** The maximum fanout (number of children) for a B+Tree node.
     *  May wish to increase for better performance for Program 3.
     */
    private static final int ORDER = 5;

    /** The floor of half the ORDER.
     */
    private static final int MID = ORDER / 2;

    /** The debug flag
     */
    private static final boolean DEBUG = true;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines nodes that are stored in the B+tree map.
     */
    private class Node
    {
        boolean   isLeaf;
        int       nKeys;
        K []      key;
        Object [] ref;

        @SuppressWarnings("unchecked")
        Node (boolean _isLeaf)
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = (K []) Array.newInstance (classK, ORDER - 1);
            if (isLeaf) {
                //ref = (V []) Array.newInstance (classV, ORDER);
                ref = new Object [ORDER];
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            } // if
        } // constructor
    } // Node inner class

    /** The root of the B+Tree
     */
    private Node root;

    /** The first (leftmost) leaf in the B+Tree
     */
    private final Node firstLeaf;

    /** The counter for the number nodes accessed (for performance testing).
     */
    private int count = 0;

    
    
    

    
    /********************************************************************************
     * Construct an empty B+Tree map.
     * @param _classK  the class for keys (K)
     * @param _classV  the class for values (V)
     */
    public BpTreeMap (Class <K> _classK, Class <V> _classV)
    {
        classK    = _classK;
        classV    = _classV;
        root      = new Node (true);
        firstLeaf = root;
    } // constructor

    
    

    
    
    /********************************************************************************
     * Return null to use the natural order based on the key type.  This requires the
     * key type to implement Comparable.
     */
    public Comparator <? super K> comparator () 
    {
        return null;
    } // comparator

    
    
    
    
    
    
    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet () //unfinished
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> ();

        //leaf level contains all values, and each node contains a last pointer that will lead to sibling
        //use enset.add("key", "value") to add key value pair
        K key;
        V val;
        
        Node n = root;
        while (n.isLeaf == false) {
         //get to first key
         n = (Node) n.ref[0];
        }
        int i = 0; 
        //go until the last reference is null (means there's no sibling leaf node)
        for (int j = 0; j < n.nKeys; j++) {
         key = n.key[j];
         val = (V) n.ref[j];
         
         enSet.add(new AbstractMap.SimpleEntry <K, V> (key, val));
         
         if (j == n.nKeys-1 && n.ref[n.ref.length-1] == null) {
          break;
         }
        }
        return enSet;
    } // entrySet

    
    
    
    

    
    /********************************************************************************
     * Given the key, look up the value in the B+Tree map.
     * @param key  the key used for look up
     * @return  the value associated with the key or null if not found
     */
    @SuppressWarnings("unchecked")
    public V get (Object key)
    {
        return find ((K) key, root);
    } // get

    
    
    
    
    

    
    /********************************************************************************
     * Put the key-value pair in the B+Tree map.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null, not the previous value for this key
     */
    public V put (K key, V value)
    {
        insert (key, value, root);
        return null;
    } // put

    
    
    
    
 
    
    /********************************************************************************
     * Return the first (smallest) key in the B+Tree map.
     * @return  the first key in the B+Tree map.
     */
    @SuppressWarnings("unchecked")
	public K firstKey () //could also return var firstLeaf?
    {
    	count++;
    	
    	Node n = root;
    	while (n.isLeaf == false) {
    		n = (Node) n.ref[0]; //take first node down until leaf node is reached
    	}
    	
        return n.key[0]; //then return first key of bottom leaf node
    } // firstKey

    
    
    
  
    
    
    /********************************************************************************
     * Return the last (largest) key in the B+Tree map.
     * @return  the last key in the B+Tree map.
     */
    @SuppressWarnings("unchecked")
	public K lastKey () 
    {
    	count++;
    	
    	Node n = root;
    	while (n.isLeaf == false) {
    		n = (Node) n.ref[n.nKeys];
    	}

        return n.key[n.nKeys-1];
    } // lastKey

    
    
    
    
 
    
    
    /********************************************************************************
     * Return the portion of the B+Tree map where key < toKey.
     * @return  the submap with keys in the range [firstKey, toKey)
     */
    public SortedMap <K,V> headMap (K toKey)
    {
    	Node current = root;
		while (!current.isLeaf){
			boolean found = false;
			for (int i = 0; i < current.nKeys; i++){
				if (toKey.compareTo(current.key[i]) >= 0){
					current = (Node) current.ref[i + 1];
					found = true;
					break;
				}
			}
			if (!found){
				current = (Node) current.ref[0];
			}
		}

		SortedMap<K, V> results = new TreeMap<>();
		for (int i = 0; i < current.nKeys; i++){
			if (current.key[i].compareTo(toKey) < 0){
				results.put(current.key[i], (V) current.ref[i]);
				continue;
			}
			break;
		}
		
		current = current.left;
		
		while(current != null){
			for (int i = 0; i < current.nKeys; i++){
				results.put(current.key[i], (V) current.ref[i]);
			}
			
			current = current.left;
		}

        return results;
    } // headMap

    
    
    
  
    
    
    /********************************************************************************
     * Return the portion of the B+Tree map where fromKey <= key.
     * @return  the submap with keys in the range [fromKey, lastKey]
     */
    public SortedMap <K,V> tailMap (K fromKey)
    {
		Node current = root;
		while (!current.isLeaf){
			boolean found = false;
			for (int i = 0; i < current.nKeys; i++){
				if (fromKey.compareTo(current.key[i]) >= 0){
					current = (Node) current.ref[i + 1];
					found = true;
					break;
				}
			}
			if (!found){
				current = (Node) current.ref[0];
			}
		}

		SortedMap<K, V> results = new TreeMap<>();
		for (int i = 0; i < current.nKeys; i++){
			if (current.key[i].compareTo(fromKey) >= 0){
				results.put(current.key[i], (V) current.ref[i]);
				continue;
			}
		}
		
		current = current.right;
		
		while(current != null){
			for (int i = 0; i < current.nKeys; i++){
				results.put(current.key[i], (V) current.ref[i]);
			}
			
			current = current.right;
		}
		
		return results;

    } // tailMap

    
    
    
    
    
    
    
    
    /********************************************************************************
     * Return the portion of the B+Tree map whose keys are between fromKey and toKey,
     * i.e., fromKey <= key < toKey.
     * @return  the submap with keys in the range [fromKey, toKey)
     */
    public SortedMap <K,V> subMap (K fromKey, K toKey)
    {
    	SortedMap head = headMap(toKey);
		SortedMap tail = tailMap(fromKey);
		head.keySet().retainAll(tail.keySet());
		return head;

    } // subMap

    
    
    
    
    
    
    /********************************************************************************
     * Return the size (number of keys) in the B+Tree.
     * @return  the size of the B+Tree
     */
    public int size ()
    {
        int sum = 0;
 
		Node smallest = root;
		while (!smallest.isLeaf){
			smallest = (Node) smallest.ref[0];
		}
		
		while(smallest != null){
			sum += smallest.nKeys;
			smallest = smallest.right;
		}

        return  sum;
    } // size

    
    
    
    
    
    
    /********************************************************************************
     * Print the B+Tree using a pre-order traveral and indenting each level.
     * @param n      the current node to print
     * @param level  the current level of the B+Tree
     */
    @SuppressWarnings("unchecked")
    private void print (Node n, int level)
    {
        out.println ("BpTreeMap");
        out.println ("-------------------------------------------");

        for (int j = 0; j < level; j++) out.print ("\t");
        out.print ("[ . ");
        for (int i = 0; i < n.nKeys; i++) out.print (n.key [i] + " . ");
        out.println ("]");
        if ( ! n.isLeaf) {
            for (int i = 0; i <= n.nKeys; i++) print ((Node) n.ref [i], level + 1);
        } // if

        out.println ("-------------------------------------------");
    } // print

    
    
    
    
    
    /********************************************************************************
     * Recursive helper function for finding a key in B+trees.
     * @param key  the key to find
     * @param ney  the current node
     */
    @SuppressWarnings("unchecked")
    private V find (K key, Node n)
    {
        count++;
        for (int i = 0; i < n.nKeys; i++) {
            K k_i = n.key [i];
            if (key.compareTo (k_i) <= 0) {
                if (n.isLeaf) {
                    return (key.equals (k_i)) ? (V) n.ref [i] : null;
                } else {
                    return find (key, (Node) n.ref [i]);
                } // if
            } // if
        } // for
        return (n.isLeaf) ? null : find (key, (Node) n.ref [n.nKeys]);
    } // find

    
    
    
    
    
    
    
    /********************************************************************************
     * Recursive helper function for inserting a key in B+trees.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @return  the node inserted into (may wish to return more information)
     */
    private Node insert (K key, V ref, Node n)
    {
        boolean inserted = false;
        if (n.isLeaf) {                                  // handle leaf node
            if (n.nKeys < ORDER - 1) {
                for (int i = 0; i < n.nKeys; i++) {
                    K k_i = n.key [i];
                    if (key.compareTo (k_i) < 0) {
                        wedgeL (key, ref, n, i);
                        inserted = true;
                        break;
                    } else if (key.equals (k_i)) {
                        out.println ("BpTreeMap.insert: attempt to insert duplicate key = " + key);
                        inserted = true;
                        break;
                    } // if
                } // for
                if (! inserted) wedgeL (key, ref, n, n.nKeys);
            } else {
                Node sib = splitL (key, ref, n);

                //  T O   B E   I M P L E M E N T E D
                
                //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                
                //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

            } // if

        } else {                                         // handle internal node

            //  T O   B E   I M P L E M E N T E D

        } // if

        if (DEBUG) print (root, 0);
        return null;                                     // FIX: return useful information
    } // insert

    
    
    
    
    
    
    
    
    
    /********************************************************************************
     * Wedge the key-ref pair into leaf node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedgeL (K key, V ref, Node n, int i)
    {
        for (int j = n.nKeys; j > i; j--) {
            n.key [j] = n.key [j-1];
            n.ref [j] = n.ref [j-1];
        } // for
        n.key [i] = key;
        n.ref [i] = ref;
        n.nKeys++;
    } // wedgeL

    /********************************************************************************
     * Wedge the key-ref pair into internal node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedgeI (K key, V ref, Node n, int i)
    {
        //	out.println ("wedgeI not implemented yet");

        //  T O   B E   I M P L E M E N T E D

    	//++++++++++++++++++++++++++++++++++++++++++++++++++++
    	
    	for (int j = n.nKeys; j > i; j--) {
			n.key[j] = n.key[j-1];
			n.ref[j+1] = n.ref[j];
		} // for
		n.key[i] = key;
		n.ref[i+1] = ref;
		n.nKeys++;
		
    	//++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    } // wedgeI

  
    
    
    
    
    /********************************************************************************
     * Split leaf node n and return the newly created right sibling node rt.
     * Split first (MID keys for both node n and node rt), then add the new key and ref.
     * @param key  the new key to insert
     * @param ref  the new value/node to insert
     * @param n    the current node
     * @return  the right sibling node (may wish to provide more information)
     */
    private Node splitL (K key, V ref, Node n)
    {
        // out.println ("splitL I think is correct");
        Node rt = new Node (true);

        int j = 0;
        for (int i = MID; i < ORDER-1; i++) { 
         rt.key[j] = n.key[i]; //put key from current node into rt
         rt.ref[j] = n.ref[i]; //update reference from current node into rt
         n.key[i] = null; //delete transfered node from current node
         n.nKeys--; //update nKey values in n and rt
         rt.nKeys++;
         
         if (i == MID) { //on first loop, update last reference to point to new rt
          n.ref[i] = (Node) rt;
         }
         else if (i == ORDER-2) { //on last loop, update last pointer from n to last pointer in rt
          rt.ref[rt.nKeys] = n.ref[ORDER-1];
          n.ref[ORDER-1] = null;
         }
         else { //otherwise, get rid of reference once transfered
          n.ref[i] = null;
         }
         j++;
        }
        //place into node **********FIND OUT WHERE IT NEEDS TO BE INSERTED
        if (key.compareTo(n.key[n.nKeys-1]) < 0) { //if new key is less
         //key belongs in n
         int place = 0;
         for (int i = 0; i < n.nKeys; i++) {
          if (key.compareTo(n.key[i]) < 0) {
           place++;
          }
         }
         wedgeL(key, ref, n, place);
        }
        else if (key.compareTo(n.key[n.nKeys-1]) == 0) {
         System.out.println("Split: Key already exists");
        }
        else { //new key is greater
         //key belongs in rt
         int place = 0;
         for (int i = 0; i < rt.nKeys; i++) {
          if (key.compareTo(rt.key[i]) < 0) {
           place++;
          }
         }
         wedgeL(key, ref, rt, place);
        }

        return rt;
    } // splitL

    
    

    
    
    
    /********************************************************************************
     * Split internal node n and return the newly created right sibling node rt.
     * Split first (MID keys for node n and MID-1 for node rt), then add the new key and ref.
     * @param key  the new key to insert
     * @param ref  the new value/node to insert
     * @param n    the current node
     * @return  the right sibling node (may wish to provide more information)
     */
    private Node splitI (K key, Node ref, Node n)
    {	
		List<Pair> pairs = new ArrayList<>();
		Object firstRef = n.ref[0];
        //out.println ("splitI not implemented yet");
        Node rt = new Node (false);

        //  T O   B E   I M P L E M E N T E D
        
		for (int i = 0; i < n.nKeys; i++){
			pairs.add(new Pair(n.key[i], n.ref[i + 1]));
		}
		pairs.add(new Pair(key, ref));
		
		Comparator<Pair> comparator = new Comparator<BpTree<K,V>.Pair>() {
			public int compare(Pair o1, Pair o2) {
				return o1.key.compareTo(o2.key);
			}
		};
		
		Collections.sort(pairs, comparator);
		
		n.ref[0] = firstRef;
		n.nKeys = 0;
		for (int i = 0; i < MID; i++){
			n.key[i] = pairs.get(i).key;
			n.ref[i + 1] = pairs.get(i).ref;
			n.nKeys++;
		}
		
		for (int i = MID; i < pairs.size(); i++){
			rt.key[i - MID] = pairs.get(i).key;
			rt.ref[i - MID+1] = pairs.get(i).ref;
			rt.nKeys++;
		}
		
        return rt;
    } // splitI

    
    
    
    
    
    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        int totalKeys    = 9;
        boolean RANDOMLY = false;

        BpTreeMap <Integer, Integer> bpt = new BpTreeMap <> (Integer.class, Integer.class);
        if (args.length == 1) totalKeys = Integer.valueOf (args [0]);
   
        if (RANDOMLY) {
            Random rng = new Random ();
            for (int i = 1; i <= totalKeys; i += 2) bpt.put (rng.nextInt (2 * totalKeys), i * i);
        } else {
            for (int i = 1; i <= totalKeys; i += 2) bpt.put (i, i * i);
        } // if

        bpt.print (bpt.root, 0);
        for (int i = 0; i <= totalKeys; i++) {
            out.println ("key = " + i + " value = " + bpt.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of nodes accessed = " + bpt.count / (double) totalKeys);
        
        out.println();
        out.println("----------------Testing-------------------");
        out.println("First key-- " + bpt.firstKey()); //added to test firstKey()
        out.println("Last key -- " + bpt.lastKey()); //added to test lastKey()
        out.println("Set view-- " + bpt.entrySet()); //added to test entrySet()
    } // main

} // BpTreeMap class
