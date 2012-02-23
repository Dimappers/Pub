package dimappers.android.pub;

import java.util.List;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubLocation;

public class PubRanker {

	List<Place> pubList;
	
	PubRanker(List<Place> list) {
		pubList = list;
	}
	
	//Use when calling from PubFinding (i.e. during the Pending screen)
	public PubLocation returnBest(PubFinding pubFinding) {
    	pubFinding.updateProgress(Constants.ChoosingPub);
		return returnBest();
	}
	
	//Use at other times
	public PubLocation returnBest() {
		//TODO: Implement this
		for(Place p : pubList) {
			if(p!=null) {return new PubLocation((float)p.geometry.location.lat,(float)p.geometry.location.lng,p.name);}
		}
		return null;
	}
}
