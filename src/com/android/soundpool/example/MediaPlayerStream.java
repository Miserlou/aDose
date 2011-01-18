package com.android.soundpool.example;

// simple abstraction for the MediaPlayer instance
//		once the SoundPool is an official API, MediaPlayerPool can aggregate
//		an instance of SoundPool and still provide the same interface
//	the abstraction is also useful if you are planning to provide sanity checks
//		when calling pause/resume/stop
public interface MediaPlayerStream
{
	void setVolume(float leftVolume, float rightVolume);
	void pause();
	void resume();
	void stop();
}
