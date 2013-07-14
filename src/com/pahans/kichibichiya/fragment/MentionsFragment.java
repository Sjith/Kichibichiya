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

package com.pahans.kichibichiya.fragment;

import com.pahans.kichibichiya.adapter.CursorStatusesAdapter;
import com.pahans.kichibichiya.provider.TweetStore.Mentions;
import com.pahans.kichibichiya.util.ServiceInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.ListView;

public class MentionsFragment extends CursorStatusesListFragment implements OnTouchListener {

	private SharedPreferences mPreferences;
	private ServiceInterface mService;

	private ListView mListView;
	private CursorStatusesAdapter mAdapter;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
				onRefreshComplete();
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, MentionsFragment.this);
				}
			} else if (BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, MentionsFragment.this);
				}
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (mService != null && mService.isMentionsRefreshing()) {
					setRefreshing(false);
				}
			}
		}
	};

	@Override
	public Uri getContentUri() {
		return Mentions.CONTENT_URI;
	}

	@Override
	public int getStatuses(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mService.getMentionsWithSinceIds(account_ids, max_ids, since_ids);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mListView.setOnTouchListener(this);
		mAdapter = getListAdapter();
		mAdapter.setMentionsHightlightDisabled(true);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		final int first_visible_position = mListView.getFirstVisiblePosition();
		final long last_viewed_id = first_visible_position > 0 ? mAdapter.findItemIdByPosition(first_visible_position)
				: -1;
		super.onLoadFinished(loader, data);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		if (remember_position) {
			final long status_id = mPreferences.getLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, -1);
			final int position = mAdapter.findItemPositionByStatusId(last_viewed_id > 0 ? last_viewed_id : status_id);
			if (position > -1 && position < mListView.getCount() && last_viewed_id != status_id) {
				mListView.setSelection(position);
			}
		}
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		switch (scrollState) {
			case SCROLL_STATE_IDLE:
				final int first_visible_position = getListView().getFirstVisiblePosition();
				final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
				mPreferences.edit().putLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, status_id).commit();
				break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_MENTIONS_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		if (getServiceInterface().isMentionsRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onTouch(final View view, final MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mService.clearNotification(NOTIFICATION_ID_MENTIONS);
				break;
			}
		}
		return false;
	}
}
