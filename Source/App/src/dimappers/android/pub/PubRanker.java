package dimappers.android.pub;

import java.util.List;

import dimappers.android.PubData.PubLocation;

public class PubRanker {

	List<Place> pubList;
	
	PubRanker(List<Place> list) {
		pubList = list;
	}
	
	public PubLocation returnBest() {
		for(Place p : pubList) {
			if(p!=null) {return new PubLocation((float)p.geometry.location.lat,(float)p.geometry.location.lng,p.name);}
		}
		return null;
	}
}
