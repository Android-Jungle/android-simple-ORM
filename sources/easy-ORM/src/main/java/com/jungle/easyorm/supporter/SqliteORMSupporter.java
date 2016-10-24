/**
 * Android Easy ORM project.
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

package com.jungle.easyorm.supporter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.jungle.easyorm.BaseEntity;
import com.jungle.easyorm.constraint.AutoIncrement;
import com.jungle.easyorm.constraint.NotColumnField;
import com.jungle.easyorm.constraint.PrimaryKey;
import com.jungle.easyorm.utils.DBUtils;
import com.jungle.easyorm.utils.EntityUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLiteORMSupporter implements ORMSupporter {

    private boolean mIsClosed = false;
    private SQLiteOpenHelper mDBHelper;
    private SQLiteDatabase mDatabase;
    private Set<String> mTableCache = new HashSet<>();


    public SQLiteORMSupporter(
            Context context, String dbFilePath,
            int dbVersion, ORMDatabaseListener dbListener) {

        mDBHelper = new ORMDatabaseOpenHelper(
                context, dbFilePath, dbVersion, this, dbListener);

        attachDatabase(mDBHelper.getWritableDatabase());
        dbListener.onLoadComplete(this);
    }

    @Override
    public void attachDatabase(SQLiteDatabase db) {
        if (mDatabase == db) {
            return;
        }

        mDatabase = db;
        mIsClosed = mDatabase == null;
        openForeignKeySupport();
    }

    @Override
    public boolean isClosed() {
        return mIsClosed;
    }

    @Override
    public void close() {
        if (mIsClosed) {
            return;
        }

        mDatabase.close();
        mDBHelper.close();

        mDatabase = null;
        mDBHelper = null;

        mIsClosed = true;
    }

    private boolean openForeignKeySupport() {
        if (mIsClosed) {
            return false;
        }

        return execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public boolean beginTransaction() {
        if (mIsClosed) {
            return false;
        }

        mDatabase.beginTransaction();
        return true;
    }

    @Override
    public boolean endTransaction() {
        if (mIsClosed) {
            return false;
        }

        mDatabase.endTransaction();
        return true;
    }

    @Override
    public boolean setTransactionSuccessful() {
        if (mIsClosed) {
            return false;
        }

        mDatabase.setTransactionSuccessful();
        return true;
    }

    @Override
    public boolean drop(Class<? extends BaseEntity> clazz) {
        String tableName = EntityUtils.getTableName(clazz);
        return drop(tableName);
    }

    @Override
    public boolean drop(String tableName) {
        if (mIsClosed || TextUtils.isEmpty(tableName)) {
            return false;
        }

        try {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            mDatabase.execSQL(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean execSQL(String sql) {
        if (mIsClosed || TextUtils.isEmpty(sql)) {
            return false;
        }

        try {
            mDatabase.execSQL(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean execSQL(String sql, Object[] args) {
        if (mIsClosed || TextUtils.isEmpty(sql)) {
            return false;
        }

        try {
            mDatabase.execSQL(sql, args);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Cursor rawQuery(String sql) {
        if (mIsClosed) {
            return null;
        }

        return mDatabase.rawQuery(sql, null);
    }

    @Override
    public double querySum(Class<? extends BaseEntity> clazz,
            String fieldName, String condition) {

        if (mIsClosed) {
            return 0;
        }

        String tableName = EntityUtils.getTableName(clazz);
        String sql = "SELECT SUM(" + fieldName + ") FROM " + tableName;
        if (!TextUtils.isEmpty(condition)) {
            sql += " WHERE " + condition;
        }

        sql += ";";
        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor == null) {
            return 0;
        }

        double sum = 0;
        if (cursor.moveToFirst()) {
            sum = cursor.getDouble(0);
        }

        cursor.close();
        return sum;
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz) {

        return queryPrimaryKeyList(clazz, primaryKeyClazz, null, null);
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(
            Class<? extends BaseEntity> clazz, Class<T> primaryKeyClazz,
            String condition, String constraint) {

        String tableName = EntityUtils.getTableName(clazz);
        return queryPrimaryKeyList(primaryKeyClazz, tableName, clazz, condition, constraint);
    }

    @Override
    public <T> List<T> queryPrimaryKeyList(BaseEntity entity, Class<T> primaryKeyClazz) {
        return queryPrimaryKeyList(primaryKeyClazz, entity.getTableName(), entity.getClass());
    }

    private <T> List<T> queryPrimaryKeyList(
            Class<T> primaryKeyClazz, String tableName, Class<? extends BaseEntity> clazz) {

        return queryPrimaryKeyList(primaryKeyClazz, tableName, clazz, null, null);
    }

    private <T> List<T> queryPrimaryKeyList(
            Class<T> primaryKeyClazz, String tableName, Class<? extends BaseEntity> clazz,
            String condition, String constraint) {

        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        EntityUtils.FieldHelper helper = EntityUtils.getFieldHelper(primaryKeyClazz);
        if (helper == null) {
            return null;
        }

        Field primaryKey = EntityUtils.getPrimaryKeyField(clazz);
        if (primaryKey == null) {
            return null;
        }

        String sql = "SELECT DISTINCT " + primaryKey.getName() + " FROM " + tableName;
        if (!TextUtils.isEmpty(condition)) {
            sql += " WHERE " + condition;
        }

        if (!TextUtils.isEmpty(constraint)) {
            sql += " " + constraint;
        }

        sql += ";";
        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }

        List<T> result = new ArrayList<T>();
        if (!cursor.moveToFirst()) {
            cursor.close();
            return result;
        }

        do {
            int type = cursor.getType(0);
            result.add((T) helper.mHelper.parseCursor(cursor, 0));
        } while (cursor.moveToNext());

        cursor.close();
        return result;
    }

    @Override
    public int queryCount(Class<? extends BaseEntity> clazz) {
        return queryCount(clazz, null);
    }

    @Override
    public int queryCount(Class<? extends BaseEntity> clazz, String condition) {
        String tableName = EntityUtils.getTableName(clazz);
        return queryCount(tableName, condition);
    }

    @Override
    public int queryCount(String tableName) {
        return queryCount(tableName, null);
    }

    @Override
    public int queryCount(String tableName, String condition) {
        if (mIsClosed) {
            return 0;
        }

        if (TextUtils.isEmpty(tableName)) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;
        if (!TextUtils.isEmpty(condition)) {
            sql += " WHERE " + condition;
        }

        sql += ";";

        int count = 0;
        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    @Override
    public boolean updateFields(String tableName, String updateSql) {
        String sql = String.format("UPDATE %s SET %s;", tableName, updateSql);
        return execSQL(sql);
    }

    @Override
    public boolean updateFields(Class<? extends BaseEntity> clazz, String updateSql) {
        String tableName = EntityUtils.getTableName(clazz);
        return updateFields(tableName, updateSql);
    }

    @Override
    public <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, int position) {

        String tableName = EntityUtils.getTableName(clazz);
        return queryByPosition(clazz, tableName, position);
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz) {

        String tableName = EntityUtils.getTableName(clazz);
        return query(clazz, tableName);
    }

    @Override
    public <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, Object primary) {

        if (primary == null) {
            return null;
        }

        String tableName = EntityUtils.getTableName(clazz);
        return queryByPrimary(clazz, tableName, primary);
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int count) {
        return query(clazz, 0, count);
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, int index, int count) {

        if (count == 0) {
            return null;
        }

        String tableName = EntityUtils.getTableName(clazz);
        return query(clazz, tableName, index, count);
    }

    @Override
    public <T extends BaseEntity> T queryByPosition(
            Class<? extends BaseEntity> clazz, String tableName, int position) {
        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        String sql = String.format("SELECT * FROM %s LIMIT %d, %d;",
                tableName, position, position + 1);
        Cursor cursor = mDatabase.rawQuery(sql, null);

        T result = null;
        if (cursor.moveToNext()) {
            result = cursorToEntity(clazz, tableName, cursor);
        }

        cursor.close();
        return result;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName) {
        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        String sql = "SELECT * FROM " + tableName + ";";
        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor != null) {
            List<T> result = cursorToEntityList(clazz, tableName, cursor);
            cursor.close();
            return result;
        }

        return null;
    }

    @Override
    public <T extends BaseEntity> T queryByPrimary(
            Class<? extends BaseEntity> clazz, String tableName, Object primary) {
        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        Field primaryKey = EntityUtils.getPrimaryKeyField(clazz);
        if (primaryKey == null) {
            return null;
        }

        String sql = "SELECT * FROM "
                + tableName
                + " WHERE " + primaryKey.getName()
                + "='" + String.valueOf(primary) + "';";

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                T entity = cursorToEntity(clazz, tableName, cursor);
                cursor.close();
                return entity;
            } else {
                cursor.close();
                return null;
            }
        }

        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> queryByCondition(
            Class<? extends BaseEntity> clazz, String condition, String constraint) {

        String tableName = EntityUtils.getTableName(clazz);
        return queryByConditionWithTableName(clazz, tableName, condition, constraint);
    }

    @Override
    public <T extends BaseEntity> List<T> queryByConditionWithTableName(
            Class<? extends BaseEntity> clazz, String tableName,
            String condition, String constraint) {

        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ").append(tableName);

        if (!TextUtils.isEmpty(condition)) {
            builder.append(" WHERE ").append(condition);
        }

        if (!TextUtils.isEmpty(constraint)) {
            builder.append(" ").append(constraint);
        }

        builder.append(";");

        Cursor cursor = mDatabase.rawQuery(builder.toString(), null);
        if (cursor != null) {
            List<T> result = cursorToEntityList(clazz, tableName, cursor);
            cursor.close();
            return result;
        }

        return null;
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int count) {
        return query(clazz, tableName, 0, count);
    }

    @Override
    public <T extends BaseEntity> List<T> query(
            Class<? extends BaseEntity> clazz, String tableName, int index, int count) {
        if (mIsClosed) {
            return null;
        }

        if (TextUtils.isEmpty(tableName)) {
            return null;
        }

        String sql = "SELECT * FROM "
                + tableName + " LIMIT " + index + "," + count + ";";
        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }

        List<T> result = cursorToEntityList(clazz, tableName, cursor);
        cursor.close();
        return result;
    }

    @Override
    public boolean remove(BaseEntity entity) {
        if (mIsClosed || entity == null) {
            return false;
        }

        if (entity.getState() != BaseEntity.State.Removed) {
            Field primaryKey = EntityUtils.getPrimaryKeyField(entity.getClass());
            if (primaryKey == null) {
                return false;
            }

            int effectRows = 0;
            try {
                Object key = primaryKey.get(entity);
                boolean result = removeByPrimaryKey(entity.getClass(), key);
                if (result) {
                    entity.setRemoved();
                }

                return result;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean remove(Class<? extends BaseEntity> clazz, String condition) {
        String tableName = EntityUtils.getTableName(clazz);
        return remove(tableName, condition);
    }

    @Override
    public boolean remove(String tableName, String condition) {
        if (mIsClosed) {
            return false;
        }

        if (TextUtils.isEmpty(tableName)) {
            return false;
        }

        try {
            String sql = "DELETE FROM " + tableName;
            if (!TextUtils.isEmpty(condition)) {
                sql += " WHERE " + condition;
            }

            sql += ";";
            mDatabase.execSQL(sql);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeAll(Class<? extends BaseEntity> clazz) {
        String tableName = EntityUtils.getTableName(clazz);
        return removeAll(tableName);
    }

    @Override
    public boolean removeAll(String tableName) {
        return remove(tableName, null);
    }

    @Override
    public boolean removeByPrimaryKey(
            Class<? extends BaseEntity> clazz, Object primary) {

        if (mIsClosed) {
            return false;
        }

        String tableName = EntityUtils.getTableName(clazz);
        if (TextUtils.isEmpty(tableName)) {
            return false;
        }

        Field primaryKey = EntityUtils.getPrimaryKeyField(clazz);
        if (primaryKey == null) {
            return false;
        }

        int effectRows = mDatabase.delete(
                tableName, primaryKey.getName() + "=?",
                new String[]{String.valueOf(primary)});

        return effectRows > 0;
    }

    @Override
    public boolean insertNew(BaseEntity entity) {
        return doStoreNewOrReplace(entity, false);
    }

    @Override
    public boolean replace(BaseEntity entity) {
        return doStoreNewOrReplace(entity, true);
    }

    private boolean doStoreNewOrReplace(BaseEntity entity, boolean isReplace) {
        if (mIsClosed || entity == null) {
            return false;
        }

        BaseEntity.State state = entity.getState();
        if (isReplace) {
            if (state != BaseEntity.State.New
                    && state != BaseEntity.State.Stored) {
                return false;
            }
        } else {
            if (state != BaseEntity.State.New) {
                return false;
            }
        }

        String tableName = entity.getTableName();
        if (!createTable(tableName, entity)) {
            return false;
        }

        entity.onPreCommit();

        long rowId = BaseEntity.INVALID_ID;
        ContentValues values = createContentValues(entity);
        try {
            if (isReplace) {
                rowId = mDatabase.replace(tableName, null, values);
            } else {
                rowId = mDatabase.insert(tableName, null, values);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        entity.setStored();
        entity.onDataCommitted(rowId);

        return rowId != BaseEntity.INVALID_ID;
    }

    @Override
    public boolean update(BaseEntity entity) {
        if (mIsClosed || entity == null) {
            return false;
        }

        Field primaryKey = EntityUtils.getPrimaryKeyField(entity.getClass());

        try {
            String condition = null;
            if (primaryKey != null) {
                Object primaryValue = primaryKey.get(entity);
                condition = String.format("%s = %s",
                        primaryKey.getName(), primaryValue.toString());
            }

            return update(entity, condition);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(BaseEntity entity, String condition) {
        if (mIsClosed || entity == null) {
            return false;
        }

        String tableName = entity.getTableName();
        if (!createTable(tableName, entity)) {
            return false;
        }

        entity.onPreCommit();
        int rowCount = 0;
        try {
            ContentValues values = createContentValues(entity);
            rowCount = mDatabase.update(tableName, values, condition, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        entity.setStored();
        entity.onDataUpdated();

        return rowCount > 0;
    }

    @Override
    public boolean createTable(Class<? extends BaseEntity> clazz) {
        BaseEntity entity = null;

        try {
            entity = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (entity == null) {
            return false;
        }

        String tableName = entity.getTableName();
        return createTable(tableName, entity);
    }

    @Override
    public boolean createTable(BaseEntity entity) {
        return createTable(entity.getTableName(), entity);
    }

    private boolean createTable(String tableName, BaseEntity entity) {
        if (mTableCache.contains(tableName)) {
            return true;
        }

        if (mIsClosed) {
            return false;
        }

        if (DBUtils.isTableExist(mDatabase, tableName)) {
            mTableCache.add(tableName);
            return true;
        }

        String createSql = EntityUtils.generateCreateStatement(entity);
        try {
            mDatabase.execSQL(createSql);
            mTableCache.add(tableName);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ContentValues createContentValues(BaseEntity entity) {
        ContentValues values = new ContentValues();
        List<Field> fields = EntityUtils.getEntityFields(entity.getClass());

        try {
            for (Field f : fields) {
                if (f.isAnnotationPresent(NotColumnField.class)) {
                    continue;
                }

                final String name = f.getName();
                final Class<?> clazz = f.getType();

                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }

                final Object ref = f.get(entity);

                boolean needCheckPrimaryKey = false;
                if (f.isAnnotationPresent(PrimaryKey.class)
                        && f.isAnnotationPresent(AutoIncrement.class)) {
                    needCheckPrimaryKey = true;
                }

                if (ref instanceof Integer || clazz == Integer.class) {
                    Integer value = (Integer) ref;
                    if (!needCheckPrimaryKey || value != BaseEntity.INVALID_ID) {
                        values.put(name, value);
                    }
                } else if (ref instanceof Long || clazz == Long.class) {
                    Long value = (Long) ref;
                    if (!needCheckPrimaryKey || value != BaseEntity.INVALID_ID) {
                        values.put(name, value);
                    }
                } else if (ref instanceof String || clazz == String.class) {
                    values.put(name, (String) ref);
                } else if (ref instanceof byte[] || clazz == byte[].class) {
                    values.put(name, (byte[]) ref);
                } else if (ref instanceof Short || clazz == Short.class) {
                    values.put(name, (Short) ref);
                } else if (ref instanceof Float || clazz == Float.class) {
                    values.put(name, (Float) ref);
                } else if (ref instanceof Double || clazz == Double.class) {
                    values.put(name, (Double) ref);
                } else if (ref instanceof Byte || clazz == Byte.class) {
                    values.put(name, (Byte) ref);
                } else if (ref instanceof Boolean || clazz == Boolean.class) {
                    values.put(name, (Boolean) ref);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return values;
    }

    private <T extends BaseEntity> List<T> cursorToEntityList(
            Class<? extends BaseEntity> clazz,
            String tableName,
            Cursor cursor) {

        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        List<T> result = new ArrayList<T>();
        do {
            T entity = cursorToEntity(clazz, tableName, cursor);
            result.add(entity);
        } while (cursor.moveToNext());

        return result;
    }

    private <T extends BaseEntity> T cursorToEntity(
            Class<? extends BaseEntity> clazz,
            String tableName,
            Cursor cursor) {

        T entity = null;

        try {
            entity = (T) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (entity == null) {
            return null;
        }

        entity.onPreLoad();

        try {
            List<Field> fields = EntityUtils.getEntityFields(clazz);
            for (Field f : fields) {
                String name = f.getName();
                int column = cursor.getColumnIndex(name);

                if (column != -1) {
                    Class<?> type = f.getType();
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }

                    if (type == Integer.class || type == int.class) {
                        f.set(entity, cursor.getInt(column));
                    } else if (type == Long.class || type == long.class) {
                        f.set(entity, cursor.getLong(column));
                    } else if (type == String.class) {
                        f.set(entity, cursor.getString(column));
                    } else if (type == byte[].class || type == Byte[].class) {
                        f.set(entity, cursor.getBlob(column));
                    } else if (type == Short.class || type == short.class) {
                        f.set(entity, (short) cursor.getInt(column));
                    } else if (type == Float.class || type == float.class) {
                        f.set(entity, cursor.getFloat(column));
                    } else if (type == Double.class || type == double.class) {
                        f.set(entity, cursor.getDouble(column));
                    } else if (type == Byte.class || type == byte.class) {
                        f.set(entity, (byte) cursor.getInt(column));
                    } else if (type == Boolean.class || type == boolean.class) {
                        f.set(entity, cursor.getInt(column) != 0);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        entity.setStored();
        entity.onDataLoaded();

        return (T) entity.toRealEntity();
    }
}
