package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.util.aesDecrypt
import cc.duduhuo.qpassword.util.aesEncrypt

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
        val contentValues = ContentValues()
        contentValues.put(Key.KEY, key.key)
        contentValues.put(Key.MODE, key.mode)
        db.insert(DBInfo.Table.TB_KEY, null, contentValues)
        contentValues.clear()
        db.close()
    }

    /**
     * 更新主密码
     *
     * @param oldKey 旧主密码对象
     * @param oldOriKey 旧主密码（未SHA-1加密）
     * @param newKey 新主密码对象
     * @param newOriKey 新主密码（未SHA-1加密）
     *
     * @return true: 主密码已更新；false: 新旧密码一致，无需更新
     */
    fun updateKey(oldKey: Key, oldOriKey: String, newKey: Key, newOriKey: String): Boolean {
        if (oldOriKey == newOriKey && oldKey.mode == newKey.mode) {
            return false
        }
        val db = mDbHelper.writableDatabase
        db.beginTransaction()
        try {
            val contentValues = ContentValues()
            contentValues.put(Key.KEY, newKey.key)
            contentValues.put(Key.MODE, newKey.mode)
            db.update(DBInfo.Table.TB_KEY, contentValues, "${Key.KEY} = ?", arrayOf(oldKey.key))
            // 更新全部密码（重新加密）
            updatePassword(db, oldKey, oldOriKey, newKey, newOriKey)
            contentValues.clear()
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
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
        val cursor = db.query(DBInfo.Table.TB_KEY, null, null, null, null, null, null)

        if (cursor.moveToNext()) {
            key = Key()
            key.key = cursor.getString(cursor.getColumnIndex(Key.KEY))
            key.mode = cursor.getInt(cursor.getColumnIndex(Key.MODE))
        }
        cursor.close()
        db.close()
        return key
    }

    private fun updatePassword(db: SQLiteDatabase, oldKey: Key, oldOriKey: String, newKey: Key, newOriKey: String) {
        val cursor = db.query(DBInfo.Table.TB_PASSWORD, null, null, null, null, null, null)

        val password = Password()
        val cv = ContentValues()
        if (oldKey.mode == Key.MODE_NO_KEY && newKey.mode != Key.MODE_NO_KEY) {
            // 直接加密
            while (cursor!!.moveToNext()) {
                password.id = cursor.getLong(cursor.getColumnIndex(Password.ID))
                password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD))
                cv.clear()
                cv.put(Password.ID, password.id)
                cv.put(Password.PASSWORD, password.password.aesEncrypt(newOriKey))
                db.update(DBInfo.Table.TB_PASSWORD, cv, "${Password.ID} = ?",
                    arrayOf(password.id.toString()))
            }
        } else if (oldKey.mode != Key.MODE_NO_KEY && newKey.mode == Key.MODE_NO_KEY) {
            // 解密后不加密
            while (cursor!!.moveToNext()) {
                password.id = cursor.getLong(cursor.getColumnIndex(Password.ID))
                password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD)).aesDecrypt(oldOriKey)
                cv.clear()
                cv.put(Password.ID, password.id)
                cv.put(Password.PASSWORD, password.password)
                db.update(DBInfo.Table.TB_PASSWORD, cv, "${Password.ID} = ?",
                    arrayOf(password.id.toString()))
            }
        } else if (oldKey.mode != Key.MODE_NO_KEY && newKey.mode != Key.MODE_NO_KEY) {
            // 先解密后加密
            while (cursor!!.moveToNext()) {
                password.id = cursor.getLong(cursor.getColumnIndex(Password.ID))
                password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD)).aesDecrypt(oldOriKey)
                cv.clear()
                cv.put(Password.ID, password.id)
                cv.put(Password.PASSWORD, password.password.aesEncrypt(newOriKey))
                db.update(DBInfo.Table.TB_PASSWORD, cv, "${Password.ID} = ?",
                    arrayOf(password.id.toString()))
            }
        }
        cursor.close()
    }
}