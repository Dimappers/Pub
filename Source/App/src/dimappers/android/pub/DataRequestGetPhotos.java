package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class DataRequestGetPhotos implements IDataRequest<String, XmlJasonObject> {

	IPubService service;

	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<XmlJasonObject> listener,
			HashMap<String, XmlJasonObject> storedData) {
			
		long userId = service.GetActiveUser().getUserId();
		if(storedData.containsKey("Photos"))
		{
			if(!storedData.get(userId).isOutOfDate())
			{
				listener.onRequestComplete(storedData.get(userId));
				return;
			}
		}
		
		try {
			XmlJasonObject myPhotos = new XmlJasonObject(service.GetFacebook().request("me/photos"));
			storedData.put("Photos", myPhotos);
			listener.onRequestComplete(myPhotos);
			return;
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
	}

	public String getStoredDataId() {
		return "JSONObject";
	}

}
