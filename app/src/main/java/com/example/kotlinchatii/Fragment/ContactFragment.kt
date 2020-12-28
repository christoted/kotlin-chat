package com.example.kotlinchatii.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinchatii.activity.MessageActivity
import com.example.kotlinchatii.Adapter.ContactAdapter
import com.example.kotlinchatii.model.Friend
import com.example.kotlinchatii.R
import kotlinx.android.synthetic.main.fragment_contact.*

private const val ARG_PARAM1 = "param1"

class ContactFragment : Fragment(), ContactAdapter.ContactItemListener {

    private var listFriends: ArrayList<Friend>? = null
    private lateinit var contactAdapter : ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listFriends = it.getParcelableArrayList(ARG_PARAM1)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contact, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        contactAdapter = ContactAdapter(listFriends!!, requireActivity(), this)
        setupRecyclerView()


        toolbar.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId) {
                R.id.searchPerson -> {
                    setHasOptionsMenu(true)
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.top_app_bar, menu)
        inflater.inflate(R.menu.top_app_bar, menu)

        val search = menu.findItem(R.id.searchPerson)
        val searchView = search as SearchView

        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.d("test-view", "onQueryTextSubmit: $p0")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.d("test-view", "onQueryTextSubmit: $p0")
                return true
            }

        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<Friend>) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                }
            }
    }

    private fun setupRecyclerView(){
        recyclerViewContact.apply {
            adapter = contactAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

    }

    override fun onContactItemClick(position: Int) {
        val friend = contactAdapter.listFriend[position]
//        val bundle = Bundle().apply {
//            putParcelable(MessageActivity.KEY_FRIEND, friend)
//        }
        Log.d("his-id-contact", "onContactItemClick: ${friend.uid}")
        val intent = Intent(activity, MessageActivity::class.java)
        intent.putExtra("friend", friend)
        startActivity(intent)
    }

}