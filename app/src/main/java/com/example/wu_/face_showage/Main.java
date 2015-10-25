package com.example.wu_.face_showage;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Handler;

public class Main extends AppCompatActivity implements View.OnClickListener {
  private ImageView mphoto;
    private Button mchoose;
    private Button mdetect;
    private TextView mtip;
    private View mwaiting;
    private String myphotonow;
    private static final int PICK_CODE=0X110;
    private Bitmap mphotoimage;
    private static final int MSG_SUCESS=0X111;
    private static final int MSG_ERROR=0X112;
    private Paint mpaint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initEvents();
        mpaint=new Paint();
    }

    private void initEvents() {
        mdetect.setOnClickListener(this);
        mchoose.setOnClickListener(this);
    }

    private void initViews() {
        mphoto= (ImageView) findViewById(R.id.photo);
        mchoose= (Button) findViewById(R.id.choose);
        mdetect= (Button) findViewById(R.id.ceshi);
        mtip= (TextView) findViewById(R.id.tip);
        mwaiting=findViewById(R.id.waiting);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==PICK_CODE){
           if(intent!=null){
               Uri uri=intent.getData();
               Cursor cursor=getContentResolver().query(uri,null,null,null,null);
               cursor.moveToFirst();
               int idx=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
               myphotonow=cursor.getString(idx);
               cursor.close();//以上拿到图片的路径。
               resizePhoto();//压缩图片
               mphoto.setImageBitmap(mphotoimage);
               mtip.setText("点击选择按钮->");
           }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void resizePhoto() {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(myphotonow,options);//只得到图片的尺寸大小。
        double ratio=Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024f);
//每张图片不能超过3M
        options.inSampleSize= (int) Math.ceil(ratio);
        options.inJustDecodeBounds=false;
        mphotoimage=BitmapFactory.decodeFile(myphotonow,options);
    }

    private android.os.Handler mHandler=new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SUCESS:
                    mwaiting.setVisibility(View.GONE);
                    JSONObject rs= (JSONObject) msg.obj;
                    preparedrebitmap(rs);
                   mphoto.setImageBitmap(mphotoimage);
                    break;
                case MSG_ERROR:
                    mwaiting.setVisibility(View.GONE);
                    String mes= (String) msg.obj;
                    if(TextUtils.isEmpty(mes)){
                        mtip.setText("Error!");
                    }
                    else{
                        mtip.setText(mes);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void preparedrebitmap(JSONObject rs) {
        Bitmap bitmap=Bitmap.createBitmap(mphotoimage.getWidth(),mphotoimage.getHeight(),mphotoimage.getConfig());
        Canvas canvas=new Canvas(bitmap);
        canvas.drawBitmap(mphotoimage,0,0,null);
        try {
            JSONArray faces=rs.getJSONArray("face");
            int faceCount=faces.length();
            mtip.setText("找到"+faceCount+"个人~");
            for(int i=0;i<faceCount;i++){
            JSONObject face=faces.getJSONObject(i);
            JSONObject posObj=face.getJSONObject("position");

             float x= (float) posObj.getJSONObject("center").getDouble("x");
             float y= (float) posObj.getJSONObject("center").getDouble("y");
             float w= (float) posObj.getDouble("width");
             float h= (float) posObj.getDouble("height");

             x=x/100*bitmap.getWidth();
             y=y/100*bitmap.getHeight();
             w=w/100*bitmap.getWidth();
             h=h/100*bitmap.getHeight();

             mpaint.setColor(Color.RED);
             mpaint.setStrokeWidth(3);
             canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mpaint);
             canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mpaint);
             canvas.drawLine(x+w/2,y-h/2,x+w/2,y+h/2,mpaint);
             canvas.drawLine(x-w/2,y+h/2,x+w/2,y+h/2,mpaint);

             int age=face.getJSONObject("attribute").getJSONObject("age").getInt("value");
             String gender=face.getJSONObject("attribute").getJSONObject("gender").getString("value");

             Bitmap agbm=buildageBitmap(age,"Male".equals(gender));
             int agebmwid=agbm.getWidth();
             int agebmwhei=agbm.getHeight();
             if(bitmap.getWidth()<mphoto.getWidth()&&bitmap.getHeight()<mphoto.getHeight()){
                 float ratio=Math.max(bitmap.getWidth()*1.0f/mphoto.getWidth(),bitmap.getHeight()*1.0f/mphoto.getHeight());
                 agbm=Bitmap.createScaledBitmap(agbm,(int)(agebmwid*ratio),(int)(agebmwhei*ratio),false);
             }
             canvas.drawBitmap(agbm,x-agbm.getWidth()/2,y-h/2-agbm.getHeight()/2,null);
             mphotoimage=bitmap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap buildageBitmap(int age,boolean ismale) {
        TextView tv= (TextView) mwaiting.findViewById(R.id.age);
        tv.setText(age + " ");
        if(ismale){
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male),null,null,null);
        }
        else {
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female),null,null,null);
        }
        tv.setDrawingCacheEnabled(true);
        Bitmap btm=Bitmap.createBitmap(tv.getDrawingCache());
        tv.destroyDrawingCache();
        return btm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.choose:
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_CODE);
                break;
            case R.id.ceshi:
                mwaiting.setVisibility(View.VISIBLE);
                if(myphotonow!=null&&!myphotonow.trim().equals("")){
                    resizePhoto();
                }
                else {
                    mphotoimage=BitmapFactory.decodeResource(getResources(),R.drawable.t4);
                }
                Facepluschoo.detect(mphotoimage, new Facepluschoo.Callback() {
                    @Override
                    public void success(JSONObject result) {
                        Message message=Message.obtain();
                        message.what=MSG_SUCESS;
                        message.obj=result;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message message=Message.obtain();
                        message.what=MSG_ERROR;
                        message.obj=exception.getErrorMessage();
                        mHandler.sendMessage(message);
                    }
                });
                break;

        }
    }
}
