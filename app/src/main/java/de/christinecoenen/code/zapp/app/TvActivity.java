package de.christinecoenen.code.zapp.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import de.christinecoenen.code.zapp.utils.system.TvHelper;

/**
 * This activity is started by the Android TV launcher when the Zapp icon is chosen.
 */
public class TvActivity extends Activity {

	private static final String CHANNEL_NAME="Zapp";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent(this,MainActivity.class);
		// create the Android TV channel starter which will launch the MainActivity
		TvHelper.getOrCreateChannel(this,CHANNEL_NAME, i.getData());
		// start the MainActivity.
		startActivity(i);
		finish();
	}


}
