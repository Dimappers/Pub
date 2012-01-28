package dimappers.android.PubData;

import java.io.Serializable;

public class AcknoledgementData implements Serializable{
	public int globalEventId;
	
	public AcknoledgementData(int globalEventId)
	{
		this.globalEventId = globalEventId;
	}
}
