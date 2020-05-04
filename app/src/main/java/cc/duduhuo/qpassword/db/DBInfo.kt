package cc.duduhuo.qpassword.db

import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.bean.Password


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:23
 * Description: 数据库常量信息
 * Remarks:
 * =======================================================
 */
object DBInfo {

    /** 数据库名称 */
    const val DB_NAME = "password.db"

    /** 当前数据库版本 */
    const val DB_VERSION = 1

    /**
     * 数据库表
     */
    object Table {
        const val TB_KEY = "password_key"
        const val TB_PASSWORD = "password"
        const val TB_GROUP = "password_group"

        const val TB_KEY_CREATE =
            "CREATE TABLE IF NOT EXISTS $TB_KEY (${Key.KEY} TEXT PRIMARY KEY,${Key.MODE} INTEGER NOT NULL);"

        const val TB_PASSWORD_CREATE = "CREATE TABLE IF NOT EXISTS $TB_PASSWORD (" +
                "${Password.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${Password.CREATE_DATE} INTEGER NOT NULL," +
                "${Password.TITLE} TEXT NOT NULL, " +
                "${Password.USERNAME} TEXT NOT NULL," +
                "${Password.PASSWORD} TEXT NOT NULL," +
                "${Password.EMAIL} TEXT NOT NULL," +
                "${Password.IS_TOP} INTEGER DEFAULT 0," +
                "${Password.NOTE} TEXT NOT NULL," +
                "${Password.GROUP_NAME} TEXT NOT NULL" +
                ");"

        const val TB_GROUP_CREATE =
            "CREATE TABLE IF NOT EXISTS $TB_GROUP (${Group.NAME} TEXT PRIMARY KEY);"
    }
}
