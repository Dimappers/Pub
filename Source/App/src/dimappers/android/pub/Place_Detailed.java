package dimappers.android.pub;

import com.google.api.client.util.Key;

public class Place_Detailed extends Place {

	/* Not implemented information, but available from JSON:
	 * "types": array of Strings
	 * "address_components": array of Objects containing "long_name", "short_name" and "types" for each part of address
	 */
	
	@Key
	public String formatted_phone_number;
	
	@Key
	public String formatted_address;
	
	@Key
	public double rating;
	
	@Key
	public String url;
}