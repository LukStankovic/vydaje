package com.stankovic.lukas.vydaje;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stankovic.lukas.vydaje.Api.ApiReader.ApiReader;
import com.stankovic.lukas.vydaje.Api.ApiRequest.ApiPostAsyncRequest;
import com.stankovic.lukas.vydaje.Api.ApiRequest.ApiParamsBuilder;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class NewEntry extends Activity {

    TextView tvLatitude;
    TextView tvLongitude;
    EditText etName;
    EditText etAmount;
    EditText etDateTime;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        loadLocation();

        etName = (EditText)findViewById(R.id.etEntryName);
        etDateTime = (EditText)findViewById(R.id.etDateTime);
        etAmount = (EditText)findViewById(R.id.etAmount);
        btnSave = (Button)findViewById(R.id.btnEntrySave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = save();
                if (status == 1) {
                    Toast.makeText(NewEntry.this, "Uloženo", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    startActivity(new Intent(NewEntry.this, MainActivity.class));
                } else if (status == -1) {
                    Toast.makeText(NewEntry.this, "Vyplň veškeré údaje", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                } else {
                    btnSave.setEnabled(true);
                    Toast.makeText(NewEntry.this, "Chyba", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int save() {
        btnSave.setEnabled(false);
        HashMap<String, String> params = new HashMap<>();

        String name = etName.getText().toString();
        String amount = etAmount.getText().toString();
        String dateTime = etDateTime.getText().toString();
        String longitude = tvLongitude.getText().toString();
        String latitude = tvLatitude.getText().toString();

        if (name.equals("") || amount.equals("") || dateTime.equals("") || longitude.equals("Zaměřuji šířku") || latitude.equals("Zaměřuji délku")) {
            return -1;
        }

        params.put("name", name);
        params.put("amount", amount);
        params.put("dateTime", dateTime);
        params.put("longitude", longitude);
        params.put("latitude", latitude);

        ApiPostAsyncRequest apiAsyncRequest = new ApiPostAsyncRequest();
        String status = "error";
        String response = "";
        try {
            response = apiAsyncRequest.execute("entry/save/", ApiParamsBuilder.buildParams(params)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        status = ApiReader.parseStatus(response);
        return status.equals("ok") ? 1 : 0;
    }


    private void loadLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener(tvLongitude, tvLatitude);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 50, locationListener);
    }
}
