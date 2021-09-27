package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActivityLaporanMaster extends AppCompatActivity {

    Toolbar appbar;
    View v ;
    Config config ;
    Database db ;
    ArrayList arrayList = new ArrayList() ;
    List<ActivityPelanggan.getterPelanggan> DaftarPelanggan;
    RecyclerView.Adapter adapter ;
    String Master,type ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_master);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config", this.MODE_PRIVATE));
        db = new Database(this);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Master = getIntent().getStringExtra("type");
        String title = "judul";

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        if (Master.equals("barang")) {
            title = ("Laporan Barang");
            adapter = new AdapterBarang(this, arrayList);
            recyclerView.setAdapter(adapter);
            getBarang("");
        } else if (Master.equals("pelanggan")) {
            title = ("Laporan Pelanggan");
            adapter = new AdapterPelanggan(this, arrayList);
            recyclerView.setAdapter(adapter);
            getPelanggan("");
        }

        final EditText eCari = (EditText) findViewById(R.id.eCari);
        eCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                arrayList.clear();
                String a = eCari.getText().toString();
                if (Master.equals("barang")) {
                    getBarang(a);
                } else if (Master.equals("pelanggan")) {
                    getPelanggan(a);
                }
            }
        });

        Function.btnBack(title, getSupportActionBar());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getPelanggan(String keyword){
        String hasil = "" ;
        if (TextUtils.isEmpty(keyword)){
            hasil="SELECT * FROM tblpelanggan WHERE idpelanggan>0";
        }else {
            hasil="SELECT * FROM tblpelanggan WHERE idpelanggan>0 AND (pelanggan LIKE '%"+keyword+"%' OR alamat LIKE '%"+keyword+"%' OR notelp LIKE '%"+keyword+"%') ORDER BY pelanggan";
        }
        Cursor c = db.sq(hasil) ;
        if(c.getCount() > 0){
            Function.setText(v,R.id.tTotal,"Jumlah Data : "+Function.intToStr(c.getCount())) ;
            while(c.moveToNext()){
                String nama = Function.getString(c,"pelanggan");
                String telp = Function.getString(c,"notelp");
                String alamat = Function.getString(c,"alamat");

                String campur = nama +"__"+alamat+"__"+telp ;
                arrayList.add(campur);
            }
        } else {
            Function.setText(v,R.id.tTotal,"Jumlah Data : 0") ;
        }
        adapter.notifyDataSetChanged();
    }

    public void getBarang(String keyword){
        String hasil = "" ;
        if (TextUtils.isEmpty(keyword)){
            hasil="SELECT * FROM qbarang";
        }else {
            hasil="SELECT * FROM qbarang WHERE (kategori LIKE '%"+keyword+"%' OR barang LIKE '%"+keyword+"%' OR satuanbesar LIKE '%"+keyword+"%' OR satuankecil LIKE '%"+keyword+"%') ORDER BY barang";
        }
        Cursor c = db.sq(hasil);
        if(c.getCount() > 0){
            Function.setText(v,R.id.tTotal,"Jumlah Data : "+Function.intToStr(c.getCount())) ;
            while(c.moveToNext()){
                String barang= Function.getString(c,"barang") ;
                String hargabesar= Function.getString(c,"hargabesar") ;
                String hargakecil= Function.getString(c,"hargakecil") ;
                double stok= c.getDouble(c.getColumnIndex("stok")) ;
                String kategori= Function.getString(c,"kategori") ;
                String satuanbesar = Function.getString(c,"satuanbesar") ;
                String satuankecil = Function.getString(c,"satuankecil") ;
                String tipe= Function.getString(c,"tipe") ;

                String campur = barang+"__"+"Rp. "+Function.removeE(hargabesar)+"__"+"Rp. "+Function.removeE(hargakecil)+"__"+Function.removeE(stok)+"__"+kategori+"__"+satuanbesar+"__"+tipe+"__"+satuankecil ;
                arrayList.add(campur);
            }
        } else {
            Function.setText(v,R.id.tTotal,"Jumlah Data : 0") ;
        }
        adapter.notifyDataSetChanged();
    }

    public void export(View view){
        Intent i = new Intent(this, ActivityExportExcel.class) ;
        i.putExtra("type",Master) ;
        startActivity(i);
    }
}





class AdapterPelanggan extends RecyclerView.Adapter<AdapterPelanggan.ViewHolder> {
    private ArrayList<String> data;
    Context c;

    public AdapterPelanggan(Context a, ArrayList<String> kota) {
        this.data = kota;
        c = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_laporan_pelanggan, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView faktur, nma, jumlah,tvopt;

        public ViewHolder(View view) {
            super(view);

            nma = (TextView) view.findViewById(R.id.tBarang);
            faktur = (TextView) view.findViewById(R.id.tFaktur);
            jumlah = (TextView) view.findViewById(R.id.tTotal);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
//        final ActivityPelanggan.getterPelanggan getter = data.get(i);
        String[] row = data.get(i).split("__");

        viewHolder.jumlah.setText("Alamat : " + row[1]);
        viewHolder.nma.setText("Nama : " + row[0]);
        viewHolder.faktur.setText("No. Telepon : " + row[2]);
    }
}

class AdapterBarang extends RecyclerView.Adapter<AdapterBarang.ViewHolder> {
    private ArrayList<String> data;
    Context c;

    public AdapterBarang(Context a, ArrayList<String> kota) {
        this.data = kota;
        c = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_laporan_barang, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView barang, beli, jual,stok,kategori,tipe;

        public ViewHolder(View view) {
            super(view);

            barang = (TextView) view.findViewById(R.id.tePelanggan);
            jual = (TextView) view.findViewById(R.id.tFaktur);
            beli = (TextView) view.findViewById(R.id.tHitung);
            stok = (TextView) view.findViewById(R.id.tStok);
            kategori = (TextView) view.findViewById(R.id.teKategori);
            tipe= (TextView) view.findViewById(R.id.teTipe);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String[] row = data.get(i).split("__");

        viewHolder.barang.setText(row[0]);
        viewHolder.beli.setText("Harga Besar : "+row[1]+" "+row[5]);
        viewHolder.jual.setText("Harga Kecil : " +row[2]+" "+row[7]);
        viewHolder.stok.setText("Stok : "+row[3]+" "+row[5]);
        viewHolder.tipe.setText("Tipe : "+row[6]);
        viewHolder.kategori.setText("Kategori : "+row[4]);
    }
}
