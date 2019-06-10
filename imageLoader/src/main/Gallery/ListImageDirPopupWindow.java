package com.example.hi.imageloaderchooseimage;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hi.imageloaderchooseimage.bean.FolderBean;
import com.example.hi.imageloaderchooseimage.util.ImageLoader;

import java.util.List;

/**
 * Created by hi on 8/23/2017.
 */
public class ListImageDirPopupWindow extends PopupWindow {

    private int mWidth;
    private int mHeight;
    private ListView mListView;
    private View mConvertView;
    //记录总图片(mFolderBean)
    private List<FolderBean>mDatas;
    public OnDirSelectedListener mListener;


    public interface OnDirSelectedListener{
        void onSelected(FolderBean folderBean);
    }
    public void setOnDirSelectedListener(OnDirSelectedListener mListener){
        this.mListener=mListener;
    }


    public ListImageDirPopupWindow(final Context context, List<FolderBean> datas) {

        calWidthAndHeiht(context);

        mConvertView= LayoutInflater.from(context).inflate(R.layout.popup_main,null);
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                if (event.getAction()==MotionEvent.ACTION_UP){
                    Toast.makeText(context,"PopupWindow触击事件被调用",Toast.LENGTH_SHORT).show();
                }
                return false;

            }
        });
        if (datas!=null)
        this.mDatas = datas;
        initView(context);
        initEvent();
    }

    /**
     * 计算popupWindow的宽和高
     * @param context
     */
    private void calWidthAndHeiht(Context context) {
        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //Metrics度量标准
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //设置PopupWindow的宽度和高度
        mWidth=outMetrics.widthPixels;
        mHeight= (int) (outMetrics.heightPixels*0.6);
    }


    private void initView(Context context ) {
        mListView= (ListView) mConvertView.findViewById(R.id.id_list_dir);
        mListView.setAdapter(new ListDirAdapter(context,mDatas));
    }
    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mListener!=null)mListener.onSelected(mDatas.get(position));
            }
        });
    }

    /**
     *填充PopupWindow的ListView
     */
    private class ListDirAdapter extends ArrayAdapter<FolderBean>{
        private  LayoutInflater mInflater;

        public ListDirAdapter(Context context,List<FolderBean> Object) {
            super(context,0,Object);
            mInflater=LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoler holder;
            if (convertView==null){
                holder=new ViewHoler();
                convertView=mInflater.inflate(R.layout.item_popup_main,parent,false);
                holder.mImg= (ImageView) convertView.findViewById(R.id.id_dir_item_image);
                holder.mDirName= (TextView) convertView.findViewById(R.id.id_dir_item_name);
                holder.mDirCount= (TextView) convertView.findViewById(R.id.id_dir_item_count);
                convertView.setTag(holder);
            }else {
                holder= (ViewHoler) convertView.getTag();
            }
            FolderBean bean=getItem(position);
            //重置
            holder.mImg.setImageResource(R.drawable.picture_no);
            // look at here
            ImageLoader.getInstance().loadImage(bean.getFirstImgPath(),holder.mImg);
            holder.mDirCount.setText(bean.getCount()+"");
            holder.mDirName.setText(bean.getName());
            return convertView;
        }

        //注意合理的运用viewholder有利于运行速度
        private class ViewHoler{
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;
        }
    }


}
