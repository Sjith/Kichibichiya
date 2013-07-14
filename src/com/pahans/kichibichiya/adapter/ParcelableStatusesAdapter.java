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

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static com.pahans.kichibichiya.model.ParcelableLocation.isValidLocation;
import static com.pahans.kichibichiya.util.Utils.formatSameDayTime;
import static com.pahans.kichibichiya.util.Utils.getAccountColor;
import static com.pahans.kichibichiya.util.Utils.getAccountScreenName;
import static com.pahans.kichibichiya.util.Utils.getAllAvailableImage;
import static com.pahans.kichibichiya.util.Utils.getBiggerTwitterProfileImage;
import static com.pahans.kichibichiya.util.Utils.getStatusBackground;
import static com.pahans.kichibichiya.util.Utils.getStatusTypeIconRes;
import static com.pahans.kichibichiya.util.Utils.getUserColor;
import static com.pahans.kichibichiya.util.Utils.getUserTypeIconRes;
import static com.pahans.kichibichiya.util.Utils.openUserProfile;
import static com.pahans.kichibichiya.util.Utils.parseURL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.app.TwidereApplication;
import com.pahans.kichibichiya.model.ImageSpec;
import com.pahans.kichibichiya.model.ParcelableStatus;
import com.pahans.kichibichiya.model.StatusViewHolder;
import com.pahans.kichibichiya.util.LazyImageLoader;
import com.pahans.kichibichiya.util.NoDuplicatesArrayList;
import com.pahans.kichibichiya.util.StatusesAdapterInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ParcelableStatusesAdapter extends BaseAdapter implements StatusesAdapterInterface, OnClickListener {

	private boolean mDisplayProfileImage, mDisplayImagePreview, mShowAccountColor, mShowAbsoluteTime, mGapDisallowed,
			mMultiSelectEnabled, mMentionsHighlightDisabled;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private float mTextSize;
	private final Context mContext;
	private final LayoutInflater mInflater;
	private final ArrayList<Long> mSelectedStatusIds;
	private final boolean mDisplayHiResProfileImage;
	private final NoDuplicatesArrayList<ParcelableStatus> mData = new NoDuplicatesArrayList<ParcelableStatus>();
	private int mNameDisplayOption;

	public ParcelableStatusesAdapter(final Context context) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(context);
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mSelectedStatusIds = application.getSelectedStatusIds();
		mProfileImageLoader = application.getProfileImageLoader();
		mPreviewImageLoader = application.getPreviewImageLoader();
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	public void add(final ParcelableStatus status) {
		if (status == null) return;
		mData.add(status);
		notifyDataSetChanged();
	}

	public void clear() {
		mData.clear();
	}

	public ParcelableStatus findItemByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableStatus status = getItem(i);
			if (status.status_id == status_id) return status;
		}
		return null;
	}

	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).status_id;
		return -1;
	}

	public int findItemPositionByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).status_id == status_id) return i;
		}
		return -1;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ParcelableStatus getItem(final int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return mData.get(position).status_id;
	}

	@Override
	public ParcelableStatus getStatus(final int position) {
		return getItem(position);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = convertView != null ? convertView : mInflater.inflate(R.layout.status_list_item, null);
		final Object tag = view.getTag();
		final StatusViewHolder holder;

		if (tag instanceof StatusViewHolder) {
			holder = (StatusViewHolder) tag;
		} else {
			holder = new StatusViewHolder(view);
			view.setTag(holder);
			holder.image_preview.setOnClickListener(this);
			holder.profile_image.setOnClickListener(this);
		}

		final ParcelableStatus status = getItem(position);

		final boolean show_gap = status.is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mShowAccountColor);

		holder.text.setText(status.text_unescaped);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, status.account_id));
		}

		if (!show_gap) {

			final String retweeted_by_name = status.retweeted_by_name;
			final String retweeted_by_screen_name = status.retweeted_by_screen_name;

			if (mMultiSelectEnabled) {
				holder.setSelected(mSelectedStatusIds.contains(status.status_id));
			} else {
				holder.setSelected(false);
			}

			holder.setUserColor(getUserColor(mContext, status.user_id));
			holder.setHighlightColor(getStatusBackground(
					mMentionsHighlightDisabled ? false : status.text_plain.contains('@' + getAccountScreenName(
							mContext, status.account_id)), status.is_favorite, status.is_retweet));

			holder.setTextSize(mTextSize);
			holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getUserTypeIconRes(status.is_verified, status.is_protected), 0);
			switch (mNameDisplayOption) {
				case NAME_DISPLAY_OPTION_CODE_NAME: {
					holder.name.setText(status.name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
					holder.name.setText(status.screen_name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				default: {
					holder.name.setText(status.name);
					holder.screen_name.setText("@" + status.screen_name);
					holder.screen_name.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (mShowAbsoluteTime) {
				holder.time.setText(formatSameDayTime(mContext, status.status_timestamp));
			} else {
				holder.time.setText(getRelativeTimeSpanString(status.status_timestamp));
			}
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location), status.has_media), 0);
			holder.reply_retweet_status
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name)
					&& !TextUtils.isEmpty(retweeted_by_screen_name)) {
				if (mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
					holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
							: mContext.getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_name));
				}
				holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
						R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : mContext
						.getString(R.string.retweeted_by, retweeted_by_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to,
						status.in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				if (mDisplayHiResProfileImage) {
					mProfileImageLoader.displayImage(
							parseURL(getBiggerTwitterProfileImage(status.profile_image_url_string)),
							holder.profile_image);
				} else {
					mProfileImageLoader.displayImage(parseURL(status.profile_image_url_string), holder.profile_image);
				}
				holder.profile_image.setOnClickListener(this);
				holder.profile_image.setTag(position);
			}
			final boolean has_preview = mDisplayImagePreview && status.has_media && status.image_preview_url != null;
			holder.image_preview.setVisibility(has_preview ? View.VISIBLE : View.GONE);
			if (has_preview) {
				mPreviewImageLoader.displayImage(status.image_preview_url, holder.image_preview);
				holder.image_preview.setTag(position);
			}
		}

		return view;
	}

	@Override
	public void onClick(final View view) {
		final Object tag = view.getTag();
		final ParcelableStatus status = tag instanceof Integer ? getStatus((Integer) tag) : null;
		if (status == null) return;
		switch (view.getId()) {
			case R.id.image_preview: {
				final ImageSpec spec = getAllAvailableImage(status.image_orig_url_string);
				if (spec != null) {
					final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, Uri.parse(spec.image_link));
					intent.setPackage(mContext.getPackageName());
					mContext.startActivity(intent);
				}
				break;
			}
			case R.id.profile_image: {
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, status.account_id, status.user_id, status.screen_name);
				}
				break;
			}
		}
	}

	public void setData(final List<ParcelableStatus> data) {
		clear();
		if (data == null) return;
		mData.addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayImagePreview(final boolean preview) {
		if (preview != mDisplayImagePreview) {
			mDisplayImagePreview = preview;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setGapDisallowed(final boolean disallowed) {
		if (mGapDisallowed != disallowed) {
			mGapDisallowed = disallowed;
			notifyDataSetChanged();
		}

	}

	@Override
	public void setMentionsHightlightDisabled(final boolean disable) {
		if (disable != mMentionsHighlightDisabled) {
			mMentionsHighlightDisabled = disable;
			notifyDataSetChanged();
		}
	}

	@Override
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

	@Override
	public void setShowAbsoluteTime(final boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	@Override
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

	public void sort(final Comparator<? super ParcelableStatus> comparator) {
		Collections.sort(mData, comparator);
	}
}
