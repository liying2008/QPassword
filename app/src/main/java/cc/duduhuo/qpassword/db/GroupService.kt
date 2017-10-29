package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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
    private val dbHelper: DatabaseHelper = DatabaseHelper(context)
    /**
     * 数据库中添加分组
     *
     * @param group 分组
     */
    fun addPasswordGroup(group: Group) {
        val db = dbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(Group.NAME, group.name)
            db.insert(DBInfo.Table.TB_GROUP, null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    /**
     * 获取数据库中的所有分组
     *
     * @return
     */
    fun getAllPasswordGroup(): List<Group> {
        val groups = mutableListOf<Group>()
        val db = dbHelper.writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(DBInfo.Table.TB_GROUP, null, null, null, null, null, null)

            while (cursor!!.moveToNext()) {
                val group = Group()
                group.name = cursor.getString(cursor.getColumnIndex(Group.NAME))
                groups.add(group)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
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
     */
    fun updatePasswdGroupName(oldGroupName: String, newGroupName: String) {
        val db = dbHelper.writableDatabase
        var rawQuery: Cursor? = null
        try {
            rawQuery = db.rawQuery("SELECT COUNT(${Group.NAME}) FROM ${DBInfo.Table.TB_GROUP} WHERE ${Group.NAME} = ?",
                arrayOf(newGroupName))
            if (rawQuery != null && rawQuery.moveToNext() && rawQuery.getInt(0) == 1) {
                // 新的分组已经存在 直接删除旧的分组
                db.delete(DBInfo.Table.TB_GROUP, "${Group.NAME} = ?", arrayOf(oldGroupName))
            } else {
                // 新的分组不存在， 更新旧的分组名称
                val contentValues = ContentValues()
                contentValues.put(Group.NAME, newGroupName)
                db.update(DBInfo.Table.TB_GROUP, contentValues, "${Group.NAME} = ?", arrayOf(oldGroupName))
            }

            val contentValues = ContentValues()
            contentValues.put(Password.GROUP_NAME, newGroupName)
            db.update(DBInfo.Table.TB_PASSWORD, contentValues, "${Password.GROUP_NAME} = ?", arrayOf(oldGroupName))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            rawQuery?.close()
            db.close()
        }
    }

    /**
     * 删除分组（同时删除该分组下的所有密码）
     * @param group 分组名称
     * @return 影响的行数
     */
    fun deletePasswordGroup(group: String): Int {
        val db = dbHelper.writableDatabase
        val count = db.delete(DBInfo.Table.TB_GROUP, "${Group.NAME} = ?", arrayOf(group))
        if (count > 0) {
            db.delete(DBInfo.Table.TB_PASSWORD, "${Password.GROUP_NAME} = ?", arrayOf(group))
        }
        db.close()
        return count
    }

}