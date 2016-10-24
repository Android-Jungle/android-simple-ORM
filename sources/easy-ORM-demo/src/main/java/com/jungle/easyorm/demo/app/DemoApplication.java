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

package com.jungle.easyorm.demo.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import com.jungle.easyorm.demo.data.Student;
import com.jungle.easyorm.supporter.ORMSupporter;
import com.jungle.easyorm.supporter.SQLiteORMSupporter;
import com.jungle.easyorm.supporter.SimpleORMDatabaseListener;

import java.io.File;
import java.io.IOException;

public class DemoApplication extends Application {

    private static DemoApplication mApp;
    private ORMSupporter mORMSupporter;


    public static DemoApplication getApp() {
        return mApp;
    }

    public ORMSupporter getORMSupporter() {
        return mORMSupporter;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mORMSupporter = new SQLiteORMSupporter(this, getDatabaseFilePath(), 1, mORMListener);
    }

    private SimpleORMDatabaseListener mORMListener = new SimpleORMDatabaseListener() {
        @Override
        public void onCreated(ORMSupporter supporter, SQLiteDatabase db) {
            super.onCreated(supporter, db);

            supporter.createTable(Student.class);
        }

        @Override
        public void onUpgrade(ORMSupporter supporter, SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onUpgrade(supporter, db, oldVersion, newVersion);
        }
    };

    private String getDatabaseFilePath() {
        String filePath = getFilesDir().getPath() + "/databases/";
        new File(filePath).mkdirs();

        filePath += "demo-orm.db";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }
}
