package org.coeg.routine.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.coeg.routine.activities.MainActivity;
import org.coeg.routine.adapters.RecentRoutinesAdapter;
import org.coeg.routine.animations.AccuracyAnimation;
import org.coeg.routine.R;
import org.coeg.routine.backend.History;
import org.coeg.routine.backend.InternalStorage;
import org.coeg.routine.backend.PreferencesStorage;
import org.coeg.routine.backend.Routine;
import org.coeg.routine.backend.RoutinesHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

public class DashboardFragment extends Fragment
{
    private static final int ANIMATION_DURATION = 1500;

    private TextView            txtName;
    private TextView            txtAccuracy;
    private ShapeableImageView  imgUser;
    private ProgressBar         pbAccuracy;
    private RecyclerView        rvRecentRoutine;

    private String  name;
    private Bitmap  profilePicture;
    private Integer lateCount;
    private Integer onTimeCount;
    private Integer routineCount;
    private Integer historyCount;
    private float accuracy = 0;

    private View view;

    RecentRoutinesAdapter mAdapter;
    private static LinkedList<Routine> routineList = new LinkedList<>();
    private static LinkedList<History> historyList = new LinkedList<>();
    private RoutinesHandler handler;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public DashboardFragment() { }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        //Fetching data from database asynchronously
        new DBAsync().execute(this.getContext());
        FetchPreferences();
        InitView(view);
        routineListTest();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Defines the xml file for the fragment
        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        }
        // Defines the xml file for the fragment
        return view;
    }

    /**
     * Initialize variable by findViewByID
     * and setup view properties
     */
    private void InitView(View view)
    {
        txtName         = view.findViewById(R.id.txt_name);
        txtAccuracy     = view.findViewById(R.id.txt_userAccuracy);
        pbAccuracy      = view.findViewById(R.id.progress_accuracy);
        rvRecentRoutine = view.findViewById(R.id.rv_recentRoutine);
        imgUser         = view.findViewById(R.id.imgUser);

        // Set view data from database
        txtName.setText(name);
        String auahgelap = String.format("%f %%", accuracy);
        txtAccuracy.setText(auahgelap);
        imgUser.setImageBitmap(profilePicture);

        mAdapter = new RecentRoutinesAdapter(this.getContext(), routineList, historyList);
        rvRecentRoutine.setAdapter(mAdapter);
        rvRecentRoutine.setLayoutManager(new LinearLayoutManager(this.getContext()));

        //Play animation according
        PlayAnimation((int) accuracy);
    }

    /**
     * Play accuracy progress animation
     * @param accuracy user accuracy according to calculation from database routine data
     */
    private void PlayAnimation(int accuracy)
    {
        AccuracyAnimation pbAnim = new AccuracyAnimation(pbAccuracy, txtAccuracy, 0, accuracy);
        pbAnim.setDuration(ANIMATION_DURATION);
        pbAccuracy.startAnimation(pbAnim);
    }

    public void routineListTest()
    {
        //routineList.add(new Routine());
    }

    private void FetchPreferences()
    {
        PreferencesStorage preferences = PreferencesStorage.getInstance();
        InternalStorage internalStorage = new InternalStorage(getContext());
        preferences.loadPreferences(this.getContext());

        name = preferences.getFullName();
        profilePicture = internalStorage.GetImageFromInternalStorage();
        lateCount = preferences.getLateCounter();
        onTimeCount = preferences.getOnTimeCounter();

        historyCount = preferences.getHistoryCounter();

        if (historyCount != 0)
        {
            accuracy = ((float) onTimeCount / historyCount) * 100;
        }
        else
        {
            accuracy = 0;
        }
    }

    private class DBAsync extends AsyncTask<Context, Integer, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Context... context) {
            try{
                historyList = new LinkedList<>();
                routineList = new LinkedList<>();
                handler = new RoutinesHandler(context[0]);
                LinkedList<History> HistoryTemp = new LinkedList<>(handler.getAllHistory());
                LinkedList<Routine> RoutineTemp = new LinkedList<>(handler.getAllRoutines());
                Log.i(getClass().toString(), "History size = " + RoutineTemp.size());
                for(int i = 0; i < handler.getHistoryCount(); i++){
                    if(HistoryTemp.get(i).getDateAsString().equals(dateFormatter.format(Calendar.getInstance().getTime())) ){
                        historyList.add(HistoryTemp.get(i));
                    }
                }
                for(int i = 0; i < handler.getRoutineCount(); i++) {
                    routineList.add(RoutineTemp.get(i));
                }
                
                for (;;) {
                    if(mAdapter != null){ break; }
                }  

                mAdapter.notifyItemInserted(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }
}
