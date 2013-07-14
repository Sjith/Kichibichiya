/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pahans.kichibichiya.loader;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.pahans.kichibichiya.model.ParcelableStatus;
import com.pahans.kichibichiya.util.CacheUsersStatusesTask;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class Twitter4JStatusLoader extends ParcelableStatusesLoader {

	private final long mMaxId, mSinceId;

	public Twitter4JStatusLoader(final Context context, final long account_id, final long max_id, final long since_id,
			final List<ParcelableStatus> data, final String class_name, final boolean is_home_tab) {
		super(context, account_id, data, class_name, is_home_tab);
		mMaxId = max_id;
		mSinceId = since_id;
	}

	public abstract List<Status> getStatuses(Paging paging) throws TwitterException;

	@Override
	public synchronized List<ParcelableStatus> loadInBackground() {
		final List<ParcelableStatus> data = getData();
		final long account_id = getAccountId();
		List<Status> statuses = null;
		final Context context = getContext();
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		try {
			final Paging paging = new Paging();
			paging.setCount(load_item_limit);
			if (mMaxId > 0) {
				paging.setMaxId(mMaxId);
			}
			if (mSinceId > 0) {
				paging.setSinceId(mSinceId);
			}
			statuses = getStatuses(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			final boolean insert_gap = load_item_limit == statuses.size() && data.size() > 0;
			Collections.sort(statuses);
			final Status min_status = statuses.size() > 0 ? Collections.min(statuses) : null;
			final long min_status_id = min_status != null ? min_status.getId() : -1;
			if (context instanceof Activity) {
				((Activity) context).runOnUiThread(CacheUsersStatusesTask.getRunnable(context, statuses, account_id));
			}
			new CacheUsersStatusesTask(context, statuses, account_id).execute();
			for (final Status status : statuses) {
				final long id = status.getId();
				deleteStatus(id);
				data.add(new ParcelableStatus(status, account_id, min_status_id > 0 && min_status_id == id
						&& insert_gap));
			}
		}
		try {
			Collections.sort(data);
		} catch (final ConcurrentModificationException e) {
			e.printStackTrace();
		}
		return data;
	}

}
