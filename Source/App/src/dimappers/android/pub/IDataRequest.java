package dimappers.android.pub;

public interface IDataRequest<DataType, RequestListener extends IRequestListener<DataType>> {
	public void performRequest(RequestListener listener);
	public void performRequest(RequestListener listener, GenericDataStore<DataType> storedData);
}
