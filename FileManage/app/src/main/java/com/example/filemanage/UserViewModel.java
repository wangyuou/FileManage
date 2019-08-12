package com.example.filemanage;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class UserViewModel extends ViewModel {
    //全选状态
    public Boolean AllSelect = false;
    //ListView的选择状态
    public Set<String> StateSet = new TreeSet<>();
    //记录目录，好返回上一级
    public List<String> DirectoryList = new ArrayList<String>();
    //device当前选中的Item
    public Integer CurrentItem = 0;

    //下面是目录选择器变量
    //Dir当前选中的Item
    public Integer DirCurrentItem = 0;
    //记录目录，好返回上一级
    public List<String> DirDirectoryList = new ArrayList<String>();

    public UserViewModel()
    {

    }
}