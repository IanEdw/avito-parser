package ru.ianedw.avitoparser.util;

import java.util.*;

public class CustomLinkedHashMap<K, V> implements Map<K, V> {
    private final HashMap<K, Node<K,V>> map;
    private final int maxSize;
    private Node<K,V> first;
    private Node<K,V> last;

    public CustomLinkedHashMap() {
        map = new HashMap<>();
        maxSize = -1;
    }

    public CustomLinkedHashMap(int maxSize) {
        map = new HashMap<>();
        this.maxSize = maxSize;
    }

    public void removeLast() {
        map.remove(last.key);
        unlinkLast();
    }

    public void removeFirst() {
        map.remove(first.key);
        unlinkFirst();
    }

    private Node<K,V> linkLast(V v, K key) {
        final Node<K,V> l = last;
        final Node<K,V> newNode = new Node<>(l, v, null, key);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        return newNode;
    }

    private Node<K,V> linkFirst(V v, K key) {
        final Node<K,V> f = first;
        final Node<K,V> newNode = new Node<>(null, v, f, key);
        first = newNode;
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }
        return newNode;
    }

    private void unlinkLast() {
        if (last == null) {
            return;
        }
        Node<K, V> l = last;
        final Node<K, V> prev = l.prev;
        l.prev = null; // help GC
        last = prev;
        if (prev == null) {
            first = null;
        } else {
            prev.next = null;
        }
    }

    private void unlinkFirst() {
        if (first == null) {
            return;
        }
        Node<K, V> f = first;
        final Node<K, V> next = f.next;
        f.next = null; // help GC
        first = next;
        if (next == null) {
            last = null;
        } else {
            next.prev = null;
        }
    }

    private V unlink(Node<K, V> x) {
        if (x == null) {
            return null;
        }
        final V element = x.value;
        final Node<K, V> next = x.next;
        final Node<K, V> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        return element;
    }

    public V putItLast(K key, V value) {
        return put(key, value);
    }

    public V putItFirst(K key, V value) {
        for (int i = map.size() + 1; i > maxSize; i--) {
            removeFirst();
        }
        Node<K,V> newNode = linkFirst(value, key);
        map.put(key, newNode);
        return newNode.value;
    }

    @Override
    public V put(K key, V value) {
        if (maxSize > 0) {
            for (int i = map.size() + 1; i > maxSize; i--) {
                removeFirst();
            }
        }
        Node<K,V> newNode = linkLast(value, key);
        map.put(key, newNode);
        return newNode.value;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key).value;
    }

    @Override
    public V remove(Object key) {
        return unlink(map.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            put(k, m.get(k));
        }
    }

    @Override
    public void clear() {
        map.clear();
        first = null;
        last = null;
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        Collection<V> collection = new ArrayList<>();
        for (Node<K, V> node : map.values()) {
            collection.add(node.value);
        }
        return collection;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, Node<K,V>>> entries = map.entrySet();
        Set<Entry<K, V>> result = new HashSet<>();
        for (Entry<K, Node<K,V>> entry : entries) {
            result.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().value));
        }
        return result;
    }

    private static class Node<K, V> {
        private Node<K, V> prev;
        private Node<K, V> next;
        private final V value;

        private final K key;

        public Node(Node<K, V> prev, V value, Node<K, V> next, K key) {
            this.prev = prev;
            this.next = next;
            this.value = value;
            this.key = key;
        }
    }
}
