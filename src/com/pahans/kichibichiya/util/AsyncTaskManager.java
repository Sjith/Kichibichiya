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

package com.pahans.kichibichiya.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import android.os.AsyncTask.Status;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsyncTaskManager {

	private final ArrayList<ManagedAsyncTask> mTasks = new ArrayList<ManagedAsyncTask>();

	private static AsyncTaskManager sInstance;

	public <T> int add(final ManagedAsyncTask task, final boolean exec, final T... params) {
		final int hashCode = task.hashCode();
		mTasks.add(task);
		if (exec) {
			execute(hashCode);
		}
		return hashCode;
	}

	public boolean cancel(final int hashCode) {
		return cancel(hashCode, true);
	}

	public boolean cancel(final int hashCode, final boolean mayInterruptIfRunning) {
		final ManagedAsyncTask task = findTask(hashCode);
		if (task != null) {
			task.cancel(mayInterruptIfRunning);
			mTasks.remove(task);
			return true;
		}
		return false;
	}

	/**
	 * Cancel all tasks added, then clear all tasks.
	 */
	public void cancelAll() {
		for (final ManagedAsyncTask task : mTasks) {
			task.cancel(true);
		}
		mTasks.clear();
	}

	public <T> boolean execute(final int hashCode, final T... params) {
		final ManagedAsyncTask task = findTask(hashCode);
		if (task != null) {
			task.execute(params == null || params.length == 0 ? null : params);
			return true;
		}
		return false;
	}

	public ArrayList<ManagedAsyncTask<?, ?, ?>> getTaskList() {
		return (ArrayList<ManagedAsyncTask<?, ?, ?>>) mTasks.clone();
	}

	public boolean hasRunningTask() {
		final ArrayList<ManagedAsyncTask> tasks_to_remove = new ArrayList<ManagedAsyncTask>();
		for (final ManagedAsyncTask task : getTaskList()) {
			if (task.getStatus() != ManagedAsyncTask.Status.RUNNING) {
				tasks_to_remove.add(task);
			}
		}
		for (final ManagedAsyncTask task : tasks_to_remove) {
			remove(task.hashCode());
		}
		return mTasks.size() > 0;
	}

	public boolean isExcuting(final int hashCode) {
		final ManagedAsyncTask task = findTask(hashCode);
		if (task != null && task.getStatus() == Status.RUNNING) return true;
		return false;
	}

	public void remove(final int hashCode) {
		try {
			mTasks.remove(findTask(hashCode));
		} catch (final ConcurrentModificationException e) {
			// Ignore.
		}
	}

	private ManagedAsyncTask findTask(final int hashCode) {
		try {
			for (final ManagedAsyncTask task : getTaskList()) {
				if (hashCode == task.hashCode()) return task;
			}
		} catch (final ConcurrentModificationException e) {

		}
		return null;
	}

	public static AsyncTaskManager getInstance() {
		if (sInstance == null) {
			sInstance = new AsyncTaskManager();
		}
		return sInstance;
	}
}
