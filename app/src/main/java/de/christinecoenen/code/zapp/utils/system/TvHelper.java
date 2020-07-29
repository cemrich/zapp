package de.christinecoenen.code.zapp.utils.system;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.media.tv.TvContract;
import android.net.Uri;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.ChannelLogoUtils;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;
import de.christinecoenen.code.zapp.BuildConfig;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.MainActivity;
import de.christinecoenen.code.zapp.model.ChannelModel;
import de.christinecoenen.code.zapp.model.IChannelList;
import de.christinecoenen.code.zapp.model.json.JsonChannelList;
import timber.log.Timber;

import static de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity.EXTRA_CHANNEL_ID;

/**
 * helper class to install channel and program starters an Android TV.
 */
public class TvHelper {

	private static final String[] CHANNELS_PROJECTION = {
		TvContractCompat.Channels._ID,
		TvContract.Channels.COLUMN_DISPLAY_NAME,
		TvContractCompat.Channels.COLUMN_BROWSABLE
	};

	public static long getOrCreateChannel(Context context, String channelName, Uri uri) {
		// only add our channel if it does not exist.
		// Note: This is persisted and thus is only done once on first app start.
		// this means that if the programs change, the app needs currently needs to be re-installed to reflect the change.
		long channelId = getChannelId(context,channelName);
		if(channelId==0) {
			Channel.Builder builder = new Channel.Builder();
			// each channel must have the type TYPE_PREVIEW
			builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
				.setDisplayName(channelName)
				.setAppLinkIntentUri(uri);

			Uri channelUri = context.getContentResolver().insert(
				TvContractCompat.Channels.CONTENT_URI, builder.build().toContentValues());

			channelId = ContentUris.parseId(channelUri);

			// use the "channel" drawable as channel logo
			BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.channel, null);
			ChannelLogoUtils.storeChannelLogo(context, channelId, drawable.getBitmap());

			TvContractCompat.requestChannelBrowsable(context, channelId);

			addPrograms(context,channelId,new JsonChannelList(context));

		}
		return channelId;
	}

	/**
	 * function taken from Android Dev sample code
	 * @param context
	 * @param channelName
	 * @return
	 */
	protected static long getChannelId(Context context, String channelName) {
		Cursor cursor =
			context.getContentResolver()
				.query(
					TvContractCompat.Channels.CONTENT_URI,
					CHANNELS_PROJECTION,
					null,
					null,
					null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				Channel channel = Channel.fromCursor(cursor);
				if (channelName.equals(channel.getDisplayName())) {
					Timber.d(
						"Channel already exists. Returning channel "
							+ channel.getId()
							+ " from TV Provider.");
					return channel.getId();
				}
			} while (cursor.moveToNext());
		}
		return 0;
	}

	/**
	 * add program starters to our primary channel.
	 * Note: Distinguish between the Android TV channel, and the programs within, which are created one per channelModel (TV channel)
	 * @param context
	 * @param channelId
	 * @param channelList
	 */
	public static void addPrograms(Context context, long channelId, IChannelList channelList) {
		for(ChannelModel cm : channelList.getList()) {

			// construct the intent to start the program
			Intent intent = new Intent(context, MainActivity.class);
			intent.setAction("play");
			intent.putExtra(EXTRA_CHANNEL_ID, cm.getId());
			Uri uri = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME));

			// get the logo. NOTE: XML drawables are not supported (at least I tried them and they don't show), so we use bitmap drawables
			int resId = context.getResources().getIdentifier("logo_" + cm.getId(), "drawable", BuildConfig.APPLICATION_ID);
			// if no image is available, we don't add the channel as a program
			if(resId==0)  continue;

			// build the program info
			PreviewProgram.Builder builder = new PreviewProgram.Builder();
			builder.setChannelId(channelId)
				.setType(TvContractCompat.PreviewPrograms.TYPE_CHANNEL)
				.setTitle(cm.getName())
				.setPosterArtAspectRatio(TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_1_1)
				.setPosterArtUri(resourceToUri(context, resId))
				.setIntentUri(uri)
				.setInternalProviderId(cm.getId());

			Timber.d("installing program " + cm.getId() + " with uri " + uri);
			Uri programUri = context.getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI,
				builder.build().toContentValues());

			long programId = ContentUris.parseId(programUri);
			// NOTE we could save this for later use, but currently not needed.
		}
	}


	/**
	 * helper function to construct Uri for resId.
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Uri resourceToUri(Context context, int resId) {
		return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
			context.getResources().getResourcePackageName(resId) + '/' +
			context.getResources().getResourceTypeName(resId) + '/' +
			context.getResources().getResourceEntryName(resId) );
	}
}
