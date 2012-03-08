package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class DataRequestGetPhotos implements IDataRequest<Long, XmlJasonObject> {

	IPubService service;
	@Override
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	@Override
	public void performRequest(IRequestListener<XmlJasonObject> listener,
			HashMap<Long, XmlJasonObject> storedData) {
			
		long userId = service.GetActiveUser().getUserId();
		if(storedData.containsKey(userId))
		{
			if(!storedData.get(userId).isOutOfDate())
			{
				listener.onRequestComplete(storedData.get(userId));
				return;
			}
		}
		
		try {
			XmlJasonObject myPhotos = new XmlJasonObject(service.GetFacebook().request("me/photos"));
			storedData.put(userId, myPhotos);
			listener.onRequestComplete(myPhotos);
			return;
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
	}

	@Override
	public String getStoredDataId() {
		return "Photos";
	}

}
