package nova.daniel.empatica.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.model.HourSlotModel;

/**
 * Adapter for the RecyclerView used in {@link nova.daniel.empatica.ui.MainActivity}.
 * <p>
 * This adapter deals with the hours for each day, so its main purpose is to contain the 24 available
 * slots per day.
 * It contains the elements of {@link HourSlotModel} which specify the appointments for each hour
 * of the day
 * While mAppointmentAdapter ({@link AppointmentViewAdapter} is the adapter for the appointments
 * for each hour of the day.
 */
public class HoursViewAdapter extends RecyclerView.Adapter<HoursViewAdapter.HourViewHolder> {

    private Context mContext;
    private List<HourSlotModel> mHourSlotModelList;
    private AppointmentViewAdapter mAppointmentAdapter;

    /**
     * Interface to respond to clicks to add/edit slots
     */
    public interface NewAppointmentClickListener {
        void onNewSlotClick(int hour);
    }

    private NewAppointmentClickListener mNewSlotListener;
    private AppointmentViewAdapter.SlotClickListener mSlotClickListener;

    /**
     * Constructor of the hours view adapter.
     *
     * @param mContext               Context
     * @param hourSlotModelList      List of {@link HourSlotModel} containing the appointments per hour for a specific date.
     * @param newAppointmentListener Listener for creating new appointments.
     * @param appointmentListener    Listener for responding to existing appointment selection.
     */
    public HoursViewAdapter(Context mContext, List<HourSlotModel> hourSlotModelList,
                            NewAppointmentClickListener newAppointmentListener, AppointmentViewAdapter.SlotClickListener appointmentListener) {
        this.mContext = mContext;
        this.mHourSlotModelList = hourSlotModelList;
        this.mNewSlotListener = newAppointmentListener;  // new appointment newAppointmentListener
        this.mSlotClickListener = appointmentListener;  // edit appointment newAppointmentListener
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hoursview_row, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        final HourSlotModel hourSlotModel = mHourSlotModelList.get(position);
        holder.mHourLabel.setText(hourSlotModel.getHourLabel());  // set each hour label

        // Setup the adapter for the appointments for each HourView row
        holder.mRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(linearLayoutManager);

        mAppointmentAdapter = new AppointmentViewAdapter(mContext, hourSlotModel.getItemArrayList(), mSlotClickListener);
        holder.mRecyclerView.setAdapter(mAppointmentAdapter);
    }

    @Override
    public int getItemCount() {
        return mHourSlotModelList.size();
    }

    /**
     * Notifies the adapters that the HourSlotModels have changed.
     *
     * @param model New model
     */
    public void notifyModelChanged(List<HourSlotModel> model){
        this.mHourSlotModelList = model;
        notifyDataSetChanged();
        mAppointmentAdapter.notifyDataSetChanged();
    }

    /**
     * The ViewHolder for each view consists of the hour label mHourLabel,
     * a view used for adding new appointments mAddSlotButton,
     * and finally, a RecyclerView containing the list of appointments for the given hour slot.
     */
    class HourViewHolder extends RecyclerView.ViewHolder {
        private TextView mHourLabel;
        private TextView mAddSlotButton;
        private RecyclerView mRecyclerView;

        HourViewHolder(View view) {
            super(view);
            mHourLabel = view.findViewById(R.id.hour_textView);
            mAddSlotButton = view.findViewById(R.id.addSlot_button);
            mRecyclerView = view.findViewById(R.id.hour_recyclerView);

            // Empty spaces in the recycler view can be clicked to add new appointments.
            View emptyRecyclerView = view.findViewById(R.id.recycler_view_container);
            emptyRecyclerView.setOnClickListener(v -> onViewClick());

            mAddSlotButton.setOnClickListener(v -> onViewClick());
        }

        void onViewClick(){
            int _hour = Integer.valueOf(mHourLabel.getText().toString().split(":")[0]);
            mNewSlotListener.onNewSlotClick(_hour);
        }

    }


}