package nova.daniel.empatica.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;

public class SlotViewAdapter extends RecyclerView.Adapter<SlotViewAdapter.SlotViewHolder> {

    private Context mContext;
    private List<Appointment> mArrayList;

    public interface SlotClickListener{
        void onSlotClick(int position);
    }
    private SlotViewAdapter.SlotClickListener mSlotLstener;


    public SlotViewAdapter(Context context, List<Appointment> arrayList, SlotClickListener listener) {
        this.mContext = context;
        this.mArrayList = arrayList;
        this.mSlotLstener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hoursview_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        int roomNumber = mArrayList.get(position).mRoom;

        Caregiver caregiver = mArrayList.get(position).getCaregiver();

        holder.mPosition = position;
        holder.mRoomTextView.setText(String.format(Locale.getDefault(), "%d", roomNumber));
        holder.mCarerTextView.setText(caregiver.getName());

        GlideApp.with(mContext)
                .load(caregiver.mPictureURL)
                .placeholder(R.drawable.ic_person_outline_white_24dp)
                .centerCrop()
                .transform(new RoundedCornersTransformation(45, 2))
                .into(holder.mCarerImageView);

        //Setting the background color depending on the room
        Resources res = mContext.getResources();
        TypedArray colors = res.obtainTypedArray(R.array.colors);
        holder.mContainer.setBackgroundColor(colors.getColor(roomNumber, 0));
        colors.recycle();
    }

    @Override
    public int getItemCount() {
        if(mArrayList == null)
            return 0;
        else
            return mArrayList.size();
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        private int mPosition;
        private View mContainer;
        private TextView mRoomTextView;
        private TextView mCarerTextView;
        private ImageView mCarerImageView;

        public SlotViewHolder(View view) {
            super(view);
            mRoomTextView = view.findViewById(R.id.room_TextView);
            mCarerTextView = view.findViewById(R.id.carerName_TextView);
            mCarerImageView = view.findViewById(R.id.slot_ImageView);
            mContainer = view.findViewById(R.id.slotContainer_layout);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSlotLstener.onSlotClick(mPosition);
                }
            });
        }
    }

}
