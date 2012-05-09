package dimappers.android.pub;

import java.util.HashMap;

public interface IDataRequest<K, T> {
	public void giveConnection(IPubService connectionInterface);
	public void performRequest(final IRequestListener<T> listener, HashMap<K, T> storedData);
	public String getStoredDataId(); //Should return a unique string unless want to share store or null if no applicable store
}
