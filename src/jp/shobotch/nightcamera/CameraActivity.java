package jp.shobotch.nightcamera;


import java.io.IOException;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class CameraActivity extends Activity {
	private static final String TAG = "camera";

	private Camera tCamera;// カメラ
	private SurfaceView tCameraView;
	private View tbuttonView;
	private FrameLayout tFrameLayout;
	private ImageView tImageView;

	private Bitmap tPicture;// 写真をbitmap化したもの

	private boolean tAutoFocusing = false;
	private boolean shutterFlag = false;

	private SurfaceHolder.Callback suCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// カメラのプレビューサイズを特定する
			Camera.Parameters params = tCamera.getParameters();
			params.setPreviewSize(width, height);
			tCamera.setParameters(params);
			Log.d(TAG, "width:" + String.valueOf(width) + " height: " + String.valueOf(height));
			tCamera.setPreviewCallback(previewCallback);
			tCamera.startPreview();

		}

		public void surfaceCreated(SurfaceHolder holder) {

			// カメラを開く
			Log.d(TAG, "カメラ読み込み");
			tCamera = Camera.open();
			try {
				tCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.w(TAG, "Camera is not start.");
				e.printStackTrace();
			}
			Log.d(TAG, "カメラ開始");

		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "カメラ停止開始");
			// カメラを停止する
			tCamera.stopPreview();
			// カメラを開放する
			tCamera.release();
			tCamera = null;
			Log.d(TAG, "カメラ停止");
		}
	};

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// ロックをかける
			camera.setPreviewCallback(null);
			//Log.d(TAG, "start preview");
			//ここは毎フレームごとに呼び出されます。
			// TODO フレーム処理関数
			if(shutterFlag == true){
				shutterFlag = false;
				Log.i(TAG, "無音で撮影！");
				Camera.Size size = camera.getParameters().getPreviewSize();
				Log.d(TAG, "保存サイズ width:" + String.valueOf(size.width) + " height: " + String.valueOf(size.height));

				int rgb[] = new int[size.width * size.height];
				decodeYUV420SP(rgb, data, size.width, size.height);

				Bitmap bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565);
				bitmap.setPixels(rgb, 0, size.width, 0, 0, size.width, size.height);
				savePicture(bitmap);
			}
			// ロック解除
			camera.setPreviewCallback(previewCallback);
		}

		// YUV420SPからRGBに変換します。
        private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
                int height) {
            final int frameSize = width * height;

            for (int j = 0, yp = 0; j < height; j++) {
                int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
                for (int i = 0; i < width; i++, yp++) {
                    int y = (0xff & ((int) yuv420sp[yp])) - 16;
                    if (y < 0)
                        y = 0;
                    if ((i & 1) == 0) {
                        v = (0xff & yuv420sp[uvp++]) - 128;
                        u = (0xff & yuv420sp[uvp++]) - 128;
                    }

                    int y1192 = 1192 * y;
                    int r = (y1192 + 1634 * v);
                    int g = (y1192 - 833 * v - 400 * u);
                    int b = (y1192 + 2066 * u);

                    if (r < 0)
                        r = 0;
                    else if (r > 262143)
                        r = 262143;
                    if (g < 0)
                        g = 0;
                    else if (g > 262143)
                        g = 262143;
                    if (b < 0)
                        b = 0;
                    else if (b > 262143)
                        b = 262143;

                    rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                            | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                }
            }
        }
	};

	private void savePicture(Bitmap bitmap){
		if(bitmap != null){
			String filePath = ImageManager.addImageAsApplication(this, bitmap);
			if(filePath != null){
				Log.d(TAG, "saved image file for " + filePath);
			}else{
				Log.e(TAG, "Error saved Picture");
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_camera);
		// フルスクリーン
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// タイトルバー非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 親元になるレイアウトを読み込む
		tFrameLayout = new FrameLayout(this);

		// カメラの表示部分を読み込む
		tCameraView = new SurfaceView(this);
		// ボタンを読み込む
		tbuttonView = new View(this);

		// カメラのSurfaceHolderを取得
		SurfaceHolder holder = tCameraView.getHolder();
		// カメラのSurfaceHolderにCallbackを実装
		holder.addCallback(suCallback);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// ビューを追加
		tFrameLayout.addView(tCameraView);
		tFrameLayout.addView(tbuttonView);
		// レイアウトを書き出し
		setContentView(tFrameLayout);

		tImageView = new ImageView(this);

	}

	private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			// TODO オートフォーカス
			Log.d(TAG, success?"撮影する":"ただのオートフォーカス");
			if(success){
				Log.d(TAG, "オートフォーカス→撮影処理開始");
				// TODO 撮影用のコールバック読み出し
			}else{
				Log.d(TAG, "オートフォーカス開始");
				camera.setPreviewCallback(previewCallback);
			}
			Log.d(TAG, "オートフォーカス完了");
			shutterFlag = true;
			tAutoFocusing = false;

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_camera, menu);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ACTION_DOWNの時だけ実行
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			//Log.d(TAG, "On touch. Start shouter.");
			if(tAutoFocusing == false){
				//オートフォーカスで無ければオートフォーカス開始
				tCamera.autoFocus(autoFocusCallback);
				tAutoFocusing = true;
			}
		}

		return true;

	}

}
