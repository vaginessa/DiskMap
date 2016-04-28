package com.wangxingyu.diskmap.opengl;

import android.graphics.Canvas;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wangxingyu.diskmap.DiskUsage;
import com.wangxingyu.diskmap.FileSystemState;


public final class FileSystemViewGPU extends SurfaceView implements FileSystemState.FileSystemView, SurfaceHolder.Callback {
    FileSystemState eventHandler;
    private AbstractRenderingThread thread;


    public FileSystemViewGPU(DiskUsage context, FileSystemState eventHandler) {
        super(context);
        this.eventHandler = eventHandler;
        setFocusable(true);
        setFocusableInTouchMode(true);
        Log.d("diskusage", "new FileSystemViewGPU");

//    setBackgroundColor(Color.GRAY);
        SurfaceHolder holder = getHolder();
//    holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        holder.setSizeFromLayout();
        holder.addCallback(this);
        eventHandler.setView(this);
        thread = new RenderingThread(context, eventHandler);
        thread.start();
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent ev) {
        final FileSystemState.MyMotionEvent myev = eventHandler.multitouchHandler.newMyMotionEvent(ev);

        thread.addEvent(new Runnable() {
            @Override
            public void run() {
                eventHandler.onTouchEvent(myev);
            }
        });

        return true;
    }

    public final void runInRenderThread(final Runnable r) {
        thread.addEvent(r);
    }

    public void requestRepaintGPU() {
        if (thread != null) {
            thread.addEmptyEvent();
        }
    }

    public void requestRepaint() {
    }

    public void requestRepaint(int l, int t, int r, int b) {
    }

    @Override
    protected final void onDraw(final Canvas canvas) {
    }

    @Override
    public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
        thread.addEvent(new Runnable() {
            @Override
            public void run() {
                eventHandler.onKeyDown(keyCode, event);
            }
        });
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_SEARCH:
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected final void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//    eventHandler.onLayout(changed, left, top, right, bottom, getWidth(), getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("diskusage", "surfaceChange = " + width + "x" + height);
        thread.addEvent(thread.new SurfaceChangedEvent(holder, width, height));
        requestRepaintGPU();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.addEvent(thread.new SurfaceAvailableEvent(holder, true));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);
        thread.addEvent(thread.new SurfaceAvailableEvent(holder, false));
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d("diskusage", "FileSystemViewGPU.onDetachedFromWindow");
        super.onDetachedFromWindow();
        thread.addEvent(thread.new ExitEvent());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        requestRepaintGPU();
    }

    @Override
    public void killRenderThread() {
        thread.addEvent(thread.new ExitEvent());
        // FIXME: doesn't work
//    try {
//      thread.join();
//    } catch (InterruptedException e) {
//      thread.interrupt();
//    }
    }
}
