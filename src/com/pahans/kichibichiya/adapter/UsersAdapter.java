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

package com.pahans.kichibichiya.adapter;

import static com.pahans.kichibichiya.util.Utils.getAccountColor;
import static com.pahans.kichibichiya.util.Utils.getBiggerTwitterProfileImage;
import static com.pahans.kichibichiya.util.Utils.getUserColor;
import static com.pahans.kichibichiya.util.Utils.getUserTypeIconRes;
import static com.pahans.kichibichiya.util.Utils.parseURL;

import java.util.ArrayList;
import java.util.List;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.app.TwidereApplication;
import com.pahans.kichibichiya.model.ParcelableUser;
import com.pahans.kichibichiya.model.UserViewHolder;
import com.pahans.kichibichiya.util.BaseAdapterInterface;
import com.pahans.kichibichiya.util.LazyImageLoader;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class UsersAdapter extends ArrayAdapter<ParcelableUser> implements BaseAdapterInterface {

	private final LazyImageLoader mProfileImageLoader;
	private boolean mDisplayProfileImage, mShowAccountColor, mMultiSelectEnabled;
	private final boolean mDisplayHiResProfileImage;
	private float mTextSize;
	private final ArrayList<Long> mSelectedUserIds;
	private final Context mContext;

	private int mNameDisplayOption;

	public UsersAdapter(final Context context) {
		super(context, R.layout.user_list_item, R.id.description);
		mContext = context;
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mProfileImageLoader = application.getProfileImageLoader();
		application.getServiceInterface();
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
		mSelectedUserIds = application.getSelectedUserIds();
	}

	public ParcelableUser findItem(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	public ParcelableUser findItemByUserId(final long user_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableUser item = getItem(i);
			if (item.user_id == user_id) return item;
		}
		return null;
	}

	public int findItemPositionByUserId(final long user_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableUser item = getItem(i);
			if (item.user_id == user_id) return i;
		}
		return -1;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		UserViewHolder holder = null;
		if (tag instanceof UserViewHolder) {
			holder = (UserViewHolder) tag;
		} else {
			holder = new UserViewHolder(view);
			view.setTag(holder);
		}
		final ParcelableUser user = getItem(position);

		if (mMultiSelectEnabled) {
			holder.setSelected(mSelectedUserIds.contains(user.user_id));
		} else {
			holder.setSelected(false);
		}

		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, user.account_id));
		}

		holder.setUserColor(getUserColor(mContext, user.user_id));

		holder.setTextSize(mTextSize);
		holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				getUserTypeIconRes(user.is_verified, user.is_protected), 0);
		switch (mNameDisplayOption) {
			case NAME_DISPLAY_OPTION_CODE_NAME: {
				holder.name.setText(user.name);
				holder.screen_name.setText(null);
				holder.screen_name.setVisibility(View.GONE);
				break;
			}
			case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
				holder.name.setText(user.screen_name);
				holder.screen_name.setText(null);
				holder.screen_name.setVisibility(View.GONE);
				break;
			}
			default: {
				holder.name.setText(user.name);
				holder.screen_name.setText("@" + user.screen_name);
				holder.screen_name.setVisibility(View.VISIBLE);
				break;
			}
		}
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(user.profile_image_url_string)),
						holder.profile_image);
			} else {
				mProfileImageLoader.displayImage(parseURL(user.profile_image_url_string), holder.profile_image);
			}
		}

		return view;
	}

	public void setData(final List<ParcelableUser> data) {
		setData(data, false);
	}

	public void setData(final List<ParcelableUser> data, final boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (final ParcelableUser user : data) {
			if (clear_old || findItemByUserId(user.user_id) == null) {
				add(user);
			}
		}
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	public void setMultiSelectEnabled(final boolean multi) {
		if (mMultiSelectEnabled != multi) {
			mMultiSelectEnabled = multi;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setNameDisplayOption(final String option) {
		if (NAME_DISPLAY_OPTION_NAME.equals(option)) {
			mNameDisplayOption = NAME_DISPLAY_OPTION_CODE_NAME;
		} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) {
			mNameDisplayOption = NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		} else {
			mNameDisplayOption = 0;
		}
	}

	public void setShowAccountColor(final boolean show) {
		if (show != mShowAccountColor) {
			mShowAccountColor = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(final float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
