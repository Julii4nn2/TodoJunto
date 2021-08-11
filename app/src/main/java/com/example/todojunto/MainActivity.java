package com.example.todojunto;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    Button btnGuardarExcel;
    private CameraPreview mPreview;
    String currentPhotoPath;
    private Camera mCamera;
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



        btnGuardarExcel = findViewById(R.id.btnGuardarExcel);
        /*texto2 =  findViewById(R.id.texto2);
        texto3 =  findViewById(R.id.texto3);
        texto4 =  findViewById(R.id.texto4);*/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        btnGuardarExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
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

        //genero un instanciado para la camara
        mCamera = getCameraInstance(0);


        //Creo una vista previa y le coloco el contenido de la actividad
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);





    }

    public void Tomardatos(View view) throws IOException {

        Boton1 = 1;
        Boton2 = 0;
        bandera=1;
        mCamera.takePicture(null,null, mPicture);
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


    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);

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

    // Se comienza el desarrollo pra el objeto camara y manejarlo de manera manual
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
    // FIN DEL DESARROLLO DE LA CAMARA MANUAL

}

