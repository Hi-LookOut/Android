package com.example.hi.imageloaderchooseimage;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.hi.imageloaderchooseimage.util.ImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
        private static Set<String> mSelectedImg=new HashSet<String>();
        private List<String> mImgPaths;
        private String mDirPath;
        private LayoutInflater mInflater;
        //mDatas是图片名称集合，dirPath是路径，如果两个合并为一时比较占内存
        public ImageAdapter(Context context, List<String>mDatas, String dirPath) {
            this.mDirPath=dirPath;
            this.mImgPaths=mDatas;
            mInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mImgPaths.size();
        }

        @Override
        public Object getItem(int positoin) {
            return mImgPaths.get(positoin);
        }

        @Override
        public long getItemId(int positoin) {
            return positoin;
        }

        @Override
        public View getView(final int positon, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            final String filePath=mDirPath+"/"+mImgPaths.get(positon);
            if (convertView==null){
                convertView=mInflater.inflate(R.layout.item_gridview,parent,false);
                viewHolder=new ViewHolder();
                viewHolder.mImag= (ImageView) convertView.findViewById(R.id.id_item_image);
                viewHolder.mSelect= (ImageButton) convertView.findViewById(R.id.id_item_select);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            //重置状态
            viewHolder.mImag.setImageResource(R.drawable.picture_no);
            viewHolder.mSelect.setImageResource(R.drawable.picture_unselected);
            viewHolder.mImag.setColorFilter(null);
            //起一个更新UI与存入缓存的作用
            System.out.println("123"+filePath);
            ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(
                    filePath,viewHolder.mImag);
            viewHolder.mImag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //已经被选择
                     if (mSelectedImg.contains(filePath)){
                        mSelectedImg.remove(filePath);
                        viewHolder.mImag.setColorFilter(null);
                        viewHolder.mSelect.setImageResource(R.drawable.picture_unselected);
                    }else {//未被选择
                        mSelectedImg.add(filePath);
                        viewHolder.mImag.setColorFilter(Color.parseColor("#77000000"));
                        viewHolder.mSelect.setImageResource(R.drawable.picture_selected);
                    }
                   // notifyDataSetChanged();因为会造成手机闪一下屏,只用改变item的状态
                }
            });
            if (mSelectedImg.contains(filePath)){
                viewHolder.mImag.setColorFilter(Color.parseColor("#77000000"));
                viewHolder.mSelect.setImageResource(R.drawable.picture_selected);
            }
            return convertView;
        }
        private class ViewHolder{
            ImageView mImag;
            ImageButton mSelect;
        }
    }