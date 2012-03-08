package dimappers.android.pub;

import java.util.HashMap;

public class GenericDataStore<K, V> {
	
	HashMap<K, V> store;
	public GenericDataStore()
	{
		store = new HashMap<K, V>();
	}
	
	public HashMap<K, V> getStore()
	{
		return store;
	}
	
	public void setStore(HashMap<K, V> t)
	{
		store = t;
	}
	
	public void addEntry(K key, V value)
	{
		
	}

}
