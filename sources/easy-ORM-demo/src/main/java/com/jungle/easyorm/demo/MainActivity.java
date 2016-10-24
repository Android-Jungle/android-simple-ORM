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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.jungle.easyorm.demo.app.DemoApplication;
import com.jungle.easyorm.demo.data.Student;
import com.jungle.easyorm.supporter.ORMSupporter;

public class MainActivity extends AppCompatActivity {

    private EditText mIdView;
    private EditText mNameView;
    private EditText mAgeView;
    private EditText mScoreView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mIdView = (EditText) findViewById(R.id.student_id);
        mNameView = (EditText) findViewById(R.id.student_name);
        mAgeView = (EditText) findViewById(R.id.student_age);
        mScoreView = (EditText) findViewById(R.id.student_score);

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewStudent();
            }
        });

        findViewById(R.id.show_students_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentsListActivity.start(MainActivity.this);
            }
        });
    }

    private void addNewStudent() {
        Student student = new Student();
        student.mId = mIdView.getText().toString();
        student.mName = mNameView.getText().toString();
        try {
            student.mAge = Integer.parseInt(mAgeView.getText().toString());
            student.mScore = Float.parseFloat(mScoreView.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        ORMSupporter supporter = DemoApplication.getApp().getORMSupporter();
        if (supporter.replace(student)) {
            showToast("Add student successfully!");
            clearInput();
        } else {
            showToast("Add student FAILED!");
        }
    }

    private void clearInput() {
        mIdView.setText(null);
        mNameView.setText(null);
        mAgeView.setText(null);
        mScoreView.setText(null);

        mIdView.requestFocus();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
