package com.example.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SecondActivity extends AppCompatActivity {
    //通过调用摄像头
    public static final int TAKE_PHOTO = 1;

    private ImageView picture;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        //获取到Button和ImageView的实例，并给Button注册上点击事件
        Button takePhoto=findViewById(R.id.take_photo);
        picture=findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File对象,用于存储拍照后的图片
                //图片命名为output_image.jpg
                //将它存放在手机SD卡的应用关联缓存目录,调用getExternalCacheDir()方法可以得到这个目录
                File outputImage=new File(getExternalCacheDir(),"output_image.jdp");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //判断设备的系统版本是否高于Android7.0
                if(Build.VERSION.SDK_INT>24){
                    //调用FileProvider的getUriForFile()方法将File对象转换成一个封装过的Uri对象
                    //因为从Android 7.0系统开始，直接使用本地真实路径的Uri被认为是不安全的，
                    // 会抛出一个FileUriExposedException异常。
                    // 而FileProvider则是一种特殊的内容提供器，
                    // 它使用了和内容提供器类似的机制来对数据进行保护，
                    // 可以选择性地将封装过的Uri共享给外部，从而提高了应用的安全性。
                    imageUri= FileProvider.getUriForFile(SecondActivity.this,"com.example.android.media.fileprovider",outputImage);
                }else{
                    //低于7.0版本调用Uri的fromFile()方法将File对象转换成Uri对象
                    //这个Uri对象标识着output_image.jpg这张图片的本地真实路径
                    imageUri=Uri.fromFile(outputImage);
                }

                //启动相机程序
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                //Intent的putExtra()方法指定图片的输出地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        Button chooseFromAlbum=findViewById(R.id.choose_from_album);
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SecondActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        // super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //调用BitmapFactory的decodeStream()方法将output_image.jpg这张照片解析成Bitmap对象，
                        // 然后把它设置到ImageView中显示出来。
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {

                        handleImageOnkitkat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    //通过访问图库
    public static final int CHOOSE_PHOTO = 2;

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private void handleImageOnkitkat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // 如果是content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // 如果是file类型的Uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            displayImage(imagePath);
        }
    }
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }
    //
    private String getImagePath(Uri uri, String selection) {
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}