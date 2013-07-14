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
import static com.pahans.kichibichiya.util.HtmlEscapeHelper.unescape;
import static com.pahans.kichibichiya.util.Utils.findStatusInDatabases;
import static com.pahans.kichibichiya.util.Utils.formatSameDayTime;
import static com.pahans.kichibichiya.util.Utils.getAccountColor;
import static com.pahans.kichibichiya.util.Utils.getAccountScreenName;
import static com.pahans.kichibichiya.util.Utils.getAllAvailableImage;
import static com.pahans.kichibichiya.util.Utils.getBiggerTwitterProfileImage;
import static com.pahans.kichibichiya.util.Utils.getPreviewImage;
import static com.pahans.kichibichiya.util.Utils.getStatusBackground;
import static com.pahans.kichibichiya.util.Utils.getStatusTypeIconRes;
import static com.pahans.kichibichiya.util.Utils.getUserColor;
import static com.pahans.kichibichiya.util.Utils.getUserTypeIconRes;
import static com.pahans.kichibichiya.util.Utils.openUserProfile;
import static com.pahans.kichibichiya.util.Utils.parseURL;

import java.util.ArrayList;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.app.TwidereApplication;
import com.pahans.kichibichiya.model.ImageSpec;
import com.pahans.kichibichiya.model.ParcelableStatus;
import com.pahans.kichibichiya.model.PreviewImage;
import com.pahans.kichibichiya.model.StatusCursorIndices;
import com.pahans.kichibichiya.model.StatusViewHolder;
import com.pahans.kichibichiya.util.LazyImageLoader;
import com.pahans.kichibichiya.util.StatusesAdapterInterface;
import com.pahans.sinhala.droid.project.SinhalaDroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CursorStatusesAdapter extends SimpleCursorAdapter implements StatusesAdapterInterface, OnClickListener {

	private boolean mDisplayProfileImage, mDisplayImagePreview, mShowAccountColor, mShowAbsoluteTime, mGapDisallowed,
			mMultiSelectEnabled, mMentionsHighlightDisabled;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private float mTextSize;
	private final Context mContext;
	private StatusCursorIndices mIndices;
	private final ArrayList<Long> mSelectedStatusIds;
	private final boolean mDisplayHiResProfileImage;
	private int mNameDisplayOption;

	public CursorStatusesAdapter(final Context context) {
		super(context, R.layout.status_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mSelectedStatusIds = application.getSelectedStatusIds();
		mProfileImageLoader = application.getProfileImageLoader();
		mPreviewImageLoader = application.getPreviewImageLoader();
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		final boolean is_gap = cursor.getShort(mIndices.is_gap) == 1;

		final boolean show_gap = is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);

		if (!show_gap) {

			final String retweeted_by_name = cursor.getString(mIndices.retweeted_by_name);
			final String retweeted_by_screen_name = cursor.getString(mIndices.retweeted_by_screen_name);
			final String text = SinhalaDroid.convert(cursor.getString(mIndices.text));
			final String screen_name = cursor.getString(mIndices.screen_name);
			final String name = SinhalaDroid.convert(cursor.getString(mIndices.name));
			final String in_reply_to_screen_name = cursor.getString(mIndices.in_reply_to_screen_name);

			final long account_id = cursor.getLong(mIndices.account_id);
			final long user_id = cursor.getLong(mIndices.user_id);
			final long status_id = cursor.getLong(mIndices.status_id);
			final long status_timestamp = cursor.getLong(mIndices.status_timestamp);
			final long retweet_count = cursor.getLong(mIndices.retweet_count);

			final boolean is_favorite = cursor.getShort(mIndices.is_favorite) == 1;
			final boolean is_protected = cursor.getShort(mIndices.is_protected) == 1;
			final boolean is_verified = cursor.getShort(mIndices.is_verified) == 1;

			final boolean has_location = !TextUtils.isEmpty(cursor.getString(mIndices.location));
			final boolean is_retweet = !TextUtils.isEmpty(retweeted_by_name)
					&& cursor.getShort(mIndices.is_retweet) == 1;
			final boolean is_reply = !TextUtils.isEmpty(in_reply_to_screen_name)
					&& cursor.getLong(mIndices.in_reply_to_status_id) > 0;

			if (mMultiSelectEnabled) {
				holder.setSelected(mSelectedStatusIds.contains(status_id));
			} else {
				holder.setSelected(false);
			}

			holder.setUserColor(getUserColor(mContext, user_id));
			if (text != null) {
				holder.setHighlightColor(getStatusBackground(
						mMentionsHighlightDisabled ? false : text.contains('@' + getAccountScreenName(mContext,
								account_id)), is_favorite, is_retweet));
			}

			holder.setAccountColorEnabled(mShowAccountColor);

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, account_id));
			}

			final PreviewImage preview = getPreviewImage(text, mDisplayImagePreview);
			final boolean has_media = preview != null ? preview.has_image : false;

			holder.setTextSize(mTextSize);

			holder.text.setText(unescape(text));
			holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, getUserTypeIconRes(is_verified, is_protected), 0);
			switch (mNameDisplayOption) {
				case NAME_DISPLAY_OPTION_CODE_NAME: {
					holder.name.setText(name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
					holder.name.setText(screen_name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				default: {
					holder.name.setText(name);
					holder.screen_name.setText("@" + screen_name);
					holder.screen_name.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (mShowAbsoluteTime) {
				holder.time.setText(formatSameDayTime(context, status_timestamp));
			} else {
				holder.time.setText(getRelativeTimeSpanString(status_timestamp));
			}
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getStatusTypeIconRes(is_favorite, has_location, has_media), 0);

			holder.reply_retweet_status.setVisibility(is_retweet || is_reply ? View.VISIBLE : View.GONE);
			if (is_retweet) {
				if (mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
					holder.reply_retweet_status.setText(retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					holder.reply_retweet_status.setText(retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_name, retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_name));
				}
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (is_reply) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to, in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				final String profile_image_url_string = cursor.getString(mIndices.profile_image_url);
				if (mDisplayHiResProfileImage) {
					mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(profile_image_url_string)),
							holder.profile_image);
				} else {
					mProfileImageLoader.displayImage(parseURL(profile_image_url_string), holder.profile_image);
				}
				holder.profile_image.setTag(position);
			}
			final boolean has_preview = mDisplayImagePreview && has_media && preview.matched_url != null;
			holder.image_preview.setVisibility(has_preview ? View.VISIBLE : View.GONE);
			if (has_preview) {
				mPreviewImageLoader.displayImage(parseURL(preview.matched_url), holder.image_preview);
				holder.image_preview.setTag(position);
			}
		}

		super.bindView(view, context, cursor);
	}

	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).getLong(mIndices.status_id);
		return -1;
	}

	public int findItemPositionByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).getLong(mIndices.status_id) == status_id) return i;
		}
		return -1;
	}

	@Override
	public Cursor getItem(final int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public ParcelableStatus getStatus(final int position) {
		final Cursor cur = getItem(position);
		final long account_id = cur.getLong(mIndices.account_id);
		final long status_id = cur.getLong(mIndices.status_id);
		return findStatusInDatabases(mContext, account_id, status_id);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof StatusViewHolder)) {
			final StatusViewHolder holder = new StatusViewHolder(view);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(this);
			holder.image_preview.setOnClickListener(this);
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

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mIndices = new StatusCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}

}
