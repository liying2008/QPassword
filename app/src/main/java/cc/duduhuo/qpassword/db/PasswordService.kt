package cc.duduhuo.qpassword.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.util.aesDecrypt
import cc.duduhuo.qpassword.util.aesEncrypt
import cc.duduhuo.qpassword.util.keyLost

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
     * @return 返回这条数据的自增主键 如果插入失败，返回- 1L
     * 如果 Config.mKey 不存在，返回 -2L
     */
    fun insertPassword(password: Password): Long {
        if (keyLost()) {
            return -2L
        }
        val id: Long
        val db = mDbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Password.CREATE_DATE, password.createDate)
        contentValues.put(Password.TITLE, password.title)
        contentValues.put(Password.USERNAME, password.username)
        if (Config.mKey!!.mode == Key.MODE_NO_KEY) {
            contentValues.put(Password.PASSWORD, password.password)
        } else {
            contentValues.put(Password.PASSWORD, password.password.aesEncrypt(Config.mOriKey))
        }
        contentValues.put(Password.EMAIL, password.email)
        contentValues.put(Password.NOTE, password.note)
        contentValues.put(Password.IS_TOP, if (password.isTop) 1 else 0)
        contentValues.put(Password.GROUP_NAME, password.groupName)
        id = db.insert(DBInfo.Table.TB_PASSWORD, null, contentValues)
        contentValues.clear()
        db.close()
        return id
    }

    /**
     * 插入密码List
     * @param passwords 要插入的密码List
     * @return 返回插入成功的数量
     * 如果 Config.mKey 不存在，返回 -2
     */
    fun insertPasswords(passwords: List<Password>): Int {
        if (keyLost()) {
            return -2
        }
        var id = 0
        val db = mDbHelper.writableDatabase
        db.beginTransaction()
        val contentValues = ContentValues()
        try {
            passwords.forEach {
                contentValues.clear()
                contentValues.put(Password.CREATE_DATE, it.createDate)
                contentValues.put(Password.TITLE, it.title)
                contentValues.put(Password.USERNAME, it.username)
                if (Config.mKey!!.mode == Key.MODE_NO_KEY) {
                    contentValues.put(Password.PASSWORD, it.password)
                } else {
                    contentValues.put(Password.PASSWORD, it.password.aesEncrypt(Config.mOriKey))
                }
                contentValues.put(Password.EMAIL, it.email)
                contentValues.put(Password.NOTE, it.note)
                contentValues.put(Password.IS_TOP, if (it.isTop) 1 else 0)
                contentValues.put(Password.GROUP_NAME, it.groupName)
                if (db.insert(DBInfo.Table.TB_PASSWORD, null, contentValues) != -1L) {
                    id++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            contentValues.clear()
            db.close()
        }
        return id
    }

    /**
     * 更新密码
     * @param password 更新的密码数据
     * @return 影响的行数 the number of rows affected
     * 返回-2: 主密码丢失
     */
    fun updatePassword(password: Password): Int {
        val result: Int
        if (keyLost()) {
            return -2
        }
        val db = mDbHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Password.TITLE, password.title)
        contentValues.put(Password.USERNAME, password.username)
        if (Config.mKey!!.mode == Key.MODE_NO_KEY) {
            contentValues.put(Password.PASSWORD, password.password)
        } else {
            contentValues.put(Password.PASSWORD, password.password.aesEncrypt(Config.mOriKey))
        }
        contentValues.put(Password.EMAIL, password.email)
        contentValues.put(Password.NOTE, password.note)
        contentValues.put(Password.IS_TOP, if (password.isTop) 1 else 0)
        contentValues.put(Password.GROUP_NAME, password.groupName)
        result = db.update(DBInfo.Table.TB_PASSWORD, contentValues, "${Password.ID} = ?",
            arrayOf(password.id.toString()))
        db.close()
        return result
    }

    /**
     * 根据id查询数据库中的密码信息
     *
     * @param id
     * @return 查询到密码信息，如果没有该数据，返回 id=-1 的 Password 对象
     * null：表示 主密码丢失
     */
    fun getPassword(id: Long): Password? {
        if (keyLost()) {
            return null
        }
        var password = Password(-1L)
        val db = mDbHelper.writableDatabase
        val cursor = db.query(DBInfo.Table.TB_PASSWORD, null, "${Password.ID} = ?", arrayOf(id.toString()), null, null, null)

        if (cursor.moveToNext()) {
            password = mapPassword(cursor)
        }
        cursor.close()
        db.close()
        return password
    }

    /**
     * 获得数据库中保存的所有密码信息
     *
     * @return 返回数据列表
     * null：主密码丢失
     */
    fun getAllPassword(): List<Password>? {
        if (keyLost()) {
            return null
        }

        val passwords = mutableListOf<Password>()
        val db = mDbHelper.writableDatabase
        var cursor: Cursor? = null
        db.beginTransaction()
        try {
            cursor = db.query(DBInfo.Table.TB_PASSWORD, null, null, null, null, null, null)

            while (cursor.moveToNext()) {
                val password = mapPassword(cursor)
                passwords.add(password)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
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
        val result: Int
        val db = mDbHelper.writableDatabase
        result = db.delete(DBInfo.Table.TB_PASSWORD, "${Password.ID} = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    /**
     * 删除多条密码数据
     *
     * @param passwords 要删除的密码 List
     */
    fun deletePasswords(passwords: List<Password>) {
        val db = mDbHelper.writableDatabase
        db.beginTransaction()
        try {
            passwords.forEach {
                db.delete(DBInfo.Table.TB_PASSWORD, "${Password.ID} = ?", arrayOf(it.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * 根据groupName获得数据库中保存的该分组下所有的密码
     *
     * @param groupName 分组名
     * @return 返回数据列表
     * null：主密码丢失
     */
    fun getAllPasswordByGroupName(groupName: String): List<Password>? {
        if (keyLost()) {
            return null
        }
        val passwords = mutableListOf<Password>()
        val db = mDbHelper.writableDatabase
        var cursor: Cursor? = null
        db.beginTransaction()
        try {
            cursor = db.query(DBInfo.Table.TB_PASSWORD, null, "${Password.GROUP_NAME} = ?", arrayOf(groupName), null, null, null)

            while (cursor.moveToNext()) {
                val password: Password = mapPassword(cursor)
                passwords.add(password)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            cursor?.close()
            db.close()
        }
        return passwords
    }

    private fun mapPassword(cursor: Cursor): Password {
        val password = Password()
        password.id = cursor.getLong(cursor.getColumnIndex(Password.ID))
        password.createDate = cursor.getLong(cursor.getColumnIndex(Password.CREATE_DATE))
        password.title = cursor.getString(cursor.getColumnIndex(Password.TITLE))
        password.username = cursor.getString(cursor.getColumnIndex(Password.USERNAME))
        if (Config.mKey!!.mode == Key.MODE_NO_KEY) {
            password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD))
        } else {
            password.password = cursor.getString(cursor.getColumnIndex(Password.PASSWORD)).aesDecrypt(Config.mOriKey)
        }
        password.email = cursor.getString(cursor.getColumnIndex(Password.EMAIL))
        password.note = cursor.getString(cursor.getColumnIndex(Password.NOTE))
        password.isTop = cursor.getInt(cursor.getColumnIndex(Password.IS_TOP)) == 1
        password.groupName = cursor.getString(cursor.getColumnIndex(Password.GROUP_NAME))
        return password
    }

}