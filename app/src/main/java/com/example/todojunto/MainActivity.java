package com.example.todojunto;

import static android.content.ContentValues.TAG;
import static android.hardware.camera2.CameraCharacteristics.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    Button btnGuardarExcel;
    //private CameraPreview mPreview;
    String currentPhotoPath;
    //private Camera mCamera;
    private SurfaceHolder mHolder;
    int bandera=0;
    int i=0;


    private SensorManager sensorManager;
    private final float[] ac = new float[3];
    private final float[] gy = new float[3];

    TextView texto2, texto3, texto4;
    Writer output;
    double Latitud, Longitud;
    String timeStamp,NombreAchivo,NombreCarpeta,timeStamp_carpeta;
    double Boton1, Boton2;
    int N_dataset = 0;    // Sera para que cada mil datos imprimamos textoAsalvar en el TXT

    //INICIO DESARROLLO CAMERA 2 API

    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice mCamera;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


    String textoASalvar;
    //String NombreAchivo;
    LocationManager locationManager;
    private String provider;

    //Minimo tiempo para updates en Milisegundos
    private static final long MIN_CAMBIO_DISTANCIA_PARA_UPDATES = 1; // 1 metros
    //Minimo tiempo para updates en Milisegundos
    private static final long MIN_TIEMPO_ENTRE_UPDATES = 1000; // 1 segundo

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.camera_preview);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        btnGuardarExcel = findViewById(R.id.btnGuardarExcel);
        /*texto2 =  findViewById(R.id.texto2);
        texto3 =  findViewById(R.id.texto3);
        texto4 =  findViewById(R.id.texto4);*/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        btnGuardarExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
                try {
                    cameraCaptureSessions.stopRepeating();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                createCameraPreview();
                bandera=0;
            }
        });
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();

        while (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ){
            requestPermissions(new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }

        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            //
        }
        // Instanciado y cosas de la API CAMERA
        /*//genero un instanciado para la camara
        mCamera = getCameraInstance(0);


        //Creo una vista previa y le coloco el contenido de la actividad
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
*/
        // FIN DE RECURSOS API CAMERA
    }


    public void Tomardatos(View view) throws IOException {

        Boton1 = 1;
        Boton2 = 0;
        bandera=1;
        takePicture();
        timeStamp_carpeta= new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());
        NombreAchivo = new String("Datos_" + timeStamp_carpeta+ ".txt");
        File file = new File(getExternalFilesDir(null), NombreAchivo);
        FileOutputStream outputStream;
        textoASalvar ="Timestamp; ax; ay; az; gx; gy; gz; Latitud; Longitud  \n";
        outputStream = new FileOutputStream(file);
        outputStream.write(textoASalvar.getBytes());
        outputStream.close();
       //texto2.setText("");
        //texto2.append("Se inicio la aplicación en el tiempo:"  + "\n" + new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date()));
        Toast.makeText(getApplicationContext(), "INICIO DE TOMA DE DATOS", Toast.LENGTH_LONG).show();


    }

    public void guardar() {
        Boton1 = 0;
        Boton2 = 1;

        //texto3.setText("");
        //texto3.append("Se finalizo la toma de datos" + "\n" + new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date()) + "\n" + "La cantidad de datos son:" + N_dataset);
        Toast.makeText(getApplicationContext(), "FIN DE TOMA DE DATOS"+"\n" + "La cantidad de datos son:" + N_dataset, Toast.LENGTH_LONG).show();
        i=i+1;

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Obtenemos una actualizaciones del acelerometro, gyroscopo y GPSa una taza constante
        // Para hacer que las operaciones sean más eficientes y reducir el consumo de energía.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        //GPS
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
            return;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, this);

        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);

        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (Boton1 == 1 && Boton2 == 0) {

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(sensorEvent.values, 0, ac,
                        0, ac.length);
                // Aca directamente lo guardo
                timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date());
                textoASalvar = timeStamp + ";" + ac[0] + ";" + ac[1] + ";" + ac[2] +  ";" + null + ";"+ null + ";" + null + ";"
                        + null + ";" + null + ";" + "\n";
                ImprimoDatos();

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                System.arraycopy(sensorEvent.values, 0, gy,
                        0, gy.length);
                timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date());
                textoASalvar = timeStamp + ";" + null + ";"+ null + ";" + null + ";" + gy[0] + ";"+ gy[1] + ";" + gy[2] + ";"
                        + null + ";" + null + ";" + "\n";
                ImprimoDatos();
            }
        }
    }

    public void ImprimoDatos() {
        File file = new File(getExternalFilesDir(null), NombreAchivo);
        //Creo un flujo de salida para poder escribir datos en el file:
        FileOutputStream outputStream = null;
        try {
            output = new BufferedWriter(new FileWriter(file, true));
            output.append(textoASalvar);
            output.close();
            N_dataset = N_dataset + 1;

        } catch (java.io.IOException e) {
            e.printStackTrace();
            Boton1 = 0;
            Boton2 = 1;
            texto3.setText("");
            texto3.append("Se produjeron errores en el tiempo:" + "\n" + new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date()) + "\n" + "La cantidad de datos son:" + N_dataset);
            Toast.makeText(getApplicationContext(), "FIN DE TOMA DE DATOS", Toast.LENGTH_LONG).show();
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /*if (N_dataset == 10000) {
            Boton1 = 0;
            Boton2 = 1;
        }*/

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Latitud = (double) (location.getLatitude());
        Longitud = (double) (location.getLongitude());
        if (Boton1 == 1 && Boton2 == 0) {
            timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date());
            textoASalvar = timeStamp + ";" + null + ";"+ null + ";" + null + ";" + null + ";"+ null + ";" + null + ";"
                    + Latitud + "; " + Longitud  + "\n";
            ImprimoDatos();
        }

    }


