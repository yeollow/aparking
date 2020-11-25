package com.example.aparking.ui.home;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aparking.Bookmark;
import com.example.aparking.MainActivity;
import com.example.aparking.Menubar;
import com.example.aparking.R;
import com.example.aparking.ui.BookmarkFragment;
import com.example.aparking.ui.Check_qr;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    MapView mapFragment;
    Button btnSelectDate, btnSelectTime;
    public LayoutInflater parent;
    View sliding; // 슬라이딩 윈도우
    boolean isUp;
    /*
    int mapWidth, mapHeight; // 원래 지도 창 사이즈
    int mapNewHeight; // 슬라이딩 시 바뀌는 지도 창 height*/
    View marker_root_view;
    TextView tv_marker;
    TextView apt_name, apt_addr; // 슬라이딩 윈도우에 띄울 아파트 이름, 주소
    String temp;
    String qrcodeString;
    View btnBookmark, btnQRcode;

    TimeSetListener timeSetListener = new TimeSetListener();
    DateSetListener dateSetListener = new DateSetListener();

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        parent = inflater;
        View root = inflater.inflate(R.layout.activity_map, container, false);

        marker_root_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_marker, null);
        tv_marker = marker_root_view.findViewById(R.id.tv_marker);

        mapFragment = root.findViewById(R.id.map);
        if (mapFragment != null) {
            mapFragment.onCreate(savedInstanceState);
        }
        mapFragment.getMapAsync(this);

        // 지도 창 사이즈 얻기
       /* mapWidth = mapFragment.getLayoutParams().width;
        mapHeight = mapFragment.getLayoutParams().height;
        mapNewHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());
*/

        btnSelectDate = (Button) root.findViewById(R.id.btnDate);
        btnSelectTime = (Button) root.findViewById(R.id.btnTime);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(inflater.getContext(),
                        dateSetListener, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(parent.getContext(),
                        timeSetListener, mHour, mMinute, false);
                timePickerDialog.show();

            }
        });
        TextView reserve = root.findViewById(R.id.reserve);
        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrcodeString = getRandomString();
                Fragment fragment = new Check_qr();
                Bundle bundle = new Bundle(1);
                bundle.putString("qrcode",qrcodeString);
                fragment.setArguments(bundle);
                ((Menubar)getActivity()).replaceFragment(R.layout.activity_check_qr,fragment);
                Toast.makeText(inflater.getContext(), "예약이 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // sliding window 처음엔 안 보이게
        sliding = (FrameLayout)root.findViewById(R.id.sliding);
        sliding.setVisibility(View.INVISIBLE);
        isUp = false;

        apt_name = sliding.findViewById(R.id.sliding_apt_name);
        apt_addr = sliding.findViewById(R.id.sliding_apt_addr);

        // 예약확인, 즐겨찾기 버튼
        btnBookmark = (LinearLayout)root.findViewById(R.id.btnBookmark);
        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new BookmarkFragment();
                ((Menubar)getActivity()).replaceFragment(R.layout.activity_bookmark,fragment);
            }
        });
        btnQRcode = (LinearLayout)root.findViewById(R.id.btnQRcode);
        btnQRcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new Check_qr();
                Bundle bundle = new Bundle(1);
                bundle.putString("qrcode",qrcodeString);
                fragment.setArguments(bundle);
                ((Menubar)getActivity()).replaceFragment(R.layout.activity_check_qr,fragment);
            }
        });

        return root;
    }


    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        //((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int price1 = 3000;
        int price2 = 2000;
        int price3 = 1900;
        int price4 = 2500;


        tv_marker.setBackgroundResource(R.drawable.marker);
        tv_marker.setTextColor(Color.WHITE);


        // 1: 복현동진아파트 대구 북구 동북로50길 10 (복현동)
        String formatted1 = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price1);
        tv_marker.setText(formatted1);

        LatLng adr1 = new LatLng(35.895073, 128.616657);
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(adr1);
        markerOptions1.title("복현동진아파트");
        markerOptions1.snippet("대구 북구 동북로50길 10 (복현동)");
        markerOptions1.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions1);

        // 2: 뉴그랜드아파트 대구 북구 경진로남1길 17-5 (복현동)
        String formatted2 = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price2);
        tv_marker.setText(formatted2);

        LatLng adr2 = new LatLng(35.895456, 128.615767);
        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(adr2);
        markerOptions2.title("뉴그랜드아파트");
        markerOptions2.snippet("대구 북구 경진로남1길 17-5 (복현동)");
        markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions2);

        // 3: 동양아파트 대구 북구 경진로남1길 20 (복현동)
        String formatted3 = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price3);
        tv_marker.setText(formatted3);

        LatLng adr3 = new LatLng(35.894874, 128.615608);
        MarkerOptions markerOptions3 = new MarkerOptions();
        markerOptions3.position(adr3);
        markerOptions3.title("동양아파트");
        markerOptions3.snippet("대구 북구 경진로남1길 20 (복현동)");
        markerOptions3.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions3);

        // 4: 석우로즈빌아파트 대구 북구 경진로12길 6-14 (복현동)
        String formatted4 = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price4);
        tv_marker.setText(formatted4);

        LatLng adr4 = new LatLng(35.893167, 128.616869);
        MarkerOptions markerOptions4 = new MarkerOptions();
        markerOptions4.position(adr4);
        markerOptions4.title("석우로즈빌아파트");
        markerOptions4.snippet("대구 북구 경진로12길 6-14 (복현동)");
        markerOptions4.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions4);

        // 5: 복현아이파크아파트 대구 북구 경대로27길 40 (복현동)
        tv_marker.setText(formatted1);

        LatLng adr5 = new LatLng(35.894312, 128.617684);
        MarkerOptions markerOptions5 = new MarkerOptions();
        markerOptions5.position(adr5);
        markerOptions5.title("복현아이파크아파트");
        markerOptions5.snippet("대구 북구 경대로27길 40 (복현동)");
        markerOptions5.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions5);

        // 6: 리치파크아파트 대구 북구 경진로51 (복현동)
        tv_marker.setText(formatted2);

        LatLng adr6 = new LatLng(35.893198, 128.617419);
        MarkerOptions markerOptions6 = new MarkerOptions();
        markerOptions6.position(adr6);
        markerOptions6.title("리치파크아파트");
        markerOptions6.snippet("대구 북구 경진로51 (복현동)");
        markerOptions6.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(parent.getContext(), marker_root_view)));
        mMap.addMarker(markerOptions6);

        // 기존에 사용하던 다음 2줄은 문제가 있습니다.
        // CameraUpdateFactory.zoomTo가 오동작하네요.
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        mMap.setOnMarkerClickListener(this); // 마커 클릭 시 슬라이딩 윈도우 뜸
        mMap.setOnMapClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(adr1, 17));
    }

    // 여기부터 Lifecycle 함수들 implement.
    // 이거 없으니까 fragment로 바꿨을 때 지도 안 뜸
    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onPause() {
        mapFragment.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapFragment.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }
    // Lifecycle 함수 끝


    // 지도의 마커 누르면 슬라이딩 창 뜸뜸
    @Override
    public boolean onMarkerClick(Marker marker) {
        /* 슬라이딩 윈도우에 set text */
        temp = marker.getTitle();
        apt_name.setText(temp);
        temp = marker.getSnippet();
        apt_addr.setText(temp);

        if (!isUp) {
            slideUp(sliding);
            isUp = !isUp;
        }

        // return false로 바꾸면 아래 두 문장 필요 X
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        return true; // return false로 바꾸면 기존 스니펫도 보임
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        // 원래꺼
        /*TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);*/

        // 두 번째 시도
        /*Animation anim = AnimationUtils.loadAnimation
                (parent.getContext(), // 현재화면의 제어권자
                        R.anim.translate_anim2);   // 에니메이션 설정 파일
        view.startAnimation(anim);*/

        // 세 번째 시도
        ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationY",view.getHeight(), 0);
        animator.setDuration(500);
        animator.start();
    }

    public void slideDown(View view) {
        // 원래꺼
        /*TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);*/

        // 두 번째 시도
        /*Animation anim = AnimationUtils.loadAnimation
                (parent.getContext(), // 현재화면의 제어권자
                        R.anim.translate_anim);   // 에니메이션 설정 파일
        view.startAnimation(anim);*/

        // 세 번째 시도
        ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationY",0, view.getHeight());
        animator.setDuration(500);
        animator.start();
        //view.setVisibility(View.GONE);
    }

    // 지도의 마커 아닌 곳을 누르면 슬라이딩 창 닫힘
    @Override
    public void onMapClick(LatLng latLng) {
        if (isUp) {
            slideDown(sliding);
            isUp = !isUp;
        }
    }

    class DateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            //btnSelectDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            btnSelectDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
        }
    }

    class TimeSetListener implements TimePickerDialog.OnTimeSetListener {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(hourOfDay < 13)
                btnSelectTime.setText(hourOfDay + ":" + minute + " A.M.");
            else
                btnSelectTime.setText(hourOfDay-12 + ":" + minute + " P.M.");
        }
    }

    public void gotoBookmark(View v){
        Fragment fragment = new HomeFragment();
        ((Menubar)getActivity()).replaceFragment(R.layout.activity_bookmark,fragment);
    }

    public void gotoCheckqr(View v){
        Fragment fragment = new HomeFragment();
        ((Menubar)getActivity()).replaceFragment(R.layout.activity_map,fragment);
    }

    private static String getRandomString()
    {
        int length = 20;
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();

        String chars[] = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

        for (int i=0 ; i<length ; i++)
        {
            buffer.append(chars[random.nextInt(chars.length)]);
        }
        return buffer.toString();
    }
}

