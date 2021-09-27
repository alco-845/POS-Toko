package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActivityPenjualanCari extends AppCompatActivity {

    Toolbar appbar;
    ArrayList arrayList = new ArrayList();
    String type;
    List<ActivityPelanggan.getterPelanggan> DaftarPelanggan;
    List<ActivityBarang.getterBarang> DaftarBarang;
    Database db;
    AdapterListBarangCari adapterBarang;
    AdapterListPelangganCari adapterPelanggan;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter ;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_cari);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        db = new Database(this);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        type = getIntent().getStringExtra("cari");
        String title = "judul";

        if (type.equals("pelanggan")) {
            title = "Cari Pelanggan";
            Function.btnBack("Cari Pelanggan",getSupportActionBar());
            getPelanggan("");
        } else if (type.equals("barang")) {
            title = "Cari Barang";
            Function.btnBack("Cari Barang",getSupportActionBar());
            getBarang("");
        }

        final EditText edtCari = (EditText) findViewById(R.id.edtCari);
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = edtCari.getText().toString();
                if (type.equals("pelanggan")){
                    getPelanggan(keyword);
                } else if (type.equals("barang")){
                    getBarang(keyword);
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

    //--------------------------------------------------------------------------------------------------------//
    public void getBarang(String keyword) {
        DaftarBarang = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recListCari);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterBarang = new AdapterListBarangCari(this, DaftarBarang);
        recyclerView.setAdapter(adapterBarang);
        String q;
        if (TextUtils.isEmpty(keyword)) {
            q = "SELECT * FROM qbarang";
        } else {
            q = "SELECT * FROM qbarang WHERE barang LIKE '%" + keyword + "%'" + Query.sOrderASC("barang");
        }
        Cursor c = db.sq(q);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                DaftarBarang.add(new ActivityBarang.getterBarang(
                        c.getInt(c.getColumnIndex("idbarang")),
                        c.getInt(c.getColumnIndex("idkategori:1")),
                        c.getInt(c.getColumnIndex("idsatuan")),
                        c.getString(c.getColumnIndex("barang")),
                        c.getDouble(c.getColumnIndex("stok")),
                        c.getInt(c.getColumnIndex("hargabesar")),
                        c.getInt(c.getColumnIndex("hargakecil")),
                        c.getString(c.getColumnIndex("satuanbesar")),
                        c.getString(c.getColumnIndex("satuankecil")),
                        c.getString(c.getColumnIndex("kategori")),
                        c.getString(c.getColumnIndex("tipe"))
                ));
            }
        }
        adapterBarang.notifyDataSetChanged();
    }

    //--------------------------------------------------------------------------------------------------------//
    public void getPelanggan(String keyword) {
        DaftarPelanggan = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recListCari);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterPelanggan = new AdapterListPelangganCari(this, DaftarPelanggan);
        recyclerView.setAdapter(adapterPelanggan);
        String q;
        if (TextUtils.isEmpty(keyword)){
            q="SELECT * FROM tblpelanggan WHERE idpelanggan>0";
        }else {
            q="SELECT * FROM tblpelanggan WHERE idpelanggan>0 AND (pelanggan LIKE '%"+keyword+"%' OR alamat LIKE '%"+keyword+"%' OR notelp LIKE '%"+keyword+"%') ORDER BY pelanggan";
        }
        Cursor c = db.sq(q);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                DaftarPelanggan.add(new ActivityPelanggan.getterPelanggan(
                        c.getInt(c.getColumnIndex("idpelanggan")),
                        c.getString(c.getColumnIndex("pelanggan")),
                        c.getString(c.getColumnIndex("alamat")),
                        c.getString(c.getColumnIndex("notelp"))
                ));
            }
        }
        adapterPelanggan.notifyDataSetChanged();
    }

    //--------------------------------------------------------------------------------------------------------//

    class AdapterListPelangganCari extends RecyclerView.Adapter<AdapterListPelangganCari.PelangganCariViewHolder> {
        private Context ctxAdapter;
        private List<ActivityPelanggan.getterPelanggan> data;

        public AdapterListPelangganCari(Context ctxAdapter, List<ActivityPelanggan.getterPelanggan> data) {
            this.ctxAdapter = ctxAdapter;
            this.data = data;
        }

        @NonNull
        @Override
        public AdapterListPelangganCari.PelangganCariViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(ctxAdapter);
            View view = inflater.inflate(R.layout.list_penjualan_cari_pelanggan, viewGroup, false);
            return new AdapterListPelangganCari.PelangganCariViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterListPelangganCari.PelangganCariViewHolder holder, int i) {
            final ActivityPelanggan.getterPelanggan getter = data.get(i);

            holder.nama.setText("Nama : "+getter.getPelanggan());
            holder.alamat.setText("Alamat : "+getter.getAlamat());
            holder.telp.setText("No. Telepon : "+getter.getNoTelp());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent terima = new Intent(ctxAdapter, ActivityPenjualan.class);
                    terima.putExtra("idpelanggan", getter.getIdPelanggan());
                    terima.putExtra("pelanggan", getter.getPelanggan());
                    ((ActivityPenjualanCari) ctxAdapter).setResult(1000, terima);
                    ((ActivityPenjualanCari) ctxAdapter).finish();
                }
            });
        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        class PelangganCariViewHolder extends RecyclerView.ViewHolder{
            private TextView nama,alamat,telp;
            public PelangganCariViewHolder(@NonNull View itemView) {
                super(itemView);
                nama=(TextView) itemView.findViewById(R.id.namapelanggan);
                alamat=(TextView) itemView.findViewById(R.id.alamat);
                telp=(TextView) itemView.findViewById(R.id.telp);
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------//
    class AdapterListBarangCari extends RecyclerView.Adapter<AdapterListBarangCari.BarangCariViewHolder>{
        private Context ctxAdapter;
        private List<ActivityBarang.getterBarang> data;

        public AdapterListBarangCari(Context ctxAdapter, List<ActivityBarang.getterBarang> data) {
            this.ctxAdapter = ctxAdapter;
            this.data = data;
        }

        @NonNull
        @Override
        public BarangCariViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            final ActivityBarang.getterBarang getter=data.get(i);
            LayoutInflater inflater = LayoutInflater.from(ctxAdapter);
            View view=inflater.inflate(R.layout.list_penjualan_cari_barang,viewGroup,false);
            return new BarangCariViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BarangCariViewHolder holder, int i) {

            final ActivityBarang.getterBarang getter=data.get(i);


            holder.barang.setText(getter.getBarang());
            holder.stok.setText("Stok : "+Function.removeE(getter.getStok()));

            holder.hargaBesar.setText("Rp. "+Integer.toString(getter.getHargaBesar()));
            holder.hargaKecil.setText("Rp. "+Integer.toString(getter.getHargaKecil()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent terima = new Intent(ctxAdapter,ActivityPenjualan.class);

                    terima.putExtra("idbarang",getter.getIdBarang());
                    terima.putExtra("idkategori",getter.getIdKategori());
                    terima.putExtra("barang",getter.getBarang());
                    terima.putExtra("stok",Function.removeE(getter.getStok()));
                    terima.putExtra("hargabesar",Integer.toString(getter.getHargaBesar()));
                    terima.putExtra("hargakecil",Integer.toString(getter.getHargaKecil()));
                    ((ActivityPenjualanCari)ctxAdapter).setResult(2000,terima);
                    ((ActivityPenjualanCari)ctxAdapter).finish();

                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class BarangCariViewHolder extends RecyclerView.ViewHolder{
            private TextView barang,stok,hargaBesar, hargaKecil;
            public BarangCariViewHolder(@NonNull View itemView) {
                super(itemView);
                barang= (TextView)itemView.findViewById(R.id.barang);
                stok= (TextView)itemView.findViewById(R.id.stok);
                hargaBesar= (TextView)itemView.findViewById(R.id.hargabesar);
                hargaKecil= (TextView)itemView.findViewById(R.id.hargakecil);
            }
        }
    }}