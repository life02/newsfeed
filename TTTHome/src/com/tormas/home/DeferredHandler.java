/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tormas.home;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

import java.util.LinkedList;

import com.tormas.home.LauncherModel.Callbacks;

/**
 * Queue of things to run on a looper thread.  Items posted with {@link #post} will not
 * be actually enqued on the handler until after the last one has run, to keep from
 * starving the thread.
 *
 * This class is fifo.
 */
public class DeferredHandler {
    private LinkedList<Runnable> mQueue = new LinkedList();
    private MessageQueue mMessageQueue = Looper.myQueue();
    private Impl mHandler = new Impl();

    private class Impl extends Handler implements MessageQueue.IdleHandler {
        public void handleMessage(Message msg) {
            Runnable r;
            synchronized (mQueue) {
                if (mQueue.size() == 0) {
                    return;
                }
                r = mQueue.removeFirst();
            }
            if(BindItemRunnable.class.isInstance(r))
            {
                backHandler.post(r);
            }
            else if(FinishBindItemRunnable.class.isInstance(r))
            {
            	if(isWorkspaceAddFinished() == false)
            	{
            		synchronized (mQueue) 
            		{
            			Log.d("DeferredHandler", " mQueue size should be 0= "+mQueue.size());
            	        mQueue.add(r);
            		}
            		this.sendEmptyMessageDelayed(1,  200);
            		return;
            	}
            	else
            	{
            		r.run();
            	}
            }
            else
            {
            	r.run();
            }
            
            synchronized (mQueue) {
                scheduleNextLocked();
            }
        }
        
        public boolean queueIdle() {
            handleMessage(null);
            return false;
        }
    }
    
    private boolean isWorkspaceAddFinished()
    {
    	return mCallback.isFinishedBindItems(mItemSize);
    }
    
    private class BindItemRunnable implements Runnable {
        Runnable mRunnable;

        BindItemRunnable(Runnable r) {
            mRunnable = r;
        }

        public void run() {
            mRunnable.run();
        }
    }
    
    private class FinishBindItemRunnable implements Runnable {
        Runnable mRunnable;

        FinishBindItemRunnable(Runnable r) {
            mRunnable = r;
        }

        public void run() {
            mRunnable.run();
        }
    }

    
    private class BackGroundHandler extends Handler {
    	public BackGroundHandler(Looper looper) {
             super(looper);
        }

        public void handleMessage(Message msg) 
        {                        
        
        }
    }
    
    
    Looper handlerLooper;
    BackGroundHandler backHandler;
    Thread bindItemThread = new Thread("forBindDesktopItem-Thread")
    {
    	public void run()
    	{
    		Looper.prepare();    		
    		backHandler = new BackGroundHandler(Looper.myLooper());
    		Looper.loop();
    	}
    };
    
    private class IdleRunnable implements Runnable {
        Runnable mRunnable;

        IdleRunnable(Runnable r) {
            mRunnable = r;
        }

        public void run() {
            mRunnable.run();
        }
    }

    public DeferredHandler() {
    	bindItemThread.start();
    	
    	
    }

    /** Schedule runnable to run after everything that's on the queue right now. */
    public void post(Runnable runnable) {
        synchronized (mQueue) {
            mQueue.add(runnable);
            if (mQueue.size() == 1) {
                scheduleNextLocked();
            }
        }
    }

    /** Schedule runnable to run when the queue goes idle. */
    public void postIdle(final Runnable runnable) {
        post(new IdleRunnable(runnable));
    }
    
    /** Schedule runnable to run when the queue goes idle. */
    public void postBind(final Runnable runnable) {
    	if(Launcher.isLowLevelHardware())
            post(new BindItemRunnable(runnable));
    	else
    		post(runnable);
    }
    
    public void postFinishBind(final Runnable runnable) {
    	if(Launcher.isLowLevelHardware())
            post(new FinishBindItemRunnable(runnable));
    	else
    		post(runnable);
    }

    public void cancelRunnable(Runnable runnable) {
        synchronized (mQueue) {
            while (mQueue.remove(runnable)) { }
        }
    }

    public void cancel() {
        synchronized (mQueue) {
            mQueue.clear();
        }
    }

    void scheduleNextLocked() {
        if (mQueue.size() > 0) {
            Runnable peek = mQueue.getFirst();
            if (peek instanceof IdleRunnable) {
                mMessageQueue.addIdleHandler(mHandler);
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }
    
    Callbacks mCallback;
    int       mItemSize;
	public void setCallback(Callbacks callbacks, int itemSize) {
		mCallback = callbacks;
		mItemSize = itemSize;
	}
}
