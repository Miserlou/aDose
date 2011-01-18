package com.android.soundpool.example;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

// Note: call MediaPlayer.play(...) and dispose() methods on one thread
//	On the MediaPlayerStream instance returned by play(...) call all methods on one thread
public final class MediaPlayerPool
{
	private final Pool mPool;
	Pool.Node lastNode;

	public MediaPlayerPool(
			final Context context,
			final String path,
			final int maxStreams,
	        final boolean asset)
	{
		Pool pool = new Pool();
		if(asset) {
    		AssetManager am = context.getAssets();
            for (int i = 0; i < maxStreams; ++i)
            {
                System.out.println("file:///android_asset/" + path);
                MediaPlayer player = new MediaPlayer(); 
                try {
                    
                    System.out.println(am.openFd(path));
                    System.out.println(path);
                    AssetFileDescriptor amfd = am.openFd(path);
                    player.setDataSource(amfd.getFileDescriptor(), amfd.getStartOffset(), amfd.getLength());
                    amfd.close();
                    player.setOnPreparedListener(new OnPreparedListener() {
    
                        public void onPrepared(MediaPlayer mp) {
                            // TODO Auto-generated method stub
                            System.out.println("Oh we prepared.");
                        }
                        
                    });
                    player.prepare();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                pool.add(player);
            }
		}
		else {
		    for (int i = 0; i < maxStreams; ++i)
            {
                MediaPlayer player = new MediaPlayer(); 
                try {
                    player.setDataSource(path);
                    player.setOnPreparedListener(new OnPreparedListener() {
    
                        public void onPrepared(MediaPlayer mp) {
                            System.out.println("Oh we prepared.");
                        }
                        
                    });
                    player.prepare();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                pool.add(player);
            }
		}
        mPool = pool;
	}
	
	// it's OK to ignore the returned result, when the sample stops playing
	//	it will return itself in the pool
    public MediaPlayerStream play(
    		final float leftVolume, 
    		final float rightVolume)
    {
        if(lastNode != null) {
            System.out.println("Releasing");
            lastNode.release();
        }
        Pool.Node node = mPool.acquire();
        node.play(leftVolume, rightVolume);
        lastNode = node;
        return node;
    }
    
    // called by MediaPlayerPools, but left it public so you can
    //		create a MediaPlayerPool instance and control it yourself
    public void dispose()
    {
        mPool.dispose();
    }
    
    // this is essentially a free list of media players, each wrapped in a node
    private static final class Pool
    {
        private Node mHead;
        // we need a mutex (lock, guard, latch, monitor, whatever you want to call it) to 
        //		synchronize between the acquire() and release() methods because acquire()
        //		is called on your thread, an release() is called on whatever thread the
        //		media player is running and is calling its completion listener
        private final Object mLatch = new Object();
        private final Object mNotEmpty = new Object();
        
        void add(final MediaPlayer player)
        {
        	Node node = new Node(player);
            node.mNext = mHead;
            mHead = node;
        }

        // continuously tries to obtain a free node from the list, if it fails, 
        //		blocks until a node is added to the list and retries
        Node acquire()
        {
            synchronized (mNotEmpty)
            {
                for (;;)
                {
                    Node node;
                    synchronized (mLatch)
                    {
                        node = mHead;
                        if (node != null)
                        {
                            mHead = node.mNext;
                            return node;
                        }
                    }
                    try
                    {
                        mNotEmpty.wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }

        // returns a node to the free list and signals *one* waiter
        //		notify(), and not notifyAll() is called to avoid 
        //		contention and busy waits
        void release(final Node node)
        {
            synchronized (mNotEmpty)
            {
                synchronized (mLatch)
                {
                    node.mNext = mHead;
                    mHead = node;
                }
                mNotEmpty.notify();
            }
        }

        void dispose()
        {
            synchronized (mLatch)
            {
                Node temp = null;
                while (mHead != null)
                {
                    temp = mHead.mNext;
                    mHead.dispose();
                    mHead = temp;
                }
            }
        }

        final class Node implements MediaPlayerStream
        {
            public MediaPlayer mPlayer;
            public Node mNext;

        	public Node(final MediaPlayer player)
            {
                this.mPlayer = player;
                player.setLooping(true);
                player.setOnCompletionListener(new OnCompletionListener()
                {
                    public final void onCompletion(final MediaPlayer player)
                    {
                    	Node.this.release();
                    }
                });
            }
        	
        	// MediaPlayerStream implementation
        	
        	public void setVolume(float leftVolume, float rightVolume)
        	{
        		mPlayer.setVolume(leftVolume, rightVolume);
        	}

            public void stop()
            {
            	mPlayer.stop();
            	release();
            }
            
            public void pause()
            {
                Node.this.release();
            	mPlayer.pause();
            }
            
            public void resume()
            {
            	mPlayer.start();
            }

            void play(float leftVolume, float rightVolume)
            {
            	MediaPlayer player = mPlayer;
            	player.setVolume(leftVolume, rightVolume);
            	player.start();
            }
            
            // book keeping
            
            void release()
            {
            	mPlayer.seekTo(0);
            	Pool.this.release(this);
            }

            void dispose()
            {
                mPlayer.release();
                mPlayer = null;
            }
        } // ~Node

    } // ~Pool

} // ~MediaPlayerPool
