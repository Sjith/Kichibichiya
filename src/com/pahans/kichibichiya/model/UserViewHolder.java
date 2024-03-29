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

package com.pahans.kichibichiya.model;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.view.ColorLabelRelativeLayout;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserViewHolder {

	public final ImageView profile_image;
	public final TextView name, screen_name, description;
	private final ColorLabelRelativeLayout content;
	private boolean account_color_enabled;
	private float text_size;

	public UserViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		profile_image = (ImageView) view.findViewById(R.id.profile_image);
		name = (TextView) view.findViewById(R.id.name);
		screen_name = (TextView) view.findViewById(R.id.screen_name);
		description = (TextView) view.findViewById(R.id.description);
	}

	public void setAccountColor(final int color) {
		content.drawRight(account_color_enabled ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		account_color_enabled = enabled;
		if (!account_color_enabled) {
			content.drawRight(Color.TRANSPARENT);
		}
	}

	public void setHighlightColor(final int color) {
		content.drawBackground(color);
	}

	public void setSelected(final boolean selected) {
		content.setBackgroundColor(selected ? 0x600099CC : Color.TRANSPARENT);
	}

	public void setTextSize(final float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			description.setTextSize(text_size);
			name.setTextSize(text_size);
			screen_name.setTextSize(text_size * 0.75f);
		}
	}

	public void setUserColor(final int color) {
		content.drawLeft(color);
	}

}
