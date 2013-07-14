package com.pahans.kichibichiya.adapter;

import com.pahans.kichibichiya.R;

import com.pahans.kichibichiya.model.ListAction;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListActionAdapter extends ArrayAdapter<ListAction> {

	public ListActionAdapter(final Context context) {
		super(context, R.layout.list_action_item, android.R.id.text1);
	}

	public ListAction findItem(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (id == getItemId(i)) return getItem(i);
		}
		return null;
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).getId();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final TextView summary_view = (TextView) view.findViewById(android.R.id.text2);
		final String summary = getItem(position).getSummary();
		summary_view.setText(summary);
		summary_view.setVisibility(!TextUtils.isEmpty(summary) ? View.VISIBLE : View.GONE);
		return view;
	}
}
