package dimappers.android.pub;

import com.google.api.client.util.Key;

public class PlaceDetail {
	
 @Key
 public String status;
	
 @Key
 public Place_Detailed result;
 
 @Override
 public String toString() {
  if (result!=null) {
   return result.toString();
  }
  return super.toString();
 }
}