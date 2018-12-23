package com.example.xukefeng.musicplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.RequiresApi;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * sd卡工具類
 * Created by liweidong on 2018/7/10.
 */

public class SdCardUtils {

    /**
     * 1、判断SDCard是否挂载
     */
    public static boolean isSDCardMounted(){
        //外部储存状态，只有值为MOUNTED的时候才会认为已经挂载
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;  //已经挂载返回true
        }
        return false;
    }

    /**
     * 2、获得SDCard的根目录/storage/sdcard
     */
    public static String getSDCardBaseDir(){
        //已经挂载则获取根目录路径，并返回
        if(isSDCardMounted()){
            File dir = Environment.getExternalStorageDirectory();
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 3、获得SDCard的全部空间大小(单位:M)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSDCardSize(){
        if( isSDCardMounted() ){
            String baseDir = getSDCardBaseDir();   //获取根目录
            StatFs statFs = new StatFs(baseDir);   //获取StatFs对象
            long blockCount = statFs.getBlockCountLong();  //通过StatFs对象获取块的数量
            long blockSize = statFs.getBlockSizeLong();    //通过StatFs对象获取每块的大小（字节）
            return ( blockCount * blockSize / 1024 / 1024 );  //块数量*每块大小/1024/1024  转化单位为M
        }
        return 0;
    }

    /**
     * 4、获取SDCard空闲空间的大小(单位:M)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSDCardFreeSize(){
        if( isSDCardMounted() ){
            String baseDir = getSDCardBaseDir();   //获取根目录
            StatFs statFs = new StatFs(baseDir);   //获取StatFs对象
            long freeBlock = statFs.getFreeBlocksLong();//通过StatFs对象获取空闲块的数量
            long blockSize = statFs.getBlockSizeLong();  //通过StatFs对象获取每块的大小（字节）
            return ( freeBlock * blockSize /1024 / 1024 );  //空闲块数量*每块大小/1024/1024  转化单位为M
        }
        return 0;
    }

    /**
     * 5、 获取SDCard可用空间的大小(单位:M)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSDCardAvailSize(){
        if( isSDCardMounted() ){
            String baseDir = getSDCardBaseDir();  //获取根目录
            StatFs statFs = new StatFs(baseDir);   //获取StatFs对象
            long availBlock = statFs.getAvailableBlocksLong();  //通过StatFs对象获取可用块的数量
            long blockSize = statFs.getBlockSizeLong();         //通过StatFs对象获取每块的大小（字节）
            return ( availBlock * blockSize / 1024 / 1024 );  //可用块数量*每块大小/1024/1024  转化单位为M
        }
        return 0;
    }

    /**
     * 6、往SDCard公有目录下保存文件 (九大公有目录中的一个，具体由type指定) /storage/sdcard/{type}/{filename}
     *     公有目录即 SDCard跟目录下的 系统创建的文件夹
     * @param data  要写入的数据
     * @param type  文件夹名
     * @param filename 文件名
     * @return boolean
     */
    public static boolean saveData2SDCardPublicDir(byte[] data, String type, String filename){
        if ( isSDCardMounted() ){
            String dir = getSDCardBaseDir() + File.separator + type;  //九大公有目录中的一个
            String file = dir + File.separator + filename;            //文件路径名
            BufferedOutputStream bos = null;                          //缓冲输出流
            try {
                bos = new BufferedOutputStream( new FileOutputStream( file ) );
                bos.write(data);  //写入数据
                bos.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {              //finally总是会执行，尽管上面已经return true;了
                if( bos != null ){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 7、往SDCard的自定义目录中保存数据 /storage/sdcard/{dir}
     */
    public static boolean saveData2SDCardCustomDir(byte[] data, String dir, String filename){
        if( isSDCardMounted() ){
            String saveDir = getSDCardBaseDir() + File.separator + dir;
            File saveFile = new File(saveDir);
            //如果不存在就创建该文件
            if( !saveFile.exists() ){
                saveFile.mkdirs();
            }
            String file = saveFile.getAbsolutePath() + File.separator + filename;
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data);
                bos.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {     //finally总是会执行，尽管上面已经return true;了
                if( bos != null ){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 8、往SDCard的私有File目录下保存文件 /storage/sdcard/Android/data/包名/files/{type}/{filename}
     */
    public static boolean saveData2SDCardPrivateFileDir(byte[] data, String type, String filename, Context context){
        if( isSDCardMounted() ) {
            File dir = context.getExternalFilesDir(type);  //获得私有File目录下
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String file = dir.getAbsolutePath() + File.separator + filename;
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data);
                bos.flush();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * 9、往SDCard的私有Cache目录下保存文件 /storage/sdcard/Android/data/包名/cache/{filename}
     */
    public static boolean saveData2SDCardPrivateCacheDir(byte[] data, String filename, Context context){
        if( isSDCardMounted() ) {
            File dir = context.getExternalCacheDir();  //获取私有Cache目录
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String file = dir.getAbsolutePath() + File.separator + filename;
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data);
                bos.flush();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     *  10、往SDCard的私有Cache目录下保存图像 /storage/sdcard/Android/data/包名/cache/{filename}
     * @param bitmap  图片资源的bitmap对象
     * @param filename 文件名
     * @param context 上下文
     * @return boolean
     */
    public static boolean saveBitmap2SDCardPrivateCacheDir(Bitmap bitmap, String filename, Context context){
        if( isSDCardMounted() ) {
            File dir = context.getExternalCacheDir();  //获取私有Cache目录
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String file = dir.getAbsolutePath() + File.separator + filename;
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                //判断为jpg还是png类型
                if (file.endsWith(".jpg") || file.endsWith(".JPG")) {
                    //图片压缩--参数（图片类型，图片质量0-100，输出流）
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                } else if (file.endsWith(".png") || file.endsWith(".PNG")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                }
                bos.flush();
                return true;  //写入成功返回true
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     *  11、从SDCard读取指定文件 /storage/sdcard/{filePath}
     * @param filePath 要读取的文件名
     * @return byte[]
     */
    public static byte[] loadFileFromSDCard(String filePath){
        if( isSDCardMounted() ) {
            File dir = new File(getSDCardBaseDir());
            if( !dir.exists() ){
                dir.mkdirs();
            }
            String file = dir.getAbsolutePath() + File.separator + filePath;
            BufferedInputStream bis = null;
            byte[] bytes = new byte[1024];
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int hasRead;
                while( true ){
                    hasRead = bis.read(bytes);
                    if( hasRead < 0 ){
                        break;
                    }
                    baos.write(bytes,0,hasRead);
                }
                baos.flush();
                return baos.toByteArray();  //返回byte的数组
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if( bis != null  ){
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 12、从SDCard读取Bitmap并返回 /storage/sdcard/{filePath}
     * @param filePath 要读取的图片名
     * @return Bitmap 返回图片资源
     */
    public static Bitmap loadBitmapFromSDCard(String filePath){
        if( isSDCardMounted() ){
            String file = getSDCardBaseDir() + File.separator + filePath;
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int hasRead;
                while(true){
                    hasRead = bis.read(bytes);
                    if( hasRead < 0 ){
                        break;
                    }
                    baos.write(bytes);
                }
                byte[] data = baos.toByteArray();
                return BitmapFactory.decodeByteArray(data,0,data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if( bis !=null){
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 13、获取SD卡公有目录路径   /storage/sdcard/{type}
     * @param type
     * @return
     */
    public static String getSDCardPublicDir(String type){
        if( isSDCardMounted() ){
            File dir = Environment.getExternalStoragePublicDirectory(type);
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 14、获取SDCard私有Cache目录路径  /storage/sdcard/Android/data/包名/cache/
     * @param context
     * @return
     */
    public static String getSDCardPrivateCacheDir(Context context){
        if( isSDCardMounted() ){
            File dir = context.getExternalCacheDir();
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 15、获取SDCard私有File目录路径 /storage/sdcard/Android/data/包名/files/{type}
     * @param context
     * @param type
     * @return
     */
    public static String getSDCardPrivateFilesDir(Context context, String type){
        if( isSDCardMounted() ){
            File dir = context.getExternalFilesDir(type);
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 16、判断一个文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExists(String filePath){
        if( isSDCardMounted()){
            File file = new File(filePath);
            return file.exists();
        }
        return false;
    }

    /**
     * 17、删除一个文件
     * @param filePath
     * @return
     */
    public static boolean removeFileFromSDCard(String filePath){
        if(isSDCardMounted()){
            String dir = getSDCardBaseDir() + File.separator + filePath;
            File file = new File(dir);
            if( file.exists() ){
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 创建目录
     * @param dicPath
     */
    public static void createFile(String dicPath){
        File file = new File(dicPath);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * 将bitmap对象保存到sd卡
     * @param filedic
     * @param fileName
     * @param bitmap
     */
    public static void saveBitmapFile(String filedic, String fileName, Bitmap bitmap){
        File file = new File(filedic, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveBitmapToSd(String filedic, String fileName, Bitmap bitmap){
        File file = new File(filedic, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
            if (bitmap != null){
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] readFile(File file) {
        // 需要读取的文件，参数是文件的路径名加文件名
        if (file.isFile()) {
            // 以字节流方法读取文件

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                // 设置一个，每次 装载信息的容器
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // 开始读取数据
                int len = 0;// 每次读取到的数据的长度
                while ((len = fis.read(buffer)) != -1) {// len值为-1时，表示没有数据了
                    // append方法往sb对象里面添加数据
                    outputStream.write(buffer, 0, len);
                }
                // 输出字符串
                return outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
        return null;
    }
}
