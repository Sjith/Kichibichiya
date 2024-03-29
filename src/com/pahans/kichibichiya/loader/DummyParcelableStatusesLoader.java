package com.pahans.kichibichiya.loader;

import java.util.Collections;
import java.util.List;

import com.pahans.kichibichiya.model.ParcelableStatus;

import android.content.Context;

public final class DummyParcelableStatusesLoader extends ParcelableStatusesLoader {

	public DummyParcelableStatusesLoader(final Context context, final long account_id, final List<ParcelableStatus> data) {
		super(context, account_id, data, null, false);
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		return Collections.emptyList();
	}

}
