package com.sajan.bktguide.rvAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sajan.bktguide.R
import com.sajan.bktguide.utils.ImageSwipeAdapter


class RvCustomAdapter(
    private val context: Context,
    private val itemList: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var account: GoogleSignInAccount? = null
    private var onRvClickListener: OnRvClickListener? = null
    private var PROFILE_VIEW = 0
    private var HEADER_VIEW = 1
    private var TOPIC_VIEW = 2

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            PROFILE_VIEW -> return ProfileViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_profile,
                    parent,
                    false
                ), context
            )
            HEADER_VIEW -> return HeaderViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_header,
                    parent,
                    false
                )
            )
            else -> return TopicViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_item,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemList = itemList[position]
        if (getItemViewType(position) == PROFILE_VIEW) {
            holder as ProfileViewHolder
            holder.tvUsername.text = itemList
            account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                holder.ivEdit.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_eye
                    )
                )
            } else {
                holder.ivEdit.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_edit_v2
                    )
                )
            }
            holder.tvUsername.setOnClickListener {
                onRvClickListener!!.onItemClicked(position)
            }
        }
        if (getItemViewType(position) == HEADER_VIEW) {
            holder as HeaderViewHolder
            holder.tvHeader.text = itemList
            if (holder.tvHeader.text == "Home") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_home
                    )
                )
            }
            if (holder.tvHeader.text == "Background") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_background
                    )
                )
            }

            if (holder.tvHeader.text == "Logout") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_logout
                    )
                )
            }

            if (holder.tvHeader.text == "Service") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_service
                    )
                )
            }

            holder.tvHeader.setOnClickListener {
                onRvClickListener!!.onItemClicked(position)
            }
        }
        if (getItemViewType(position) == TOPIC_VIEW) {
            holder as TopicViewHolder
            holder.tvTopic.text = itemList
            holder.tvTopic.setOnClickListener {
                onRvClickListener!!.onItemClicked(position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                PROFILE_VIEW
            }
            1 -> {
                HEADER_VIEW
            }
            2 -> {
                HEADER_VIEW
            }
            3 -> {
                TOPIC_VIEW
            }
            4 -> {
                HEADER_VIEW
            }
            5 -> {
                TOPIC_VIEW
            }
            6 -> {
                TOPIC_VIEW
            }
            else -> {
                HEADER_VIEW
            }
        }
    }

    fun onSetClickListener(onRvClickListener: OnRvClickListener) {
        this.onRvClickListener = onRvClickListener
    }

    class ProfileViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        private val vpImageSwipe: ViewPager = itemView.findViewById(R.id.vpImageSwipe)
        private val imageSwipeAdapter = ImageSwipeAdapter(context)

        init {
            vpImageSwipe.adapter = imageSwipeAdapter
            vpImageSwipe.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {

                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) { //this is triggered when the switch to a new page is complete
                        val lastPosition = vpImageSwipe.adapter!!.count - 1
                        val currentPosition = vpImageSwipe.currentItem
                        if (currentPosition == lastPosition) {
                            vpImageSwipe.setCurrentItem(0, false) //false so we don't animate
                        }
                    }
                }
            })
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
    }

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTopic: TextView = itemView.findViewById(R.id.tvTopic)
    }

    interface OnRvClickListener {
        fun onItemClicked(position: Int)
    }
}
