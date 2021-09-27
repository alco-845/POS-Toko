package com.komputerkit.pointofsaletokopluskeuangan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ActivityBayar extends AppCompatActivity {

    Toolbar appbar;
    String faktur, jumlah, pelanggan,deviceid;
    SharedPreferences getPrefs ;
    double cashback, pay;
    View v;
    Config config, temp;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bayar);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config", this.MODE_PRIVATE));
        temp = new Config(getSharedPreferences("temp", this.MODE_PRIVATE));
        db = new Database(this);
        pay = 0;

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Bayar",getSupportActionBar());

        Cursor c=db.sq("SELECT * FROM tblorder WHERE faktur='"+faktur+"'");
        c.moveToNext();
        Function.setText(v,R.id.jumlahbayar,"0");

        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        deviceid = Function.getDecrypt(getPrefs.getString("deviceid","")) ;
        faktur = getIntent().getStringExtra("faktur");
        Function.setText(v, R.id.teFaktur, faktur);

        setText();
        calculate();

        final EditText in = (EditText) findViewById(R.id.jumlahbayar);
        in.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
//                double kembali=Function.strToDouble(Function.getText(v,R.id.jumlahbayar))-Function.strToDouble(Function.unNumberFormat(Function.getText(v,R.id.totalbayar)));
//                Function.setText(v,R.id.kembali,Function.removeComma(Function.numberFormat(Function.doubleToStr(kembali))));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void cek(){
        Cursor c = db.sq(Query.selectwhere("qorder")+Query.sWhere("bayar", "0"));
        if (c.getCount()>0){
            c.moveToLast();
            String faktur = Function.getString(c, "faktur");
            Function.setText(v, R.id.edtNomorFaktur, faktur);
        } else {
            getFaktur();
        }
    }

    private void getFaktur(){
        List<Integer> idorder = new ArrayList<Integer>();
        String q="SELECT idtransaksi FROM tbltransaksi";
        Cursor c = db.sq(q);
        if (c.moveToNext()){
            do {
                idorder.add(c.getInt(0));
            }while (c.moveToNext());
        }
        String tempFaktur="";
        int IdFaktur=0;
        if (c.getCount()==0){
            tempFaktur=faktur.substring(0,faktur.length()-1)+"1";
        }else {
            IdFaktur = idorder.get(c.getCount()-1)+1;
            tempFaktur = faktur.substring(0,faktur.length()-String.valueOf(IdFaktur).length())+String.valueOf(IdFaktur);
        }
        Function.setText(v,R.id.edtNomorFaktur,tempFaktur);
    }

    public void cetak(View view) {
    }

    public void calculate() {
        double masuk = Function.strToDouble(Function.getText(v, R.id.jumlahbayar));
        double jum = Function.strToDouble(jumlah);
        if (masuk > jum) {
            double kembali = masuk - jum;
            cashback = kembali;
            Function.setText(v, R.id.kembali, Function.removeE(kembali));
        } else if (masuk == jum) {
            double kembali = 0;
            cashback = kembali;
            Function.setText(v, R.id.kembali, Function.removeE(kembali));
        } else {
            double kembali = jum - masuk;
            cashback = kembali;
            Function.setText(v, R.id.kembali, "-" + Function.removeE(kembali));
        }
    }

    public void setText() {
        Cursor c = db.sq(Query.selectwhere("tblorder") + Query.sWhere("faktur", faktur));
        pelanggan = temp.getCustom("idpelanggan", "");
        if (c.getCount() > 0) {
            c.moveToNext();
            pay = Function.strToDouble(Function.getString(c, "total"));
            jumlah = Function.getString(c, "total");
            Function.setText(v, R.id.totalbayar, Function.removeE(jumlah));
        } else {
            Intent i = new Intent(this, ActivityPenjualan.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    public String convertDate(String date){
        String[] a = date.split("/") ;
        return a[2]+a[1]+a[0];
    }

    public void bayar(View view) {
        List<Integer> idtrans = new ArrayList<Integer>();
        String e="SELECT idtransaksi FROM tbltransaksi";
        String fa = "00000000";
        Cursor cu = db.sq(e);
        if (cu.moveToNext()){
            do {
                idtrans.add(cu.getInt(0));
            }while (cu.moveToNext());
        }
        String tempFaktur="", tempfa="";
        int IdFaktur=0;
        if (cu.getCount()==0){
            tempFaktur=faktur.substring(0,faktur.length()-1)+"1";
            tempfa=fa.substring(0,fa.length()-1)+"1";
        }else {
            IdFaktur = idtrans.get(cu.getCount()-1)+1;
            tempFaktur = faktur.substring(0,faktur.length()-String.valueOf(IdFaktur).length())+String.valueOf(IdFaktur);
            tempfa = fa.substring(0,fa.length()-String.valueOf(IdFaktur).length())+String.valueOf(IdFaktur);
        }

        String keterangan = "Penjualan";
        String sald;
        int stat = 0;

        Cursor c = db.sq("SELECT * FROM tblorder");
        c.moveToLast();
        String tgl = Function.getString(c, "tglorder");
        String hmasuk = Function.getString(c, "total");
        Cursor cur = db.sq("SELECT * FROM tbltransaksi");
        if (cur.getCount()>0){
            cur.moveToLast();
            sald = Function.getString(cur, "saldo");
        } else {
            sald = "0";
        }
        double saldo = Function.strToDouble(hmasuk) + Function.strToDouble(sald);

        String[] p = {Function.getText(v, R.id.jumlahbayar), Function.doubleToStr(cashback), faktur};
        String[] simpan ={
                tgl,
                tempFaktur,
                tempfa,
                keterangan,
                hmasuk,
                String.valueOf(saldo),
                String.valueOf(stat)
        };
        String q = Query.splitParam("UPDATE tblorder SET bayar=?,kembali=? WHERE faktur=?  ",p);
        String w = Query.splitParam("INSERT INTO tbltransaksi (tgltransaksi,notransaksi,fakturtransaksi,keterangantransaksi,masuk,saldo,status) VALUES (?,?,?,?,?,?,?)",simpan);

        double masuk = Function.strToDouble(Function.getText(v, R.id.jumlahbayar));
        double jum = Function.strToDouble(jumlah);
        if (masuk < jum) {
            Toast.makeText(this, "Uang Pembayaran Kurang", Toast.LENGTH_SHORT).show();
        } else {
            if (db.exc(q) && db.exc(w)) {
//                temp.setCustom("idpelanggan", "");
//                temp.setCustom("idbarang", "");
//                temp.setCustom("tglorder", "");
//                temp.setCustom("faktur", "");
                tambahlimit();
                open();
            } else {
                Toast.makeText(this, "Pembayaran Gagal", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void open() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Apakah Anda ingin untuk cetak Struk ?");
        alertDialogBuilder.setPositiveButton("Cetak",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //yes
                        Intent i = new Intent(ActivityBayar.this, ActivityCetak.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("faktur", faktur);
                        startActivity(i);
                    }
                });

        alertDialogBuilder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(ActivityBayar.this, ActivityPenjualan.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void tambahlimit(){
        boolean status = ActivityTransaksi.status;
        if (!status){
            int batas = Function.strToInt(config.getCustom("penjualan", "1"))+1;
            config.setCustom("penjualan", Function.intToStr(batas));
        }
    }
}
