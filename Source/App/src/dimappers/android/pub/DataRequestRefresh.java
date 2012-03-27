package dimappers.android.pub;

import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.User;

public class DataRequestRefresh implements IDataRequest<Long, PubEventArray> {

	private IPubService service;
	private boolean fullRefresh;
	
	public DataRequestRefresh(boolean fullRefresh)
	{
		this.fullRefresh = fullRefresh;
	}
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<PubEventArray> listener, HashMap<Long, PubEventArray> storedData) {
		Log.d(Constants.MsgInfo, "Running refresh");
		
		Document xmlRequest = new Document();
		Element root = new Element("Message");
		xmlRequest.setRootElement(root);
		
		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.addContent(MessageType.refreshMessage.toString());
		root.addContent(messageTypeElement);
		
		RefreshData refreshMessage = new RefreshData(service.GetActiveUser(), fullRefresh);
		root.addContent(refreshMessage.writeXml());
		
		Socket socket;
		try {
			socket = DataSender.sendDocument(xmlRequest);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		}  
		Document returnDocument;
		try
		{
			returnDocument = DataSender.readTillEndOfMessage(socket.getInputStream());
		} catch (Exception e)
		{
			listener.onRequestFail(e);
			return;
		}
		
		RefreshResponse response = new RefreshResponse(returnDocument.getRootElement().getChild(RefreshResponse.class.getSimpleName()));
		listener.onRequestComplete(new PubEventArray(response.getEvents()));	
	}

	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}
