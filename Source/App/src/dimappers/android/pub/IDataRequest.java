package dimappers.android.pub;

import java.util.ArrayList;
import java.util.HashMap;

public interface IDataRequest<K, T> {
	public void giveConnection(IPubService connectionInterface);
	public void performRequest(final IRequestListener<T> listener, HashMap<K, T> storedData);
	public String getStoredDataId(); //Should return a unique id unless sharing a HashMap with another DataRequest
}
