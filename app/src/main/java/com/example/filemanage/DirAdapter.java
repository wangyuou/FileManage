package com.example.filemanage;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirAdapter extends BaseAdapter {
    private Context mContent;
    private UserViewModel mUserData;
    public List<File> mList = new ArrayList<File>();
    private class ViewHolder {
        ImageView PathIcon;
        TextView Path;
    }

    private void getFileInfo(String Path) {
        if (Path.isEmpty()) {
            return ;
        }
        else
        {
            File Files = new File(Path);
            if(Files.isDirectory())//目录
            {
                File[] SubFiles = Files.listFiles();
                if(SubFiles == null) {//目录为空
                    ;
                }
                else{
                    for (File file:SubFiles) {
                        mList.add(file);
                    }
                }
            }
            else {//文件
                mList.add(Files);
            }
        }
    }

    public DirAdapter(Context Content,String Path,UserViewModel Data)
    {
        mContent = Content;
        mUserData = Data;
        //添加Path到list
        mUserData.DirDirectoryList.add(Path);
        getFileInfo(Path);
        System.out.println(Path);
        System.out.println(mList);
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
            view = LayoutInflater.from(mContent).inflate(R.layout.pathitem,viewGroup,false);//动态加载控件，通过布局文件item_list_animal
            Holder = new ViewHolder();
            Holder.PathIcon = (ImageView) view.findViewById(R.id.PathIcon);
            Holder.Path = (TextView) view.findViewById(R.id.Path);

            view.setTag(Holder);   //将Holder存储到convertView中
        }else{
            Holder = (ViewHolder) view.getTag();
        }
        if(mList.get(i).isDirectory())
        {
            Holder.PathIcon.setImageResource(R.mipmap.file);
        }
        else
        {
            Holder.PathIcon.setImageResource(R.mipmap.image);
        }
        if(mUserData.DirCurrentItem == i)
        {
            view.setBackgroundColor(mContent.getResources().getColor(R.color.FileItem));
        }
        else
        {
            view.setBackgroundColor(Color.WHITE);
        }
        Holder.Path.setText(mList.get(i).getName());
        return view;
    }

    public void setCurrentItem(Integer pos)
    {
        mUserData.DirCurrentItem = pos;
    }

    public void refresh()
    {
        //刷新List
        mList.clear();
        getFileInfo(mUserData.DirDirectoryList.get(mUserData.DirDirectoryList.size()-1));
        notifyDataSetChanged();
    }
}
