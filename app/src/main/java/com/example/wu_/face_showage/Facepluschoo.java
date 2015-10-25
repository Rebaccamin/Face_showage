package com.example.wu_.face_showage;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.apache.http.HttpRequest;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by wu-成敏 on 2015/10/25.  实现测试年龄的具体操作
 */
public class Facepluschoo {
    public interface Callback{
        void success(JSONObject result);
        void error(FaceppParseException exception);
    }
    public static void detect(final Bitmap bm, final Callback callback)
    {
       new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    HttpRequests request=new HttpRequests(Constant.key,Constant.password,true,true);
                    //将bitmap转化成二进制格式
                    Bitmap bmsmall=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight());
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    bmsmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bytes=stream.toByteArray();
                    PostParameters parameters=new PostParameters();
                    parameters.setImg(bytes);
                    JSONObject jsonObject=request.detectionDetect(parameters);
                    Log.e("TAG",jsonObject.toString());
                    if(callback!=null) callback.success(jsonObject);

                } catch (FaceppParseException e) {
                    e.printStackTrace();

                    if (callback!=null)
                        callback.error(e);
                }
            }
        }).start();
    }
}
