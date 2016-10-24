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

package com.jungle.easyorm;

import java.util.List;

public abstract class BaseEntity {

    public static final long INVALID_ID = -1;


    public static enum State {
        New,            // new entity.
        Stored,         // entity stored in ORM.
        Removed,        // entity was removed.
    }

    private BaseEntity.State mState = BaseEntity.State.New;


    public BaseEntity() {
    }

    public BaseEntity toRealEntity() {
        return this;
    }

    public void reset() {
        setNew();
    }

    public State getState() {
        return mState;
    }

    public void setNew() {
        mState = State.New;
    }

    public void setStored() {
        mState = State.Stored;
    }

    public void setRemoved() {
        mState = State.Removed;
    }

    public String getTableName() {
        return getClass().getSimpleName();
    }

    public void onPreLoad() {
    }

    public void onPreCommit() {
    }

    public void onDataLoaded() {
    }

    public void onDataCommitted(long rowId) {
    }

    public void onDataUpdated() {
    }

    public static String and(String left, String right) {
        return String.format("(%s) AND (%s)", left, right);
    }

    public static String or(String left, String right) {
        return String.format("(%s) OR (%s)", left, right);
    }

    public static String not(String condition) {
        return String.format("NOT (%s)", condition);
    }

    public static String between(String left, String right) {
        return String.format("BETWEEN %s AND %s", left, right);
    }

    public static String in(List<Object> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("IN (");
        for (Object val : list) {
            builder.append(String.valueOf(val)).append(", ");
        }

        builder.append(")");
        return builder.toString();
    }
}
