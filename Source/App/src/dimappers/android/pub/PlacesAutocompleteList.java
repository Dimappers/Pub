package dimappers.android.pub;

import java.util.List;

import com.google.api.client.util.Key;
 
public class PlacesAutocompleteList {
 
 @Key
 public List<PlaceAutoComplete> predictions;
 
 public static class PlaceAutoComplete {
 
  @Key
  public String id;
 
  @Key
  public String description;
 
  @Key
  public String reference;
 
  @Override
  public String toString() {
   return description + " - " + id + " - " + reference;
  }
 
 }
}