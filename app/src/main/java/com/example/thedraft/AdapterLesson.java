package com.example.thedraft;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class AdapterLesson extends ArrayAdapter<LessonModel> {
    private Context context;
    private int resourceLayout;
    private static LayoutInflater inflater = null;

    public AdapterLesson(Context context, int resource, ArrayList<LessonModel> lessons) {
        super(context, resource, lessons);

        this.context = context;
        resourceLayout = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(resourceLayout, null);
        }

        LessonModel lesson = getItem(position);

        TextView lessonName = v.findViewById(R.id.lessonName);
        TextView lessonCreator = v.findViewById(R.id.lessonCreator);

        lessonName.setText(lesson.getName());
        lessonCreator.setText("whatever");

        return v;
    }
}
