package com.example.teamscollaboration.fragments;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.example.teamscollaboration.Models.MembersModel;
import com.example.teamscollaboration.Models.TasksModel;
import com.example.teamscollaboration.Models.WorkSpaceModel;
import com.example.teamscollaboration.R;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class CalendarFragment extends Fragment {
    FragmentCalendarBinding binding;
    MaterialCalendarView calendarView;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    List<WorkSpaceModel> workSpaceModelList = new ArrayList<>();
    List<TasksModel> tasksModelList = new ArrayList<>();
    CalendarDay previousSelectedDate;

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
        binding.lottieAnimationView.setAnimation(R.raw.calendaranimation);
        binding.lottieAnimationView.addValueCallback(
                new KeyPath("checkmark", "**"),
                LottieProperty.COLOR_FILTER,
                new SimpleLottieValueCallback<ColorFilter>() {
                    @Override
                    public ColorFilter getValue(LottieFrameInfo<ColorFilter> frameInfo) {
                        return new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
                    }
                }
        );
        binding.calendarView.setLeftArrow(R.drawable.back);
        binding.calendarView.setRightArrow(R.drawable.next);
        retrieveWorkSpaces();
    }

    private void highlightTaskDeadlines() {
        Map<CalendarDay, List<String>> dateDetailsMap = getTaskDeadlines();// Fetch your task deadlines from the database
        List<CalendarDay> taskDeadlines = new ArrayList<>(dateDetailsMap.keySet());
        //Log.d("calendarCheck", "highlightTaskDeadlines: " + taskDeadlines.get(0));
        // Mark deadlines on the calendar
        if (!taskDeadlines.isEmpty()) {
            List<DayViewDecorator> decorators = new ArrayList<>();
            for (CalendarDay deadline : taskDeadlines) {
                decorators.add(new EventDecorator(deadline, dateDetailsMap.get(deadline), false));
            }

            for (DayViewDecorator decorator : decorators) {
                calendarView.addDecorator(decorator);
            }
        }
        // Setup listener for date selection
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                List<String> details = dateDetailsMap.get(date); // Get details list for the selected date
                if (details != null && !details.isEmpty()) {
                    DateDetailsBottomSheet dialog = DateDetailsBottomSheet.newInstance(details);
                    dialog.show(getParentFragmentManager(), "DateDetailsBottomSheet");
                } else {
                    List<String> detail = new ArrayList<>();
                    detail.add("No Event Scheduled");
                    DateDetailsBottomSheet dialog = DateDetailsBottomSheet.newInstance(detail);
                    dialog.show(getParentFragmentManager(), "DateDetailsBottomSheet");
                }
                boolean isMatched = false;
                for (CalendarDay calendarDay : taskDeadlines) {
                    if (calendarDay.equals(date)) {
                        isMatched = true;
                        break;
                    }
                }
                if (previousSelectedDate != null && !previousSelectedDate.equals(date)) {
                    widget.addDecorator(new EventDecorator(previousSelectedDate, new ArrayList<>(), false));
                }
                if (!isMatched) {
                    widget.addDecorator(new EventDecorator(date, new ArrayList<>(), true));
                    previousSelectedDate = date;
                }
            }
        });
    }

    // Example method that returns a list of task deadlines as CalendarDay objects
    private Map<CalendarDay, List<String>> getTaskDeadlines() {
        Map<CalendarDay, List<String>> dateDetailsMap = new HashMap<>();
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
        List<String> dateDetails;
        Boolean isSelected;

        public EventDecorator(CalendarDay date, List<String> dateDetails, Boolean isSelected) {
            this.date = date;
            this.dateDetails = dateDetails;
            this.isSelected = isSelected;
            this.highlightDrawable = ContextCompat.getDrawable(calendarView.getContext(), R.drawable.circle_background);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            if (isSelected) {
                if (dateDetails.isEmpty()) {
                    view.addSpan(new ForegroundColorSpan(Color.WHITE));
                }
            } else {
                if (dateDetails.size() > 1) {
                    view.addSpan(new DotSpan(10, Color.RED));
                }
                if (!dateDetails.isEmpty()) {
                    view.addSpan(new ForegroundColorSpan(Color.WHITE));  // Text color for contrast
                    view.addSpan(new StyleSpan(Typeface.BOLD)); // Bold style for highlighted dates
                    view.setBackgroundDrawable(highlightDrawable);
                    view.addSpan(new RelativeSizeSpan(1.0f));
                }
                if (dateDetails.isEmpty()) {
                    view.addSpan(new ForegroundColorSpan(Color.BLACK));
                }
            }

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
                        Log.d("calendarCheck", "retrieveWorkspaces: " + workSpaceKey);
                        databaseReference.child("Workspaces").child(workSpaceKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        WorkSpaceModel workSpaceModel = snapshot.getValue(WorkSpaceModel.class);
                                        Log.d("calendarCheck", "retrieveWorkspaces: " + workSpaceModel.toString());
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

    /* private void retrieveTasks() {
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
                                                 // After loading tasks for this workspace, check if all workspaces are processed
                                                 if (workSpaceModel == workSpaceModelList.get(workSpaceModelList.size() - 1)) {
                                                     Log.d("calendarCheck", "onDataChange: highlightTaskDeadlines method called");
                                                     highlightTaskDeadlines();
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
                    else {
                         // After loading tasks for this workspace, check if all workspaces are processed
                         if (workSpaceModel == workSpaceModelList.get(workSpaceModelList.size() - 1)) {
                             Log.d("calendarCheck", "onDataChange: highlightTaskDeadlines method called");
                             highlightTaskDeadlines();
                         }
                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {
                     Log.d("calendarCheck", "onCancelled: " + error.getDetails());
                 }
             });
         }*/
    private void retrieveTasks() {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        WorkSpaceModel workSpaceModell = new WorkSpaceModel();

        for (WorkSpaceModel workSpaceModel : workSpaceModelList) {
            workSpaceModell = workSpaceModel;
            chain = chain.thenCompose(ignored -> processWorkspaceTasks(workSpaceModel));
        }

        WorkSpaceModel finalWorkSpaceModell = workSpaceModell;
        chain.thenRun(() -> {
            if (finalWorkSpaceModell == workSpaceModelList.get(workSpaceModelList.size() - 1)) {
                Log.d("calendarCheck", "onDataChange: highlightTaskDeadlines method called");
                highlightTaskDeadlines();
            }
           /* Log.d("calendarCheck", "All workspaces processed. Calling highlightTaskDeadlines.");
            highlightTaskDeadlines();*/
        }).exceptionally(error -> {
            Log.d("calendarCheck", "Error in processing tasks: " + error.getMessage());
            return null;
        });
    }

    private CompletableFuture<Void> processWorkspaceTasks(WorkSpaceModel workSpaceModel) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        databaseReference.child("Tasks").child(workSpaceModel.getWorkSpaceKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CompletableFuture<Void> innerChain = CompletableFuture.completedFuture(null);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String taskKey = dataSnapshot.getKey();

                        innerChain = innerChain.thenCompose(ignored -> processTask(workSpaceModel.getWorkSpaceKey(), taskKey));
                    }

                    innerChain.whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            Log.d("calendarCheck", "Error in inner task processing: " + throwable.getMessage());
                            future.completeExceptionally(throwable);
                        } else {
                            future.complete(null);
                        }
                    });
                } else {
                    Log.d("calendarCheck", "No tasks found for workspace: " + workSpaceModel.getWorkSpaceKey());
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("calendarCheck", "Workspace tasks retrieval cancelled: " + error.getDetails());
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return future;
    }

    private CompletableFuture<Void> processTask(String workspaceKey, String taskKey) {
        CompletableFuture<Void> taskFuture = new CompletableFuture<>();

        databaseReference.child("Tasks").child(workspaceKey).child(taskKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TasksModel tasksModel = snapshot.getValue(TasksModel.class);

                    if (tasksModel != null) {
                        Log.d("calendarCheck", "Processing task: " + tasksModel);

                        List<MembersModel> membersModels = tasksModel.getMembersList();
                        for (MembersModel membersModel : membersModels) {
                            if (membersModel.getuID().equals(auth.getCurrentUser().getUid())) {
                                tasksModelList.add(tasksModel);
                                Log.d("calendarCheck", "Task added: " + tasksModel);
                                break;
                            }
                        }
                    }
                }
                taskFuture.complete(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("calendarCheck", "Task retrieval cancelled: " + error.getDetails());
                taskFuture.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return taskFuture;
    }
}