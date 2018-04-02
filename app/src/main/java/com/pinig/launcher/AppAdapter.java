package com.pinig.launcher;

/**
 * Created by varun on 8/3/18.
 */

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pinig.pinigb2blauncher.R;

public class AppAdapter extends BaseAdapter {

    private Context myContext;
    private List<ResolveInfo> MyAppList;
    PackageManager myPackageManager;

    AppAdapter(Context c, List<ResolveInfo> appIntentList) {
        myContext = c;
        MyAppList = appIntentList;
        myPackageManager = c.getPackageManager();
    }

    @Override
    public int getCount() {
        return MyAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return MyAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ResolveInfo resolveInfo = MyAppList.get(position);
        if (convertView == null) {
            view = inflater.inflate(R.layout.grid_single,null);
            final TextView textView = (TextView)view.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView)view.findViewById(R.id.grid_image);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityInfo clickedActivityInfo = resolveInfo.activityInfo;
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setClassName(clickedActivityInfo.applicationInfo.packageName, clickedActivityInfo.name);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    myContext.startActivity(intent);
                }
            });
            textView.setText(resolveInfo.loadLabel(myPackageManager));
            imageView.setImageDrawable(resolveInfo.loadIcon(myPackageManager));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {view = convertView;}
        return view;

    }
}