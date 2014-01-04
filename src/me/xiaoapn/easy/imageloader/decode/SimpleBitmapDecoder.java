/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaoapn.easy.imageloader.decode;

import java.io.InputStream;

import me.xiaoapn.easy.imageloader.ImageLoader;
import me.xiaoapn.easy.imageloader.util.ImageSize;
import me.xiaoapn.easy.imageloader.util.IoUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * 位图解码器
 */
public class SimpleBitmapDecoder implements BitmapDecoder{
	private String logName;
	
	/**
	 * 创建位图解码器
	 */
	public SimpleBitmapDecoder(){
		this.logName = getClass().getSimpleName(); 
	}
	
	@Override
	public Bitmap decode(OnNewBitmapInputStreamListener onNewBitmapInputStreamListener, ImageSize targetSize, ImageLoader imageLoader, String requestName) {
		Bitmap bitmap = null;
		Options options = new Options();
		int outWidth = 0;
		int outHeight = 0;
		
		InputStream inputStream = onNewBitmapInputStreamListener.onNewBitmapInputStream();
		if(inputStream != null){
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, options);
			IoUtils.closeSilently(inputStream);
			
			inputStream = onNewBitmapInputStreamListener.onNewBitmapInputStream();
			if(inputStream != null){
				outWidth = options.outWidth;
				outHeight = options.outHeight;
				options.inSampleSize = calculateInSampleSize(options, targetSize.getWidth(), targetSize.getHeight());
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(inputStream, null, options);
				IoUtils.closeSilently(inputStream);
			}
		}
		
		if(imageLoader.getConfiguration().isDebugMode()){
			writeLog(imageLoader, requestName, bitmap != null, outWidth, outHeight, targetSize, options.inSampleSize, bitmap);
		}
		
		return bitmap;
	}
	
	/**
	 * 输出LOG
	 * @param imageLoader
	 * @param requestName
	 * @param success
	 * @param outWidth
	 * @param outHeight
	 * @param inSimpleSize
	 * @param bitmap
	 */
	private void writeLog(ImageLoader imageLoader, String requestName, boolean success, int outWidth, int outHeight, ImageSize targetSize, int inSimpleSize, Bitmap bitmap){
		String log = new StringBuffer(logName)
		.append("：").append(success?"解码成功":"解码失败")
		.append("：").append("原图尺寸").append("=").append(outWidth).append("x").append(outHeight)
		.append("：").append("目标尺寸").append("=").append(targetSize.getWidth()).append("x").append(targetSize.getHeight())
		.append("；").append("缩小").append("=").append(inSimpleSize)
		.append("；").append("最终尺寸").append("=").append(bitmap.getWidth()).append("x").append(bitmap.getHeight())
		.append("；").append(requestName)
		.toString();
		if(success){
			Log.d(imageLoader.getConfiguration().getLogTag(), log);
		}else{
			Log.w(imageLoader.getConfiguration().getLogTag(), log);
		}
	}
	
	/**
	 * 计算样本尺寸
	 * @param options
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
		int inSampleSize = 1;

		final int height = options.outHeight;
	    final int width = options.outWidth;
	    if (height > targetHeight || width > targetWidth) {
	        do{
	            inSampleSize *= 2;
	        }while ((height/inSampleSize) > targetHeight && (width/inSampleSize) > targetWidth); 
	    }

	    return inSampleSize;
	}
}