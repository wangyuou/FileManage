package com.example.filemanage;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

public class FileUtil {

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName
     *            要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(Context contend,String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(contend,fileName);
            else
                if(deleteDirectory(contend,fileName))
                {
                    return file.delete();
                }
                else
                {
                    return false;
                }
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(Context contend,String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                //Toast.makeText(contend,"删除文件:"+fileName+"失败!",Toast.LENGTH_LONG).show();
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            //Toast.makeText(contend,"删除文件:"+fileName+"不存在!",Toast.LENGTH_LONG).show();
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir
     *            要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(Context content,String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            //Toast.makeText(content,"删除目录不存在!",Toast.LENGTH_LONG).show();
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = FileUtil.deleteFile(content,files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = FileUtil.deleteDirectory(content,files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建文件
     *
     * @param filePath 文件地址
     * @param fileName 文件名
     * @return
     */
    public static boolean createFile(Context content, String filePath, String fileName) {

        String strFilePath = filePath + fileName;

        File file = new File(filePath);
        if (!file.exists()) {
            /**  注意这里是 mkdirs()方法  可以创建多个文件夹 */
            file.mkdirs();
        }

        File subfile = new File(strFilePath);

        if (!subfile.exists()) {
            try {
                boolean b = subfile.createNewFile();
                if(!b) {
                    System.out.println("文件已经存在!");
                    //Toast.makeText(content, "文件已经存在!", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static boolean renameFile(Context content,String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        System.out.println("old:"+oldPath);
        System.out.println("New:"+newPath);
        if(newFile.isFile())
        {
            System.out.println("文件已经存在!");
            //Toast.makeText(content,newFile.getName()+"已经存在!",Toast.LENGTH_LONG).show();
            return false;

        }
        //执行重命名
        if(oleFile.renameTo(newFile))
        {
            System.out.println("成功!");
            //Toast.makeText(content,oleFile.getName()+"命名为:"+newFile.getName()+"成功!",Toast.LENGTH_LONG).show();
            return true;
        }
        else
        {
            System.out.println("失败!");
            //Toast.makeText(content,oldPath+"命名为:"+newPath+"失败!",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * 复制文件
     *
     * @param fromFile 要复制的文件目录
     * @param toFile   要粘贴的文件目录
     * @return 是否复制成功
     */
    public static boolean copy(Context content,String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return false;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {

                copy(content,currentFiles[i].getPath() + "/", toFile +File.separator+currentFiles[i].getName());

            } else//如果当前项为文件则进行文件拷贝
            {
                CopySdcardFile(content,currentFiles[i].getPath(), toFile + File.separator+currentFiles[i].getName());
            }
        }
        return true;
    }


    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static boolean CopySdcardFile(Context content,String fromFile, String toFile) {

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;

        } catch (Exception ex) {
            System.out.println("拷贝失败!");
            //Toast.makeText(content,"拷贝失败!",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * 重命名文件夹
     *
     * @param oldPath 原来的文件夹地址
     * @param newPath 新的文件夹地址
     */
    public static boolean renameDirectory(Context content,String oldPath, String newPath) {

        if(oldPath == null || newPath == null)
        {
            System.out.println("参数不能为空!");
            return false;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        System.out.println("old:" + oldPath);
        System.out.println("New:" + newPath);
        File[] files = oldFile.listFiles();
        if(files.length == 0)//空文件夹
        {
            if(newFile.mkdirs())
            {
                //删除原有空文件夹
                if(!oldFile.delete())
                {
                    System.out.println("删除原文件夹失败!");
                    //Toast.makeText(content,"删除原文件夹失败!",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            else
            {
                System.out.println("创建文件夹失败!");
                return false;
                //Toast.makeText(content,"创建文件夹失败!",Toast.LENGTH_LONG).show();
                //return false;
            }
        }
        else {
            for (File file : files) {
                if (file.isFile()) {
                    //判断文件夹是否存在
                    if(!newFile.isDirectory()) {
                        //把文件移到新位置,首先创建文件夹
                        if (!newFile.mkdirs()) {
                            System.out.println("创建文件夹失败!");
                            //Toast.makeText(content, "创建文件夹失败!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    //移动文件
                    file.renameTo(new File(newFile+File.separator+file.getName()));
                }
                else
                {
                    renameDirectory(content,file.getAbsolutePath().toString(),newPath+File.separator+file.getName());
                    //删除当前旧文件夹
                    file.delete();
                }
            }
        }
        oldFile.delete();
        return true;
    }

    /**
     * 获取文件夹大小
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file){

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    size = size + getFolderSize(fileList[i]);

                }else{
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }

    /**
     * 格式化单位
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size/1024;
        if(kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte/1024;
        if(megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte/1024;
        if(gigaByte < 1) {
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte/1024;
        if(teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
}

