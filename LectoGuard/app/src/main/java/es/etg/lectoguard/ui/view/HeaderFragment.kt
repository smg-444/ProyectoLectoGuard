package es.etg.lectoguard.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import es.etg.lectoguard.databinding.FragmentHeaderBinding

class HeaderFragment : Fragment() {
    private var _binding: FragmentHeaderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHeaderBinding.inflate(inflater, container, false)
        val title = arguments?.getString("title") ?: ""
        binding.tvHeaderTitle.text = title
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String): HeaderFragment {
            val fragment = HeaderFragment()
            val args = Bundle()
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }
    }
} 