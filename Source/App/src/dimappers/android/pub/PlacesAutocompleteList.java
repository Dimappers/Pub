package dimappers.android.pub;

import java.util.List;

import com.google.api.client.util.Key;
 
public class PlacesAutocompleteList {
 
	@Key String status;
	
 @Key public List<PlaceAutoComplete> predictions;
 
 	public static class PlaceAutoComplete {
 		
 		@Key String description;
 		@Key String reference;
 		@Key List<Term> terms;
 		
 		
 		public String toString()
 		{
 			return description + ": " + reference;
 		}
 		
 		public static class Term {
 			@Key String value;
 			@Key int offset;
 		}
 	}

}