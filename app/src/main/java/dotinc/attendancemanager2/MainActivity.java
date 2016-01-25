package dotinc.attendancemanager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dotinc.attendancemanager2.Adapters.AttendanceAdapter;
import dotinc.attendancemanager2.Adapters.MainViewPagerAdapter;
import dotinc.attendancemanager2.Fragements.HeaderFragment;
import dotinc.attendancemanager2.Fragements.SecondFragment;
import dotinc.attendancemanager2.Objects.TimeTableList;
import dotinc.attendancemanager2.Utils.AttendanceDatabase;
import dotinc.attendancemanager2.Utils.ProgressPageIndicator;
import dotinc.attendancemanager2.Utils.SubjectDatabase;
import dotinc.attendancemanager2.Utils.TimeTableDatabase;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView, exclRecyclerView;
    private CoordinatorLayout root;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ViewPager pager;
    private ArrayList<Fragment> pageList;
    private ProgressPageIndicator indicator;
    private FloatingActionButton fab;
    private Button attendAll;

    private int dayCode;

    private ArrayList<TimeTableList> allSubjectsArrayList;      //add
    private ArrayList<TimeTableList> arrayList;

    AttendanceDatabase database;
    TimeTableDatabase timeTableDatabase;
    TimeTableList timeTableList;
    SubjectDatabase subjectDatabase;                            //add

    private Boolean isViewopened = false;


    void instantiate() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        root = (CoordinatorLayout) findViewById(R.id.root);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        attendAll = (Button) findViewById(R.id.attend_all);

        pager = (ViewPager) findViewById(R.id.pager);
        indicator = (ProgressPageIndicator) findViewById(R.id.pageIndicator);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        exclRecyclerView = (RecyclerView) findViewById(R.id.extra_class_recycler_view);
        exclRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exclRecyclerView.setHasFixedSize(true);

        pageList = new ArrayList<>();
        pageList.add(new HeaderFragment());
        pageList.add(new SecondFragment());
        allSubjectsArrayList = new ArrayList<>();

        timeTableList = new TimeTableList();
        database = new AttendanceDatabase(this);
        timeTableDatabase = new TimeTableDatabase(this);
        subjectDatabase = new SubjectDatabase(this);

        dayCode = getdaycode();
        timeTableList.setDayCode(dayCode);
        arrayList = timeTableDatabase.getSubjects(timeTableList);

    }
    private void extraClass(){
        timeTableList.setDayCode(dayCode);                //daycode
        arrayList = timeTableDatabase.getSubjects(timeTableList);
        allSubjectsArrayList = subjectDatabase.getAllSubjectsForExtra();
        for (int i = 0; i < arrayList.size(); i++)
            Log.d("option_arl", arrayList.get(i).getSubjectName());
        for (int i = 0; i < allSubjectsArrayList.size(); i++)
            Log.d("option_all", allSubjectsArrayList.get(i).getSubjectName());

        for (int i = 0; i < arrayList.size(); i++) {
            for (int j = 0; j < allSubjectsArrayList.size(); j++) {
                if ((allSubjectsArrayList.get(j).getSubjectName().equals(arrayList.get(i).getSubjectName())))
                    allSubjectsArrayList.remove(j);
            }
        }
        for (int i = 0; i < allSubjectsArrayList.size(); i++)
            Log.d("option_extra", allSubjectsArrayList.get(i).getSubjectName());
        exclRecyclerView.setAdapter(new AttendanceAdapter(this, allSubjectsArrayList));
    }
    private int getdaycode() {
        int day_code = 1;
        Date date = new Date();
        String myDate;
        SimpleDateFormat format = new SimpleDateFormat("EEE");
        myDate = format.format(date.getTime());
        switch (myDate) {
            case "Mon":
                day_code = 1;
                break;
            case "Tue":
                day_code = 2;
                break;
            case "Wed":
                day_code = 3;
                break;
            case "Thu":
                day_code = 4;
                break;
            case "Fri":
                day_code = 5;
                break;
            case "Sat":
                day_code = 6;
                break;
        }
        return day_code;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instantiate();
        extraClass();
        getSupportActionBar().setTitle("Wednesday");

        recyclerView.setAdapter(new AttendanceAdapter(this, arrayList));
        pager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager(), pageList));

        pager.addOnPageChangeListener(new CustomOnPageChangeListener());
        indicator.setViewPager(pager);

        exclRecyclerView.setAdapter(new AttendanceAdapter(this, arrayList));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    final View extraView = findViewById(R.id.extra_class_layout);
                    Animator anim = null;
                    int cx = extraView.getWidth();
                    int cY = 0;

                    if (!isViewopened) {
                        int finalRadius = Math.max(extraView.getWidth(), extraView.getHeight() + 1000);

                        anim = ViewAnimationUtils.createCircularReveal(extraView, cx, cY, 0, finalRadius);
                        anim.setDuration(1000).setInterpolator(new DecelerateInterpolator(1));
                        isViewopened = true;
                        extraView.setVisibility(View.VISIBLE);
                        anim.start();
                        fab.hide();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fab.setImageResource(R.mipmap.ic_done_white_36dp);
                                fab.show();
                            }
                        }, 300);

                    } else {
                        int finalRadius = 0;
                        anim = ViewAnimationUtils.createCircularReveal(extraView,
                                cx, cY, extraView.getHeight() + 1000, finalRadius);
                        anim.setDuration(500).setInterpolator(new DecelerateInterpolator(1));
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                extraView.setVisibility(View.INVISIBLE);
                            }
                        });
                        anim.start();
                        fab.hide();
                        isViewopened = false;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fab.setImageResource(R.mipmap.ic_add_white_36dp);
                                fab.show();
                            }
                        }, 300);
                    }
                }
            }

        });


        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fullAttendance();
                }
                return true;
            }
        });


        attendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "asdadjkjhasdhsiu", Toast.LENGTH_SHORT).show();
                markedAtt();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void fullAttendance() {

        final View fullAttView = findViewById(R.id.full_att_layout);
        Animator anim = null;

        int cX = fullAttView.getWidth();
        int cY = 0;

        int finalRadius = Math.max(fullAttView.getWidth(), fullAttView.getHeight() + 1000);

        anim = ViewAnimationUtils.createCircularReveal(fullAttView, cX, cY, 0, finalRadius);
        anim.setDuration(500).setInterpolator(new DecelerateInterpolator(1));
        fullAttView.setVisibility(View.VISIBLE);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                params.setAnchorId(View.NO_ID);
                fab.setLayoutParams(params);
                fab.setVisibility(View.GONE);
            }
        });
        anim.start();

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void markedAtt() {
        final View fullAttView = findViewById(R.id.full_att_layout);

        Animator anim = null;

        int cX = fullAttView.getWidth();
        int cY = 0;

        int maxRadius = 0;

        anim = ViewAnimationUtils.createCircularReveal(fullAttView, cX, cY, fullAttView.getWidth(), maxRadius);
        anim.setDuration(500).setInterpolator(new DecelerateInterpolator(1));
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fullAttView.setVisibility(View.GONE);
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                layoutParams.setAnchorId(R.id.appbar_layout);
                fab.setLayoutParams(layoutParams);
                fab.show();
            }
        });
        anim.start();
    }


    private class CustomOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    indicator.setViewPager(pager, 0);
                    indicator.setFillColor(getResources().getColor(android.R.color.white));
                    break;
                case 1:
                    indicator.setViewPager(pager, 1);
                    indicator.setFillColor(getResources().getColor(android.R.color.white));
                    break;
                default:
                    break;
            }
            super.onPageSelected(position);
        }
    }


//    private class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> {
//        @Override
//        public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_main_row, parent, false);
//            return new ExampleViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(ExampleViewHolder holder, int position) {
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return 6;
//        }
//
//        class ExampleViewHolder extends RecyclerView.ViewHolder {
//            public ExampleViewHolder(View itemView) {
//                super(itemView);
//            }
//        }
//    }
}
