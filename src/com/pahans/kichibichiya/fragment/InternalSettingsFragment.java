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

import com.pahans.kichibichiya.activity.InternalSettingsActivity;
import com.pahans.kichibichiya.model.Panes;

public class InternalSettingsFragment extends ActivityHostFragment<InternalSettingsActivity> implements Panes.Left {

	@Override
	protected Class<InternalSettingsActivity> getActivityClass() {
		return InternalSettingsActivity.class;
	}
}
