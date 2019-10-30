package com.example.hi.imageloaderchooseimage;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hi.imageloaderchooseimage.bean.FolderBean;
import com.example.hi.imageloaderchooseimage.util.PermissionsUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity  {

    private GridView mGridView;
    private RelativeLayout mBottomLy;
    private TextView mDirName;
    private TextView mDirCount;
    //记录当前文件夹下所有.jpeg .png .jpg图片名字
    private List<String> mImgs;
    private ImageAdapter mImageAdapter;
    //记录当前文件夹的路径
    private File mCurrentDir;
    //记录文件夹包含图片的数量
    private  int mCount;
    //所用存放图片文件夹第一张图片对象
    private List<FolderBean> mFolderBeans=new ArrayList<FolderBean>();
    private ProgressDialog mProgressDialog;
    //发送消息的标志
    private static final int DATA_LOADED=0x110;
    private  ListImageDirPopupWindow mDirPopupWindow;
    //一般在子线程和UI交互时有Handler
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==DATA_LOADED){
                if (mProgressDialog!=null&&mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                //绑定数据到View中
                dataView();
                initDirPopupWindow();
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initDatas();

        initEvent();
    }

    @Override
    protected void onDestroy() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void initDirPopupWindow() {
        mDirPopupWindow=new ListImageDirPopupWindow(this,mFolderBeans);
        mDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });
        mDirPopupWindow.setOnDirSelectedListener(new ListImageDirPopupWindow.OnDirSelectedListener() {
            @Override
            public void onSelected(FolderBean folderBean) {
                mCurrentDir=new File(folderBean.getDir());
                mImgs=Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {

                        if (filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png"))
                            return true;
                        return false;
                    }
                }));
                mCount=mImgs.size();
                dataView();
                mDirPopupWindow.dismiss();
            }
        });
    }

    /**
     * 调用imageAdapter填充选择不同文件夹的布局,由于第一持家
     */
    private void dataView() {

        if (mCurrentDir==null){
            Toast.makeText(getApplicationContext(),"未扫描到任何图片",Toast.LENGTH_SHORT).show();
            return;
        }
        mImageAdapter=new ImageAdapter(this,mImgs,mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mImageAdapter);
        mDirCount.setText(mCount +"");
        mDirName.setText(mCurrentDir.getName());
    }

    private void initView() {
        mGridView= (GridView) findViewById(R.id.id_gridview);
        mBottomLy= (RelativeLayout) findViewById(R.id.id_bottom);
        mDirName= (TextView) findViewById(R.id.id_dir_name);
        mDirCount= (TextView) findViewById(R.id.id_dir_count);
    }
    /**
     * 利用ContentProvider扫描手机中的所有图片，最后获取的不是所有图片信息，
     * 而是所有包含图片文件夹的信息，填充到FolderBean
     */
    private void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(getApplicationContext(),"不存在内存卡",Toast.LENGTH_SHORT).show();
            return;
        }
        //动态授权
        PermissionsUtils pu = PermissionsUtils.getInstance();
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        pu.grantPermissions(MainActivity.this, permissions);

        mProgressDialog=ProgressDialog.show(this,null,"正在加载");
        new Thread(){
          public  void run(){
              //获取相机图片的uri
              Uri mImgUri= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
              ContentResolver cr=MainActivity.this.getContentResolver();
              //注意经过测试"=? or "的or左右两边必须各加一个空格
              Cursor cursor=cr.query(mImgUri,null,MediaStore.Images.Media.MIME_TYPE+"=? or "+MediaStore.Images.Media.MIME_TYPE+"=? or "+ MediaStore.Images.Media.MIME_TYPE+"=?",
                     new String[]{"image/jpeg","image/png","image/jpg"}, MediaStore.Images.Media.DATE_MODIFIED);

              //记录文件夹地址的
              Set<String>mDirPaths=new HashSet<String>();
              while (cursor.moveToNext()){
                  //getColumnIndex（）返回uri
                  String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                  File parentFile=new File(path).getParentFile();
                  if (parentFile==null) continue;
                  String dirpath=parentFile.getAbsolutePath();
                  FolderBean folderBean=null;
                  //只记录包含图片的文件夹名称和该文件夹第一张图片路径
                  if (mDirPaths.contains(dirpath)){
                     continue;
                  }else {
                      mDirPaths.add(dirpath);
                      folderBean=new FolderBean();
                      folderBean.setDir(dirpath);
                      folderBean.setFirstImgPath(path);
                  }
                  if (parentFile.list()==null) continue;
                  //对每个文件夹里的图片进行扫描基数
                  int picSize=parentFile.list(new FilenameFilter() {
                      @Override
                      public boolean accept(File dir, String filename) {
                          if (filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png")) return true;
                      return false;
                  }
                  }).length;
                  folderBean.setCount(picSize);
                  //以含有图片最多的文件夹为首展示图片
                  if (picSize> mCount){
                      mCount =picSize;
                      mCurrentDir=parentFile;
                  }
                  Log.i("info",folderBean.getName());
                  mFolderBeans.add(folderBean);
          }
              cursor.close();
              mImgs=Arrays.asList(mCurrentDir.list());
              //通知Handler扫描图片完成
              mHandler.sendEmptyMessage(DATA_LOADED);
        }
      }.start();

    }

    private void initEvent() {
        mBottomLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
                mDirPopupWindow.showAsDropDown(mBottomLy,0,0);
                lightOff();
            }
        });
    }
    /**
     * 内容区域变亮
     */
    private void lightOn() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=1.0f;
        getWindow().setAttributes(lp);
    }
    /**
     * 内容区域变暗
     */
    private void lightOff() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=0.3f;
        getWindow().setAttributes(lp);
    }


}
