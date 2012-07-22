package dimappers.android.pub;

import java.util.List;

import dimappers.android.pub.AppUser;
import android.content.Context;
import android.widget.ArrayAdapter;

public class GeneralGuestListAdapter extends ArrayAdapter<AppUser> {

	public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, AppUser[] objects)
	{
		super(context, resource, textViewResourceId, objects);
	}

	public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, List<AppUser> objects)
	{
		super(context, resource, textViewResourceId, objects);
	}

}
