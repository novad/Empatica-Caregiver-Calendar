package nova.daniel.empatica.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;

/**
 * Adapter for a RecyclerView that contains a list of appointments.
 * Displays information of the {@link Caregiver} associated with each appointment (name and picture) and the respective room.
 * The colour for each appointment slot depends on the selected room as specified in the @colors resource.
 * Setup in {@link HoursViewAdapter} for each hour slot, and rendered in {@link nova.daniel.empatica.ui.MainActivity}.
 */
public class AppointmentViewAdapter extends RecyclerView.Adapter<AppointmentViewAdapter.AppointmentViewHolder> {

    private Context mContext;
    private List<Appointment> mAppointmentsList;

    /**
     * Interface listener for clicking on single appointment slots.
     */
    public interface SlotClickListener{
        void onSlotClick(int position, long date);
    }

    private AppointmentViewAdapter.SlotClickListener mSlotListener;

    /**
     * Constructor for the adapter.
     *
     * @param context   Context.
     * @param arrayList List of appointments.
     * @param listener  Listener for click events of appointment views.
     */
    AppointmentViewAdapter(Context context, List<Appointment> arrayList, SlotClickListener listener) {
        this.mContext = context;
        this.mAppointmentsList = arrayList;
        this.mSlotListener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hoursview_slot, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.mPosition = position;
        int roomNumber = mAppointmentsList.get(position).mRoom;

        Caregiver caregiver = mAppointmentsList.get(position).mCaregiver;

        holder.mRoomTextView.setText(String.format(Locale.getDefault(), "%d", roomNumber));
        holder.mCarerTextView.setText(caregiver.getName());

        // Glide call to fetch and display the caregivers picture.
        GlideApp.with(mContext)
                .load(caregiver.mPictureURL)
                .placeholder(R.drawable.ic_person_outline_white_24dp)
                .centerCrop()
                .transform(new RoundedCornersTransformation(45, 2))
                .into(holder.mCarerImageView);

        //Setting the background color depending on the room number
        Resources res = mContext.getResources();
        TypedArray colors = res.obtainTypedArray(R.array.colors);
        holder.mContainer.setBackgroundColor(colors.getColor(roomNumber, 0));
        colors.recycle();
    }

    @Override
    public int getItemCount() {
        if (mAppointmentsList == null)
            return 0;
        else
            return mAppointmentsList.size();
    }

    /**
     * The ViewHolder for each appointment consists of text views for the room number, and caregiver name.
     * Also consists of an image view to display the caregiver's picture.
     */
    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private int mPosition;
        private View mContainer;  //Container view
        private TextView mRoomTextView;
        private TextView mCarerTextView;
        private ImageView mCarerImageView;

        AppointmentViewHolder(View view) {
            super(view);
            mRoomTextView = view.findViewById(R.id.room_TextView);
            mCarerTextView = view.findViewById(R.id.carerName_TextView);
            mCarerImageView = view.findViewById(R.id.slot_ImageView);
            mContainer = view.findViewById(R.id.slotContainer_layout);

            // Set the container view to respond to clicks, send the appointment ID and its date
            view.setOnClickListener(v -> mSlotListener.onSlotClick(
                    mAppointmentsList.get(mPosition).appointmentId,
                    mAppointmentsList.get(mPosition).mDate.getTime()));
        }
    }
}
