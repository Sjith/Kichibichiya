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

public class PreviewImage {
	public final boolean has_image;
	public final String matched_url, orig_url;

	public PreviewImage(final boolean has_image, final String matched_url, final String orig_url) {
		this.has_image = has_image;
		this.matched_url = matched_url;
		this.orig_url = orig_url;
	}

	public PreviewImage(final ImageSpec spec, final String orig_url) {
		this(spec != null && spec.thumbnail_link != null, spec.thumbnail_link, orig_url);
	}

}