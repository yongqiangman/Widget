/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.widget.app

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import com.squareup.picasso.Picasso
import com.yqman.wdiget.HorizontalDotView
import com.yqman.wdiget.HorizontalScrollPage
import com.yqman.wdiget.ToastHelper
import com.yqman.wdiget.recyclerView.item.ItemDividerDecoration
import com.yqman.wdiget.recyclerView.item.ItemLineDecoration
import com.yqman.wdiget.recyclerView.item.SimpleLoadMoreFooter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.title_bar_collapsed_main.*
import kotlinx.android.synthetic.main.title_bar_expanded_main.*
import kotlin.collections.ArrayList



class MainActivity: AppCompatActivity(), HorizontalScrollPage.OnItemClickedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBanner()
        initRecyclerView()
    }

    private fun initBanner() {
        val view : HorizontalScrollPage = findViewById(R.id.scroll_page)
        val dot : HorizontalDotView = findViewById(R.id.dot_page)
        view.setItemClickListener(this)
        view.setItemSelectedListener { selected, sum ->
            dot.updatePos(selected, sum)
        }
        view.setImageLoader { imageView, url ->
            Picasso.get().load(url).into(imageView)
        }
        val list = ArrayList<String>().apply {
            add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1535099255&di=aa27e9eb2b7bce3a1a7b6e60d6c613b8&imgtype=jpg&er=1&src=http%3A%2F%2Fi1.hdslb.com%2Fbfs%2Farchive%2F763293ce06bf1e684ef0ea3da43ae5008d8564b8.jpg")
            add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534504536037&di=685e4c0cb8cbe41d4c6bc89a92267792&imgtype=0&src=http%3A%2F%2Fwww.wallcoo.com%2Fflower%2FAmazing_Color_Flowers_2560x1600_III%2Fwallpapers%2F2560x1600%2FFlowers_Wallpapers_91.jpg")
        }
        view.setImageResource(list)
        Handler().postDelayed({view.setImageResource(ArrayList())}, 2_000)
        Handler().postDelayed({
            list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534504536036&di=0019339b4e285e1a3d279ab9efdc1f36&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20170324%2F663c85eae74f4320a3e382f03af76d52_th.jpg")
            list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534504536036&di=75ef000500890fd5d7c37d0c23e31436&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fe%2F58fb005766f6b.jpg")
            view.setImageResource(list)
        }, 5_000)
        view.registerLifecycleObserver(this)
        initTitleBar()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = ItemDisplayAnimation()
        recyclerView.addItemDecoration(ItemDividerDecoration(30))
        recyclerView.addItemDecoration(ItemLineDecoration(this))
        recyclerView.adapter = SimpleAdapter().apply {
            loadMoreFooter = SimpleLoadMoreFooter(this@MainActivity).apply {
                setLoadMoreListener {
                    ToastHelper.showToast(this@MainActivity, "上拉刷新")
                }
            }
            setNoMoreFooter(LayoutInflater.from(this@MainActivity).inflate(R.layout.no_more_data, null))
            addHeaderView(LayoutInflater.from(this@MainActivity).inflate(R.layout.simple_head, null))
            addFooterView(LayoutInflater.from(this@MainActivity).inflate(R.layout.simple_footer, null))
            enableLoadMoreEvent(true)
            data = ArrayList<Item>().apply {
                add(Item("item 1"))
                add(Item("item 2"))
                add(Item("item 3"))
                add(Item("item 4"))
                add(Item("item 5"))
                add(Item("item 6"))
                add(Item("item 7"))
                add(Item("item 8"))
                add(Item("item 9"))
                add(Item("item 10"))
                add(Item("item 11"))
            }
            setItemClickListener { item, position ->  ToastHelper.showToast(this@MainActivity, "item ${item.name}")}
        }
    }

    override fun onClickItem(pos: Int) {
        Log.d("MainActivity", "pos $pos")
        ToastHelper.showToast(this, "index$pos")
    }

    private fun initTitleBar() {
        val url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534504536036&di=75ef000500890fd5d7c37d0c23e31436&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fe%2F58fb005766f6b.jpg"
        Picasso.get().load(url).into(title_expanded_img)
        collapsibleLayout_main.setOnCollapsibleListener {
            Log.d("MainActivity", "ratio $it")
            title_collapsed_content.alpha = it
        }
    }
}