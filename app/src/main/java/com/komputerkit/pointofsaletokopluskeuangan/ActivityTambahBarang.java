package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;

import java.util.List;

public class ActivityTambahBarang extends AppCompatActivity {

    Toolbar appbar;
    ActivityBarang.AdapterListBarang adapter;
    List<ActivityBarang.getterBarang> DaftarBarang;
    Spinner spKat;
    Spinner spSatuan;
    Button bSimpan;
    TextInputEditText edtNamaBarang, edtStok, edtHargaBesar, edtHargakecil;
    String namaBarang;
    Integer idBarang, idKat, idSat,idbarangg;
    String stok, hargaBesar, hargaKecil;
    String sTitip="", sKat="", sSat="", q, deviceid;
    SharedPreferences getPrefs ;
    Database db;
    View v;
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_barang);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Tambah Barang",getSupportActionBar());

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));

        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        deviceid = Function.getDecrypt(getPrefs.getString("deviceid","")) ;

        Database db = new Database(this);

        bSimpan = (Button) findViewById(R.id.bSimpan);
        edtNamaBarang = (TextInputEditText) findViewById(R.id.eBarang);
        edtStok = (TextInputEditText) findViewById(R.id.eStok);
        edtHargaBesar = (TextInputEditText) findViewById(R.id.eHargaBesar);
        edtHargakecil = (TextInputEditText) findViewById(R.id.eHargaKecil);

        final List<String> getIdKat = db.getIdKategori();
        spKat = (Spinner) findViewById(R.id.spKat);
        getKategoriData();
        spKat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sKat = parent.getItemAtPosition(pos).toString();
                idKat = Function.strToInt(getIdKat.get(parent.getSelectedItemPosition()).toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final List<String> getIdSat = db.getIdSatuan();
        spSatuan=(Spinner)findViewById(R.id.spSatuan);
        getSatuanData();
        spSatuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sSat = parent.getItemAtPosition(position).toString();
                idSat = Function.strToInt(getIdSat.get(parent.getSelectedItemPosition()).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner sp = (Spinner) findViewById(R.id.spTitip);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sTitip = parent.getItemAtPosition(pos).toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Bundle extra = getIntent().getExtras();
        if (extra == null) {
            //Insert
            idBarang = null;
        } else {
            idBarang = extra.getInt("idbarang");
            idKat = extra.getInt("idkategori");
            idSat = extra.getInt("idsatuan");
            sTitip = extra.getString("tipe");
            Cursor c = db.sq("SELECT * FROM tblbarang WHERE idbarang="+Function.intToStr(idBarang));
            c.moveToNext();
            spKat.setSelection(Function.strToInt(Function.getString(c, "idkategori"))-1);
            spSatuan.setSelection(Function.strToInt(Function.getString(c, "idsatuan"))-1);
            sp.setSelection(Function.strToInt(extra.getString("tipe")));
            edtNamaBarang.setText(extra.getString("barang"));
            edtStok.setText(extra.getString("stok"));
            edtHargaBesar.setText(extra.getString("hargabesar"));
            edtHargakecil.setText(extra.getString("hargakecil"));
        }

        bSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                namaBarang = edtNamaBarang.getText().toString();
                stok = edtStok.getText().toString();
                hargaBesar = edtHargaBesar.getText().toString();
                hargaKecil = edtHargakecil.getText().toString();

                if (namaBarang.equals("") || stok.equals("") || hargaBesar.equals("") || hargaKecil.equals("") || stok.equals("0") || hargaBesar.equals("0") || hargaKecil.equals("0")) {
                    Toast.makeText(ActivityTambahBarang.this, "Isi data terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    Database db = new Database(ActivityTambahBarang.this);
                    if (idBarang == (null) || idKat == (null) || idSat == (null)){
                        if (sKat.equals("")||sSat.equals("")||sTitip.equals("")){
                            Toast.makeText(ActivityTambahBarang.this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show();
                        }else {
                            if (db.insertBarang(idKat,idSat,namaBarang, Function.changeComa(stok), hargaBesar, hargaKecil, sTitip)) {
                                Toast.makeText(ActivityTambahBarang.this, "Tambah Barang berhasil", Toast.LENGTH_SHORT).show();
                                tambahlimit();
                                finish();
                            } else {
                                Toast.makeText(ActivityTambahBarang.this, "Tambah data gagal", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        if (db.updateBarang(idBarang, idKat, idSat, namaBarang, Function.changeComa(stok), hargaBesar, hargaKecil, sTitip)) {
                            Toast.makeText(ActivityTambahBarang.this, "Berhasil memperbaharui data", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ActivityTambahBarang.this, "Gagal memperbaharui data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void tambahlimit() {
        boolean status = ActivityBarang.status;
        if (!status){
            int batas = Function.strToInt(config.getCustom("barang", "1"))+1;
            config.setCustom("barang", Function.intToStr(batas));
        }
    }

    private void getKategoriData(){
        Database db = new Database(this);
        List<String> labels = db.getKategori();

        ArrayAdapter<String> data = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,labels);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKat.setAdapter(data);
    }

    private void getSatuanData(){
        Database db = new Database(this);
        List<String> labels = db.getSatuan();

        ArrayAdapter<String> data = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,labels);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSatuan.setAdapter(data);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
