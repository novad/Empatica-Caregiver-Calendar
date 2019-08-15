package nova.daniel.empatica.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nova.daniel.empatica.model.HourSlotModel;
import nova.daniel.empatica.R;

public class HoursViewAdapter extends RecyclerView.Adapter<HoursViewAdapter.HourViewHolder> {

    private Context mContext;
    private List<HourSlotModel> mHourSlotModelList;
    private SlotViewAdapter mSlotAdapter;

    public interface NewSlotClickListener{
        void onNewSlotClick(int hour);
    }
    private NewSlotClickListener mNewSlotLstener;
    private SlotViewAdapter.SlotClickListener mSlotClickListener;


    public HoursViewAdapter(Context mContext, List<HourSlotModel> hourSlotModelList,
                            NewSlotClickListener listener, SlotViewAdapter.SlotClickListener slotListener) {
        this.mContext = mContext;
        this.mHourSlotModelList = hourSlotModelList;
        this.mNewSlotLstener = listener;
        this.mSlotClickListener = slotListener;
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
        holder.mHourLabel.setText(hourSlotModel.getHourLabel());

//        holder.mRecyclerView.setHasFixedSize(true);
        holder.mRecyclerView.setNestedScrollingEnabled(false);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(linearLayoutManager);

        mSlotAdapter = new SlotViewAdapter(mContext, hourSlotModel.getItemArrayList(), mSlotClickListener);
        holder.mRecyclerView.setAdapter(mSlotAdapter);

    }

    @Override
    public int getItemCount() {
        return mHourSlotModelList.size();
    }


    public void notifySlotChanged(List<HourSlotModel> model){
        this.mHourSlotModelList = model;
        notifyDataSetChanged();
        mSlotAdapter.notifyDataSetChanged();
    }

    class HourViewHolder extends RecyclerView.ViewHolder {
        private TextView mHourLabel;
        private TextView mAddSlotButton;
        private RecyclerView mRecyclerView;

        public HourViewHolder(View view) {
            super(view);
            mHourLabel = view.findViewById(R.id.hour_textView);
            mRecyclerView = view.findViewById(R.id.hour_recyclerView);
            mAddSlotButton = view.findViewById(R.id.addSlot_button);

            View emptyRecyclerView = view.findViewById(R.id.recycler_view_container);
            emptyRecyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick();
                }
            });

            mAddSlotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick();
                }
            });
        }
        public void onViewClick(){
            int _hour = Integer.valueOf(mHourLabel.getText().toString().split(":")[0]);
            mNewSlotLstener.onNewSlotClick(_hour);
        }

    }


}