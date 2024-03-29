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

import java.util.List;

import com.pahans.kichibichiya.model.ParcelableUser;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;

public class UserBlocksLoader extends IDsUsersLoader {

	public UserBlocksLoader(final Context context, final long account_id, final long max_id,
			final List<ParcelableUser> users_list) {
		super(context, account_id, max_id, users_list);
	}

	@Override
	public IDs getIDs() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		return twitter.getBlockingUsersIDs();
	}

}
