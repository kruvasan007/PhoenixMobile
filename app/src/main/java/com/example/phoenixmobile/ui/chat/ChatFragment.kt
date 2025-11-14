package com.example.phoenixmobile.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import com.example.phoenixmobile.ui.chat.adapter.ImprovedMessageAdapter

class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var graphView: OptimizedGraphView
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnClearGraph: Button
    private lateinit var typingIndicator: TypingIndicatorView

    private lateinit var messageAdapter: ImprovedMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupUI()
        observeViewModel()

        // Проверяем, нужно ли отправить базовый запрос
        viewModel.checkForInitialQuery()
    }

    private fun initViews(view: View) {
        graphView = view.findViewById(R.id.graph_view)
        recyclerMessages = view.findViewById(R.id.recycler_messages)
        etMessage = view.findViewById(R.id.et_message)
        btnSend = view.findViewById(R.id.btn_send)
        btnClearGraph = view.findViewById(R.id.btn_clear_graph)
        typingIndicator = view.findViewById(R.id.typing_indicator)
    }

    private fun setupRecyclerView() {
        messageAdapter = ImprovedMessageAdapter()
        recyclerMessages.adapter = messageAdapter
        recyclerMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
    }

    private fun setupUI() {
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                etMessage.setText("")
            }
        }

        btnClearGraph.setOnClickListener {
            viewModel.clearGraph()
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    recyclerMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.triplets.observe(viewLifecycleOwner) { triplets ->
            graphView.setTriplets(triplets)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                typingIndicator.startTypingAnimation()
            } else {
                typingIndicator.stopTypingAnimation()
            }
            btnSend.isEnabled = !loading
            btnClearGraph.isEnabled = !loading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}
