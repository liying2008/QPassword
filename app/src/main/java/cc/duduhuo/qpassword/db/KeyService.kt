package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:04
 * Description:
 * Remarks:
 * =======================================================
 */
class KeyService(context: Context) {
    private val mDbHelper: DatabaseHelper = DatabaseHelper(context)
    /**
     * 向数据库中添加一个主密码
     *
     * @param key 主密码对象
     */
    fun addKey(key: Key) {
        val db = mDbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(Key.KEY, key.key)
            contentValues.put(Key.MODE, key.mode)
            db.insert(DBInfo.Table.TB_KEY, null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    /**
     * 更新主密码
     *
     * @param oldKey 旧主密码
     * @param newKey 新主密码
     */
    fun updateKey(oldKey: Key, newKey: Key) {
        val db = mDbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(Key.KEY, newKey.key)
            contentValues.put(Key.MODE, newKey.mode)
            db.update(DBInfo.Table.TB_KEY, contentValues, "${Key.KEY} = ?", arrayOf(oldKey.key))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    /**
     * 获取主密码（SHA1 加密后的）
     *
     * @return Key or Null
     */
    fun getKey(): Key? {
        val db = mDbHelper.writableDatabase
        var key: Key? = null
        var cursor: Cursor? = null
        try {
            cursor = db.query(DBInfo.Table.TB_KEY, null, null, null, null, null, null)

            if (cursor!!.moveToNext()) {
                key = Key()
                key.key = cursor.getString(cursor.getColumnIndex(Key.KEY))
                key.mode = cursor.getInt(cursor.getColumnIndex(Key.MODE))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return key
    }
}