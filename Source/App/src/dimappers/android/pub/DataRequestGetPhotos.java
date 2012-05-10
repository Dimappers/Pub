package dimappers.android.pub;

import java.util.HashMap;

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
			Log.d(Constants.MsgInfo, "Already have photos stored.");
			if(!storedData.get("Photos").isOutOfDate())
			{
				Log.d(Constants.MsgInfo, "They are in date.");
				listener.onRequestComplete(storedData.get("Photos"));
				return;
			}
		}
		
		Log.d(Constants.MsgWarning, "No in date photos available - Getting photos from Facebook.");
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
