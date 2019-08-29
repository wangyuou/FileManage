package com.example.filemanage;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UserViewModel extends ViewModel {
    //全选状态
    public boolean AllSelect = false;
    //ListView的选择状态
    public Set<String> StateSet = new HashSet<>();
    //记录目录，好返回上一级
    public List<String> DirectoryList = new LinkedList<String>();
    //device当前选中的Item
    public int CurrentItem = 0;

    //下面是目录选择器变量
    //Dir当前选中的Item
    public int DirCurrentItem = 0;
    //记录目录，好返回上一级
    public List<String> DirDirectoryList = new LinkedList<String>();

    public UserViewModel()
    {

    }
}