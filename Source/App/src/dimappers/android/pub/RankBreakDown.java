package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RankBreakDown extends Activity {

	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_breakdown);
		AppUser person = (AppUser) getIntent().getSerializableExtra("person");
		
		((TextView)findViewById(R.id.postsFromWho)).setText(""+person.PostsFromWho);
		((TextView)findViewById(R.id.postsTagged)).setText(""+person.PostsTagged);
		((TextView)findViewById(R.id.postsLiked)).setText(""+person.PostsLiked);
		((TextView)findViewById(R.id.postsWithYou)).setText(""+person.PostsWithYou);
		((TextView)findViewById(R.id.postsComments)).setText(""+person.PostsComments);
		((TextView)findViewById(R.id.postsTaggedInComments)).setText(""+person.PostsTaggedInComment);
		
		((TextView)findViewById(R.id.photosFromWho)).setText(""+person.PhotosFromWho);
		((TextView)findViewById(R.id.photosTagged)).setText(""+person.PhotosTagged);
		((TextView)findViewById(R.id.photosComment)).setText(""+person.PhotosComments);
		((TextView)findViewById(R.id.photoslike)).setText(""+person.PhotosLiked);
		
		((TextView)findViewById(R.id.historyStore)).setText(""+person.History);
		((TextView)findViewById(R.id.callLog)).setText(""+person.CallLogTotal);
		
		((TextView)findViewById(R.id.totalRank)).setText(""+person.getRank());
	}
}
