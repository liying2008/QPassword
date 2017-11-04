package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import cc.duduhuo.qpassword.bean.Password
import java.util.ArrayList

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 17:02
 * Description:
 * Remarks:
 * =======================================================
 */
class PasswordService(context: Context) {
    private val mDbHelper: DatabaseHelper = DatabaseHelper(context)
    /**
     * 插入一条密码
     * @param password 要插入的密码
     * @return 返回这条数据的自增主键 如果插入失败，返回-1
     */
    fun insertPassword(password: Password): Long {
        var id: Long = -1L
        val db = mDbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(Password.CREATE_DATE, password.createDate)
            contentValues.put(Password.TITLE, password.title)
            contentValues.put(Password.USERNAME, password.username)
            contentValues.put(Password.PASSWORD, password.password)
            contentValues.put(Password.EMAIL, password.email)
            contentValues.put(Password.NOTE, password.note)
            contentValues.put(Password.IS_TOP, if (password.isTop) 1 else 0)
            contentValues.put(Password.GROUP_NAME, password.groupName)
            id = db.insert(DBInfo.Table.TB_PASSWORD, null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return id
    }

    /**
     * 更新密码
     * @param password 更新的密码数据
     * @return 影响的行数 the number of rows affected
     */
    fun updatePassword(password: Password): Int {
        var result = 0
        val db = mDbHelper.writableDatabase
        try {
            val contentValues = ContentValues()
            contentValues.put(Password.TITLE, password.title)
            contentValues.put(Password.USERNAME, password.username)
            contentValues.put(Password.PASSWORD, password.password)
            contentValues.put(Password.EMAIL, password.email)
            contentValues.put(Password.NOTE, password.note)
            contentValues.put(Password.IS_TOP, if (password.isTop) 1 else 0)
            contentValues.put(Password.GROUP_NAME, password.groupName)

            result = db.update(DBInfo.Table.TB_PASSWORD, contentValues, "${Password.ID} = ?",
                arrayOf(password.id.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return result
    }

    /**
     * 根据id查询数据库中的密码信息
     *
     * @param id
     * @return 查询到密码信息，如果没有该数据，返回null
     */
    fun getPassword(id: Long): Password? {
        var password: Password? = null
        val db = mDbHelper.writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(DBInfo.Table.TB_PASSWORD, null, "${Password.ID} = ?", arrayOf(id.toString()), null, null, null)

            if (cursor!!.moveToNext()) {
                password = mapPassword(cursor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return password
    }

    private fun mapPassword(cursor: Cursor): Password {
        val password = Password()
        password.id = cursor.getLong(cursor.getColumnIndex(Password.ID))
        password.createDate = cursor.getLong(cursor.getColumnIndex(Password.CREATE_DATE))
        password.title = cursor.getString(cursor.getColumnIndex(Password.TITLE))
        password.username = cursor.getString(cursor.getColumnIndex(Password.USERNAME))
        password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD))
        password.email = cursor.getString(cursor.getColumnIndex(Password.EMAIL))
        password.note = cursor.getString(cursor.getColumnIndex(Password.NOTE))
        password.isTop = cursor.getInt(cursor.getColumnIndex(Password.IS_TOP)) == 1
        password.groupName = cursor.getString(cursor.getColumnIndex(Password.GROUP_NAME))
        return password
    }

    /**
     * 获得数据库中保存的所有密码信息
     *
     * @return 返回数据列表
     */
    fun getAllPassword(): List<Password> {
        val passwords = mutableListOf<Password>()
        val db = mDbHelper.writableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.query(DBInfo.Table.TB_PASSWORD, null, null, null, null, null, null)

            while (cursor!!.moveToNext()) {
                val password = mapPassword(cursor)
                passwords.add(password)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return passwords
    }

    /**
     * 删除一条密码数据
     *
     * @param id 删除的id
     * @return the number of rows affected if a whereClause is passed in, 0
     * otherwise. To remove all rows and get a count pass "1" as the
     * whereClause.
     */
    fun deletePassword(id: Long): Int {
        var result = -1
        val db = mDbHelper.writableDatabase
        result = db.delete(DBInfo.Table.TB_PASSWORD, "${Password.ID} = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    /**
     * 根据groupName获得数据库中保存的该分组下所有的密码
     *
     * @param groupName 分组名
     * @return 返回数据列表
     */
    fun getAllPasswordByGroupName(groupName: String): List<Password> {
        val passwords = ArrayList<Password>()
        val db = mDbHelper.writableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.query(DBInfo.Table.TB_PASSWORD, null, "${Password.GROUP_NAME} = ?", arrayOf(groupName), null, null, null)

            while (cursor!!.moveToNext()) {
                val password: Password = mapPassword(cursor)
                passwords.add(password)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return passwords
    }

}