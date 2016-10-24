# android-easy-ORM 简介

### 1、简介

`android-easy-ORM` 如你所见，是一款 Android 平台上基于 SQLite 的**简易** ORM 框架。特点如下：

- 方便易用；
- 接口众多；
- 利用反射自动 Load & Save。

由于是动态 `反射` 型库，显然它的性能不及如 [greenDAO](https://github.com/greenrobot/greenDAO) 这种编译型 ORM 框架。但它的易用性是一个方便的点——你可以在你的工作中综合考虑采用。如果你只是比较简单的数据存储，数据加载/保存时耗用的反射的那点性能对你的系统影响在可接受范围内，你可以考虑用 `android-easy-ORM`。

### 2、使用方法

```java
compile 'com.jungle.easyorm:android-easy-ORM:1.0'
```

### 3、接口

这里只展示部分接口，其他接口具体请参考代码。

|接口|接口描述|
|---|---|
|createTable|创建表|
|drop|删除表|
|execSQL|执行 SQL 语句|
|queryPrimaryKeyList|查询某个表所有的主键（支持条件和约束）|
|querySum|查询某个字段的和（支持条件）|
|queryCount|查询个数（支持条件）|
|query、queryByPrimary、queryByCondition、queryByPosition|查询，返回结果的 List（支持条件、约束、LIMIT 等等）|
|remove、removeAll、removeByPrimaryKey|删除某个数据（支持条件）|
|insertNew|插入新数据（采用 insert 语句插入）|
|update|更新某个数据（采用 update 语句更新，支持条件）|
|replace|替换某个数据（采用 replace 语句替换）|

### 4、表定义

表的定义比较简单，使用 `POJO` 类定义即可。然后采用一些字段约束。约束用 Java 注解表述，具体列表如下：

|注解|约束|
|---|---|
|ORMTable(`tableName`)|表名|
|PrimaryKey|主键|
|ForeignKey(`clazz`)|外键|
|CompositePrimaryKey|联合主键|
|AutoIncrement|自增字段|
|NotNull|类似于 “FIELD TEXT **NOT NULL**”|
|DefaultNull(`defValue`)|类似于 “FIELD TEXT **DEFAULT defValue**”|
|UNIQUE|类似于 “FIELD TEXT **UNIQUE**”|
|UniqueField|类似于 “CREATE TABLE tbl(..., **UNIQUE**(FIELD1, FIELD2, FIELD3)) ”|
|NotColumnField|这个字段非表字段，将在 `Load&Save` 时忽略|
|UseParentFields|将使用父类的所有字段创建表，忽略本类的所有字段|

### 5、BaseEntity 介绍

表定义类称为 `Entity` 实体，所有的 Entity 类均需派生自 `com.jungle.easyorm.BaseEntity`。BaseEntity 有一些方法需要注意：

|BaseEntity 方法|用途 & 含义|
|---|---|
|setNew、setStored、setRemoved|更改 Entity 的状态|
|reset|相当于 setNew|
|onPreLoad|在数据从数据库 Load 到 Entity 之前调用|
|onDataLoaded|在数据从数据库 Load 完成之后调用|
|onPreCommit|在数据将要写入数据库之前调用|
|onDataCommitted|在数据写入数据库之后调用|
|onDataUpdated|在调用 `ORMSupporter.update` 方法更新数据到数据库后调用|
|**toRealEntity**|在使用一系列 `query` 方法获取数据时调用，用于将本 Entity 转换为其他 Entity|

### 6、ORMSupporter 使用示例

```java
final int DB_VERSION = 1;
String dbFilePath = getDatabaseFilePath();

ORMSupporter supporter = new SQLiteORMSupporter(context, dbFilePath, DB_VERSION, new SimpleORMDatabaseListener() {

    @Override
    public void onCreated(ORMSupporter supporter, SQLiteDatabase db) {
        super.onCreated(supporter, db);

        // create table.
        supporter.createTable(GeneralAccountInfo.class);
    }

    @Override
    public void onUpgrade(ORMSupporter supporter, SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(supporter, db, oldVersion, newVersion);

        // drop unused table.
        supporter.drop(DownloadItem.class);

        // upgrade logic.
        if (oldVersion == 4 && newVersion == 5) {
            // ...
        }
    }
});


// load data
List<GeneralAccountInfo> accouns = supporter.query(GeneralAccountInfo.class);

List<GeneralAccountInfo> accouns = supporter.queryByCondition(
    GeneralAccountInfo.class, BaseEntity.and("id = 12458", "registerTime < 1477302247"));

// ...
```

### 7、表定义 & 使用示例

结合使用表字段的 `UseParentFields` 和 `BaseEntity.toRealEntity` 方法，可以实现一些特殊效果。下面是一个例子：

```java
/**
 * BaseMessage
 */
public class BaseMessage extends BaseEntity {

    public static class MessageType {
        public static final int ANY = 0;
        public static final int TEXT = 1;
        public static final int PICTURE = 2;
    }


    @PrimaryKey
    @AutoIncrement
    public long mMsgLocalId = INVALID_ID;

    @UniqueField
    public long mMsgGuid;

    public long mFromUid;
    public long mToUid;
    public int mMessageType = MessageType.ANY;
    public long mTimestamp;
    public byte[] mMsgContent;

    @NotColumnField
    public String mFromNickName;

    public BaseMessage() {
    }

    public BaseMessage(BaseMessage msg) {
        mMsgLocalId = msg.mMsgLocalId;
        // etc ...
    }

    @Override
    public BaseMessage toRealEntity() {
        switch (mMessageType) {
            case MessageType.TEXT:
                if (!(this instanceof TextMessage)) {
                    return new TextMessage(this);
                }
            case MessageType.PICTURE:
                if (!(this instanceof PictureMessage)) {
                    return new PictureMessage(this);
                }
            default:
                break;
        }

        return this;
    }

    @Override
    public String getTableName() {
        return getTableName(mToUid);
    }

    public static String getTableName(long toUid) {
        String prefix = "fake";
        return prefix + "_" + MiscUtils.generateMD5String(String.valueOf(toUid));
    }
}
```

我们定义了一个基础的聊天消息，而子消息类型有 `TextMessage` 、`PictureMessage` 多种。 消息体在 `mMsgContent` 这个字段里面。我们希望从数据库获取消息时，拿到的消息可以灵活转换为对应的 Java 对象。我们可以看出在 **`toRealEntity`** 方法里面，通过 `mMessageType` 做了转换。

子消息定义如下：

```java
/*
 * TextMessage
 */
@UseParentFields
public class TextMessage extends BaseMessage {

    private String mText;

    public TextMessage() {
        // ...
    }

    public TextMessage(BaseMessage message) {
        super(message);
    }

    public void decodeMessage() {
        try {
            PbMessage.Text text = PbMessage.Text.parseFrom(mMsgContent);
            mText = new String(text.getContent().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    // ...
}

/*
 * TextMessage
 */
@UseParentFields
public class PictureMessage extends BaseMessage {

    public int mImageType;
    public int mImageWidth;
    public int mImageHeight;
    public int mImageSizeBytes;
    public String mImageThumbUrl;
    public String mImageUrl;

    public PictureMessage() {
        // ...
    }

    public PictureMessage(BaseMessage message) {
        super(message);
    }

    public void decodeMessage() {
        // ...
    }
}
```

我们看到，子消息均用 **`UseParentFields`** 做了约束，表示在子消息写入数据库时，按父类 `BaseMessage` 进行反射写入，忽略子消息类的字段。使用如下：

```java
ORMSupporter supporter = ...;

// query
long friendUid = 145701L;
String tableName = BaseMessage.getTableName(friendUid);
List<BaseMessage> messages = supporter.query(BaseMessage.class, tableName);

for (BaseMessage msg : messages) {
    if (msg instance TextMessage) {
        TextMessage text = (TextMessage) msg;
        // ...
    }
}

// create a new message
TextMessage msg = new TextMessage();
msg.mFromUid = friendUid;
msg.mMessageType = BaseMessage.MessageType.TEXT;
// fill other fields...
//

// save it to database
supporter.replace(msg);
```
