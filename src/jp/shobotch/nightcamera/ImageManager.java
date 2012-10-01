package jp.shobotch.nightcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class ImageManager {
	private static final String TAG = "ImageManager";

	private static long getDateTaken(){
		return System.currentTimeMillis();
	}

	private static String createName(){
		return createTime() + ".jpg";
	}

	private static String createTime() {
        return DateFormat.format("yyyyMMddkkmmss", getDateTaken()).toString();
    }

	public static String addImageAsApplication(Context context, Bitmap bitmap){
		String name = createName();
		//アプリケーション名を取得
		String appName = context.getResources().getString(R.string.app_name);

		//SDカードに "/アプリケーション名/"のディレクトリパスを作る
		String path = Environment.getExternalStorageDirectory().toString() + "/" + appName;
		return addImageAsApplication(context, name, path, name, bitmap, null);
	}

	public static String addImageAsApplication(Context context, String name, String directory,
			String filename, Bitmap source, byte[] jpegData){
		OutputStream outputStream = null;
		//String filePath = directory + "/" + filename;
		try{
			// ディレクトリが無ければ作成
			File dir = new File(directory);
			if(!dir.exists()){
				dir.mkdirs();
				Log.d(TAG, "mkdir " + dir.toString());
			}
			// ファイルを出力
			File file = new File(directory, filename);
			if(file.createNewFile()){
				outputStream = new FileOutputStream(file);
				if(source != null){
					source.compress(CompressFormat.JPEG, 100, outputStream);
				}else{
					outputStream.write(jpegData);
				}
			}
		}catch(FileNotFoundException ex){
			Log.w(TAG, ex);
			return null;
		}catch (IOException ex) {
			Log.w(TAG, ex);
			return null;
		}finally{
			if(outputStream != null){
				try{
					outputStream.close();
				}catch(Throwable t){
					Log.d(TAG, "File not closed?");
				}
			}
		}
		return null;

	}

}
