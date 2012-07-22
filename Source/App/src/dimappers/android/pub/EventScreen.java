package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EventScreen extends ListActivity {
	
	protected PubEvent event;
	protected IPubService service;
	
	
	protected List<AppUser> createAppUserList()
	{
		User[] array = event.GetUserArray();
		List<AppUser> list = new ArrayList<AppUser>(array.length);
		for(int i=0; i < array.length; i++)
		{
			try {
				list.add(AppUser.AppUserFromUser(array[i], service.GetFacebook()));
			} catch (Exception e1) {
				e1.printStackTrace();
				Log.d(Constants.MsgError, "Error creating AppUser for User " + array[i].getUserId());
			}
		}
		return list;
	}
	
	public class GeneralGuestListAdapter extends ArrayAdapter<AppUser> 
	{

		public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, AppUser[] objects)
		{
			super(context, resource, textViewResourceId, objects);
		}

		public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, List<AppUser> objects)
		{
			super(context, resource, textViewResourceId, objects);
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			convertView = super.getView(position, convertView, parent);

			TextView freeFromText = (TextView) convertView.findViewById(R.id.time);
			
			UserStatus userStatus =  event.GetGoingStatusMap().get(getItem(position));
			
			switch(userStatus.goingStatus)
			{
				case notGoing :
				{
					freeFromText.setText("Nah");
					break;
				}
				case going :
				{
					if(userStatus.freeFrom.equals(event.GetStartTime()) || userStatus.freeFrom.before(event.GetStartTime()))
					{
						//The user has either said this time or an earlier time and hence is free
						freeFromText.setText("Up for it!");
					}
					else
					{
						freeFromText.setText(PubEvent.GetFormattedDate(userStatus.freeFrom));
					}
					break;
				}
				case maybeGoing : 
				{
					freeFromText.setText("");
					break;
				}
			}
			
			return convertView;
		}

	}
}
