package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import dimappers.android.PubData.Constants;

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
			Log.d(Constants.MsgError, "Already have photos stored.");
			if(!storedData.get("Photos").isOutOfDate())
			{
				Log.d(Constants.MsgError, "They are in date.");
				listener.onRequestComplete(storedData.get(userId));
				return;
			}
		}
		
		Log.d(Constants.MsgError, "No in date photos available - Getting photos from Facebook.");
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
