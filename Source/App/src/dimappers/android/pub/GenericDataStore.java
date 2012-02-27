package dimappers.android.pub;

public class GenericDataStore<T> {
	
	T store;
	public GenericDataStore()
	{
		store = null;
	}
	
	public T getStore()
	{
		return store;
	}
	
	public void setStore(T t)
	{
		store = t;
	}

}
