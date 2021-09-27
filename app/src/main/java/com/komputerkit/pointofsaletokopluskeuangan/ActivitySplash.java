package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        String l = "1. Ubah Identitas Sesuai dengan toko di menu no. 2\n" +
//                   "2. Isi Master dengan Benar di menu no. 3\n" +
//                    "3. Isi transaksi untuk proses penjualan di menu no.4\n" +
//                    "4. Lihat Laporan untuk melihat hasil penjualan di menu no.5" ;
//        View v = this.findViewById(android.R.id.content);
//        FFunction.setText(v,R.id.tips,l) ;

                new Handler().postDelayed(new Runnable() {


                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        Intent i = new Intent(ActivitySplash.this, ActivityIntro.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                }, 3000);
    }
}
