package de.tum.in.cm.android.eddystonemanager.gui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import de.tum.in.cm.android.eddystonemanager.R;
import de.tum.in.cm.android.eddystonemanager.controller.SpeechController;
import de.tum.in.cm.android.eddystonemanager.speech.SpeechAction;
import de.tum.in.cm.android.eddystonemanager.utils.app.AppStorage;
import de.tum.in.cm.android.eddystonemanager.utils.general.GUIUtils;

public class CameraFragment extends Fragment implements ImageCallback {

  private static final String TAG = CameraFragment.class.getSimpleName();

  private static final int ZOOM_STEPS = 5;
  private static final int MIN_ZOOM_LEVEL = 0;
  private static final String FILE_ENDING = ".jpg";
  private static final int MAX_PREVIEW_WIDTH = 1920; // guaranteed by Camera2 API
  private static final int MAX_PREVIEW_HEIGHT = 1080; // guaranteed by Camera2 API
  private static final int MAX_IMAGES = 2;
  private static final int STATE_PREVIEW = 0;
  private static final int STATE_WAITING_LOCK = 1;
  private static final int STATE_WAITING_PRECAPTURE = 2;
  private static final int STATE_WAITING_NON_PRECAPTURE = 3;
  private static final int STATE_PICTURE_TAKEN = 4;
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  private SpeechController speechController;
  private String filename;
  private File imageFile;
  private TextureView cameraView;
  private ImageView imageView;

  private CameraDevice cameraDevice;
  private String cameraId;
  private CameraManager cameraManager;
  private HandlerThread backgroundThread;
  private Handler backgroundHandler;
  private ImageReader imageReader;
  private TextView zoomTextView;
  private TextView zoomMaxTextView;

  private int sensorOrientation;
  private Size cameraPreviewSize;
  private boolean flashSupported;
  private Semaphore cameraOpenCloseLock; // prevent app from exiting before closing the camera.
  private CaptureRequest.Builder previewRequestBuilder; // for camera preview
  private CameraCaptureSession captureSession; // for camera preview
  private CaptureRequest previewRequest;
  private int cameraState;
  private Rect zoom;
  private int zoomLevel;
  private int zoomMax;
  private int zoomStepSize;

  public void init(SpeechController speechController, String filename) {
    this.speechController = speechController;
    this.filename = filename;
    this.cameraOpenCloseLock = new Semaphore(1);
    this.cameraState = STATE_PREVIEW;
    this.zoomLevel = MIN_ZOOM_LEVEL;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setImageFile(new File(AppStorage.STORAGE_PATH + getFilename() + FILE_ENDING));
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    getSpeechController().speak(SpeechController.CAMERA_USAGE, SpeechAction.Camera);
    View view = inflater.inflate(R.layout.camera, container, false);
    this.cameraView = (TextureView) view.findViewById(R.id.cameraView);
    this.imageView = (ImageView) view.findViewById(R.id.imageView);
    this.zoomTextView = (TextView) view.findViewById(R.id.zoomValue);
    this.zoomMaxTextView = (TextView) view.findViewById(R.id.zoomMax);
    return view;
  }

