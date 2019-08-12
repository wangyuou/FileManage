package com.example.filemanage;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.design.animation.Positioning;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class FileAdapter extends BaseAdapter {
    private enum FileCtrlType {MOVEDIRECTORY,COPYDIRECTORY,DELETE,MOVEFILE,COPYFILE,RENAMEDIRECTORY,RENAMEFILE};
    private Context mContent = null;
    public UserViewModel mUserData  = null;
    public List<File> mFileList = new ArrayList<File>();
    private String mDestDirectory = null;

    //路径选择器监听器
    private class PathSelectClick implements View.OnClickListener{

        private DirAdapter mDirAdapter;
        private TextView mPath;
        public PathSelectClick(DirAdapter dirAdapter,TextView path)
        {
            mDirAdapter = dirAdapter;
            mPath = path;
        }
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.URoot://返回U盘根目录
                    mUserData.DirDirectoryList.clear();
                    mUserData.DirDirectoryList.add(Environment.getRootDirectory().toString());
                    mPath.setText(Environment.getRootDirectory().toString());
                    mDirAdapter.refresh();
                    break;
                case R.id.FRoot://返回fdisk根目录
                    mUserData.DirDirectoryList.clear();
                    mUserData.DirDirectoryList.add(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk");
                    mPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk");
                    mDirAdapter.refresh();
                    break;
                case R.id.Return://返回上一级目录
                    if(mUserData.DirDirectoryList.size() < 2)
                    {
                        Toast.makeText(mContent, R.string.RootDirectory, Toast.LENGTH_LONG).show();
                        return ;
                    }
                    mUserData.DirDirectoryList.remove(mUserData.DirDirectoryList.size()-1);
                    mPath.setText(mUserData.DirDirectoryList.get(mUserData.DirDirectoryList.size()-1));
                    mDirAdapter.refresh();
                    break;
                case R.id.EnterDirectory://进入目录
                    String path = mDirAdapter.mList.get(mUserData.DirCurrentItem).getAbsolutePath();
                    mUserData.DirDirectoryList.add(path);
                    mDirAdapter.setCurrentItem(0);
                    mPath.setText(path);
                    mDirAdapter.refresh();
                    break;
                default:
            }
        }
    };


    //计算文件大小Task
    class SizeAsyncTask extends AsyncTask<File,Void,String> {

        private TextView mText = null;
        private Integer mPos = 0;
        public SizeAsyncTask(TextView text,Integer pos)
        {
            mText = text;
            mPos = pos;
        }
        @Override
        protected String doInBackground(File... files) {
            if(mPos >= mFileList.size())
            {
                return "";
            }
            return FileUtil.getFormatSize(FileUtil.getFolderSize(mFileList.get(mPos)));
        }

        //onPostExecute用于UI的更新.此方法的参数为doInBackground方法返回的值.
        @Override
        protected void onPostExecute(String Size) {
            super.onPostExecute(Size);
            if(mText != null) {
                mText.setText(Size);
            }
        }
    }

    //文件操作同步栈
    private class FilesTask extends AsyncTask<File, String, String> {

        private String mTitle[] = {"移动文件夹", "复制文件夹", "删除","移动文件","复制文件","重命名文件夹","重命名文件"};
        private FileCtrlType mType;
        private final ProgressDialog Progress = new ProgressDialog(mContent);
        private String DestDirectory = null;

        public void setDestDirectory(String destDirectory) {
            DestDirectory = destDirectory;
        }

        public FilesTask(FileCtrlType type) {
            mType = type;
            //设置标题
            Progress.setTitle(mTitle[mType.ordinal()]);
            //设置提示信息
            //设置ProgressDialog 是否可以按返回键取消；
            Progress.setCancelable(true);
            Progress.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
            Progress.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int i) {
                    onCancelled();
                }
            });

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //显示Progress
            if(Progress != null) {
                Progress.show();
            }
        }

        @Override
        protected String doInBackground(File... files) {
            //获取文件名
            File file = (File) files[0];
                publishProgress("正在" + mTitle[mType.ordinal()] + ":" + file.getAbsolutePath());
                switch (mType) {
                    case DELETE:
                        FileUtil.delete(mContent, file.getAbsolutePath());
                        break;
                    case COPYDIRECTORY:
                        System.out.println(file.getAbsolutePath());
                        System.out.println(mDestDirectory);
                        FileUtil.copy(mContent,file.getAbsolutePath(),mDestDirectory);
                        break;
                    case MOVEDIRECTORY:
                        FileUtil.renameDirectory(mContent,file.getAbsolutePath(),mDestDirectory);
                        break;
                    case MOVEFILE:
                        FileUtil.renameFile(mContent, file.getAbsolutePath(), mDestDirectory);
                        break;
                    case COPYFILE:
                        FileUtil.CopySdcardFile(mContent, file.getAbsolutePath(), mDestDirectory);
                        break;
                    case RENAMEDIRECTORY:
                        FileUtil.renameDirectory(mContent,file.getAbsolutePath(),mDestDirectory);
                        break;
                    case RENAMEFILE:
                        FileUtil.renameFile(mContent, file.getAbsolutePath(), mDestDirectory);
                        break;
                    default:

                // Escape early if cancel() is called
                if (isCancelled())
                {
                    break;
                }
            }
            return file.getName();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //刷新Progress
            //setProgressPercent(progress[0]);
            Progress.setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(String name) {
            super.onPostExecute(name);
            //关闭progress
            Progress.dismiss();
            //清除选择器
            mUserData.StateSet.remove(name);
            //刷新ListView
            refresh();
        }

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
                        mFileList.add(file);
                    }
                }
            }
            else {//文件
                mFileList.add(Files);
            }
        }
    }
    private class ViewHolder {
        ToggleButton FileSelect;
        ImageView FileTypeIcon;
        TextView FileName;
        Button FileRename;
        Button FileCopy;
        Button FileMove;
        Button FileDelete;
        TextView FileDate;
        TextView FileType;
        TextView FileSize;
    }
    public FileAdapter(Context Content, String Path, UserViewModel Data)
    {
        mContent = Content;
        mUserData = Data;
        mUserData.DirectoryList.add(Path);
        getFileInfo(mUserData.DirectoryList.get(mUserData.DirectoryList.size()-1));
    }
    private class SelectClick implements View.OnClickListener{
        private  ViewHolder mHodler;
        private View mConvertView;
        private Integer mPos = 0;
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            if(mHodler == null)
            {
                return ;
            }
            switch (v.getId())
            {
                case R.id.FileSelect:
                    if(((ToggleButton)v).isChecked()) {
                        mHodler.FileRename.setVisibility(View.VISIBLE);
                        mHodler.FileDelete.setVisibility(View.VISIBLE);
                        mHodler.FileMove.setVisibility(View.VISIBLE);
                        mHodler.FileCopy.setVisibility(View.VISIBLE);
                        mUserData.StateSet.add(mFileList.get(mPos).getName());
                        mConvertView.setBackgroundColor(mContent.getResources().getColor(R.color.FileItem));
                    }
                    else
                    {
                        mHodler.FileRename.setVisibility(View.INVISIBLE);
                        mHodler.FileDelete.setVisibility(View.INVISIBLE);
                        mHodler.FileMove.setVisibility(View.INVISIBLE);
                        mHodler.FileCopy.setVisibility(View.INVISIBLE);
                        mUserData.StateSet.remove(mFileList.get(mPos).getName());
                        mConvertView.setBackgroundColor(Color.WHITE);
                    }
                    break;
                case R.id.FileDelete: {
                    AlertDialog.Builder bulider = new AlertDialog.Builder(mContent);
                    //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                    bulider.setTitle("删除");
                    bulider.setMessage("你确定要删除吗?");
                    bulider.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            //删除文件或目录
                            FilesTask DeleteTask = new FilesTask(FileCtrlType.DELETE);
                            DeleteTask.execute(mFileList.get(mPos));
                        }
                    });
                    bulider.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    });
                    bulider.create().show();
                }
                    break;
                case R.id.FileRename: {
                    final AlertDialog.Builder bulider = new AlertDialog.Builder(mContent);
                    //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                    bulider.setTitle("重命名");
                    bulider.setMessage("请输入新文件名?");
                    final View InputView = LayoutInflater.from(mContent).inflate(R.layout.filerenamedialog,null);
                    bulider.setView(InputView);
                    final EditText NameInput = (EditText) InputView.findViewById(R.id.RenameInput);
                    final TextWatcher EditWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s){
                            if(NameInput.getText().length() <= 0 || NameInput.getText().length() >= 255)
                            {
                                NameInput.setError(mContent.getString(R.string.FileRenameLenError));
                                InputView.findViewById(R.id.Confirm).setEnabled(false);
                            }
                            else if(NameInput.getText().toString().contains("/"))
                            {
                                NameInput.setError(mContent.getString(R.string.FileRenameFormatError));
                                InputView.findViewById(R.id.Confirm).setEnabled(false);
                            }
                            else
                            {
                                InputView.findViewById(R.id.Confirm).setEnabled(true);
                            }

                        }
                    };
                    NameInput.addTextChangedListener(EditWatcher);
                    final AlertDialog Dialog = bulider.show();
                    //bulider.create().show();
                    //确认
                    InputView.findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //执行Rename
                            mDestDirectory = mFileList.get(mPos).getAbsolutePath().replace(mFileList.get(mPos).getName(),NameInput.getText().toString().trim());
                            if(mFileList.get(mPos).isDirectory())
                            {
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.MOVEDIRECTORY);
                                DeleteTask.execute(mFileList.get(mPos));
                            }
                            else if(mFileList.get(mPos).isFile())
                            {
                                FilesTask RenameTask = new FilesTask(FileCtrlType.MOVEFILE);
                                RenameTask.execute(mFileList.get(mPos));
                            }
                            mUserData.StateSet.remove(NameInput.getText().toString().trim());
                            refresh();
                            //关闭对话框
                            Dialog.dismiss();
                        }
                    });
                    //取消按钮
                    InputView.findViewById(R.id.Cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //关闭对话框
                            Dialog.dismiss();
                        }
                    });

                }
                    break;
                case R.id.FileMove:
                {
                    final AlertDialog.Builder bulider = new AlertDialog.Builder(mContent);
                    //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                    bulider.setTitle("请选择目标目录");
                    final View InputView = LayoutInflater.from(mContent).inflate(R.layout.directoryselectdialog,null);
                    bulider.setView(InputView);

                    final AlertDialog Dialog = bulider.show();
                    //bulider.create().show();
                    WindowManager m = (WindowManager)mContent.getSystemService(Context.WINDOW_SERVICE);
                    Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                    android.view.WindowManager.LayoutParams p = Dialog.getWindow().getAttributes();  //获取对话框当前的参数值
                    Point size = new Point();
                    d.getSize(size);
                    p.height = (int) (size.x * 0.5);   //高度设置为屏幕的0.5
                    p.width = (int) (size.y * 0.5);    //宽度设置为屏幕的0.5
                    Dialog.getWindow().setAttributes(p);
                    final DirAdapter DirAdapter = new DirAdapter(mContent, Environment.getRootDirectory().getAbsolutePath(), mUserData);
                    ListView PathList = (ListView)InputView.findViewById(R.id.PathList);
                    PathList.setAdapter(DirAdapter);
                    PathList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            DirAdapter.setCurrentItem(i);
                            DirAdapter.refresh();
                        }
                    });
                    final TextView path = (TextView)(InputView.findViewById(R.id.Path));
                    path.setText(Environment.getRootDirectory().getAbsolutePath());
                    PathSelectClick PathSelect = new PathSelectClick(DirAdapter,path);
                    InputView.findViewById(R.id.URoot).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.FRoot).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.Return).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.EnterDirectory).setOnClickListener(PathSelect);
                    //确认
                    InputView.findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //执行Move
                            final String NewName = path.getText().toString().trim();
                            if(mFileList.get(mPos).isDirectory())
                            {
                                mDestDirectory = NewName+File.separator+mFileList.get(mPos).getName();
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.MOVEDIRECTORY);
                                DeleteTask.execute(mFileList.get(mPos));

                            }
                            else if(mFileList.get(mPos).isFile())
                            {
                                mDestDirectory = NewName+File.separator+mFileList.get(mPos).getName();
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.MOVEFILE);
                                DeleteTask.execute(mFileList.get(mPos));
                            }
                            //关闭对话框
                              Dialog.dismiss();
                        }
                    });
                    //取消按钮
                    InputView.findViewById(R.id.Cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //关闭对话框
                            Dialog.dismiss();
                        }
                    });

                }
                    break;
                case R.id.FileCopy:
                {
                    final AlertDialog.Builder bulider = new AlertDialog.Builder(mContent);
                    //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                    bulider.setTitle("拷贝");
                    final View InputView = LayoutInflater.from(mContent).inflate(R.layout.directoryselectdialog,null);
                    bulider.setView(InputView);

                    final AlertDialog Dialog = bulider.show();
                    //bulider.create().show();
                    WindowManager m = (WindowManager)mContent.getSystemService(Context.WINDOW_SERVICE);
                    Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                    android.view.WindowManager.LayoutParams p = Dialog.getWindow().getAttributes();  //获取对话框当前的参数值
                    Point size = new Point();
                    d.getSize(size);
                    p.height = (int) (size.x * 0.5);   //高度设置为屏幕的0.5
                    p.width = (int) (size.y * 0.5);    //宽度设置为屏幕的0.5
                    Dialog.getWindow().setAttributes(p);
                    final DirAdapter DirAdapter = new DirAdapter(mContent, Environment.getRootDirectory().getAbsolutePath(), mUserData);
                    ListView PathList = (ListView)InputView.findViewById(R.id.PathList);
                    PathList.setAdapter(DirAdapter);
                    PathList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            DirAdapter.setCurrentItem(i);
                            DirAdapter.refresh();
                        }
                    });
                    final TextView path = (TextView)(InputView.findViewById(R.id.Path));
                    path.setText(Environment.getRootDirectory().getAbsolutePath());
                    PathSelectClick PathSelect = new PathSelectClick(DirAdapter,path);
                    InputView.findViewById(R.id.URoot).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.FRoot).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.Return).setOnClickListener(PathSelect);
                    InputView.findViewById(R.id.EnterDirectory).setOnClickListener(PathSelect);
                    //bulider.create().show();
                    //确认
                    InputView.findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //执行Move
                            final String NewName = path.getText().toString().trim();
                            if(mFileList.get(mPos).isDirectory())
                            {
                                mDestDirectory = NewName+File.separator+mFileList.get(mPos).getName();
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.COPYDIRECTORY);
                                DeleteTask.execute(mFileList.get(mPos));
                            }
                            else if(mFileList.get(mPos).isFile())
                            {
                                mDestDirectory = NewName+File.separator+mFileList.get(mPos).getName();
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.COPYFILE);
                                DeleteTask.execute(mFileList.get(mPos));
                            }

                            //mUserData.StateSet.remove(mFileList.get(mPos).getName());
                            //refresh();
                            //关闭对话框
                            Dialog.dismiss();
                        }
                    });
                    //取消按钮
                    InputView.findViewById(R.id.Cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //关闭对话框
                            Dialog.dismiss();
                        }
                    });

                }
                    break;
                    default:
            }

        }
        public void setHolder(ViewHolder Holder)
        {
            mHodler = Holder;
        }
        public void setView(View convertView)
        {
            mConvertView = convertView;
        }
        public  void setPos(Integer pos)
        {
            mPos = pos;
        }
    }

    @Override
    public int getCount()//获取当前条目数
    {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) //根据位置获取对象的数据
    {
        return mFileList.get(position);//返回文件名
    }

    @Override
	public long getItemId(int position)//根据位置获取对象Id
    {
        return position;
    }

    //在这里面创建Item的内容，并且根据Position来设置数据
    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        //就是一个持有者的类，他里面一般没有方法，只有属性，作用就是一个临时的储存器，把你getView方法中每次返回的View存起来，可以下次再用。
        //这样做的好处就是不必每次都到布局文件中去拿到你的View，提高了效率。
        ViewHolder Holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContent).inflate(R.layout.filedetails,parent,false);//动态加载控件，通过布局文件item_list_animal
            Holder = new ViewHolder();
            Holder.FileSelect = (ToggleButton) convertView.findViewById(R.id.FileSelect);
            Holder.FileTypeIcon = (ImageView) convertView.findViewById(R.id.FileTypeIcon);
            Holder.FileName = (TextView) convertView.findViewById(R.id.FileName);
            Holder.FileDelete = (Button) convertView.findViewById(R.id.FileDelete);
            Holder.FileMove = (Button) convertView.findViewById(R.id.FileMove);
            Holder.FileCopy = (Button) convertView.findViewById(R.id.FileCopy);
            Holder.FileRename = (Button) convertView.findViewById(R.id.FileRename);

            Holder.FileDate = (TextView) convertView.findViewById(R.id.FileDate);

            Holder.FileType = (TextView) convertView.findViewById(R.id.FileType);

            Holder.FileSize = (TextView) convertView.findViewById(R.id.FileSize);
            convertView.setTag(Holder);   //将Holder存储到convertView中
        }else{
            Holder = (ViewHolder) convertView.getTag();
        }
        SelectClick mSelectButton = new SelectClick();
        mSelectButton.setHolder(Holder);
        mSelectButton.setView(convertView);
        mSelectButton.setPos(position);
        Holder.FileSelect.setOnClickListener(mSelectButton);
        if(mUserData.StateSet.contains(mFileList.get(position).getName()))
        {
            Holder.FileSelect.setChecked(true);
            Holder.FileRename.setVisibility(View.VISIBLE);
            Holder.FileDelete.setVisibility(View.VISIBLE);
            Holder.FileMove.setVisibility(View.VISIBLE);
            Holder.FileCopy.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(mContent.getResources().getColor(R.color.FileItem));
        }
        else
        {
            Holder.FileSelect.setChecked(false);
            Holder.FileRename.setVisibility(View.INVISIBLE);
            Holder.FileDelete.setVisibility(View.INVISIBLE);
            Holder.FileMove.setVisibility(View.INVISIBLE);
            Holder.FileCopy.setVisibility(View.INVISIBLE);
            convertView.setBackgroundColor(Color.WHITE);
        }
        //设置操作按钮事件监听
        Holder.FileRename.setOnClickListener(mSelectButton);
        //移动按钮事件监听
        Holder.FileMove.setOnClickListener(mSelectButton);
        //删除
        Holder.FileDelete.setOnClickListener(mSelectButton);
        //拷贝
        Holder.FileCopy.setOnClickListener(mSelectButton);
        //类型图标、类型
        File file = mFileList.get(position);
        if(file.isDirectory()) {
            Holder.FileTypeIcon.setImageResource(R.mipmap.file);
            Holder.FileType.setText("文件夹");
        }
        else
        {
            Holder.FileTypeIcon.setImageResource(R.mipmap.image);
            Holder.FileType.setText("文件");
        }
        //文件名字
        Holder.FileName.setText(file.getName());
        //日期
        long time=file.lastModified();
        SimpleDateFormat dateformatter = new
                SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String FileDate=dateformatter.format(time);
        Holder.FileDate.setText(FileDate);
        //开启线程递归计算文件大小
        if(mFileList.get(position).isDirectory()) {
            Holder.FileSize.setText("...");
            SizeAsyncTask SizeTask = new SizeAsyncTask(Holder.FileSize, position);
            //相当于线程的start
            SizeTask.execute(mFileList.get(position));
        }
        else
        {
            Holder.FileSize.setText(FileUtil.getFormatSize(mFileList.get(position).length()));
        }
        return convertView;
    }
    public void refresh()
    {
        //先清除原始数据
        mFileList.clear();
        getFileInfo(mUserData.DirectoryList.get(mUserData.DirectoryList.size()-1));
        notifyDataSetChanged();
    }
    public Set<String> getALLItem()
    {
        Set<String> Ret = new TreeSet<>();
        File[] Files =  mFileList.toArray(new File[0]);
        for(File file:Files)
        {
            Ret.add(file.getName());
        }
        return Ret;
    }
}

