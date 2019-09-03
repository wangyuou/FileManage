package com.example.filemanage;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;


public class MainActivity<onRequestPermissionsResult> extends AppCompatActivity implements PermissionCallbacks{
    private List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();
    private DeviceAdapter DeviceAdapter = null;
    private UserViewModel userData  = null;
    private FileAdapter mFileAdapter = null;
    private ListView FileList = null;
    private PopupWindow Menu = null;
    private boolean IsFirst = false;
    private static int test = 0;
    //权限参数
    String[] params = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    /**
     * 随便赋值的一个唯一标识码
     */
    public static final int WRITE_EXTERNAL_STORAGE = 100;

    private enum FileCtrlType {MOVE,COPY,DELETE};
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
                    userData.DirDirectoryList.clear();
                    userData.DirDirectoryList.add(Environment.getRootDirectory().toString());
                    mPath.setText(Environment.getRootDirectory().toString());
                    mDirAdapter.refresh();
                    break;
                case R.id.FRoot://返回fdisk根目录
                    userData.DirDirectoryList.clear();
                    userData.DirDirectoryList.add(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk");
                    mPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk");
                    mDirAdapter.refresh();
                    break;
                case R.id.Return://返回上一级目录
                    if(userData.DirDirectoryList.size() < 2)
                    {
                        Toast.makeText(MainActivity.this, R.string.RootDirectory, Toast.LENGTH_LONG).show();
                        return ;
                    }
                    userData.DirDirectoryList.remove(userData.DirDirectoryList.size()-1);
                    mPath.setText(userData.DirDirectoryList.get(userData.DirDirectoryList.size()-1));
                    mDirAdapter.refresh();
                    break;
                case R.id.EnterDirectory://进入目录
                    if(userData.DirCurrentItem >= mDirAdapter.mList.size())
                    {
                        return ;
                    }
                    if(mDirAdapter.mList.get(userData.DirCurrentItem).isDirectory())
                    {
                        String path = mDirAdapter.mList.get(userData.DirCurrentItem).getAbsolutePath();
                        userData.DirDirectoryList.add(path);
                        mDirAdapter.setCurrentItem(0);
                        mPath.setText(path);
                        mDirAdapter.refresh();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, R.string.NoEnterDocument, Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
            }
        }
    };
    private class FilesTask extends AsyncTask<List<File>, String, Void> {

        private String mTitle[] = {getString(R.string.Move),getString(R.string.Copy),getString(R.string.Delete)};
        private FileCtrlType mType;
        private String DestDirectory = null;
        private final ProgressDialog Progress = new ProgressDialog(MainActivity.this);

        public void setDestDirectory(String destDirectory) {
            DestDirectory = destDirectory;
        }

        public FilesTask(FileCtrlType type)
        {
            mType = type;
            //设置标题
            Progress.setTitle(mTitle[mType.ordinal()]);
            //设置提示信息
            //设置ProgressDialog 是否可以按返回键取消；
            Progress.setCancelable(true);
            Progress.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
            Progress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Cancel), new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface d, int i) {
                    onCancelled();
                }
            });

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //显示Progress
            Progress.show();
        }

        @Override
        protected Void doInBackground(List<File>... lists) {

            //获取删除文件名
            List<File> list = (List<File>)lists[0];
            int count = list.size();
            for (int i = 0; i < count; i++) {
                publishProgress("正在"+mTitle[mType.ordinal()]+":"+list.get(i).getAbsolutePath());
                switch (mType)
                {
                    case DELETE:
                        FileUtil.delete(MainActivity.this, list.get(i).getAbsolutePath());
                        break;
                    case COPY:
                        if(list.get(i).isDirectory())
                        {
                            FileUtil.copy(MainActivity.this, list.get(i).getAbsolutePath(), DestDirectory +File.separator+list.get(i).getName());
                        }
                        else {
                            FileUtil.CopySdcardFile(MainActivity.this, list.get(i).getAbsolutePath(), DestDirectory +File.separator+list.get(i).getName());
                        }
                        break;
                    case MOVE:
                        if(list.get(i).isDirectory()) {
                            FileUtil.renameDirectory(MainActivity.this, list.get(i).getAbsolutePath(), DestDirectory +File.separator+list.get(i).getName());
                        }
                        else
                        {
                            FileUtil.renameFile(MainActivity.this, list.get(i).getAbsolutePath(), DestDirectory +File.separator+list.get(i).getName());
                        }
                        break;
                        default:
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //刷新Progress
            //setProgressPercent(progress[0]);
            Progress.setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //关闭progress
            Progress.dismiss();
            //清除选择器
            userData.StateSet.clear();
            //刷新ListView
            mFileAdapter.refresh();

        }

    }
    private View.OnClickListener MoreMenuOnclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(userData.StateSet.size() == 0)
            {
                Toast.makeText(MainActivity.this, R.string.ChooseFileOrDirectory, Toast.LENGTH_LONG).show();
                Menu.dismiss();
                return ;
            }
            switch (view.getId())
            {
                case R.id.Delete://删除选择的文件
                    {
                        AlertDialog.Builder bulider = new AlertDialog.Builder(MainActivity.this);

                        //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                        bulider.setTitle(R.string.Delete);
                        bulider.setMessage(R.string.RequestDelete);
                        bulider.setPositiveButton(R.string.Confirm, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                                //删除文件或目录
                                FilesTask DeleteTask = new FilesTask(FileCtrlType.DELETE);
                                if(DeleteTask != null) {
                                    //获取选中的目录或文件
                                    List<File> list = new ArrayList<>();
                                    String[] Name = userData.StateSet.toArray(new String[0]);
                                    for (int i = 0; i < Name.length; i++) {
                                        list.add(new File(userData.DirectoryList.get(userData.DirectoryList.size() - 1) + File.separator + Name[i]));
                                    }
                                    DeleteTask.execute(list);
                                }
                            }
                        });
                        bulider.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        });
                        bulider.create().show();

                    }

                     break;
                case R.id.Copy://拷贝选择的文件
                    {
                        final AlertDialog.Builder bulider = new AlertDialog.Builder(MainActivity.this);
                        //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                        bulider.setTitle(R.string.CopyToDirectory);
                        final View InputView = LayoutInflater.from(MainActivity.this).inflate(R.layout.directoryselectdialog,null);
                        bulider.setView(InputView);

                        final AlertDialog Dialog = bulider.show();
                        //bulider.create().show();
                        WindowManager m = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                        android.view.WindowManager.LayoutParams p = Dialog.getWindow().getAttributes();  //获取对话框当前的参数值
                        Point size = new Point();
                        d.getSize(size);
                        p.height = (int) (size.x * 0.5);   //高度设置为屏幕的0.5
                        p.width = (int) (size.y * 0.5);    //宽度设置为屏幕的0.5
                        Dialog.getWindow().setAttributes(p);
                        final DirAdapter DirAdapter = new DirAdapter(MainActivity.this, Environment.getRootDirectory().getAbsolutePath(), userData);
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
                        userData.DirDirectoryList.clear();
                        userData.DirDirectoryList.add(Environment.getRootDirectory().getAbsolutePath());
                        PathSelectClick PathSelect = new PathSelectClick(DirAdapter,path);
                        InputView.findViewById(R.id.URoot).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.FRoot).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.Return).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.EnterDirectory).setOnClickListener(PathSelect);
                        //确认
                        InputView.findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(userData.DirectoryList.get(userData.DirectoryList.size() - 1).equals(path.getText().toString()))
                                {
                                    Toast.makeText(MainActivity.this, R.string.SameDirectory, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    FilesTask CopyTask = new FilesTask(FileCtrlType.COPY);
                                    if (CopyTask != null) {
                                        CopyTask.setDestDirectory(path.getText().toString());
                                        //获取选中的目录或文件
                                        List<File> list = new ArrayList<>();
                                        String[] Name = userData.StateSet.toArray(new String[0]);
                                        for (int i = 0; i < Name.length; i++) {
                                            list.add(new File(userData.DirectoryList.get(userData.DirectoryList.size() - 1) + File.separator + Name[i]));
                                        }
                                        CopyTask.execute(list);
                                    }
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
                case R.id.Move://移动选择的文件
                    {
                        final AlertDialog.Builder bulider = new AlertDialog.Builder(MainActivity.this);
                        //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                        bulider.setTitle(R.string.MoveToDirectory);
                        final View InputView = LayoutInflater.from(MainActivity.this).inflate(R.layout.directoryselectdialog,null);
                        bulider.setView(InputView);

                        final AlertDialog Dialog = bulider.show();
                        //bulider.create().show();
                        WindowManager m = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                        android.view.WindowManager.LayoutParams p = Dialog.getWindow().getAttributes();  //获取对话框当前的参数值
                        Point size = new Point();
                        d.getSize(size);
                        p.height = (int) (size.x * 0.5);   //高度设置为屏幕的0.5
                        p.width = (int) (size.y * 0.5);    //宽度设置为屏幕的0.5
                        Dialog.getWindow().setAttributes(p);
                        final DirAdapter DirAdapter = new DirAdapter(MainActivity.this, Environment.getRootDirectory().getAbsolutePath(), userData);
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
                        userData.DirDirectoryList.clear();
                        userData.DirDirectoryList.add(Environment.getRootDirectory().getAbsolutePath());
                        PathSelectClick PathSelect = new PathSelectClick(DirAdapter,path);
                        InputView.findViewById(R.id.URoot).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.FRoot).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.Return).setOnClickListener(PathSelect);
                        InputView.findViewById(R.id.EnterDirectory).setOnClickListener(PathSelect);
                        //确认
                        InputView.findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(userData.DirectoryList.get(userData.DirectoryList.size() - 1).equals(path.getText().toString()))
                                {
                                    Toast.makeText(MainActivity.this, R.string.SameDirectory, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    FilesTask CopyTask = new FilesTask(FileCtrlType.MOVE);
                                    if (CopyTask != null) {
                                        CopyTask.setDestDirectory(path.getText().toString());
                                        //获取选中的目录或文件
                                        List<File> list = new ArrayList<>();
                                        String[] Name = userData.StateSet.toArray(new String[0]);
                                        for (int i = 0; i < Name.length; i++) {
                                            list.add(new File(userData.DirectoryList.get(userData.DirectoryList.size() - 1) + File.separator + Name[i]));
                                        }
                                        CopyTask.execute(list);
                                    }
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
                     default:

            }
            //关闭Menu
            Menu.dismiss();
        }
    };
    private View.OnClickListener ButOnclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.Exit://退出应用
                    finish();
                    break;
                case R.id.AllButton://名称
                    userData.AllSelect = !userData.AllSelect;
                    if(userData.AllSelect)
                    {
                        //选中全部item
                        userData.StateSet.clear();
                        userData.StateSet.addAll(mFileAdapter.getALLItem());
                        mFileAdapter.refresh();
                    }
                    else
                    {
                        userData.StateSet.clear();
                        mFileAdapter.refresh();
                    }
                    View ListHead = (View) findViewById(R.id.ListHead);
                    ToggleButton All = (ToggleButton)ListHead.findViewById(R.id.AllButton);
                    All.setChecked(userData.AllSelect);
                    break;
                case R.id.New://新建文件夹
                    final AlertDialog.Builder bulider = new AlertDialog.Builder(MainActivity.this);
                    //bulider.setIcon(R.drawable.ic_launcher);//在title的左边显示一个图片
                    bulider.setTitle(R.string.Hint);
                    bulider.setMessage(R.string.EntitleFile);
                    final View InputView = LayoutInflater.from(MainActivity.this).inflate(R.layout.filerenamedialog,null);
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
                                NameInput.setError(getString(R.string.DirectoryNameLenError));
                                InputView.findViewById(R.id.Confirm).setEnabled(false);
                            }
                            else if(NameInput.getText().toString().contains("/"))
                            {
                                NameInput.setError(getString(R.string.DirectoryNameFormatError));
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
                            File file = new File(userData.DirectoryList.get(userData.DirectoryList.size()-1)+File.separator+NameInput.getText());
                            if(!file.mkdirs())
                            {
                                Toast.makeText(MainActivity.this,R.string.CreatFileFail,Toast.LENGTH_LONG).show();
                            }
                            mFileAdapter.refresh();
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
                    break;
                case R.id.Return:
                        if(userData.DirectoryList.size() == 1)
                        {
                            Toast.makeText(MainActivity.this,R.string.RootDirectory,Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            userData.DirectoryList.remove(userData.DirectoryList.size()-1);
                            userData.StateSet.clear();
                            mFileAdapter.refresh();
                            TextView text = (TextView) findViewById(R.id.Path);
                            text.setText(userData.DirectoryList.get(userData.DirectoryList.size()-1));
                        }
                    break;
                case R.id.Open://进入文件夹
                        //获取当前选择的目录
                        if(userData.StateSet.size() == 0)
                        {
                            Toast.makeText(MainActivity.this,R.string.ChooseFileOrDirectory,Toast.LENGTH_LONG).show();
                        }
                        else if(userData.StateSet.size() > 1)
                        {
                            Toast.makeText(MainActivity.this,R.string.OnlyOneDirectory,Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            String[] array = userData.StateSet.toArray(new String[0]);
                            if(new File(userData.DirectoryList.get(userData.DirectoryList.size()-1)+File.separator+array[0]).isDirectory()) {
                                //添加当前路径到目录链表
                                userData.DirectoryList.add(userData.DirectoryList.get(userData.DirectoryList.size() - 1) + File.separator + array[0]);
                                mFileAdapter.refresh();
                                TextView text = (TextView) findViewById(R.id.Path);
                                text.setText(userData.DirectoryList.get(userData.DirectoryList.size() - 1));
                                //进入前清除状态
                                userData.StateSet.clear();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this,R.string.DontIsDirectory,Toast.LENGTH_LONG).show();
                            }
                        }
                    break;
                    case R.id.More:
                        //弹出窗口
                        Menu = new PopupWindow(MainActivity.this);
                        Menu.setBackgroundDrawable(getDrawable(R.drawable.morestyle));
                        View menu = getLayoutInflater().inflate(R.layout.moremenu,null);
                        Menu.setContentView(menu);
                        Menu.setFocusable(true);
                        //todo
                        WindowManager m = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                        Point size = new Point();
                        d.getSize(size);
                        Menu.setHeight((int) (size.x * 0.15));
                        Menu.setWidth((int) (size.y * 0.15));
                        Menu.showAsDropDown(MainActivity.this.findViewById(R.id.More),-30,10);
                        menu.findViewById(R.id.Delete).setOnClickListener(MoreMenuOnclick);
                        menu.findViewById(R.id.Copy).setOnClickListener(MoreMenuOnclick);
                        menu.findViewById(R.id.Move).setOnClickListener(MoreMenuOnclick);
                        break;
                    default:
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.println(1000, "123", "onResume");
        //Toast.makeText(this, test++, Toast.LENGTH_SHORT).show();
        //必选要加判断，不然每次从权限设置界面返回都要调用
        if(IsFirst)
        {
            IsFirst = false;
            if (!EasyPermissions.hasPermissions(this, params)) {
                EasyPermissions.requestPermissions(this, getString(R.string.AccessExternalStorage), WRITE_EXTERNAL_STORAGE, params);
            }
        }
    }

    //当在设置界面返回时
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(resultCode);
        System.out.println(data);
        switch (requestCode) {
            //当从软件设置界面，返回当前程序时候
            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                //刷新界面
                mFileAdapter.refresh();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //请求权限成功
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //如果没有添加注解，在这里调用方法,刷新界面
        mFileAdapter.refresh();
    }

    //请求权限拒绝
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        System.out.println(perms);
        //当勾选不弹出对话框和拒绝权限时
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        else if (!EasyPermissions.hasPermissions(this, params)) {
            //这里响应的是除了AppSettingsDialog这个弹出框，剩下的两个弹出框被拒绝或者取消的效果
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉顶部标题
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        userData = ViewModelProviders.of(this).get(UserViewModel.class);
        String Path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk";
        File file = new File(Path);
        if(!file.isDirectory())
        {
            file.mkdirs();
        }
        mFileAdapter = new FileAdapter(this,Path,userData);
        //显示当前路径
        final TextView Text = (TextView)(findViewById(R.id.Path));
        Text.setText(Path);

        findViewById(R.id.Exit).setOnClickListener(ButOnclick);
        FileList = (ListView) findViewById(R.id.FileList);

        //getLayoutInflater().inflate(R.layout.listheader,l);
        //View v = (View) getLayoutInflater().inflate(R.layout.listheader,null);
        final View v = (View) LayoutInflater.from(this).inflate(R.layout.listheader,null);
        v.findViewById(R.id.AllButton).setOnClickListener(ButOnclick);
        FileList.addHeaderView(v);
        FileList.setDividerHeight(2);
        FileList.setAdapter(mFileAdapter);
        FileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(userData.StateSet.contains(mFileAdapter.mFileList.get(i-1).getName()))
                {
                    userData.StateSet.remove(mFileAdapter.mFileList.get(i - 1).getName());
                }
                else
                {
                    userData.StateSet.add(mFileAdapter.mFileList.get(i - 1).getName());
                }
                mFileAdapter.refresh();
            }
        });
        FileList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                View ListHead = (View) findViewById(R.id.ListHead);
                ToggleButton All = (ToggleButton)ListHead.findViewById(R.id.AllButton);
                //ListHead.setMinimumHeight(mFileAdapter.getItemHeight());
                ListHead.setVisibility(i>=1?View.VISIBLE:View.GONE);
                All.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userData.AllSelect = !userData.AllSelect;

                        if(userData.AllSelect)
                        {
                            //选中全部item
                            userData.StateSet.clear();
                            userData.StateSet.addAll(mFileAdapter.getALLItem());
                            mFileAdapter.refresh();
                        }
                        else
                        {
                            userData.StateSet.clear();
                            mFileAdapter.refresh();
                        }
                        ToggleButton Toggle= (ToggleButton)v.findViewById(R.id.AllButton);
                        Toggle.setChecked(userData.AllSelect);
                    }
                });
            }
        });
        //新建文件夹
        Button NewButton = (Button)findViewById(R.id.New);
        NewButton.setOnClickListener(ButOnclick);
        //返回上一级
        Button ReturnButton = (Button)findViewById(R.id.Return);
        ReturnButton.setOnClickListener(ButOnclick);
        //进入文件夹
        Button OpenButton = (Button)findViewById(R.id.Open);
        OpenButton.setOnClickListener(ButOnclick);
        //更多
        Button MoreButton = (Button)findViewById(R.id.More);
        MoreButton.setOnClickListener(ButOnclick);
        //设备列表
        Intent itent = getIntent();
        String ADDDeviceName = itent.getStringExtra("ADDDeviceName");
        String ReDeviceName = itent.getStringExtra("ReDeviceName");
        List<String> list = new ArrayList<>();
        if(ADDDeviceName != null)
        {
            list.add(ADDDeviceName);
        }
        if(ReDeviceName != null)
        {
            if(list.contains(ReDeviceName))
            {
                list.remove(ReDeviceName);
            }
        }
        list.add("fdisk");
        list.add("U盘");
        DeviceAdapter = new DeviceAdapter(this,list,userData);
        ListView listView = (ListView) findViewById(R.id.DeviceList);
        listView.setAdapter(DeviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != userData.CurrentItem)
                {
                    DeviceAdapter.setCurrentItem(i);
                    DeviceAdapter.refresh();
                    //改变ListView内容
                    userData.DirectoryList.clear();
                    if(i == 0)
                    {
                        userData.DirectoryList.add(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"fdisk");
                    }
                    else if(i == 1)
                    {
                        userData.DirectoryList.add(Environment.getRootDirectory().toString());
                    }
                    userData.StateSet.clear();
                    mFileAdapter.refresh();
                    //刷新当前路径
                    Text.setText(userData.DirectoryList.get(userData.DirectoryList.size()-1));
                }
            }
        });
        IsFirst = true;
    }
}
