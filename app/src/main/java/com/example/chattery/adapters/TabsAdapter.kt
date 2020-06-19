package com.example.chattery.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.example.chattery.ui.fragments.ChatsFragment
import com.example.chattery.ui.fragments.FriendsFragment
import com.example.chattery.ui.fragments.RequestsFragment

class TabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val tabsTitles = listOf("Requests","Chats","Friends")

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> RequestsFragment()
            1 -> ChatsFragment()
            2 -> FriendsFragment()
            else -> Fragment()
        }

    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence? = tabsTitles.get(position)
}
