package com.android.soundpool.example;

import java.util.HashMap;

import android.content.Context;

// Note: no error checking, no logging
// all public methods of this class are intentionally not thread safe and are supposed
//	to be called either in the main or in the game thread; you can call add() on 1 thread,
//	get() on a 2nd thread, and dispose() on a 3rd one, just don't call them simultaneously
public final class MediaPlayerPools
{
	private final Context mContext;
	private final HashMap<String, MediaPlayerPool> mPools = new HashMap<String, MediaPlayerPool>();
	
	// create it in your activity's onCreate (or anywhere else you can get hold to a context)
	public MediaPlayerPools(final Context context)
	{
		mContext = context;
	}
	
	public MediaPlayerPool add(
			final String filepath,
			final int maxStreams)
	{
		MediaPlayerPool pool = new MediaPlayerPool(mContext, filepath, maxStreams);
		mPools.put(filepath, pool);
		return pool;
	}
	
	public MediaPlayerPool get(final int resourceId)
	{
		return mPools.get(resourceId);
	}
	
	public void dispose()
	{
		for (MediaPlayerPool pool : mPools.values())
		{
			pool.dispose();
		}
		mPools.clear();
	}
}
