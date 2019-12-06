package com.example.encount.user

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.encount.LoginDataClassList
import com.example.encount.NavigationActivity
import com.example.encount.R
import com.example.encount.SQLiteHelper
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_user_login.*
import okhttp3.*
import java.io.IOException

/**
 * やってること
 * メアドとパスワードをサーバに送信し、サーバからログイン成功(true)が帰ってきたらホーム画面に飛ばす
 *
 * 製作者：中村
 */

class UserLogin : AppCompatActivity() {

    private val _helper = SQLiteHelper(this@UserLogin)
    var mail = ""
    var pass = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        val progress = findViewById<ProgressBar>(R.id.loginProgress)
        progress.visibility = View.GONE

        loginbtn.setOnClickListener {

            mail = usermail.text.toString()
            pass = userpass.text.toString()
            info.text = ""

            if(mail != "" && pass != ""){

                progress.visibility = View.VISIBLE
                LoginDataPost().execute()
            }
            else{

                info.text = "ユーザーまたはパスワードが入力されていません"
            }
        }

        usernew.setOnClickListener {

            startActivity(Intent(this, UserSingin::class.java))
        }
    }

    private inner class LoginDataPost() : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/UserLogin.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            //formに要素を追加
            formBuilder.add("mail",mail)
            formBuilder.add("pass",pass)
            //リクエストの内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                return response.body()!!.string()
            }
            catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {

            val db = _helper.writableDatabase
            var loginFlag = Gson().fromJson(result, LoginDataClassList::class.java)
            val progress = findViewById<ProgressBar>(R.id.loginProgress)
            progress.visibility = View.GONE

            if(loginFlag.userLoginFlag) {

                val sqlDelete = "delete from userInfo"
                var stmt = db.compileStatement(sqlDelete)
                stmt.executeUpdateDelete()

                val sqlInsert = "insert into userInfo (user_id) values (?)"
                stmt = db.compileStatement(sqlInsert)
                stmt.bindLong(1, loginFlag.userId)
                stmt.executeInsert()
                goProflie()
            }
            else{

                info.text = loginFlag.result
            }
        }
    }

    override fun onDestroy() {

        _helper.close()
        super.onDestroy()
    }

    fun goProflie(){

        startActivity(Intent(this, NavigationActivity::class.java))
    }
}
