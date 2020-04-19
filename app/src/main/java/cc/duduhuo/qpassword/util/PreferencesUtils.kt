package cc.duduhuo.qpassword.util

import android.content.Context


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 17:33
 * Description: SharedPreferences工具类
 * Remarks:
 * =======================================================
 */

object PreferencesUtils {
    private const val PREFERENCES_NAME = "qpassword"

    /**
     * put string preferences
     *
     * @param context   context
     * @param key The name of the preference to modify
     * @param value The new value for the preference
     */
    fun putString(context: Context, key: String, value: String) {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(key, value).apply()
    }

    /**
     * get string preferences
     *
     * @param context  context
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a string
     */
    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return settings.getString(key, defaultValue)!!
    }

    /**
     * put int preferences
     *
     * @param context  context
     * @param key The name of the preference to modify
     * @param value The new value for the preference
     */
    fun putInt(context: Context, key: String, value: Int) {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt(key, value).apply()
    }

    /**
     * get int preferences
     *
     * @param context  context
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a int
     */
    fun getInt(context: Context, key: String, defaultValue: Int = -1): Int {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return settings.getInt(key, defaultValue)
    }

    /**
     * put long preferences
     *
     * @param context  context
     * @param key The name of the preference to modify
     * @param value The new value for the preference
     */
    fun putLong(context: Context, key: String, value: Long) {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putLong(key, value).apply()
    }

    /**
     * get long preferences
     *
     * @param context  context
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a long
     */
    fun getLong(context: Context, key: String, defaultValue: Long = -1): Long {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return settings.getLong(key, defaultValue)
    }

    /**
     * put float preferences
     *
     * @param context  context
     * @param key The name of the preference to modify
     * @param value The new value for the preference
     */
    fun putFloat(context: Context, key: String, value: Float) {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putFloat(key, value).apply()
    }

    /**
     * get float preferences
     *
     * @param context  context
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a float
     */
    fun getFloat(context: Context, key: String, defaultValue: Float = -1F): Float {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return settings.getFloat(key, defaultValue)
    }

    /**
     * put boolean preferences
     *
     * @param context  context
     * @param key The name of the preference to modify
     * @param value The new value for the preference
     */
    fun putBoolean(context: Context, key: String, value: Boolean) {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(key, value).apply()
    }

    /**
     * get boolean preferences
     *
     * @param context  context
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a boolean
     */
    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val settings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return settings.getBoolean(key, defaultValue)
    }

}
