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

package com.jungle.easyorm.demo.data;

import com.jungle.easyorm.BaseEntity;
import com.jungle.easyorm.constraint.NotNull;
import com.jungle.easyorm.constraint.PrimaryKey;

public class Student extends BaseEntity {

    @PrimaryKey
    @NotNull
    public String mId;

    public String mName;
    public int mAge;
    public double mScore;
}
