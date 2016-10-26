/**
 * Android Simple ORM project.
 *
 * Copyright 2016 Arno Zhang <zyfgood12@163.com>
 *
 * Date 2015/08/19
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

package com.jungle.simpleorm.supporter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.jungle.simpleorm.BaseEntity;

import java.util.List;

public class MockORMSupporter implements ORMSupporter {

    @Override
    public void attachDatabase(SQLiteDatabase db) {
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean beginTransaction() {
        return false;
    }

    @Override
    public boolean endTransaction() {
        return false;
    }

    @Override
    public boolean setTransactionSuccessful() {
        return false;
    }

    @Override
    public boolean createTable(Class<? extends BaseEntity> clazz) {
        return false;
    }

    @Override
    public boolean createTable(BaseEntity entity) {
        return false;
    }

    @Override
    public boolean drop(Class<? extends BaseEntity> clazz) {
        return false;
    }

    @Override
    public boolean drop(String tableName) {
        return false;
    }

    @Override
    public boolean execSQL(String sql) {
        return false;
    }

    @Override
    public boolean execSQL(String sql, Object[] args) {
        return false;
    }

    @Override
    public Cursor rawQuery(String sql) {
        return null;
    }

    @Override
    public double querySum(Class<? extends BaseEntity> clazz,
            String fieldName, String condition) {
        return 0;
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz) {
        return null;
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz,
            String condition, String constraint) {
        return null;
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(
            BaseEntity entity, Class<T> primaryKeyClazz) {
        return null;
    }

    @Override
    public int queryCount(Class<? extends BaseEntity> clazz) {
        return 0;
    }

    @Override
    public int queryCount(Class<? extends BaseEntity> clazz, String condition) {
        return 0;
    }

    @Override
    public int queryCount(String tableName) {
        return 0;
    }

    @Override
    public int queryCount(String tableName, String condition) {
        return 0;
    }

    @Override
    public boolean updateFields(String tableName, String updateSql) {
        return false;
    }

    @Override
    public boolean updateFields(Class<? extends BaseEntity> clazz, String updateSql) {
        return false;
    }

    @Override
    public <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, int position) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz) {
        return null;
    }

    @Override
    public <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, Object primary) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> queryByCondition(
            Class<? extends BaseEntity> clazz, String condition, String constraint) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int count) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int index, int count) {
        return null;
    }

    @Override
    public <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, String tableName, int position) {
        return null;
    }

    @Override
    public <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, String tableName, Object primary) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> queryByConditionWithTableName(
            Class<? extends BaseEntity> clazz, String tableName, String condition, String constraint) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int count) {
        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int index, int count) {
        return null;
    }

    @Override
    public boolean remove(BaseEntity entity) {
        return false;
    }

    @Override
    public boolean remove(Class<? extends BaseEntity> clazz, String condition) {
        return false;
    }

    @Override
    public boolean remove(String tableName, String condition) {
        return false;
    }

    @Override
    public boolean removeAll(Class<? extends BaseEntity> clazz) {
        return false;
    }

    @Override
    public boolean removeAll(String tableName) {
        return false;
    }

    @Override
    public boolean removeByPrimaryKey(Class<? extends BaseEntity> clazz, Object primary) {
        return false;
    }

    @Override
    public boolean insertNew(BaseEntity entity) {
        return false;
    }

    @Override
    public boolean replace(BaseEntity entity) {
        return false;
    }

    @Override
    public boolean update(BaseEntity entity) {
        return false;
    }

    @Override
    public boolean update(BaseEntity entity, String condition) {
        return false;
    }
}