//CAMERA API INI
    /*// Se comienza el desarrollo pra el objeto camara y manejarlo de manera manual
// se consigue un instanciado de camra
    public Camera getCameraInstance(int cameraselection){
        Camera c = null;
        try {
            c = Camera.open(cameraselection); // attempt to get a Camera instance
        }
        catch (Exception e){
            Toast.makeText(this, "La camara no esta disponible",Toast.LENGTH_LONG).show();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    // Genero una vista previa de lo que esta viendo la camara para poder hacer las capturas
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{



        public CameraPreview(Context context, Camera camera){
            super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);

            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                //  Toast.makeText(this,"Error al configurar la vista previa",Toast.LENGTH_LONG).show();
                //Log.d(TAG, "Error setting camera preview: " + e.getMessage()
            }

        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                //Toast.makeText(this,"Error al iniciar la vista previa",Toast.LENGTH_LONG).show();
                //Log.d(Tag, "Error starting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            mCamera.release();
        }
    }

    // Se desarrolla la captura de imagenes
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            NombreCarpeta = new String("Carpetas" + i );


            File NombreCarpeta = null;
            try{
                NombreCarpeta = crearImagen();
            }catch (IOException e){

            }
            if (NombreCarpeta == null){
//                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(NombreCarpeta);
                fos.write(data);
                fos.close();
                mCamera.startPreview();
                if (bandera == 1){
                    mCamera.takePicture(null,null, mPicture);
                }
            } catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

    };

    private File crearImagen() throws IOException {

            String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-ms").format(new Date());
            String nombreImagen = "foto" + timeStamp + "_";

            File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES +'_'+ timeStamp_carpeta);
            File imagen = File.createTempFile(nombreImagen, ".jpg", directorio);

            currentPhotoPath = imagen.getAbsolutePath();
            return imagen;


    }
    // FIN DEL DESARROLLO DE LA CAMARA MANUAL*/
//CAMERA API FIN





    //INICIO DESARROLLO CAMERA2 API




    // INICIALIZO EL TEXTURE LISTENER

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            // Se abre la camara
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            //Aqui se transforma el tamaño de captura de imagen de acuerdo con ancho y alto

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // Esta sentencia es ejecutada cuando se abre la camara

            Log.e(TAG, "onOpened");
            mCamera = camera;
            createCameraPreview();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCamera.close();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(@NonNull CameraDevice camera, int i) {
            mCamera.close();
            mCamera = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };


    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
// INICIO METODO PARA TOMAR FOTOS
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void takePicture() {
        if (null == mCamera) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCamera.getId());
            android.util.Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private File crearImagen() throws IOException {

                    String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-ms").format(new Date());
                    String nombreImagen = "foto" + timeStamp + "_";

                    File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES +'_'+ timeStamp_carpeta);
                    File imagen = File.createTempFile(nombreImagen, ".jpg", directorio);

                    currentPhotoPath = imagen.getAbsolutePath();
                    return imagen;


                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    // GUARDO EN CARPETA CON TIMESTAMP Y FOTO CON TIMESTAMP
                    File NombreCarpeta = null;
                    try{
                        NombreCarpeta = crearImagen();
                    }catch (IOException e){

                    }
                    if (NombreCarpeta == null){
//                Log.d(TAG, "Error creating media file, check storage permissions");
                        return;
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(NombreCarpeta);
                        fos.write(bytes);
                        fos.close();
                    }catch (FileNotFoundException e){
//                Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            mCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        //session.setRepeatingRequest(captureRequest, captureListener, mBackgroundHandler);
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    // FIN METODO PARA TOMAR FOTOS


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            //texture.setDefaultBufferSize(imageDimension.min(), imageDimension.max());
            Surface surface = new Surface(texture);
            captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            mCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {//The camera is already closed
                    if (null == mCamera) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == mCamera) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != mCamera) {
            mCamera.close();
            mCamera = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    // FIN DESARROLLO CAMERA2 API



}

