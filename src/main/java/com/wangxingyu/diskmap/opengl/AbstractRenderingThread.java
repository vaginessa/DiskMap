package com.wangxingyu.diskmap.opengl;

import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public abstract class AbstractRenderingThread extends Thread {
  public abstract boolean renderFrame(GL10 gl);
  public abstract void sizeChanged(GL10 gl, int w, int h);
  public abstract void createResources(GL10 gl);
  public abstract void releaseResources(GL10 gl);
  
  private class ExitException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }
  
  private final ArrayList<Runnable> events = new ArrayList<>();
  /**
   * True when surfaceAvailable callback was received from surfaceHolder and 
   * surfaceDestroyed wasn't yet received.
   * egl is initialized able to render.
   */
  private boolean surfaceAvailable = false;
  
  /**
   * Window geometry received by SurfaceChangedEvent().
   */
  private boolean sizeInitialized = false;
  
  /**
   * Repaint was requested due to unfinished animation on drawFrame().
   */
  private boolean renderLoop = true;
  
  
  /**
   * Repaint was requested using requestRepaintGPU() call.
   */
  private boolean repaintEvent = false;
  
  /**
   * Stop rendering thread request received.
   */
  private boolean stopRenderingThread = false;
  private EglTools eglTools;
  GL10 gl;
  
  @Override
  public void run() {
    eglTools = new EglTools();
    gl = eglTools.getGL();
    
    try {
      while (true) {
        runEvents();
        renderLoop = renderFrame(gl);
        eglTools.swapBuffers();
      }
      
    } catch (ExitException e) {
      Log.d("diskusage", "rendering thread exited cleanly");
    } catch (InterruptedException e) {
      Log.d("diskusage", "rendering thread was interrupted");

    }
  }
  
  
  public void runEvents() throws InterruptedException {
    while (true) {
      Runnable e;
      synchronized (events) {
        if (events.isEmpty()) {
          if (stopRenderingThread && !surfaceAvailable) {
            Log.d("diskusage", "*** Rendering thread is about to finish. ***");
            throw new ExitException();
          }
          if (surfaceAvailable  && sizeInitialized && !stopRenderingThread
              && (renderLoop || repaintEvent)) {
            repaintEvent = false;
            return;
          }

          events.wait();
          continue;
        }
        e = events.remove(0);
      }
      if (e instanceof ControlEvent || !stopRenderingThread) {
        e.run();
      }
    }
  }
  
  public void addEvent(Runnable event) {
    synchronized (events) {
      events.add(event); 
      events.notify();
    }
  }

  private abstract class ControlEvent implements Runnable {
    public abstract void run();
  }
  
  public class SurfaceAvailableEvent extends ControlEvent {
    private boolean a;
    private SurfaceHolder holder;
    
    public SurfaceAvailableEvent(SurfaceHolder holder, boolean available) {
      this.holder = holder;
      a = available;
    }
    public void run() {
      surfaceAvailable = a;
      if (a) {
        eglTools.initSurface(holder);
        createResources(gl);
      } else {
        eglTools.destroySurface();
        releaseResources(gl);
      }
    }
  }
  
  public class ExitEvent extends ControlEvent {
    public void run() {
      stopRenderingThread = true;
      releaseResources(gl);
    }
  }
  
  public class SurfaceChangedEvent extends ControlEvent {
    SurfaceHolder holder;
    int w, h;
    public SurfaceChangedEvent(SurfaceHolder holder, int w, int h) {
      this.holder = holder;
      this.w = w;
      this.h = h;
    }
    
    public void run() {
      sizeChanged(gl, w, h);
      sizeInitialized = (w > 0 && h > 0);
    }
  }
  
  private class EglTools {
    private final EGL10 egl;
    private final EGLDisplay eglDisplay;
    private final EGLContext eglContext;
    private final EGLConfig eglConfig;
    private EGLSurface surface;

    public EglTools() {
      egl = (EGL10) EGLContext.getEGL();
      eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
      int[] version = new int[2];
      egl.eglInitialize(eglDisplay, version);

      int[] configSpec = {
          EGL10.EGL_DEPTH_SIZE,   6,
          EGL10.EGL_NONE
      };

      final EGLConfig[] matched_configs = new EGLConfig[1];
      int num_configs[] = new int[1];
      egl.eglChooseConfig(eglDisplay, configSpec, matched_configs, 1, num_configs);
      eglConfig = matched_configs[0];
      
      eglContext = egl.eglCreateContext(
          eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null);
    }
    
    public GL10 getGL() {
      return (GL10) eglContext.getGL();
    }
    
    public void initSurface(SurfaceHolder holder) {
      Log.d("diskusage", "*** init surface ****");
      
      // Note: I haven't found how to avoid race condition with surfaceCreated
      // and surfaceDestroyed in SurfaceHolder.Callback and the renderer thread.
      try {
        surface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, holder, null);
        egl.eglMakeCurrent(eglDisplay, surface, surface, eglContext);
      } catch (Exception e) {
        Log.e("diskusage", "initSurface", e);
      }
    }
    
    public void destroySurface() {
      Log.d("diskusage", "*** destroy surface ***");
      try {
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, surface);
        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglTerminate(eglDisplay);
      } catch (Exception e) {
        Log.e("diskusage", "destroySurface", e);
      }
    }
    
    public void swapBuffers() {
      egl.eglSwapBuffers(eglDisplay, surface);
    }
  }

  public void addEmptyEvent() {
    synchronized (events) {
      repaintEvent = true;
      events.notify();
    }
  }
}
