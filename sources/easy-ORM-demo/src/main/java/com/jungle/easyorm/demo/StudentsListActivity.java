/**
 * Android Easy ORM Demo project.
 *
 * Copyright 2016 Arno Zhang <zyfgood12@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jungle.easyorm.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.jungle.easyorm.demo.app.DemoApplication;
import com.jungle.easyorm.demo.data.Student;
import com.jungle.easyorm.supporter.ORMSupporter;

import java.util.Iterator;
import java.util.List;

public class StudentsListActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, StudentsListActivity.class);
        context.startActivity(intent);
    }


    private RecyclerView mRecyclerView;
    private List<Student> mStudentList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.student_list_title);
        setContentView(R.layout.activity_student_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        loadStudentList();
    }

    private void loadStudentList() {
        ORMSupporter supporter = DemoApplication.getApp().getORMSupporter();
        mStudentList = supporter.query(Student.class);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteStudent(String id) {
        ORMSupporter supporter = DemoApplication.getApp().getORMSupporter();
        if (!supporter.removeByPrimaryKey(Student.class, id)) {
            showToast("Delete student FAILED!");
            return;
        }

        showToast("Delete student successfully!");

        for (Iterator<Student> iterator = mStudentList.iterator(); iterator.hasNext(); ) {
            Student student = iterator.next();
            if (TextUtils.equals(student.mId, id)) {
                iterator.remove();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(final View itemView) {
            super(itemView);

            itemView.findViewById(R.id.delete_student).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String id = (String) itemView.getTag();
                            deleteStudent(id);
                        }
                    });
        }

        public void update(Student student) {
            TextView idView = (TextView) itemView.findViewById(R.id.student_id);
            TextView nameView = (TextView) itemView.findViewById(R.id.student_name);
            TextView ageView = (TextView) itemView.findViewById(R.id.student_age);
            TextView scoreView = (TextView) itemView.findViewById(R.id.student_score);

            idView.setText(student.mId);
            nameView.setText(student.mName);
            ageView.setText(String.valueOf(student.mAge));
            scoreView.setText(String.valueOf(student.mScore));

            itemView.setTag(student.mId);
        }
    }

    private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter<ItemHolder>() {
        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.layout_student_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.update(mStudentList.get(position));
        }

        @Override
        public int getItemCount() {
            return mStudentList != null ? mStudentList.size() : 0;
        }
    };
}