  // Called when the fragment is visible to the user and actively running
  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();
    if (getCameraView().isAvailable()) {
      openCamera(getCameraView().getWidth(), getCameraView().getHeight());
    } else {
      getCameraView().setSurfaceTextureListener(getSurfaceTextureListener());
    }
  }

  @Override
  public void onPause() {
    stopBackgroundThread();
    super.onPause();
  }

  private void startBackgroundThread() {
    setBackgroundThread(new HandlerThread("CameraBackground"));
    getBackgroundThread().start();
    setBackgroundHandler(new Handler(getBackgroundThread().getLooper()));
  }

  private void stopBackgroundThread() {
    getBackgroundThread().quitSafely();
    try {
      getBackgroundThread().join();
      setBackgroundThread(null);
      setBackgroundHandler(null);
    } catch (InterruptedException e) {
      Log.e(TAG, "error stop background thread", e);
    }
  }

  // Initiate a still image capture
  public void takePicture() {
    lockFocus();
  }

  public void close() {
    closeCamera();
    getFragmentManager().popBackStackImmediate();
  }

  // Lock camera focus as first step for a still image capture
  private void lockFocus() {
    try {
      // Tell camera how to lock focus
      getPreviewRequestBuilder().set(CaptureRequest.CONTROL_AF_TRIGGER,
              CameraMetadata.CONTROL_AF_TRIGGER_START);
      // Tell capture callback to wait for focus lock
      setCameraState(STATE_WAITING_LOCK);
      getCaptureSession().capture(getPreviewRequestBuilder().build(),
              captureCallback, getBackgroundHandler());
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  private void openCamera(int width, int height) {
    setUpCameraOutputs(width, height);
    configureTransform(width, height);
    try {
      if (!getCameraOpenCloseLock().tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      getCameraManager().openCamera(getCameraId(), cameraStateCallback, getBackgroundHandler());
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    } catch (SecurityException e) {
      Log.e(TAG, "no permission to access camera", e);
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while trying to lock camera opening", e);
    }
  }

  private void closeCamera() {
    try {
      getCameraOpenCloseLock().acquire();
      if (getCaptureSession() != null) {
        getCaptureSession().close();
        setCaptureSession(null);
      }
      if (getCameraDevice() != null) {
        getCameraDevice().close();
        setCameraDevice(null);
      }
      if (getImageReader() != null) {
        getImageReader().close();
        setImageReader(null);
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while trying to lock camera closing", e);
    } finally {
      getCameraOpenCloseLock().release();
    }
  }

  // Create camera capture session for camera preview
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = getCameraView().getSurfaceTexture();
      texture.setDefaultBufferSize(getCameraPreviewSize().getWidth(), getCameraPreviewSize().getHeight());
      // Output surface to start preview.
      Surface surface = new Surface(texture);
      // CaptureRequest.Builder with output Surface
      setPreviewRequestBuilder(getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW));
      getPreviewRequestBuilder().addTarget(surface);
      // CameraCaptureSession for camera preview
      getCameraDevice().createCaptureSession(Arrays.asList(surface, getImageReader().getSurface()),
              captureStateCallback, null);
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    this.cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      for (String cameraId : getCameraManager().getCameraIdList()) {
        setCameraId(cameraId);
        
        CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(cameraId);
        // Don't use front facing camera
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
          continue;
        }

        setZoomMax(characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10);
        setZoomMaxText(getZoomMax());
        setZoomStepSize(getZoomMax() / ZOOM_STEPS);

        // For still image captures, we use the largest available size.
        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());
        setImageReader(ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                ImageFormat.JPEG, MAX_IMAGES));
        getImageReader().setOnImageAvailableListener(onImageAvailableListener,
                getBackgroundHandler());

        // Check if swap dimension to get preview size relative to sensor coordinate
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        setSensorOrientation(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
        boolean swappedDimensions = false;
        switch (displayRotation) {
          case Surface.ROTATION_0:
          case Surface.ROTATION_180:
            if (getSensorOrientation() == 90 || getSensorOrientation() == 270) {
              swappedDimensions = true;
            }
            break;
          case Surface.ROTATION_90:
          case Surface.ROTATION_270:
            if (getSensorOrientation() == 0 || getSensorOrientation() == 180) {
              swappedDimensions = true;
            }
            break;
          default:
            Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;
        if (swappedDimensions) {
          rotatedPreviewWidth = height;
          rotatedPreviewHeight = width;
          maxPreviewWidth = displaySize.y;
          maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
          maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }
        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
          maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }

        setCameraPreviewSize(chooseOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest));

        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        setFlashSupported(available == null ? false : available);
        return;
      }
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    } catch (NullPointerException e) {
      GUIUtils.showCloseAlertDialog(getActivity(),
              "Camera", "This device doesn\\'t support Camera2 API.");
    }
  }

  private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
    if (isFlashSupported()) {
      requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
              CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }
  }

  private static Size chooseOptimalPreviewSize(Size[] choices, int textureViewWidth,
                                               int textureViewHeight, int maxWidth,
                                               int maxHeight, Size aspectRatio) {
    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
              option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth &&
                option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }
    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  // Configures transformation to camera texture view
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (getCameraView() == null || getCameraPreviewSize() == null || activity == null) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, getCameraPreviewSize().getHeight(),
            getCameraPreviewSize().getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale = Math.max(
              (float) viewHeight / getCameraPreviewSize().getHeight(),
              (float) viewWidth / getCameraPreviewSize().getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (rotation == Surface.ROTATION_180) {
      matrix.postRotate(180, centerX, centerY);
    }
    getCameraView().setTransform(matrix);
  }

  public void zoom(String zoom) {
    try {
      CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(getCameraId());
      Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
      if (zoom != null) {
        if (zoom.contains("more")) {
          int newZoomLevel = getZoomLevel() + getZoomStepSize();
          if (newZoomLevel <= getZoomMax()) {
            setZoomLevel(newZoomLevel);
          } else {
            setZoomLevel(getZoomMax());
          }
        } else if (zoom.contains("less")) {
          int newZoomLevel = getZoomLevel() - getZoomStepSize();
          if (newZoomLevel >= MIN_ZOOM_LEVEL) {
            setZoomLevel(newZoomLevel);
          } else {
            setZoomLevel(MIN_ZOOM_LEVEL);
          }
        } else if (zoom.contains("min")) {
          setZoomLevel(MIN_ZOOM_LEVEL);
        } else if (zoom.contains("max")) {
          setZoomLevel(getZoomMax());
        }
      }
      int minW = m.width() / getZoomMax();
      int minH = m.height() / getZoomMax();
      int difW = m.width() - minW;
      int difH = m.height() - minH;
      int cropW = difW / 100 * getZoomLevel();
      int cropH = difH / 100 * getZoomLevel();
      cropW -= cropW & 3;
      cropH -= cropH & 3;
      setZoom(new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH));
      getPreviewRequestBuilder().set(CaptureRequest.SCALER_CROP_REGION, getZoom());
      setZoomText(getZoomLevel());
      getCaptureSession().setRepeatingRequest(getPreviewRequestBuilder().build(),
              captureCallback, null);
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  private final CameraCaptureSession.StateCallback captureStateCallback
          = new CameraCaptureSession.StateCallback() {

    @Override
    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
      // Camera already closed
      if (getCameraDevice() == null) {
        return;
      }
      // Session ready > start displaying preview
      setCaptureSession(cameraCaptureSession);
      try {
        // Auto focus should be continuous for camera preview
        getPreviewRequestBuilder().set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        // Flash is automatically enabled when necessary
        setAutoFlash(getPreviewRequestBuilder());

        // Start displaying camera preview
        setPreviewRequest(getPreviewRequestBuilder().build());
        getCaptureSession().setRepeatingRequest(getPreviewRequest(),
                captureCallback, getBackgroundHandler());
      } catch (CameraAccessException e) {
        logCameraAccessException(e);
      }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
      Log.e(TAG, "failed to create camera preview session");
    }
  };

  // Handles events related to JPEG capture
  private final CameraCaptureSession.CaptureCallback captureCallback
          = new CameraCaptureSession.CaptureCallback() {

    private void process(CaptureResult result) {
      switch (getCameraState()) {
        case STATE_PREVIEW: {
          // Nothing to do > camera preview is working normally.
          break;
        }
        case STATE_WAITING_LOCK: {
          Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
          if (afState == null) {
            captureStillPicture();
          } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                  CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
            // CONTROL_AE_STATE can be null on some devices
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (aeState == null ||
                    aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
              setCameraState(STATE_PICTURE_TAKEN);
              captureStillPicture();
            } else {
              runPrecaptureSequence();
            }
          }
          break;
        }
        case STATE_WAITING_PRECAPTURE: {
          // CONTROL_AE_STATE can be null on some devices
          Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
          if (aeState == null ||
                  aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                  aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
            setCameraState(STATE_WAITING_NON_PRECAPTURE);
          }
          break;
        }
        case STATE_WAITING_NON_PRECAPTURE: {
          // CONTROL_AE_STATE can be null on some devices
          Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
          if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
            setCameraState(STATE_PICTURE_TAKEN);
            captureStillPicture();
          }
          break;
        }
      }
    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
      process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
      process(result);
    }

  };

  // Capture still picture: capture callback or lock focus
  private void captureStillPicture() {
    try {
      final Activity activity = getActivity();
      if (activity == null || getCameraDevice() == null) {
        return;
      }
      // This is the CaptureRequest.Builder that we use to take a picture.
      final CaptureRequest.Builder captureBuilder =
              getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      captureBuilder.addTarget(getImageReader().getSurface());

      // Use the same AE and AF modes as the preview
      captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
              CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
      setAutoFlash(captureBuilder);

      // Orientation
      int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

      CameraCaptureSession.CaptureCallback CaptureCallback
              = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
          unlockFocus();
        }
      };
      if (getZoom() != null) {
        captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, getZoom());
      }
      getCaptureSession().stopRepeating();
      getCaptureSession().capture(captureBuilder.build(), CaptureCallback, null);
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  // Run precapture sequence for capturing still image
  private void runPrecaptureSequence() {
    try {
      // Tell camera how to trigger
      getPreviewRequestBuilder().set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
              CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
      // Tell capture callback to wait for the precapture sequence to be set
      setCameraState(STATE_WAITING_PRECAPTURE);
      getCaptureSession().capture(getPreviewRequestBuilder().build(), captureCallback,
              getBackgroundHandler());
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  // JPEG orientation from specified screen rotation
  private int getOrientation(int rotation) {
    return (ORIENTATIONS.get(rotation) + getSensorOrientation() + 270) % 360;
  }

  private void unlockFocus() {
    try {
      // Reset auto-focus trigger
      getPreviewRequestBuilder().set(CaptureRequest.CONTROL_AF_TRIGGER,
              CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
      setAutoFlash(getPreviewRequestBuilder());
      getCaptureSession().capture(getPreviewRequestBuilder().build(), captureCallback,
              getBackgroundHandler());
      // Camera go back to normal state of preview
      setCameraState(STATE_PREVIEW);
      getCaptureSession().setRepeatingRequest(getPreviewRequest(), captureCallback,
              getBackgroundHandler());
    } catch (CameraAccessException e) {
      logCameraAccessException(e);
    }
  }

  // Handles several lifecycle events on a texture view
  private final TextureView.SurfaceTextureListener surfaceTextureListener
          = new TextureView.SurfaceTextureListener() {

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
      openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
      configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {}

  };

  // Camera device changes its state
  private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
      // Camera opened > start camera preview
      getCameraOpenCloseLock().release();
      setCameraDevice(cameraDevice);
      createCameraPreviewSession();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      getCameraOpenCloseLock().release();
      cameraDevice.close();
      setCameraDevice(null);
    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
      getCameraOpenCloseLock().release();
      cameraDevice.close();
      setCameraDevice(null);
      Activity activity = getActivity();
      if (activity != null) {
        activity.finish();
      }
    }

  };

  private final ImageReader.OnImageAvailableListener onImageAvailableListener
          = new ImageReader.OnImageAvailableListener() {
    @Override
    public void onImageAvailable(ImageReader reader) {
      getBackgroundHandler().post(new ImageSaver(reader.acquireNextImage(),
              getImageFile(), CameraFragment.this));
    }
  };

  @Override
  public void onImageSaved() {
    getSpeechController().speak("Picture okay", SpeechAction.ConfirmPicture);
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {

        getCameraDevice().close();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(getImageFile().getPath(), options);
        getImageView().setVisibility(View.VISIBLE);
        getImageView().setBackgroundColor(Color.BLACK);
        getImageView().setImageBitmap(bitmap);
      }
    });
  }

  public void reopen() {
    openCamera(getCameraView().getWidth(), getCameraView().getHeight());
    setZoom(null);
    setZoomLevel(0);
    setZoomText(0);
  }

  // Compares two sizes based on their areas
  private static class CompareSizesByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
      return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
              (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  private void logCameraAccessException(CameraAccessException e) {
    Log.d(TAG, "cannot access camera", e);
  }

  private void setImageFile(File imageFile) {
    this.imageFile = imageFile;
  }

  private void setZoomStepSize(int zoomStepSize) {
    this.zoomStepSize = zoomStepSize;
  }

  private void setZoomMax(float zoomMax) {
    this.zoomMax = (int) zoomMax;
  }

  private void setZoomText(int zoom) {
    getZoomTextView().setText(String.valueOf(zoom) + "x");
  }

  private void setZoomMaxText(int zoomMax) {
    getZoomMaxTextView().setText(String.valueOf(zoomMax) + "x");
  }

  private void setImageReader(ImageReader imageReader) {
    this.imageReader = imageReader;
  }

  private void setBackgroundThread(HandlerThread backgroundThread) {
    this.backgroundThread = backgroundThread;
  }

  private void setBackgroundHandler(Handler backgroundHandler) {
    this.backgroundHandler = backgroundHandler;
  }

  private void setZoomLevel(int zoomLevel) {
    this.zoomLevel = zoomLevel;
  }

  private void setZoom(Rect zoom) {
    this.zoom = zoom;
  }

  private void setCameraState(int cameraState) {
    this.cameraState = cameraState;
  }

  private void setPreviewRequest(CaptureRequest previewRequest) {
    this.previewRequest = previewRequest;
  }

  private void setCaptureSession(CameraCaptureSession captureSession) {
    this.captureSession = captureSession;
  }

  private void setPreviewRequestBuilder(CaptureRequest.Builder previewRequestBuilder) {
    this.previewRequestBuilder = previewRequestBuilder;
  }

  private void setSensorOrientation(int sensorOrientation) {
    this.sensorOrientation = sensorOrientation;
  }

  private void setCameraPreviewSize(Size cameraPreviewSize) {
    this.cameraPreviewSize = cameraPreviewSize;
  }

  private void setFlashSupported(boolean flashSupported) {
    this.flashSupported = flashSupported;
  }

  private void setCameraId(String cameraId) {
    this.cameraId = cameraId;
  }

  private void setCameraDevice(CameraDevice cameraDevice) {
    this.cameraDevice = cameraDevice;
  }

  private Semaphore getCameraOpenCloseLock() {
    return this.cameraOpenCloseLock;
  }

  private int getSensorOrientation() {
    return this.sensorOrientation;
  }

  private Size getCameraPreviewSize() {
    return this.cameraPreviewSize;
  }

  private boolean isFlashSupported() {
    return this.flashSupported;
  }

  private String getCameraId() {
    return this.cameraId;
  }

  private SpeechController getSpeechController() {
    return this.speechController;
  }

  private String getFilename() {
    return this.filename;
  }

  public File getImageFile() {
    return this.imageFile;
  }

  private HandlerThread getBackgroundThread() {
    return this.backgroundThread;
  }

  private Handler getBackgroundHandler() {
    return this.backgroundHandler;
  }

  private TextureView.SurfaceTextureListener getSurfaceTextureListener() {
    return this.surfaceTextureListener;
  }

  private CameraManager getCameraManager() {
    return this.cameraManager;
  }

  private TextureView getCameraView() {
    return this.cameraView;
  }

  private ImageReader getImageReader() {
    return this.imageReader;
  }

  private CameraDevice getCameraDevice() {
    return this.cameraDevice;
  }

  private CaptureRequest.Builder getPreviewRequestBuilder() {
    return this.previewRequestBuilder;
  }

  private CameraCaptureSession getCaptureSession() {
    return this.captureSession;
  }

  private CaptureRequest getPreviewRequest() {
    return this.previewRequest;
  }

  private int getCameraState() {
    return this.cameraState;
  }

  private Rect getZoom() {
    return this.zoom;
  }

  private int getZoomLevel() {
    return zoomLevel;
  }

  private TextView getZoomTextView() {
    return this.zoomTextView;
  }

  private TextView getZoomMaxTextView() {
    return this.zoomMaxTextView;
  }

  private int getZoomMax() {
    return this.zoomMax;
  }

  private int getZoomStepSize() {
    return this.zoomStepSize;
  }

  private ImageView getImageView() {
    return this.imageView;
  }

}
