package com.example.randomanimal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = findViewById<TextView>(R.id.text) //テキスト
        val show = findViewById<ImageView>(R.id.showview) //画像を表示するImageView
        val image_drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.stop, null); //待機時画像設定
        val btn = findViewById<Button>(R.id.button) //抽選ボタン
        val url = listOf("https://dog.ceo/api/breeds/image/random",
            "https://thatcopy.pw/catapi/rest/","https://randomfox.ca/floof/") //URL一覧
        val animal_name = listOf("イヌ","ネコ","キツネ") //名前一覧
        val client = OkHttpClient()

        btn.setOnClickListener { //ボタンが押されたら
            show.setImageDrawable(image_drawable)
            val random = url.indices.random() //乱数生成
            val request = Request.Builder() //Okhttp3の準備
                .url(url[random])
                .build()
            //非同期通信でWeb APIにアクセス
            client.newCall(request).enqueue(object:Callback{
                val mainHandler : Handler = Handler(Looper.getMainLooper())
                override fun onFailure(call: Call, e: IOException) { //エラー時の処理
                    Toast.makeText(this@MainActivity,"通信エラーです\n壁紙が取得できませんでした",Toast.LENGTH_LONG).show()
                    Log.d("通信エラー",e.toString())
                }

                override fun onResponse(call: Call, response: Response) { //成功時の処理
                    try{
                        lateinit var res : String
                        when(url[random]){ //URLでパース先を判断
                            "https://dog.ceo/api/breeds/image/random" -> res = JSONObject(response.body!!.string()).getString("message")
                            "https://thatcopy.pw/catapi/rest/" -> res = JSONObject(response.body!!.string()).getString("url")
                            "https://randomfox.ca/floof/" -> res = JSONObject(response.body!!.string()).getString("image")
                            else -> {
                                res = "error"
                                Toast.makeText(this@MainActivity,"予期せぬエラー",Toast.LENGTH_LONG).show()
                            }
                        }
                        Log.d("番号", random.toString())
                        if(res != "error"){ //予期せぬエラー時を除く
                            mainHandler.post(Runnable{
                                text.text = animal_name[random]
                                Glide.with(this@MainActivity) //
                                    .load(res) //JSONからパースしたurlを読み込み
                                    .override(750,800)
                                    .into(show) //ImageViewに表示
                            })
                        }
                    }catch(e: JSONException){ //JSONパース失敗時の処理
                        Toast.makeText(this@MainActivity,"JSON解析エラー",Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}