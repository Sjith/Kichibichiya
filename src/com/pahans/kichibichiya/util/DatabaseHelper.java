package com.pahans.kichibichiya.util;

import static com.pahans.kichibichiya.util.DatabaseUpgradeHelper.safeUpgrade;

import java.util.HashMap;

import com.pahans.kichibichiya.Constants;
import com.pahans.kichibichiya.provider.TweetStore.Accounts;
import com.pahans.kichibichiya.provider.TweetStore.CachedStatuses;
import com.pahans.kichibichiya.provider.TweetStore.CachedTrends;
import com.pahans.kichibichiya.provider.TweetStore.CachedUsers;
import com.pahans.kichibichiya.provider.TweetStore.DirectMessages;
import com.pahans.kichibichiya.provider.TweetStore.Drafts;
import com.pahans.kichibichiya.provider.TweetStore.Filters;
import com.pahans.kichibichiya.provider.TweetStore.Mentions;
import com.pahans.kichibichiya.provider.TweetStore.Statuses;
import com.pahans.kichibichiya.provider.TweetStore.Tabs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DatabaseHelper extends SQLiteOpenHelper implements Constants {

	public DatabaseHelper(final Context context, final String name, final int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.beginTransaction();
		db.execSQL(createTable(TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true));
		db.execSQL(createTable(TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true));
		db.execSQL(createTable(TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true));
		db.execSQL(createTable(TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true));
		db.execSQL(createTable(TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true));
		db.execSQL(createTable(TABLE_CACHED_STATUSES, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
		db.execSQL(createTable(TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES,
				true));
		db.execSQL(createTable(TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS,
				DirectMessages.Outbox.TYPES, true));
		db.execSQL(createTable(TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true));
		db.execSQL(createTable(TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true));
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db);
	}

	private String createTable(final String tableName, final String[] columns, final String[] types,
			final boolean create_if_not_exists) {
		if (tableName == null || columns == null || types == null || types.length != columns.length
				|| types.length == 0)
			throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
		final StringBuilder stringBuilder = new StringBuilder(create_if_not_exists ? "CREATE TABLE IF NOT EXISTS "
				: "CREATE TABLE ");

		stringBuilder.append(tableName);
		stringBuilder.append(" (");
		final int length = columns.length;
		for (int n = 0, i = length; n < i; n++) {
			if (n > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(columns[n]).append(' ').append(types[n]);
		}
		return stringBuilder.append(");").toString();
	}

	private void handleVersionChange(final SQLiteDatabase db) {
		final HashMap<String, String> account_db_table_alias = new HashMap<String, String>();
		account_db_table_alias.put(Accounts.SCREEN_NAME, "username");
		account_db_table_alias.put(Accounts.NAME, "username");
		account_db_table_alias.put(Accounts.ACCOUNT_ID, "user_id");
		safeUpgrade(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true, false, account_db_table_alias);
		safeUpgrade(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true, true, null);
		safeUpgrade(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true, true, null);
		safeUpgrade(db, TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true, false, null);
		safeUpgrade(db, TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true, true, null);
		safeUpgrade(db, TABLE_CACHED_STATUSES, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true, true, null);
		safeUpgrade(db, TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true, false, null);
		safeUpgrade(db, TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true, false, null);
		safeUpgrade(db, TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true, false, null);
		safeUpgrade(db, TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES, true,
				true, null);
		safeUpgrade(db, TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS, DirectMessages.Outbox.TYPES, true,
				true, null);
		safeUpgrade(db, TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true, true, null);
		safeUpgrade(db, TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true, false, null);
	}

}
