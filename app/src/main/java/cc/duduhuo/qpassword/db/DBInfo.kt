package cc.duduhuo.qpassword.db

import cc.duduhuo.qpassword.bean.Group
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
    val DB_NAME = "password.db"
    /** 数据库版本 */
    val DB_VERSION = 1

    /**
     * 数据库表
     */
    object Table {
        val TB_PASSWORD = "password"
        val TB_GROUP = "password_group"
        val TB_PASSWORD_CREATE = "CREATE TABLE IF NOT EXISTS " + TB_PASSWORD + " (" +
            Password.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Password.CREATE_DATE + " INTEGER NOT NULL," +
            Password.TITLE + " TEXT NOT NULL, " +
            Password.USERNAME + " TEXT NOT NULL," +
            Password.PASSWORD + " TEXT NOT NULL," +
            Password.EMAIL + " TEXT NOT NULL," +
            Password.IS_TOP + " INTEGER DEFAULT 0," +
            Password.NOTE + " TEXT NOT NULL," +
            Password.GROUP_NAME + " TEXT NOT NULL" +
            ");"
        val TB_GROUP_CREATE = "CREATE TABLE IF NOT EXISTS " + TB_GROUP + " (" +
            Group.NAME + " TEXT PRIMARY KEY" +
            ");"
    }
}
