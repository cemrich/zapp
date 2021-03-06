package de.christinecoenen.code.zapp.app.livestream.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ChannelListAdapter
import de.christinecoenen.code.zapp.app.livestream.ui.list.adapter.ListItemListener
import de.christinecoenen.code.zapp.databinding.FragmentChannelListBinding
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper.isInsideMultiWindow
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager

class ChannelListFragment : Fragment(), ListItemListener {

	companion object {
		fun newInstance() = ChannelListFragment()
	}


	private lateinit var channelList: ISortableChannelList
	private lateinit var gridAdapter: ChannelListAdapter


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		channelList = SortableVisibleJsonChannelList(requireContext())
		gridAdapter = ChannelListAdapter(channelList, this)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val binding = FragmentChannelListBinding.inflate(inflater, container, false)
		val channelGridView = binding.gridviewChannels

		ViewCompat.setNestedScrollingEnabled(channelGridView, true)
		channelGridView.layoutManager = GridAutofitLayoutManager(requireContext(), 320)
		channelGridView.adapter = gridAdapter

		return binding.root
	}

	override fun onStart() {
		super.onStart()

		if (isInsideMultiWindow(requireActivity())) {
			resumeActivity()
		}
	}

	override fun onResume() {
		super.onResume()

		channelList.reloadChannelOrder()

		if (!isInsideMultiWindow(requireActivity())) {
			resumeActivity()
		}
	}

	override fun onPause() {
		super.onPause()

		if (!isInsideMultiWindow(requireActivity())) {
			pauseActivity()
		}
	}

	override fun onStop() {
		super.onStop()

		if (isInsideMultiWindow(requireActivity())) {
			pauseActivity()
		}
	}

	override fun onItemClick(channel: ChannelModel) {
		val intent = ChannelDetailActivity.getStartIntent(context, channel.id)
		startActivity(intent)
	}

	override fun onItemLongClick(channel: ChannelModel, view: View) {
		val menu = PopupMenu(context, view, Gravity.TOP or Gravity.END)
		menu.inflate(R.menu.activity_channel_list_context)
		menu.show()
		menu.setOnMenuItemClickListener { menuItem -> onContextMenuItemClicked(menuItem, channel) }
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem, channel: ChannelModel): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_share -> {
				startActivity(Intent.createChooser(channel.videoShareIntent, getString(R.string.action_share)))
				true
			}
			else -> false
		}
	}

	private fun pauseActivity() {
		gridAdapter.pause()
	}

	private fun resumeActivity() {
		gridAdapter.resume()
	}
}
