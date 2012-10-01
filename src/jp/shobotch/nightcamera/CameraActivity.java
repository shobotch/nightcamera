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

	private Camera tCamera;// �J����
	private SurfaceView tCameraView;
	private View tbuttonView;
	private FrameLayout tFrameLayout;
	private ImageView tImageView;

	private Bitmap tPicture;// �ʐ^��bitmap����������

	private boolean tAutoFocusing = false;
	private boolean shutterFlag = false;

	private SurfaceHolder.Callback suCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// �J�����̃v���r���[�T�C�Y����肷��
			Camera.Parameters params = tCamera.getParameters();
			params.setPreviewSize(width, height);
			tCamera.setParameters(params);
			Log.d(TAG, "width:" + String.valueOf(width) + " height: " + String.valueOf(height));
			tCamera.setPreviewCallback(previewCallback);
			tCamera.startPreview();

		}

		public void surfaceCreated(SurfaceHolder holder) {

			// �J�������J��
			Log.d(TAG, "�J�����ǂݍ���");
			tCamera = Camera.open();
			try {
				tCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.w(TAG, "Camera is not start.");
				e.printStackTrace();
			}
			Log.d(TAG, "�J�����J�n");

		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "�J������~�J�n");
			// �J�������~����
			tCamera.stopPreview();
			// �J�������J������
			tCamera.release();
			tCamera = null;
			Log.d(TAG, "�J������~");
		}
	};

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// ���b�N��������
			camera.setPreviewCallback(null);
			//Log.d(TAG, "start preview");
			//�����͖��t���[�����ƂɌĂяo����܂��B
			// TODO �t���[�������֐�
			if(shutterFlag == true){
				shutterFlag = false;
				Log.i(TAG, "�����ŎB�e�I");
				Camera.Size size = camera.getParameters().getPreviewSize();
				Log.d(TAG, "�ۑ��T�C�Y width:" + String.valueOf(size.width) + " height: " + String.valueOf(size.height));

				int rgb[] = new int[size.width * size.height];
				decodeYUV420SP(rgb, data, size.width, size.height);

				Bitmap bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565);
				bitmap.setPixels(rgb, 0, size.width, 0, 0, size.width, size.height);
				savePicture(bitmap);
			}
			// ���b�N����
			camera.setPreviewCallback(previewCallback);
		}

		// YUV420SP����RGB�ɕϊ����܂��B
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
		// �t���X�N���[��
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// �^�C�g���o�[��\��
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// �e���ɂȂ郌�C�A�E�g��ǂݍ���
		tFrameLayout = new FrameLayout(this);

		// �J�����̕\��������ǂݍ���
		tCameraView = new SurfaceView(this);
		// �{�^����ǂݍ���
		tbuttonView = new View(this);

		// �J������SurfaceHolder���擾
		SurfaceHolder holder = tCameraView.getHolder();
		// �J������SurfaceHolder��Callback������
		holder.addCallback(suCallback);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// �r���[��ǉ�
		tFrameLayout.addView(tCameraView);
		tFrameLayout.addView(tbuttonView);
		// ���C�A�E�g�������o��
		setContentView(tFrameLayout);

		tImageView = new ImageView(this);

	}

	private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			// TODO �I�[�g�t�H�[�J�X
			Log.d(TAG, success?"�B�e����":"�����̃I�[�g�t�H�[�J�X");
			if(success){
				Log.d(TAG, "�I�[�g�t�H�[�J�X���B�e�����J�n");
				// TODO �B�e�p�̃R�[���o�b�N�ǂݏo��
			}else{
				Log.d(TAG, "�I�[�g�t�H�[�J�X�J�n");
				camera.setPreviewCallback(previewCallback);
			}
			Log.d(TAG, "�I�[�g�t�H�[�J�X����");
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
		// ACTION_DOWN�̎��������s
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			//Log.d(TAG, "On touch. Start shouter.");
			if(tAutoFocusing == false){
				//�I�[�g�t�H�[�J�X�Ŗ�����΃I�[�g�t�H�[�J�X�J�n
				tCamera.autoFocus(autoFocusCallback);
				tAutoFocusing = true;
			}
		}

		return true;

	}

}
