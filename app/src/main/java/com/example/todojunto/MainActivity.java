package com.example.todojunto;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    Button btnGuardarExcel;

    private SensorManager sensorManager;
    private final float[] ac = new float[3];
    private final float[] gy = new float[3];

    TextView texto2, texto3, texto4;
    Writer output;
    double Latitud, Longitud;
    String timeStamp;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGuardarExcel = findViewById(R.id.btnGuardarExcel);
        texto2 =  findViewById(R.id.texto2);
        texto3 =  findViewById(R.id.texto3);
        texto4 =  findViewById(R.id.texto4);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        btnGuardarExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        // Permisos
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else if (androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
            return;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
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

    }

    public void Tomardatos(View view) throws IOException {

        Boton1 = 1;
        Boton2 = 0;
        //String NombreAchivo = new String("Datos_" + i + ".txt");
        File file = new File(getExternalFilesDir(null), "DATOS.txt");
        FileOutputStream outputStream;
        textoASalvar ="Timestamp, ax, ay, az, gx, gy, gz, Latitud, Longitud  \n";
        outputStream = new FileOutputStream(file);
        outputStream.write(textoASalvar.getBytes());
        outputStream.close();
        texto2.setText("");
        texto2.append("Se inicio la aplicación en el tiempo:"  + "\n" + new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date()));

    }

    public void guardar() {
        Boton1 = 0;
        Boton2 = 1;

        texto3.setText("");
        texto3.append("Se finalizo la toma de datos" + "\n" + new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date()) + "\n" + "La cantidad de datos son:" + N_dataset);
        Toast.makeText(getApplicationContext(), "FIN DE TOMA DE DATOS", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
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
                textoASalvar = timeStamp + "," + ac[0] + "," + ac[1] + "," + ac[2] + "," + " " + "," + " " + "," + " " + ","
                        + " "+ "," + " " + "," + "\n";
                ImprimoDatos();

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                System.arraycopy(sensorEvent.values, 0, gy,
                        0, gy.length);
                timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss.SSSSSSS").format(new Date());
                textoASalvar = timeStamp + "," + "" + "," + " " + "," + " " + "," + gy[0] + "," + gy[1] + "," + gy[2] + ","
                        + " "+ ", " + " " + ", " + "\n";
                ImprimoDatos();
            }
        }
    }

    public void ImprimoDatos() {
        //String NombreAchivo = new String("Datos_" + i + ".txt");
        File file = new File(getExternalFilesDir(null), "DATOS.txt");
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
            textoASalvar = timeStamp + ", " + " " + ", " + " " + ", " + " " + ", " + " " + ", " + " " + ", " + "" + ", "
                    + Latitud + ", " + Longitud  + "\n";
            ImprimoDatos();
        }

    }
}

