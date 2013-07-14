package com.pahans.kichibichiya.util;

import static com.pahans.kichibichiya.util.Utils.makeCachedUserContentValues;
import static com.pahans.kichibichiya.util.Utils.makeStatusContentValues;

import java.util.ArrayList;
import java.util.List;

import com.pahans.kichibichiya.provider.TweetStore.CachedStatuses;
import com.pahans.kichibichiya.provider.TweetStore.CachedUsers;
import com.pahans.kichibichiya.provider.TweetStore.Statuses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

public class CacheUsersStatusesTask extends AsyncTask<Void, Void, Void> {

	private final ArrayList<ContentValues> all_statuses;
	private final ContentResolver resolver;

	public CacheUsersStatusesTask(final Context context, final ArrayList<ContentValues> all_statuses) {
		resolver = context.getContentResolver();
		this.all_statuses = all_statuses;
	}

	public CacheUsersStatusesTask(final Context context, final List<twitter4j.Status> statuses, final long account_id) {
		resolver = context.getContentResolver();
		all_statuses = new ArrayList<ContentValues>();
		for (final twitter4j.Status status : statuses) {
			all_statuses.add(makeStatusContentValues(status, account_id));
		}
	}

	@Override
	protected Void doInBackground(final Void... args) {

		final List<ContentValues> cached_users_list = new ArrayList<ContentValues>();
		final List<Long> user_ids = new ArrayList<Long>(), status_ids = new ArrayList<Long>();

		for (final ContentValues values : all_statuses) {
			if (values == null) {
				continue;
			}
			final long user_id = values.getAsLong(Statuses.USER_ID);
			status_ids.add(values.getAsLong(Statuses.STATUS_ID));
			if (!user_ids.contains(user_id)) {
				user_ids.add(user_id);
				cached_users_list.add(makeCachedUserContentValues(values));
			}

		}

		resolver.delete(CachedUsers.CONTENT_URI,
				CachedUsers.USER_ID + " IN (" + ListUtils.toString(user_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedUsers.CONTENT_URI,
				cached_users_list.toArray(new ContentValues[cached_users_list.size()]));
		resolver.delete(CachedStatuses.CONTENT_URI,
				CachedStatuses.STATUS_ID + " IN (" + ListUtils.toString(status_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedStatuses.CONTENT_URI, all_statuses.toArray(new ContentValues[all_statuses.size()]));

		return null;
	}

	public static Runnable getRunnable(final Context context, final List<twitter4j.Status> statuses,
			final long account_id) {
		return new Runnable() {
			@Override
			public void run() {
				new CacheUsersStatusesTask(context, statuses, account_id).execute();
			}
		};
	}
}
