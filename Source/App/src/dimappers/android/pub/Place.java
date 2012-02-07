package dimappers.android.pub;

import com.google.api.client.util.Key;

public class Place {
 
 @Key
 public String id;
 
 @Key
 public String name;
  
 @Key
 public String reference;
 
 @Key
 public Geometry geometry;
 
 @Key
 public String icon;
 
 @Key
 public String vicinity;
 
 @Override
 public String toString() {
  return name;
 }
  
}