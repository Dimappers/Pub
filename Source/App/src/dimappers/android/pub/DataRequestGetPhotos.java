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
			if(!storedData.get("Photos").isOutOfDate())
			{
				listener.onRequestComplete(storedData.get(userId));
				return;
			}
		}
		
		XmlJasonObject myPhotos;
		try {
			myPhotos = new XmlJasonObject(service.GetFacebook().request("me/photos"));
			storedData.put("Photos", myPhotos);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
		
		listener.onRequestComplete(myPhotos);
	}

	public String getStoredDataId() {
		return "JSONObject";
	}

}
