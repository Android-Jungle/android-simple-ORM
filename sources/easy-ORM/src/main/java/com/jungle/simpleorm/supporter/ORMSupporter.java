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

public interface ORMSupporter {

    void attachDatabase(SQLiteDatabase db);

    boolean isClosed();

    void close();


    //
    // transaction
    //
    boolean beginTransaction();

    boolean endTransaction();

    boolean setTransactionSuccessful();


    //
    // createTable
    //
    boolean createTable(Class<? extends BaseEntity> clazz);

    boolean createTable(BaseEntity entity);


    //
    // dropTable
    //
    boolean drop(Class<? extends BaseEntity> clazz);

    boolean drop(String tableName);

    //
    // exec SQL statement.
    //
    boolean execSQL(String sql);

    boolean execSQL(String sql, Object[] args);

    //
    // query PRIMARY KEY list
    //
    <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz);

    <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz,
            String condition, String constraint);

    <T> List<T> queryPrimaryKeyList(
            BaseEntity entity, Class<T> primaryKeyClazz);

    //
    // raw query
    //
    Cursor rawQuery(String sql);

    //
    // query sum
    //
    double querySum(Class<? extends BaseEntity> clazz, String fieldName, String condition);

    //
    // queryCount
    //
    int queryCount(Class<? extends BaseEntity> clazz);

    int queryCount(Class<? extends BaseEntity> clazz, String condition);

    int queryCount(String tableName);

    int queryCount(String tableName, String condition);


    //
    // updateFields
    //
    boolean updateFields(String tableName, String updateSql);

    boolean updateFields(Class<? extends BaseEntity> clazz, String updateSql);


    //
    // queryByEntity
    //
    <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, int position);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz);

    <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, Object primary);

    <T extends BaseEntity> List<T> queryByCondition(
            Class<? extends BaseEntity> clazz, String condition, String constraint);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int count);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int index, int count);


    //
    // query by table name
    //
    <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, String tableName, int position);

    <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, String tableName, Object primary);

    <T extends BaseEntity> List<T> queryByConditionWithTableName(
            Class<? extends BaseEntity> clazz, String tableName, String condition, String constraint);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int count);

    <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int index, int count);


    //
    // remove
    //
    boolean remove(BaseEntity entity);

    boolean remove(Class<? extends BaseEntity> clazz, String condition);

    boolean remove(String tableName, String condition);

    boolean removeAll(Class<? extends BaseEntity> clazz);

    boolean removeAll(String tableName);

    boolean removeByPrimaryKey(Class<? extends BaseEntity> clazz, Object primary);


    //
    // insertNew & replace & update
    //
    boolean insertNew(BaseEntity entity);

    boolean replace(BaseEntity entity);

    boolean update(BaseEntity entity);

    boolean update(BaseEntity entity, String condition);
}
