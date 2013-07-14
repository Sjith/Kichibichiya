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

package com.pahans.kichibichiya.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView {

	private final float mRadius;
	private final Path mPath = new Path();
	private Bitmap mRounder;
	private Paint mPaint;

	public RoundCorneredImageView(final Context context) {
		this(context, null);
	}

	public RoundCorneredImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundCorneredImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SetLayerTypeAccessor.setLayerType(this, View.LAYER_TYPE_SOFTWARE, null);
		}
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.radius });
		mRadius = a.getDimensionPixelSize(0, 0);
		a.recycle();
		createPath();
	}

	@Override
	public void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (mRounder != null && mPaint != null) {
				canvas.drawBitmap(mRounder, 0, 0, mPaint);
			}
		} else {
			// Workaround for pre-ICS devices, without anti alias.
			try {
				canvas.clipPath(mPath);
			} catch (final UnsupportedOperationException e) {
				// This shouldn't happen, but in order to keep app running, I
				// simply ignore this Exception.
			}
		}
	}

	@Override
	public void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		if (w > 0 && h > 0) {
			mRounder = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			final Canvas canvas = new Canvas(mRounder);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
			mPaint.setColor(Color.BLACK);
			canvas.drawRoundRect(new RectF(0, 0, w, h), mRadius, mRadius, mPaint);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			createPath();
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void createPath() {
		final float density = getResources().getDisplayMetrics().density;
		mPath.reset();
		mPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), 4 * density, 4 * density, Path.Direction.CW);
	}

	static class SetLayerTypeAccessor {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public static void setLayerType(final View view, final int layerType, final Paint paint) {
			view.setLayerType(layerType, paint);
		}
	}
}
