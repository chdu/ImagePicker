package dev.chdu.picker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created on 5/24/2016.
 */
public class ImageLoader {

    private static ImageLoader mInstance;

    private LruCache<String, Bitmap> mLruCache;

    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREAD_COUNT = 1;
    private Type mType = Type.LIFO;
    private LinkedList<Runnable> mTaskQueue;
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    private Handler mUIHandler;
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    public enum Type {
        FIFO, LIFO
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * ImageLoader初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        // 后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // 从线程池取出一个任务执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据path为imageview设置图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageBean imageBean = (ImageBean) msg.obj;
                    String path = imageBean.path;
                    ImageView imageView = imageBean.imageView;
                    Bitmap bitmap = imageBean.bitmap;
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };
        }

        Bitmap bitmap = getBitmapFromLruCache(path);
        if (bitmap != null) {
            refreshBitmap(path, imageView, bitmap);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    // Bitmap加载
                    // 1.获得图片需要显示的大小
                    ImageSize size = getImageViewSize(imageView);
                    // 2.压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path, size.width, size.height);
                    // 3.把图片加入到缓存
                    addBitmapToLruCache(path, bm);
                    refreshBitmap(path, imageView, bm);
                    mSemaphoreThreadPool.release();
                }
            });
        }
    }

    private void refreshBitmap(String path, ImageView imageView, Bitmap bm) {
        Message msg = Message.obtain();
        ImageBean imageBean = new ImageBean();
        imageBean.path = path;
        imageBean.imageView = imageView;
        imageBean.bitmap = bm;
        msg.obj = imageBean;
        mUIHandler.sendMessage(msg);
    }

    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据图片需求宽高和实际宽高计算InSampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 根据ImageView获取适当的压缩尺寸
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize size = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        int width = imageView.getWidth(); // 获取imageview的实际宽度
        if (width <= 0) {
            width = imageView.getLayoutParams().width; // 获取imageview在layout中的实际宽度
        }
        if (width <= 0) {
            width = imageView.getMaxWidth();
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }

        int height = imageView.getHeight(); // 获取imageview的实际高度
        if (height <= 0) {
            height = imageView.getLayoutParams().height; // 获取imageview在layout中的实际高度
        }
        if (height <= 0) {
            height = imageView.getMaxHeight();
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }

        size.width = width;
        size.height = height;
        return size;
    }

    /**
     * 从任务队列取出Runnable
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        } else if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        }
        return null;
    }

    /**
     * 将Runnable加入任务队列
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x999);
    }

    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private class ImageBean {
        String path;
        ImageView imageView;
        Bitmap bitmap;
    }

    private class ImageSize {
        int width;
        int height;
    }
}
