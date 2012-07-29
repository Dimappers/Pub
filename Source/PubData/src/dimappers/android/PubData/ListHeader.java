package dimappers.android.PubData;

public class ListHeader implements EventListItem {
	
	private String header;
	
	public ListHeader(String header)
	{
		this.header = header;
	}
	
	public String getHeader() {return header;}

}
