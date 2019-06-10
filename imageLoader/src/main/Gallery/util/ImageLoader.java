package com.example.hi.imageloaderchooseimage.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 */
public class ImageLoader {
    //让类有一个对象
    private static ImageLoader mInstance;
    //图片缓存核心对象
    private LruCache<String, Bitmap> mLruCache;
    //线程池
    private ExecutorService mThreadPool;

    //
    private static final int DEAFULT_THREAD_COUNT = 1;
    //队列的调度方式
    private Type mType = Type.LIFO;

    public enum Type {
        FIFO, LIFO
    }

    private LinkedList<Runnable> mTaskQueue;
    //后台轮询线程
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    //UI线程中的Handler
    private Handler mUIHandler;
    //防止mPoolThreadHandler还没new完时就sendMessage
    //防止mPoolThreadHandler还没new完时就sendMessage
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    //控制任务执行的数量
    private Semaphore mSemaphoreThreadPool;

    private ImageLoader(int ThreadCount, Type type) {
        init(ThreadCount, type);
    }

    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        //后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        try {
                            mSemaphoreThreadPool.acquire();
                            //线程池去取出一个任务进行执行
                            mThreadPool.execute(getTask());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
        //获取我们应用此时此刻的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            //计算每个图片占据的bitmap大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        //创建threadCount数量线程的线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;

        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {

            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
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

    public void loadImage(final String path, final ImageView imageView) {

        //这里我们用到的setTag当标志作用，保证加载图片时滑动了屏幕，显示图片就应该为滑动后的图片
        imageView.setTag(path);


        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //获取得到图片，为imageview回调设置图片
                    ImgBeanHoler holder = (ImgBeanHoler) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageView = holder.imageView;
                    String path = holder.path;
                    //将path与getTag存储路径进行比较
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }
        //根据path在缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(path);
        if (bm != null) {
            refreashBitmap(bm, path, imageView);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //加载图片和压缩图片
                    //1.获得图片需要显示的大小
                    List<Integer> list=new ArrayList<>();
                    //获取容器宽高
                    ViewSize imageSize =new ViewSize(imageView);
                    //2.压缩图片
                    Bitmap bm = decodeSampleBitmapFromPath(path, imageSize.getWidth(), imageSize.getHeight());
                    //3.把图片加入到缓存
                    addBitmapToLruCache(path, bm);
                    refreashBitmap(bm, path, imageView);

                    mSemaphoreThreadPool.release();
                }
            });
        }
    }





    private void refreashBitmap(Bitmap bm, String path, ImageView imageView) {
        Message message = Message.obtain();
        ImgBeanHoler holder = new ImgBeanHoler();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入缓存
     *
     * @param path
     * @param bm
     */
    private synchronized void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null)
                mLruCache.put(path, bm);
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
    private Bitmap decodeSampleBitmapFromPath(String path, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        //获取得图片的宽高，并不把图片加载到内存中
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = caculateInSampleSize(options, width, height);
        options.inPurgeable = true;
        //把图片加载到内存中
        options.inJustDecodeBounds = false;
        //使用获取到的压缩比值再次解析图片                                                                                                                        报内存溢出
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算压缩值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public int caculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }



    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);

        //开启一个信号量

        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    private Bitmap getBitmapFromLruCache(String path) {
        return mLruCache.get(path);
    }

    private class ImgBeanHoler {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    public class ImageSize {
        int width;
        int height;
    }
}

