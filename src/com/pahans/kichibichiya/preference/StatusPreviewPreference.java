package com.pahans.kichibichiya.preference;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static com.pahans.kichibichiya.util.Utils.formatSameDayTime;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.Constants;
import com.pahans.kichibichiya.model.StatusViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatusPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	final LayoutInflater mInflater;
	StatusViewHolder mHolder;
	final SharedPreferences mPreferences;

	public StatusPreviewPreference(final Context context) {
		this(context, null);
	}

	public StatusPreviewPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (mHolder == null) return;
		if (PREFERENCE_KEY_TEXT_SIZE.equals(key)) {
			setTextSize();
		} else if (PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE.equals(key)) {
			setProfileImage();
		} else if (PREFERENCE_KEY_INLINE_IMAGE_PREVIEW.equals(key)) {
			setImagePreview();
		} else if (PREFERENCE_KEY_SHOW_ABSOLUTE_TIME.equals(key)) {
			setTime();
		} else if (PREFERENCE_KEY_NAME_DISPLAY_OPTION.equals(key)) {
			setName();
		}
	}

	@Override
	protected void onBindView(final View view) {
		mHolder = new StatusViewHolder(view);
		mHolder.profile_image.setImageResource(R.drawable.ic_launcher);
		mHolder.image_preview.setImageResource(R.drawable.twidere_promotional_graphic);
		mHolder.text.setText("Kichibichiya is an open source twitter client for Android.");
		mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_has_media, 0);
		mHolder.reply_retweet_status.setVisibility(View.GONE);
		setName();
		setImagePreview();
		setProfileImage();
		setTextSize();
		setTime();
		super.onBindView(view);
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		return mInflater.inflate(R.layout.status_list_item, null);
	}

	private void setImagePreview() {
		if (mHolder == null) return;
		mHolder.image_preview
				.setVisibility(mPreferences.getBoolean(PREFERENCE_KEY_INLINE_IMAGE_PREVIEW, false) ? View.VISIBLE
						: View.GONE);
	}

	private void setName() {
		final String option = mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH);
		if (NAME_DISPLAY_OPTION_NAME.equals(option)) {
			mHolder.name.setText("Kichibichiya twitter client");
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) {
			mHolder.name.setText("kichibichiya");
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else {
			mHolder.name.setText("Kichibichiya twitter client");
			mHolder.screen_name.setText("@kichibichiya");
			mHolder.screen_name.setVisibility(View.VISIBLE);
		}
	}

	private void setProfileImage() {
		if (mHolder == null) return;
		mHolder.profile_image
				.setVisibility(mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true) ? View.VISIBLE
						: View.GONE);
	}

	private void setTextSize() {
		if (mHolder == null) return;
		mHolder.setTextSize(mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE));
	}

	private void setTime() {
		if (mHolder == null) return;
		if (mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false)) {
			mHolder.time.setText(formatSameDayTime(getContext(), System.currentTimeMillis() - 360000));
		} else {
			mHolder.time.setText(getRelativeTimeSpanString(System.currentTimeMillis() - 360000));
		}
	}

}
