package com.example.aparking.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aparking.MainActivity;
import com.example.aparking.Menubar;
import com.example.aparking.R;
import com.example.aparking.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Check_qr extends Fragment {
    private Button navigationBtn;
    private Button callbtn;

    private Button closeBtn;
    private TextView info, cancel, telenum;
    private String text = null;

    private final String packageName = "com.nhn.android.nmap";
    Intent intent;

    private TextView apart_name, apart_addr, date;
    String tel;
    ViewGroup view;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference qrcode_storage = mDatabase.child("user_qr");

    public static Check_qr newinstance(){
        return new Check_qr();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.activity_check_qr, container, false);


        navigationBtn = root.findViewById(R.id.nav_btn);
        closeBtn = root.findViewById(R.id.close_btn);

        view = container;
        callbtn = root.findViewById(R.id.call_btn);
        info = root.findViewById(R.id.qrcheckInfotext);
        cancel = root.findViewById(R.id.qrcheck_cancel);
        telenum = root.findViewById(R.id.textView6);
        apart_name = root.findViewById(R.id.textView2);
        apart_addr = root.findViewById(R.id.textView4);
        date = root.findViewById(R.id.qrcheck_Date);

        info.setText("예약된 주차장이 없습니다.");

        ImageView iv = root.findViewById(R.id.qrcode);
        text = getArguments().getString("qrcode");
        if(text != null) {
            info.setText("주차장 QR코드 인식기에 인식시켜주세요");
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            String name = getArguments().getString("apart_name");
            String addr = getArguments().getString("apart_addr");
            tel = getArguments().getString("apart_tel");
            try {
                apart_name.setText(name);
                apart_addr.setText(addr);
                telenum.setText("관리자 번호 :"+tel);
                BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                iv.setImageBitmap(bitmap);
            } catch (Exception e) {
            }
        }else{
            iv.setImageBitmap(null);
            iv.setImageResource(R.drawable.aparking);
            //iv.setColorFilter(Color.parseColor("#27BDBDBD"), PorterDuff.Mode.MULTIPLY);
            iv.setColorFilter(Color.parseColor("#B7BDBDBD"), PorterDuff.Mode.SRC_IN);
            cancel.setVisibility(View.INVISIBLE);
            callbtn.setVisibility(View.INVISIBLE);
            telenum.setVisibility(View.INVISIBLE);
            date.setVisibility(View.INVISIBLE);
        }


        View view = inflater.inflate(R.layout.activity_map, null);
        closeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Fragment fragment = new HomeFragment();
                ((Menubar) getActivity()).replaceFragment(R.layout.activity_map, fragment);

            }
        });

        navigationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(intent);
            }
        });

        callbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String temp = "tel:";
                ((Menubar)getActivity()).call(temp+tel);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(root.getContext());
                alertDialogBuilder.setTitle("예약 취소");
                alertDialogBuilder.setMessage("예약취소하겠습니까?");
                alertDialogBuilder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        text = null;
                        qrcode_storage.setValue("null");
                        Toast.makeText(root.getContext(),"예약이 취소되었습니다.",Toast.LENGTH_LONG).show();
                        Fragment fragment = new Check_qr();
                        Bundle bundle = new Bundle(1);
                        bundle.putString("qrcode",text);
                        fragment.setArguments(bundle);
                        Fragment fragment_h = new HomeFragment();
                        Bundle bundle2 = new Bundle(1);
                        bundle.putString("qrcode",text);
                        fragment.setArguments(bundle);
                        ((Menubar)getActivity()).replaceFragment(R.layout.activity_check_qr,fragment);
                    }
                });
                alertDialogBuilder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialogBuilder.show();
            }
        });

        return root;
    }
/*
    @Override
    public void onStart() {
        super.onStart();
        db_read();
    }

    public void db_read(){
        qrcode_storage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               // text = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

 */
}
