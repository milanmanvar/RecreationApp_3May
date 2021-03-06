package com.recreation.recreationapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.model.ClubTimeTable_New;
import com.recreation.recreationapp.util.Utils;
import com.recreation.recreationapp.view.ClubTimeTableDetailActivity;

import java.util.ArrayList;

/**
 * Created by utsav.k on 28-03-2016.
 */
public class ClubDetailTimeTableListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ClubTimeTable_New> listItem;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private int tempPos = -1;
    private ReCreationApplication reCreationApplication;

    public ClubDetailTimeTableListAdapter(Context context,
                                          ArrayList<ClubTimeTable_New> items) {
        this.context = context;
        this.listItem = items;
        this.reCreationApplication = (ReCreationApplication) context.getApplicationContext();
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public ClubTimeTable_New getItem(int position) {
        return listItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_club_timetable_list, null);
            holder.txtName = (TextView) convertView
                    .findViewById(R.id.row_club_timetable_txtDay);
//            holder.imgInfo = (ImageView) convertView
//                    .findViewById(R.id.row_club_location_imgInfo);
//            holder.imgHour = (ImageView) convertView
//                    .findViewById(R.id.row_club_location_imgHour);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0)
            holder.txtName.setText("Today");
        else if (position == 1)
            holder.txtName.setText("Tomorrow");
        else
            holder.txtName.setText(listItem.get(position).getDay().toString().trim().substring(0, 1).toUpperCase() + listItem.get(position).getDay().toString().trim().substring(1));

        convertView.setTag(holder.txtName.getText().toString());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<ClubTimeTable_New> list = reCreationApplication.getDatabase().getClubTimeTableDetailFromNameAndDay(listItem.get(position).getClubName(), listItem.get(position).getDay());

                for(int i = 0; i < list.size(); i++){

                    ClubTimeTable_New data = list.get(i);
                    reCreationApplication.getDatabase().checkEntryExistOrNot(data.getClubName(),data.getClassName(),data.getInstructor(),data.getDuration(),data.getTime(),data.getDay(),data.getClassType(),data.getDesc()
                    ,data.getLocation(),data.getId());
                }


                Intent iClubTimeTableDetial = new Intent(context, ClubTimeTableDetailActivity.class);
                iClubTimeTableDetial.putExtra("timetable", listItem.get(position));
                iClubTimeTableDetial.putExtra("title",v.getTag().toString());
                iClubTimeTableDetial.putExtra("date", Utils.getDateFromCurrentDate(position));



                context.startActivity(iClubTimeTableDetial);
            }
        });

        return convertView;
    }


    public class ViewHolder {
        private TextView txtName;
        private ImageView imgInfo, imgHour;
    }

}
