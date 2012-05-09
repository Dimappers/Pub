package dimappers.android.pub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import dimappers.android.PubData.PubLocation;

public class LocationChanger {

	public static void changeLocation(final LocationRequiringActivity a) {
		final EditText loc = new EditText(a.getApplicationContext());
		
		AlertDialog.Builder builder;
		if(a instanceof ChoosePub)
		{
			builder = new AlertDialog.Builder(a)
				.setMessage("Enter a different location to search at:")
				.setTitle("No pubs found");
		}
		else 
		{
			builder = new AlertDialog.Builder(a)
				.setMessage("Enter your current location:")
				.setTitle("Change Location");
		}
		
		builder
		.setCancelable(true)  
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				if(a instanceof Organise) {((Organise)a).progbar.setVisibility(View.VISIBLE);}
				
				DataRequestReverseGeocoder request1 = new DataRequestReverseGeocoder(a.getApplicationContext(), loc.getText().toString());
				a.service.addDataRequest(request1, new IRequestListener<XmlableDoubleArray>(){

					@Override
					public void onRequestFail(Exception e) {
						if(a instanceof Organise) {((Organise) a).removeProgBar();}
						a.failure(2);
					}

					@Override
					public void onRequestComplete(XmlableDoubleArray data) {
						
						final double lat = data.getArray()[0];
						final double lng = data.getArray()[1];
						
						DataRequestPubFinder request2 = new DataRequestPubFinder(lat, lng);
						a.service.addDataRequest(request2, new IRequestListener<PlacesList>(){

							@Override
							public void onRequestComplete(PlacesList data) {
								PubLocation best = new PubRanker(data.results, a.event, a.service.getHistoryStore()).returnBest();
								if(best==null)
								{
									if(a instanceof Organise) {((Organise) a).removeProgBar();}
									a.failure(0);
								}
								else
								{
									a.event.SetPubLocation(best);
									a.success(lat, lng, loc.getText().toString());
								}
								}

							@Override
							public void onRequestFail(Exception e) {
								if(a instanceof Organise) {((Organise) a).removeProgBar();}
								a.failure(1);
							}});
					}});
				dialog.cancel();
			}
		})
		.setView(loc)
		.show(); 
	}
}
