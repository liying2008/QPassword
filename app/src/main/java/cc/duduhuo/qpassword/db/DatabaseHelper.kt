package cc.duduhuo.qpassword.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Group

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:58
 * Description:
 * Remarks:
 * =======================================================
 */
class DatabaseHelper(private val mContext: Context) : SQLiteOpenHelper(mContext, DBInfo.DB_NAME, null, DBInfo.DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.beginTransaction()
            try {
                //创建数据表
                db.execSQL(DBInfo.Table.TB_KEY_CREATE)
                db.execSQL(DBInfo.Table.TB_PASSWORD_CREATE)
                db.execSQL(DBInfo.Table.TB_GROUP_CREATE)
                // 插入一个默认分组
                val sql = "INSERT INTO ${DBInfo.Table.TB_GROUP}(" + Group.NAME + ") VALUES('" + mContext.getString(R.string.group_default) + "')"
                db.execSQL(sql)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}