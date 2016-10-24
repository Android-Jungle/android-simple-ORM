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

package com.jungle.easyorm.utils;

import android.database.Cursor;
import android.text.TextUtils;
import com.jungle.easyorm.BaseEntity;
import com.jungle.easyorm.constraint.AutoIncrement;
import com.jungle.easyorm.constraint.CompositePrimaryKey;
import com.jungle.easyorm.constraint.DefaultNull;
import com.jungle.easyorm.constraint.ForeignKey;
import com.jungle.easyorm.constraint.NotColumnField;
import com.jungle.easyorm.constraint.NotNull;
import com.jungle.easyorm.constraint.PrimaryKey;
import com.jungle.easyorm.constraint.Unique;
import com.jungle.easyorm.constraint.UniqueField;
import com.jungle.easyorm.constraint.UseParentFields;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityUtils {

    public interface FieldCursorHelper {
        public Object parseCursor(Cursor cursor, int columnIndex);
    }


    public static class FieldHelper {
        public FieldHelper(String type, FieldCursorHelper helper) {
            mFieldType = type;
            mHelper = helper;
        }

        public String mFieldType;
        public FieldCursorHelper mHelper;
    }


    private static Map<Class<?>, FieldHelper> mFieldHelperList = new HashMap<>();

    private static Map<Class<? extends BaseEntity>, List<Field>> mCacheFields = new HashMap<>();

    private static Map<Class<? extends BaseEntity>, Field> mCachePrimaryKeys = new HashMap<>();


    static {

        FieldHelper boolHelper = new FieldHelper("INTEGER", new FieldCursorHelper() {
            @Override
            public Boolean parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getInt(columnIndex) != 0;
            }
        });

        FieldHelper byteHelper = new FieldHelper("INTEGER", new FieldCursorHelper() {
            @Override
            public Byte parseCursor(Cursor cursor, int columnIndex) {
                return (byte) cursor.getInt(columnIndex);
            }
        });

        FieldHelper shortHelper = new FieldHelper("INTEGER", new FieldCursorHelper() {
            @Override
            public Short parseCursor(Cursor cursor, int columnIndex) {
                return (short) cursor.getInt(columnIndex);
            }
        });

        FieldHelper intHelper = new FieldHelper("INTEGER", new FieldCursorHelper() {
            @Override
            public Integer parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getInt(columnIndex);
            }
        });

        FieldHelper integerHelper = new FieldHelper("INTEGER", new FieldCursorHelper() {
            @Override
            public Long parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getLong(columnIndex);
            }
        });

        FieldHelper floatHelper = new FieldHelper("REAL", new FieldCursorHelper() {
            @Override
            public Float parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getFloat(columnIndex);
            }
        });

        FieldHelper doubleHelper = new FieldHelper("REAL", new FieldCursorHelper() {
            @Override
            public Double parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getDouble(columnIndex);
            }
        });

        FieldHelper stringHelper = new FieldHelper("TEXT", new FieldCursorHelper() {
            @Override
            public String parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getString(columnIndex);
            }
        });

        FieldHelper blobHelper = new FieldHelper("BLOB", new FieldCursorHelper() {
            @Override
            public byte[] parseCursor(Cursor cursor, int columnIndex) {
                return cursor.getBlob(columnIndex);
            }
        });


        mFieldHelperList.put(byte.class, integerHelper);
        mFieldHelperList.put(boolean.class, integerHelper);
        mFieldHelperList.put(short.class, integerHelper);
        mFieldHelperList.put(int.class, integerHelper);
        mFieldHelperList.put(long.class, integerHelper);
        mFieldHelperList.put(Byte.class, integerHelper);
        mFieldHelperList.put(Boolean.class, integerHelper);
        mFieldHelperList.put(Short.class, integerHelper);
        mFieldHelperList.put(Integer.class, integerHelper);
        mFieldHelperList.put(Long.class, integerHelper);

        mFieldHelperList.put(Float.class, floatHelper);
        mFieldHelperList.put(float.class, floatHelper);

        mFieldHelperList.put(double.class, doubleHelper);
        mFieldHelperList.put(Double.class, doubleHelper);

        mFieldHelperList.put(String.class, stringHelper);

        mFieldHelperList.put(byte[].class, blobHelper);
        mFieldHelperList.put(Byte[].class, blobHelper);
    }


    public static FieldHelper getFieldHelper(Class<?> clazz) {
        return mFieldHelperList.get(clazz);
    }

    public static String getTableName(Class<? extends BaseEntity> clazz) {
        try {
            return clazz.newInstance().getTableName();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateCreateStatement(BaseEntity entity) {
        String tableName = entity.getTableName();
        List<Field> fields = getEntityFields(entity.getClass());
        final int count = fields.size();

        String compositePrimaryKey = null;
        List<String> compositePrimaryKeys = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            Field f = fields.get(i);
            FieldHelper type = mFieldHelperList.get(f.getType());

            if (type != null) {
                if (f.isAnnotationPresent(CompositePrimaryKey.class)) {
                    CompositePrimaryKey key = f.getAnnotation(CompositePrimaryKey.class);
                    String compositeName = key.value();
                    if (!TextUtils.isEmpty(compositeName)) {
                        compositePrimaryKey = compositeName;
                    }

                    compositePrimaryKeys.add(f.getName());
                }
            }
        }

        boolean hasCompositeKey = !compositePrimaryKeys.isEmpty();
        if (hasCompositeKey && TextUtils.isEmpty(compositePrimaryKey)) {
            compositePrimaryKey = "composite_key";
        }

        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        builder.append(tableName).append(" (");

        String uniqueFields = "";
        String foreignKeyName = null;
        Class<? extends BaseEntity> foreignKeyClazz = null;

        for (int i = 0; i < count; ++i) {
            Field f = fields.get(i);
            String name = f.getName();
            FieldHelper type = mFieldHelperList.get(f.getType());

            if (type != null) {
                //
                // name TYPE
                //
                builder.append(name).append(" ").append(type.mFieldType);

                if (!hasCompositeKey) {
                    //
                    // when has composite-key, skip PRIMARY-KEY.
                    //
                    if (f.isAnnotationPresent(PrimaryKey.class)) {
                        builder.append(" PRIMARY KEY");

                        if (f.isAnnotationPresent(AutoIncrement.class)) {
                            builder.append(" AUTOINCREMENT");
                        }
                    }
                }

                if (f.isAnnotationPresent(ForeignKey.class)) {
                    ForeignKey key = f.getAnnotation(ForeignKey.class);
                    foreignKeyClazz = key.value();
                    foreignKeyName = name;
                }

                if (f.isAnnotationPresent(Unique.class)) {
                    builder.append(" UNIQUE");
                }

                if (f.isAnnotationPresent(NotNull.class)) {
                    builder.append(" NOT NULL");
                } else if (f.isAnnotationPresent(DefaultNull.class)) {
                    DefaultNull defaultNull = f.getAnnotation(DefaultNull.class);
                    builder.append(" DEFAULT ").append(defaultNull.value());
                }

                if (f.isAnnotationPresent(UniqueField.class)) {
                    if (!TextUtils.isEmpty(uniqueFields)) {
                        uniqueFields += ", ";
                    }

                    uniqueFields += name;
                }

                if (i < count - 1) {
                    builder.append(", ");
                }
            }
        }

        if (hasCompositeKey) {
            //
            // CONSTRAINT composite_key PRIMARY KEY (field1, field2, ...)
            //
            builder.append(", CONSTRAINT ").append(compositePrimaryKey).append(" PRIMARY KEY (");
            int keyCount = compositePrimaryKeys.size();
            for (int i = 0; i < keyCount; ++i) {
                builder.append(compositePrimaryKeys.get(i));
                if (i < keyCount - 1) {
                    builder.append(", ");
                }
            }

            builder.append(")");
        }

        if (!TextUtils.isEmpty(uniqueFields)) {
            builder.append(", UNIQUE(").append(uniqueFields).append(")");
        }

        if (foreignKeyClazz != null && !TextUtils.isEmpty(foreignKeyName)) {
            Field field = getPrimaryKeyField(foreignKeyClazz);
            if (field != null) {
                //
                // FOREIGN KEY(foreignKeyName) REFERENCES outerTableName(outerFieldName)
                //
                String outerFieldName = field.getName();
                String outerTableName = getTableName(foreignKeyClazz);
                builder.append(", FOREIGN KEY(").append(foreignKeyName).append(") REFERENCES ")
                        .append(outerTableName).append("(").append(outerFieldName).append(")");
            }
        }

        builder.append(");");
        return builder.toString();
    }

    private static Class<? extends BaseEntity> getEntityClass(Class<? extends BaseEntity> clazz) {
        while (clazz.isAnnotationPresent(UseParentFields.class)) {
            Class<?> superClazz = clazz.<BaseEntity>getSuperclass();
            if (superClazz == BaseEntity.class) {
                return (Class<? extends BaseEntity>) superClazz;
            }

            clazz = (Class<? extends BaseEntity>) clazz.<BaseEntity>getSuperclass();
        }

        return clazz;
    }

    public static List<Field> getEntityFields(Class<? extends BaseEntity> clazz) {

        clazz = getEntityClass(clazz);

        List<Field> result = mCacheFields.get(clazz);
        if (result != null) {
            return result;
        }

        result = new ArrayList<Field>();
        Field[] fields = clazz.getFields();

        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())
                    || f.isAnnotationPresent(NotColumnField.class)) {
                // exclude static member and NotColumnField.
                continue;
            }

            result.add(f);
        }

        mCacheFields.put(clazz, result);
        return result;
    }

    public static Field getPrimaryKeyField(Class<? extends BaseEntity> clazz) {
        clazz = getEntityClass(clazz);

        Field primaryKey = mCachePrimaryKeys.get(clazz);
        if (primaryKey != null) {
            return primaryKey;
        }

        Field[] fields = clazz.getFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }

                mCachePrimaryKeys.put(clazz, f);
                return f;
            }
        }

        return null;
    }
}
