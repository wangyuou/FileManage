package com.example.filemanage;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context mContent;
    private UserViewModel mUserData;
    private List<String> mList = new ArrayList<String>();
    private class ViewHolder {
        ImageView DeviceIcon;
        TextView DeviceName;
    }

    public DeviceAdapter(Context Content,List<String> list,UserViewModel Data)
    {
        mContent = Content;
        mList = list;
        mUserData = Data;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder Holder = null;
        if(view == null){
            view = LayoutInflater.from(mContent).inflate(R.layout.devicelist,viewGroup,false);//动态加载控件，通过布局文件item_list_animal
            Holder = new ViewHolder();
            Holder.DeviceIcon = (ImageView) view.findViewById(R.id.DeviceIcon);
            Holder.DeviceName = (TextView) view.findViewById(R.id.DeviceName);

            view.setTag(Holder);   //将Holder存储到convertView中
        }else{
            Holder = (ViewHolder) view.getTag();
        }
        if(mUserData.CurrentItem == i)
        {
            view.setBackgroundColor(mContent.getResources().getColor(R.color.FileItem));
        }
        else
        {
            view.setBackgroundColor(Color.WHITE);
        }
        Holder.DeviceName.setText(mList.get(i));
        return view;
    }

    public void setCurrentItem(Integer pos)
    {
        mUserData.CurrentItem = pos;
    }

    public void refresh()
    {
        notifyDataSetChanged();
    }
}
