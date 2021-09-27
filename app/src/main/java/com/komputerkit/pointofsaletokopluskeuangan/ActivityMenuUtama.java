package com.komputerkit.pointofsaletokopluskeuangan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import static com.komputerkit.pointofsaletokopluskeuangan.ActivityUtility.WRITE_EXST;

public class ActivityMenuUtama extends AppCompatActivity {

    AlertDialog.Builder dialog;
    LayoutInflater inflater;
    TextView ya, tidak;
    Database db;
    View view, dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_utama);

        db = new Database(this);
        view = this.findViewById(android.R.id.content);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

        Video();
    }

    private void Video(){
        SharedPreferences sp = getSharedPreferences("MyPrefs", 0);
        if (sp.getBoolean("satu", true)){
            sp.edit().putBoolean("satu", false).apply();
            Dialog();
        }
    }

    private void Dialog(){
        dialog = new AlertDialog.Builder(this);
        inflater = getLayoutInflater();
        dialogView  = inflater.inflate(R.layout.video_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);

        ya = dialogView.findViewById(R.id.ya);
        tidak = dialogView.findViewById(R.id.tidak);

        final AlertDialog dial = dialog.create();

        ya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/playlist?list=PLfTB96jbjODxxZ-cyh1YHeUxabnpZ_aHe")) ;
                startActivity(i);
                dial.cancel();
            }
        });

        tidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.cancel();
            }
        });

        dial.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor c=db.sq(Query.selectwhere("tblidentitas")+Query.sWhere("idtoko","1"));
        c.moveToNext();
        Function.setText(view,R.id.tittle,Function.upperCaseFirst(Function.getString(c,"namatoko")));
        Function.setText(view,R.id.sub,Function.getString(c,"alamattoko"));
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(ActivityMenuUtama.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityMenuUtama.this, permission)) {
                ActivityCompat.requestPermissions(ActivityMenuUtama.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(ActivityMenuUtama.this, new String[]{permission}, requestCode);
            }
        }
    }

    public void master(View view) {
        Intent intent = new Intent(this, ActivityMaster.class);
        startActivity(intent);
    }

    public void laporan(View view) {
        Intent intent = new Intent(this, ActivityLaporan.class);
        startActivity(intent);
    }

    public void transaksi(View view) {
        Intent intent = new Intent(this, ActivityTransaksi.class);
        startActivity(intent);
    }

    public void utiliti(View view) {
        Intent intent = new Intent(this, ActivityUtility.class);
        startActivity(intent);
    }

    public void tentang(View view) {
        Intent intent = new Intent(this, ActivityTentang.class);
        startActivity(intent);
    }

    private void keluar(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.create();
        alert.setMessage("Apakah anda yakin ingin keluar?");
        alert.setPositiveButton("ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    @Override
    public void onBackPressed() {
        keluar();
    }
}
