package com.tec2.buspaychofer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

public class Home extends AppCompatActivity {

    Context context;
    IntentIntegrator integrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = getApplicationContext();
        integrator = new IntentIntegrator(this);
    }

    public void lanzarEscaner(View v) {
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setPrompt("Presente su codigo para pagar");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Log.d("ESCANER", result.getContents());

                cobroHecho(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void cobroHecho(String data) {
        try {
            Long fecha = System.currentTimeMillis() / 1000;
            JSONObject obj = new JSONObject(data);
            HashMap<String, Object> operationData = new HashMap<>();
            operationData.put("tipo", 0);
            operationData.put("monto", obj.get("monto"));
            operationData.put("origen", obj.get("origen"));
            operationData.put("numbus", 404);
            operationData.put("ruta", "Tarahumara Inverso");
            operationData.put("timestamp", fecha);

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);

            DatabaseReference paymentsUserRef = firebaseDatabase.getReference("accounts").child((String) obj.get("origen")).child("payments");
            paymentsUserRef.push().setValue(operationData);

            DatabaseReference newCityRef = firebaseDatabase.getReference("payments");
            newCityRef.push().setValue(operationData)
                    .addOnSuccessListener(aVoid -> startActivity(new Intent(this, ResultActivity.class)))
                    .addOnFailureListener(e -> Snackbar.make(this.getCurrentFocus(), "Ocurrio un error", Snackbar.LENGTH_LONG).show());

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + data + "\"" + t.getMessage());
        }

    }
}
