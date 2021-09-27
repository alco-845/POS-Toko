package com.komputerkit.pointofsaletokopluskeuangan;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class ActivityLaporanTransaksi extends AppCompatActivity {

    Toolbar appbar;
    String type, jenisapp;
    View v;
    Config config;
    Database db;
    ArrayList arrayList = new ArrayList();
    String dari, ke;
    Calendar calendar;
    int year, month, day;

    SharedPreferences getPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_transaksi);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        String title = "judul";

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config", this.MODE_PRIVATE));
        db = new Database(this);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        type = getIntent().getStringExtra("type");
        jenisapp = getResources().getString(R.string.barang);

        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

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
                if (type.equals("pendapatan")) {
                    getPendapatan(a);
                } else if (type.equals("return")) {
                    getReturn(a);
                }
            }
        });

        setText();
        if (type.equals("pendapatan")) {
            title = ("Laporan Pendapatan");
            getPendapatan("");
        } else if (type.equals("return")) {
            title = ("Laporan Return");
            getReturn("");
        }
        Function.btnBack(title, getSupportActionBar());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void export(View view) {
        Intent i = new Intent(this, ActivityExportExcel.class);
        i.putExtra("type", type);
        startActivity(i);
    }

    public void setText() {
        dari = Function.setDatePicker(year, month + 1, day);
        ke = Function.setDatePicker(year, month + 1, day);
        String now = Function.setDatePickerNormal(year, month + 1, day);
        Function.setText(v, R.id.eKe, now);
        Function.setText(v, R.id.eDari, now);
    }

    public void getPendapatan(String cari) {
        arrayList.clear();
        String q = "";
        if (TextUtils.isEmpty(cari)) {
            q = Query.selectwhere("qorder") + "bayar>0 AND " + Query.sBetween("tglorder", dari, ke) + " LIMIT 30";
        } else {
            q = Query.selectwhere("qorder") + "bayar>0 AND (" + Query.sLike("pelanggan", cari) + " OR " + Query.sLike("faktur", cari) + ") AND " + Query.sBetween("tglorder", dari, ke);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recHutang);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new AdapterPendapatan(this, arrayList);
        recyclerView.setAdapter(adapter);
        Cursor c = db.sq(q);
        if (c.getCount() > 0) {
            double total = 0;
            double back = 0;
            double pay = 0;
            while (c.moveToNext()) {
                String nama = Function.getString(c, "pelanggan");
                String jumlah = Function.getString(c, "total");
                String bayar = Function.getString(c, "bayar");
                String kembali = Function.getString(c, "kembali");
                String tgl = Function.getString(c, "tglorder");
                String faktur = Function.getString(c, "faktur");

                String campur = Function.getCampur(faktur+ "\n" + Function.dateToNormal(tgl)+ "\nPelanggan : " +nama , "Total   : Rp. " + Function.removeE(jumlah),
                        "Bayar   : Rp. " + Function.removeE(bayar) + "\n" +
                                "Kembali : Rp. " + Function.removeE(kembali));
                arrayList.add(campur);
                total += Function.strToDouble(jumlah);
                back += Function.strToDouble(kembali);
                pay += Function.strToDouble(bayar);
            }
            Function.setText(v, R.id.tCaption, "Pendapatan \t: Rp. " + Function.removeE(total));
            Function.setText(v, R.id.tValue2, "Kembali \t\t: Rp.  " + Function.removeE(back));
            Function.setText(v, R.id.tValue, "Pembayaran \t: Rp.  " + Function.removeE(pay));
            Function.setText(v, R.id.tJumlahData, "Jumlah Data : " + String.valueOf(c.getCount()));
        } else {
            Function.setText(v, R.id.tCaption, "Pendapatan : Rp. 0");
            Function.setText(v, R.id.tValue, "Kembali    : Rp. 0");
            Function.setText(v, R.id.tValue2, "Pembayaran : Rp. 0");
            Function.setText(v, R.id.tJumlahData, "Jumlah Data : 0");
        }
        adapter.notifyDataSetChanged();
    }

    public void getReturn(String cari) {
        arrayList.clear();
        String q = "";
        if (TextUtils.isEmpty(cari)) {
            q = Query.selectwhere("qreturn") + Query.sBetween("tglreturn", dari, ke) + " LIMIT 30";
        } else {
            q = Query.selectwhere("qreturn") + Query.sLike("(barang", cari) + " OR " + Query.sLike("fakturbayar", cari) + ") AND " + Query.sBetween("tglreturn", dari, ke);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recHutang);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new AdapterPendapatan(this, arrayList);
        recyclerView.setAdapter(adapter);
        Cursor c = db.sq(q);
        if (c.getCount() > 0) {
            int total = 0;
            while (c.moveToNext()) {
                String faktur = Function.getString(c, "fakturbayar");
                String brg = Function.getString(c, "barang");
                String tgl = Function.getString(c, "tglreturn");
                String jumlah = Function.getString(c, "jumlah");

                String campur = Function.getCampur(faktur, Function.dateToNormal(tgl)+"\n"+brg, "Jumlah Return : " + jumlah + " Barang");
                arrayList.add(campur);
                total += Function.strToInt(jumlah);
            }
            Function.setText(v, R.id.tJumlahData, "Jumlah Data : " + String.valueOf(c.getCount()));
            Function.setText(v, R.id.tCaption, "Return :");
            Function.setText(v, R.id.tValue, Function.intToStr(total) + " " + jenisapp);
            Function.setText(v, R.id.tValue2, "");
        } else {
            Function.setText(v, R.id.tCaption, "Return :");
            Function.setText(v, R.id.tValue, "0 " + jenisapp);
            Function.setText(v, R.id.tValue2, "");
            Function.setText(v, R.id.tJumlahData, "Jumlah Data : 0");
        }
        adapter.notifyDataSetChanged();
    }

    public void dateDari(View view) {
        setDate(1);
    }

    public void dateKe(View view) {
        setDate(2);
    }

    public void filtertgl() {
        String a = Function.getText(v, R.id.eCari);
        if (type.equals("pendapatan")) {
            getPendapatan(a);
        } else if (type.equals("return")) {
            getReturn(a);
        }
    }

    //start date time picker
    public void setDate(int i) {
        showDialog(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 1) {
            return new DatePickerDialog(this, edit1, year, month, day);
        } else if (id == 2) {
            return new DatePickerDialog(this, edit2, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener edit1 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int thn, int bln, int day) {
            Function.setText(v, R.id.eDari, Function.setDatePickerNormal(thn, bln + 1, day));
            dari = Function.setDatePicker(thn, bln + 1, day);
            filtertgl();
        }
    };

    private DatePickerDialog.OnDateSetListener edit2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int thn, int bln, int day) {
            Function.setText(v, R.id.eKe, Function.setDatePickerNormal(thn, bln + 1, day));
            ke = Function.setDatePicker(thn, bln + 1, day);
            filtertgl();
        }
    };
    //end date time picker
}

    class AdapterPendapatan extends RecyclerView.Adapter<AdapterPendapatan.ViewHolder> {
        private ArrayList<String> data;
        Context c;

        public AdapterPendapatan(Context a, ArrayList<String> kota) {
            this.data = kota;
            c = a;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_laporan_transaksi_pendapatan, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView faktur, nma, jumlah;

            public ViewHolder(View view) {
                super(view);

                nma = (TextView) view.findViewById(R.id.tHitung);
                faktur = (TextView) view.findViewById(R.id.tBarang);
                jumlah = (TextView) view.findViewById(R.id.tNama);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            String[] row = data.get(i).split("__");

            viewHolder.jumlah.setText(row[2]);
            viewHolder.nma.setText(row[1]);
            viewHolder.faktur.setText(row[0]);
        }
    }