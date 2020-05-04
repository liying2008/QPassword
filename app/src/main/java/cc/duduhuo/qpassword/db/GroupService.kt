package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 17:02
 * Description:
 * Remarks:
 * =======================================================
 */
class GroupService(context: Context) {
    private val mDbHelper: DatabaseHelper = DatabaseHelper(context)

    /**
     * 数据库中添加分组
     *
     * @param group 分组
     */
    fun addGroup(group: Group) {
        val db = mDbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Group.NAME, group.name)
        db.insert(DBInfo.Table.TB_GROUP, null, contentValues)
        contentValues.clear()
        db.close()
    }

    /**
     * 获取数据库中的所有分组
     *
     * @return
     */
    fun getAllGroups(): List<Group> {
        val groups = mutableListOf<Group>()
        val db = mDbHelper.writableDatabase
        var cursor: Cursor? = null
        db.beginTransaction()
        try {
            cursor = db.query(DBInfo.Table.TB_GROUP, null, null, null, null, null, null)

            while (cursor.moveToNext()) {
                val group = Group()
                group.name = cursor.getString(cursor.getColumnIndex(Group.NAME))
                groups.add(group)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            cursor?.close()
            db.close()
        }
        return groups
    }

    /**
     * 更新分组名称
     *
     * @param oldGroupName 旧名字
     * @param newGroupName 新名字
     * @param merge 是否是合并分组
     */
    fun updateGroupName(oldGroupName: String, newGroupName: String, merge: Boolean) {
        val db = mDbHelper.writableDatabase
        db.beginTransaction()
        try {
            if (merge) {
                // 新的分组已经存在 直接删除旧的分组
                db.delete(DBInfo.Table.TB_GROUP, "${Group.NAME} = ?", arrayOf(oldGroupName))
            } else {
                // 新的分组不存在， 更新旧的分组名称
                val contentValues = ContentValues()
                contentValues.put(Group.NAME, newGroupName)
                db.update(DBInfo.Table.TB_GROUP, contentValues, "${Group.NAME} = ?", arrayOf(oldGroupName))
                contentValues.clear()
            }
            val contentValues = ContentValues()
            contentValues.put(Password.GROUP_NAME, newGroupName)
            db.update(DBInfo.Table.TB_PASSWORD, contentValues, "${Password.GROUP_NAME} = ?", arrayOf(oldGroupName))
            contentValues.clear()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * 删除分组（同时删除该分组下的所有密码）
     * @param groupName 分组名称
     * @return 影响的行数
     */
    fun deleteGroup(groupName: String): Int {
        val db = mDbHelper.writableDatabase
        val count = db.delete(DBInfo.Table.TB_GROUP, "${Group.NAME} = ?", arrayOf(groupName))
        if (count > 0) {
            db.delete(DBInfo.Table.TB_PASSWORD, "${Password.GROUP_NAME} = ?", arrayOf(groupName))
        }
        db.close()
        return count
    }

}