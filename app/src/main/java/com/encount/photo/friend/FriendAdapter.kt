package com.encount.photo.friend

//import kotlinx.android.synthetic.main.post_list.view.*
//import kotlinx.android.synthetic.main.post_list.view.UserIcon
//import kotlinx.android.synthetic.main.post_list.view.UserId

/**
 * やってること
 * カスタムListViewを適用するためのクラス
 *
 * 製作者：中村
 */

/*
class FriendAdapter(val context: Context, val posts: List<PostList>): BaseAdapter() {

    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return posts.count()
    }

    override fun getItem(position: Int): PostList {
        return posts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = layoutInflater.inflate(R.layout.la_friend_item, parent, false)
        view.FriendName.text   = posts[position].name
        view.UserId.text       = posts[position].userid
        Glide.with(context).load(posts[position].image).into(view.UserIcon)

        return view
    }
}*/

