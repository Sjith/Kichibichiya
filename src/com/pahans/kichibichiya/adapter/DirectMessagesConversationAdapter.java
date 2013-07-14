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

import static com.pahans.kichibichiya.util.Utils.findDirectMessageInDatabases;
import static com.pahans.kichibichiya.util.Utils.formatToLongTimeString;
import static com.pahans.kichibichiya.util.Utils.getBiggerTwitterProfileImage;
import static com.pahans.kichibichiya.util.Utils.openUserProfile;
import static com.pahans.kichibichiya.util.Utils.parseURL;

import java.net.URL;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.model.DirectMessageConversationViewHolder;
import com.pahans.kichibichiya.model.DirectMessageCursorIndices;
import com.pahans.kichibichiya.model.ParcelableDirectMessage;
import com.pahans.kichibichiya.util.DirectMessagesAdapterInterface;
import com.pahans.kichibichiya.util.LazyImageLoader;
import com.pahans.kichibichiya.util.OnLinkClickHandler;
import com.pahans.kichibichiya.util.TwidereLinkify;
import com.pahans.sinhala.droid.project.SinhalaDroid;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class DirectMessagesConversationAdapter extends SimpleCursorAdapter implements DirectMessagesAdapterInterface,
		OnClickListener {

	private boolean mDisplayProfileImage;
	private final LazyImageLoader mImageLoader;
	private float mTextSize;
	private final Context mContext;
	private DirectMessageCursorIndices mIndices;
	private final boolean mDisplayHiResProfileImage;
	private int mNameDisplayOption;

	public DirectMessagesConversationAdapter(final Context context, final LazyImageLoader loader) {
		super(context, R.layout.direct_message_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		mImageLoader = loader;
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final DirectMessageConversationViewHolder holder = (DirectMessageConversationViewHolder) view.getTag();

		final long account_id = cursor.getLong(mIndices.account_id);
		final long message_timestamp = cursor.getLong(mIndices.message_timestamp);
		final boolean is_outgoing = cursor.getInt(mIndices.is_outgoing) == 1;
		final String name = SinhalaDroid.convert(cursor.getString(mIndices.sender_name));
		final String screen_name = cursor.getString(mIndices.sender_screen_name);
		holder.setTextSize(mTextSize);
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
		final FrameLayout.LayoutParams lp = (LayoutParams) holder.name_container.getLayoutParams();
		lp.gravity = is_outgoing ? Gravity.LEFT : Gravity.RIGHT;
		holder.name_container.setLayoutParams(lp);
		holder.text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		final TwidereLinkify linkify = new TwidereLinkify(holder.text);
		linkify.setOnLinkClickListener(new OnLinkClickHandler(context, account_id));
		linkify.addAllLinks();
		holder.text.setMovementMethod(null);
		holder.text.setGravity(is_outgoing ? Gravity.LEFT : Gravity.RIGHT);
		holder.time.setText(formatToLongTimeString(mContext, message_timestamp));
		holder.time.setGravity(is_outgoing ? Gravity.RIGHT : Gravity.LEFT);
		holder.profile_image_left.setVisibility(mDisplayProfileImage && is_outgoing ? View.VISIBLE : View.GONE);
		holder.profile_image_right.setVisibility(mDisplayProfileImage && !is_outgoing ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			final String sender_profile_image_url_string = cursor.getString(mIndices.sender_profile_image_url);
			final URL sender_profile_image_url = parseURL(mDisplayHiResProfileImage ? getBiggerTwitterProfileImage(sender_profile_image_url_string)
					: sender_profile_image_url_string);

			mImageLoader.displayImage(sender_profile_image_url, holder.profile_image_left);
			mImageLoader.displayImage(sender_profile_image_url, holder.profile_image_right);
			holder.profile_image_left.setTag(position);
			holder.profile_image_right.setTag(position);
		}

		super.bindView(view, context, cursor);
	}

	@Override
	public ParcelableDirectMessage findItem(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return getDirectMessage(i);
		}
		return null;
	}

	public ParcelableDirectMessage getDirectMessage(final int position) {
		final Cursor item = getItem(position);
		final long account_id = item.getLong(mIndices.account_id);
		final long message_id = item.getLong(mIndices.message_id);
		return findDirectMessageInDatabases(mContext, account_id, message_id);
	}

	@Override
	public Cursor getItem(final int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof DirectMessageConversationViewHolder)) {
			final DirectMessageConversationViewHolder holder = new DirectMessageConversationViewHolder(view);
			view.setTag(holder);
			holder.profile_image_left.setOnClickListener(this);
			holder.profile_image_right.setOnClickListener(this);
		}
		return view;
	}

	@Override
	public void onClick(final View view) {
		final Object tag = view.getTag();
		final ParcelableDirectMessage status = tag instanceof Integer ? getDirectMessage((Integer) tag) : null;
		if (status == null) return;
		switch (view.getId()) {
			case R.id.profile_image_left:
			case R.id.profile_image_right: {
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, status.account_id, status.sender_id, status.sender_screen_name);
				}
				break;
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
	public void setTextSize(final float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mIndices = new DirectMessageCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}
}
