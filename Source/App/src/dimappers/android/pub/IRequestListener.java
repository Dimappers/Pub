package dimappers.android.pub;

public interface IRequestListener<DataType> {

	void onRequestComplete(DataType data);
	void onRequestFail(Exception e);
}
