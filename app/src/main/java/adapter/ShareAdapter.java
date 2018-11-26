package adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tinymonster.stepcount.R;
import com.tinymonster.stepcount.pace;

import java.util.List;

/**
 * Created by TinyMonster on 2018/5/18.
 */

public class ShareAdapter extends ArrayAdapter<pace>{
    private int resourceId;
    public ShareAdapter(Context context, int textViewResourceId, List<pace> objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pace tempace=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView item_name=(TextView)view.findViewById(R.id.item_name);
        TextView item_time=(TextView)view.findViewById(R.id.item_time);
        TextView item_pacenum=(TextView)view.findViewById(R.id.item_pacenum);
        item_name.setText(tempace.name);
        item_time.setText(tempace.time);
        item_pacenum.setText(tempace.pacenum);
        return view;
    }
}
