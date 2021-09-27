package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;
import java.util.List;

public class ActivityBarang extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    Toolbar appbar;
    RecyclerView listbarang;
    AdapterListBarang adapter;
    List<getterBarang> DaftarBarang;
    View v;
    ArrayList arrayList = new ArrayList() ;
    String type,deviceid;
    Database db;
    SharedPreferences getPrefs ;

    Config config;

    static boolean status;
    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barang);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Barang",getSupportActionBar());
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        db = new Database(this);
        v = this.findViewById(android.R.id.content);
        type = getIntent().getStringExtra("type") ;
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));

        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        deviceid = Function.getDecrypt(getPrefs.getString("deviceid","")) ;

        bp = new BillingProcessor(this, Function.getBase64Code(), this);
        bp.initialize();

        final EditText eCari = (EditText) findViewById(R.id.eCari) ;
        eCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String a = eCari.getText().toString() ;
                getBarang(a);
            }
        });

        if (!bp.isPurchased("postpkpro")){
            status = false;
        } else {
            status = true;
        }
    }

    private boolean limit(String item) {
        int batas = Function.strToInt(config.getCustom(item, "1"));
        if (batas>5){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getBarang(String keyword){
        DaftarBarang = new ArrayList<>();
        listbarang = (RecyclerView) findViewById(R.id.listbarang);
        listbarang.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listbarang.setLayoutManager(layoutManager);
        adapter = new AdapterListBarang(this,DaftarBarang);
        listbarang.setAdapter(adapter);
        String q;

        if (TextUtils.isEmpty(keyword)){
            q="SELECT * FROM qbarang";
        }else {
            q="SELECT * FROM qbarang WHERE (kategori LIKE '%"+keyword+"%' OR barang LIKE '%"+keyword+"%' OR satuanbesar LIKE '%"+keyword+"%' OR satuankecil LIKE '%"+keyword+"%') ORDER BY barang";
        }
        Cursor cur=db.sq(q);
        while(cur.moveToNext()){
            DaftarBarang.add(new getterBarang(
                    cur.getInt(cur.getColumnIndex("idbarang")),
                    cur.getInt(cur.getColumnIndex("idkategori:1")),
                    cur.getInt(cur.getColumnIndex("idsatuan")),
                    cur.getString(cur.getColumnIndex("barang")),
                    cur.getDouble(cur.getColumnIndex("stok")),
                    cur.getInt(cur.getColumnIndex("hargabesar")),
                    cur.getInt(cur.getColumnIndex("hargakecil")),
                    cur.getString(cur.getColumnIndex("satuanbesar")),
                    cur.getString(cur.getColumnIndex("satuankecil")),
                    cur.getString(cur.getColumnIndex("kategori")),
                    cur.getString(cur.getColumnIndex("tipe"))
            ));
        }
        adapter.notifyDataSetChanged();
    }

    public void tambah(View view){
        if (status){
            Intent intent = new Intent(this, ActivityTambahBarang.class);
            startActivity(intent);
        } else {
            if (limit("barang")){
                Intent intent = new Intent(this, ActivityTambahBarang.class);
                startActivity(intent);
            } else {
                bp.purchase(this, "postpkpro");
            }
        }
    }

    public void UpdateData(List<ActivitySatuan.getterSatuan> DaftarBaru){
        DaftarBaru = DaftarBaru;
        notify();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBarang("");
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        status = true;
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    static class AdapterListBarang extends RecyclerView.Adapter<AdapterListBarang.BarangViewHolder>{
        private Context ctxAdapter;
        private List<getterBarang> data;

        public AdapterListBarang(Context ctx, List<getterBarang> viewData) {
            this.ctxAdapter = ctx;
            this.data = viewData;
        }

        @NonNull
        @Override
        public BarangViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(ctxAdapter);
            View view = inflater.inflate(R.layout.list_barang,viewGroup,false);
            return new BarangViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final BarangViewHolder holder, final int i) {
            final getterBarang getter = data.get(i);
            holder.barang.setText(getter.getBarang());
            holder.stok.setText(Function.removeE(getter.getStok())+" "+getter.getSatuanbesar());
            holder.hargabesar.setText("Rp. "+Function.removeE(getter.getHargaBesar())+" / "+getter.getSatuanbesar());
            holder.hargakecil.setText("Rp. "+Function.removeE(getter.getHargaKecil())+" / "+getter.getSatuankecil());
            holder.kategori.setText("Kategori : "+String.valueOf(getter.getKategori()));
            holder.tipe.setText("Tipe : "+String.valueOf(getter.getTipe()));
            holder.opt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(ctxAdapter,holder.opt);
                    popupMenu.inflate(R.menu.option_item);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu_update:
                                    Intent intent = new Intent(ctxAdapter,ActivityTambahBarang.class);
                                    intent.putExtra("idbarang",getter.getIdBarang());
                                    intent.putExtra("idkategori",getter.getIdKategori());
                                    intent.putExtra("idsatuan",getter.getIdSatuan());
                                    intent.putExtra("barang",getter.getBarang());
                                    intent.putExtra("stok",Function.removeE(getter.getStok()));
                                    intent.putExtra("hargabesar",String.valueOf(getter.getHargaBesar()));
                                    intent.putExtra("hargakecil",String.valueOf(getter.getHargaKecil()));
                                    intent.putExtra("tipe",String.valueOf(getter.getTipe()));
                                    ctxAdapter.startActivity(intent);
                                    break;

                                case R.id.menu_delete:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ctxAdapter);
                                    builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Database db = new Database(ctxAdapter);
                                            if (db.deleteBarang(getter.getIdBarang())){
                                                data.remove(i);
                                                notifyDataSetChanged();
                                                Toast.makeText(ctxAdapter, "Delete barang "+getter.getBarang()+" berhasil", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(ctxAdapter, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    builder.setTitle("Hapus "+getter.getBarang())
                                            .setMessage("Anda yakin ingin menghapus "+getter.getBarang()+" dari data barang");
                                    builder.show();
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class BarangViewHolder extends RecyclerView.ViewHolder{
            TextView barang, stok, hargabesar, hargakecil, kategori, tipe, opt;
            public BarangViewHolder(@NonNull View itemView) {
                super(itemView);
                kategori=(TextView)itemView.findViewById(R.id.tvKategori);
                barang=(TextView)itemView.findViewById(R.id.tvBarang);
                stok=(TextView)itemView.findViewById(R.id.tvStok);
                hargabesar=(TextView)itemView.findViewById(R.id.tvHargaBesar);
                hargakecil=(TextView)itemView.findViewById(R.id.tvHargaKecil);
                tipe=(TextView)itemView.findViewById(R.id.tvTipe);
                opt=(TextView)itemView.findViewById(R.id.tvOpt);
            }
        }
    }
    static class getterBarang{
        private int idBarang;
        private int idKategori;
        private int idSatuan;
        private String barang;
        private double stok;
        private int hargaBesar;
        private int hargaKecil;
        private String satuanbesar;
        private String satuankecil;
        private String kategori;
        private String tipe;

        public getterBarang(int idBarang, int idKategori, int idSatuan, String barang, double stok, int hargaBesar, int hargaKecil, String satuanbesar, String satuankecil, String kategori, String tipe) {
            this.idBarang = idBarang;
            this.idKategori = idKategori;
            this.idSatuan = idSatuan;
            this.barang = barang;
            this.stok = stok;
            this.hargaBesar = hargaBesar;
            this.hargaKecil = hargaKecil;
            this.satuanbesar = satuanbesar;
            this.satuankecil = satuankecil;
            this.kategori = kategori;
            this.tipe = tipe;
        }

        getterBarang() {
        }

        public int getIdBarang() {
            return idBarang;
        }

        public int getIdKategori() { return idKategori; }

        public int getIdSatuan() { return idSatuan; }

        public String getBarang() {
            return barang;
        }

        public double getStok() {
            return stok;
        }

        public int getHargaBesar() {
            return hargaBesar;
        }

        public int getHargaKecil() {
            return hargaKecil;
        }

        public String getSatuanbesar() {
            return satuanbesar;
        }

        public String getSatuankecil() {
            return satuankecil;
        }

        public String getKategori() {
            return kategori;
        }

        public String getTipe() {
            return tipe;
        }
    }
}
