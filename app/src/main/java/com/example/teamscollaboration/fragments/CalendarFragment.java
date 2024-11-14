package com.example.teamscollaboration.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
import com.example.teamscollaboration.WorkSpacesList;
import com.example.teamscollaboration.databinding.FragmentCalendarBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class CalendarFragment extends Fragment {
    FragmentCalendarBinding binding;
    MaterialCalendarView calendarView;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<WorkSpaceModel> workSpaceModelList = new ArrayList<>();
    List<TasksModel> tasksModelList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarView = binding.calendarView;
        retrieveWorkSpaces();
    }

    private void highlightTaskDeadlines() {
        Map<CalendarDay, List<String>> dateDetailsMap = getTaskDeadlines(); // Fetch your task deadlines from the database
        // Setup listener for date selection
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                List<String> details = dateDetailsMap.get(date); // Get details list for the selected date
                if (details != null && !details.isEmpty()) {
                    DateDetailsBottomSheet dialog = DateDetailsBottomSheet.newInstance(details);
                    dialog.show(getParentFragmentManager(), "DateDetailsBottomSheet");
                }else{
                    List<String> detail = new ArrayList<>();
                    detail.add("No Event Scheduled");
                    DateDetailsBottomSheet dialog = DateDetailsBottomSheet.newInstance(detail);
                    dialog.show(getParentFragmentManager(), "DateDetailsBottomSheet");
                }
            }
        });
        List<CalendarDay> taskDeadlines = new ArrayList<>(dateDetailsMap.keySet());
        // Mark deadlines on the calendar
        if(!taskDeadlines.isEmpty()){
            List<DayViewDecorator> decorators = new ArrayList<>();
            for (CalendarDay deadline : taskDeadlines) {
                decorators.add(new EventDecorator(deadline));
            }

            for (DayViewDecorator decorator : decorators) {
                calendarView.addDecorator(decorator);
            }
        }

    }

    // Example method that returns a list of task deadlines as CalendarDay objects
    private Map<CalendarDay, List<String>> getTaskDeadlines() {
        Map<CalendarDay, List<String>> dateDetailsMap  = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Log.d("calendarCheck", "getTaskDeadlines: getTaskDeadline Method called");
        if (!tasksModelList.isEmpty()) {
            for (TasksModel tasksModel : tasksModelList) {
                Log.d("calendarCheck", "getTaskDeadlines: " + tasksModel.toString());
                try {
                    // Get the deadline date string from the model
                    String deadlineStr = tasksModel.getDeadLine();

                    // Parse the date string into a Date object
                    Date date = dateFormat.parse(deadlineStr);

                    // Use Calendar to extract day, month, and year
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    // Create CalendarDay object
                    CalendarDay calendarDay = CalendarDay.from(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,  // Calendar.MONTH is zero-based, so add 1
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

                    // Check if the date already exists in the map
                    if (!dateDetailsMap.containsKey(calendarDay)) {
                        // If not, create a new list for this date
                        dateDetailsMap.put(calendarDay, new ArrayList<>());
                    }

                    // Add the task name to the list for this date
                    dateDetailsMap.get(calendarDay).add(tasksModel.getTaskName());

                } catch (ParseException e) {
                    e.printStackTrace(); // Handle parse exception if needed
                }
            }
        }
        return dateDetailsMap;
    }

    // Custom decorator to highlight the deadline dates with modern styling
    class EventDecorator implements DayViewDecorator {
        private CalendarDay date;
        private Drawable highlightDrawable;

        public EventDecorator(CalendarDay date) {
            this.date = date;
            this.highlightDrawable = ContextCompat.getDrawable(calendarView.getContext(), R.drawable.circle_background);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, Color.WHITE));
            view.addSpan(new ForegroundColorSpan(Color.WHITE));  // Text color for contrast
            view.addSpan(new StyleSpan(Typeface.BOLD)); // Bold style for highlighted dates
            view.setBackgroundDrawable(highlightDrawable);
            view.addSpan(new RelativeSizeSpan(1.0f));
        }
    }


    private void retrieveWorkSpaces() {
        databaseReference.child("Workspaces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int workspaceCount = (int) snapshot.getChildrenCount(); // Get total count of workspaces
                    AtomicInteger completedCount = new AtomicInteger(0); // Counter for completed listeners
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String workSpaceKey = dataSnapshot.getKey();
                        Log.d("calendarCheck", "retrieveTasks: " + workSpaceKey);
                        databaseReference.child("Workspaces").child(workSpaceKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        WorkSpaceModel workSpaceModel = snapshot.getValue(WorkSpaceModel.class);
                                        Log.d("calendarCheck", "retrieveTasks: " + workSpaceModel.toString());
                                        if (workSpaceModel.getAdminId().equals(auth.getCurrentUser().getUid())) {
                                            workSpaceModelList.add(workSpaceModel);
                                        }
                                        List<MembersModel> membersModelList = workSpaceModel.getMembersList();
                                        for (MembersModel membersModel : membersModelList) {
                                            if (membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                                workSpaceModelList.add(workSpaceModel);
                                            }
                                        }
                                        // Increment completed count and check if all listeners have finished
                                        if (completedCount.incrementAndGet() == workspaceCount) {
                                            retrieveTasks(); // Call retrieveTasks only after all listeners are complete
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("errorCheck", "onCancelled: " + error.getDetails());
                                    }
                                });
                    }
                } else {
                    retrieveTasks();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("workSpaces", "onCancelled: error in retrieving workspaces");
            }
        });
    }

    private void retrieveTasks() {
        for (WorkSpaceModel workSpaceModel : workSpaceModelList) {
            Log.d("calendarCheck", "retrieveTasks: " + workSpaceModel.toString());
            databaseReference.child("Tasks").child(workSpaceModel.getWorkSpaceKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String taskKey = dataSnapshot.getKey();
                            Log.d("calendarCheck", "onDataChange: " + taskKey);
                            databaseReference.child("Tasks").child(workSpaceModel.getWorkSpaceKey()).child(taskKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                TasksModel tasksModel = snapshot.getValue(TasksModel.class);
                                                Log.d("calendarCheck", "onDataChange: " + tasksModel.toString());
                                                List<MembersModel> membersModels = tasksModel.getMembersList();
                                                for (MembersModel membersModel : membersModels) {
                                                    if (membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                                        tasksModelList.add(tasksModel);
                                                        Log.d("calendarCheck", "onDataChange: task model added " + tasksModel.toString());
                                                        break;
                                                    }
                                                }
                                            }
                                            // After loading tasks for this workspace, check if all workspaces are processed
                                            if (workSpaceModel == workSpaceModelList.get(workSpaceModelList.size() - 1)) {
                                                Log.d("calendarCheck", "onDataChange: highlightTaskDeadlines method called");
                                                highlightTaskDeadlines();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d("calendarCheck", "onCancelled: " + error.getDetails());
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("calendarCheck", "onCancelled: " + error.getDetails());
                }
            });
        }
    }
}