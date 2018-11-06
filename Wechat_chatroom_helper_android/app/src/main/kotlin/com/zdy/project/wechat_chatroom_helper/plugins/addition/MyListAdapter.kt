package com.zdy.project.wechat_chatroom_helper.plugins.addition

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zdy.project.wechat_chatroom_helper.plugins.addition.hook.DataBaseHook
import de.robv.android.xposed.XposedHelpers
import java.util.*

class MyListAdapter(val context: Activity, val data: MutableList<DataModel>, val C: Clazz) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val context = parent.context as Context

        val itemView = LinearLayout(context)
        val data = data[position]

        itemView.gravity = Gravity.CENTER_VERTICAL
        itemView.orientation = LinearLayout.HORIZONTAL

        val name = TextView(context)
        name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)
        name.maxLines = 1
        val addition = TextView(context)
        addition.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        addition.maxLines = 1


        name.text = data.sayhiuser
        addition.text = if (data.isAdd == 0) "未添加" else "已添加"
        addition.setTextColor(if (data.isAdd == 0) 0xFFFF8888.toInt() else 0xFF40C040.toInt())

        addition.setPadding(0, 0, 0, 0)

        itemView.addView(name)
        itemView.addView(addition)

        itemView.setOnClickListener {

            val addContactClass = C.m
            val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
            constructor.isAccessible = true
            val m = constructor.newInstance(data.sayhiuser, data.ticket, data.scene)
            val auDF = XposedHelpers.callStaticMethod(C.au, "DF")
            XposedHelpers.callMethod(auDF, "a", m, 0)
        }

        return itemView
    }

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = data.size

    fun addAllContact() {

        val data = data.filter { it.isAdd == 0 }

        if (data.isEmpty()) return

        val timer = Timer()

        val timerTask = object : TimerTask() {

            var position = 0

            override fun run() {
                if (position == data.size) {

                    timer.cancel()
                    cancel()

                    return
                }

                val item = data[position]
                val addContactClass = C.m
                val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
                constructor.isAccessible = true
                val m = constructor.newInstance(item.sayhiuser, item.ticket, item.scene)
                val auDF = XposedHelpers.callStaticMethod(C.au, "DF")
                XposedHelpers.callMethod(auDF, "a", m, 0)

                Handler(context.mainLooper).post { Toast.makeText(context, "当前第${position} 个，共${data.size}个, 当前间隔 ${SpecialPluginEntry.time} 毫秒", Toast.LENGTH_SHORT).show() }

                position++
            }
        }
        timer.schedule(timerTask, 0, SpecialPluginEntry.time)

    }

    fun clearAllContact() {
        val data = data.filter { it.isAdd == 1 }
        if (data.isEmpty()) return

        data.forEach {
            val sayhiuser = it.sayhiuser

            try {
                XposedHelpers.callMethod(DataBaseHook.msgDataBase, "delete",
                        "LBSVerifyMessage", "sayhiuser=?", arrayOf(sayhiuser))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        Handler(context.mainLooper).postDelayed(object : Runnable {
            override fun run() {
                context.finish()
                context.startActivity(Intent(context, C.NearbySayHiListUI))
            }

        }, 500)

    }

    inner class AddHandler(var list: List<DataModel>, var context: Context) : Handler(context.mainLooper) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val index = msg.what

            if (index < list.size) {

                val item = list.get(index)

                val addContactClass = C.m
                val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
                constructor.isAccessible = true
                val m = constructor.newInstance(item.sayhiuser, item.ticket, item.scene)
                val auDF = XposedHelpers.callStaticMethod(C.au, "DF")
                XposedHelpers.callMethod(auDF, "a", m, 0)

                Toast.makeText(context, "当前第$index 个，共${list.size}个, 当前间隔 ${SpecialPluginEntry.time} 毫秒", Toast.LENGTH_SHORT).show()

                if (index < list.size - 1)
                    sendMessageDelayed(Message.obtain(this, index + 1), SpecialPluginEntry.time)
            }
        }
    }


    companion object {

        fun getDialogView(context: Context): ViewGroup {

            val listView = ListView(context)
            listView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600)
            listView.id = android.R.id.list

            val editText = EditText(context)
            editText.id = android.R.id.edit
            editText.background = null
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.setPadding(0, 28, 0, 8)


            val container = LinearLayout(context)
            container.orientation = LinearLayout.VERTICAL

            container.setPadding(100, 60, 100, 60)
            container.addView(listView)
            container.addView(editText)

            return container
        }
    }

}