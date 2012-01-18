package dimappers.android.PubData;

public class RefreshData {

	private String 	userId;
	private boolean fullUpdate;
	
	//Constructor
	public RefreshData(String userId, boolean fullUpdate) {
		this.userId 	= userId;
		this.fullUpdate = fullUpdate;
	}
	
	//Encapsulation
	public String 	getUserId() 		{ return this.userId; }
	public boolean 	isFullUpdate() 	{ return this.fullUpdate; }
}
