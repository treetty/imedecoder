package com.misingularity.util;

import java.util.ArrayList;

/**
 * This is utility maintains to bidirection mapping between key and id.
 * Created by xiaoyun on 1/20/15.
 */
public class KeyIdMap {

    private static class Node {
        public String key;
        public int index;
        public Node next = null;

        public Node(String k, int i) {
            key = k;
            index = i;
        }
    }

    private Node[] nodes = null;
    private ArrayList<String> keys = null;

    public KeyIdMap(ArrayList<String> keys) {
        this.keys = keys;
        this.keys.trimToSize();
        // We will use the standard 0.75f hit rate.
        nodes = new Node[(int)MathUtils.nextPrime(4*keys.size()/3)];
        for (int i = 0; i < keys.size(); ++i) {
            int hashCode = MurmurHash.hash32(keys.get(i));
            int index = (hashCode % nodes.length + nodes.length)%nodes.length;
            if (nodes[index] == null) {
                nodes[index] = new Node(keys.get(i), i);
            } else {
                Node node = nodes[index];
                while (node.next != null) node = node.next;
                node.next = new Node(keys.get(i), i);
            }
        }
    }

    /**
     * @param index
     * @return the key at the index position.
     */
    public String getKey(int index) {
        return keys.get(index);
    }

    /**
     * @return the number of keys in the map.
     */
    public int size() {
        return keys.size();
    }

    /**
     * @param key
     * @return the index of the key.
     */
    public int getIndex(String key) {
        int hashCode = MurmurHash.hash32(key);
        int index = (hashCode % nodes.length + nodes.length)%nodes.length;
        Node node = nodes[index];
        while (node != null) {
            if (node.key.equals(key)) {
                return node.index;
            } else {
                node = node.next;
            }
        }
        return -1;
    }
}
