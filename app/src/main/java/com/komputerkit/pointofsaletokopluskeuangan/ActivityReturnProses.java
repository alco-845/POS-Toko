package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ActivityReturnProses extends AppCompatActivity {

    Toolbar appbar;
    String idorderdetail,idbarang,faktur,jumlah,stok,satjual,nil,tot,hjual,idorder, bayar ;
    View v ;
    Config config ;
    Database db ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_proses);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Return",getSupportActionBar());

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));
        db = new Database(this) ;

        idorderdetail = getIntent().getStringExtra("idorderdetail") ;
        Cursor cursor = db.sq("SELECT * FROM qcart WHERE idorderdetail="+idorderdetail);
        cursor.moveToNext();
        satjual = Function.getString(cursor, "satuanjual");
        nil = Function.getString(cursor, "nilai");
//        jumlahh = Function.getString(cursor, "jumlah");
        stok = Function.getString(cursor,"stok");

        setText() ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setText(){
        Cursor c = db.sq(Query.selectwhere("qorderdetail")+Query.sWhere("idorderdetail",idorderdetail)) ;
        c.moveToNext() ;
        idbarang = Function.getString(c,"idbarang:1") ;
        idorder = Function.getString(c,"idorder:1") ;
        String barang = Function.getString(c,"barang") ;
        jumlah = Function.getString(c,"jumlah") ;
        faktur = Function.getString(c,"faktur") ;
        tot = Function.getString(c,"total") ;
        bayar = Function.getString(c,"bayar") ;
        hjual = Function.getString(c,"hargajual") ;

        Function.setText(v,R.id.eJumBarang,Function.removeE(jumlah)) ;
        Function.setText(v,R.id.efaktur,faktur) ;
        Function.setText(v,R.id.eBarang,barang) ;
    }

    public void proses(View view){
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

        String tgl = Function.getDate("yyyyMMdd") ;
        String jum = Function.getText(v,R.id.eJumReturn) ;
        double kurang, hasil=0.0, jdetail=0.0, kur, kem, sal;
        String ret="", sald, ket = "Return", stat="1";

        Cursor c = db.sq("SELECT * FROM tblorderdetail WHERE idorderdetail="+idorderdetail);
        c.moveToNext();
        String status = Function.getString(c, "return");
        String j = Function.getString(c, "jumlah");

        if (status.equals("0")){
            ret = "1";
        }

        if (satjual.equals("1")){
            kurang = Function.strToDouble(jumlah);
        } else {
            kurang = Function.strToDouble(jum)/Function.strToDouble(nil);
        }

        Cursor cursor = db.sq("SELECT * FROM tbltransaksi");
        cursor.moveToLast();
        sald = Function.getString(cursor, "saldo");

        hasil =  Function.strToDouble(stok) + kurang;
        jdetail =  Function.strToDouble(j) - Function.strToDouble(jum);
        kur =  Function.strToDouble(tot) - (Function.strToDouble(jum) * Function.strToDouble(hjual));
        kem =  Function.strToDouble(bayar) - kur;
        sal = Function.strToDouble(sald) - (Function.strToDouble(bayar) - kur);
        if (jum.equals("0")){
            Toast.makeText(this, "Masukan Jumlah Barang Dengan Benar", Toast.LENGTH_SHORT).show();
        }else {
            if (Function.strToInt(jum) <= Function.strToInt(jumlah)) {
                if (!TextUtils.isEmpty(jum)) {
                    String[] p = {idbarang,
                            faktur,
                            tgl,
                            jum};

                    String[] simpan ={
                            tgl,
                            tempFaktur,
                            tempfa,
                            ket,
                            String.valueOf(kem),
                            String.valueOf(sal),
                            stat
                    };

                    String q = Query.splitParam("INSERT INTO tblreturn (idbarang,fakturbayar,tglreturn,jumlah)VALUES (?,?,?,?)", p);
                    String q1 = Query.splitParam("UPDATE tblorderdetail SET jumlah="+jdetail+",return="+ret+" WHERE idorderdetail="+idorderdetail);

                    db.exc("UPDATE tblbarang SET stok="+hasil+ " WHERE idbarang="+idbarang);

                    db.exc("UPDATE tblorder SET total="+kur+",kembali="+kem+" WHERE idorder="+idorder);

                    db.exc(Query.splitParam("INSERT INTO tbltransaksi (tgltransaksi,notransaksi,fakturtransaksi,keterangantransaksi,keluar,saldo,status) VALUES (?,?,?,?,?,?,?)",simpan));
//                    String q1 = "DELETE FROM tblorderdetail WHERE idorderdetail=" + idorderdetail;
                    if (db.exc(q) && db.exc(q1)) {
                        Toast.makeText(this, "Return Berhasil", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, ActivityReturn.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        Toast.makeText(this, "Return Gagal", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Mohon isi dengan benar", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Jumlah Barang melebihi pemesanan", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
