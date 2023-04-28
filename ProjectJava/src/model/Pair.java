package model;

/**
 * Pair of generic types of key and value
 * @author astratakis
 * @author petroud
 * @author gfraggias
 * @author aavraam
 *
 * @param <K> Key
 * @param <V> Value
 */
public class Pair<K, V> {
	
	/**
	 * Constructs a new pair of key and value
	 * @param key	Generic key
	 * @param value	Generic value
	 */
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public final K key;
	public final V value;

}
