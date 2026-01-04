package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityReadingListsBinding
import es.etg.lectoguard.databinding.DialogCreateListBinding
import es.etg.lectoguard.domain.model.ReadingList
import es.etg.lectoguard.ui.viewmodel.ReadingListViewModel
import es.etg.lectoguard.utils.PrefsHelper
import androidx.fragment.app.commit
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingListsActivity : BaseActivity() {
    private lateinit var binding: ActivityReadingListsBinding
    private val viewModel: ReadingListViewModel by viewModels()
    private lateinit var adapter: ReadingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Mis Listas"))
        }

        // Configurar RecyclerView
        binding.rvReadingLists.layoutManager = LinearLayoutManager(this)
        adapter = ReadingListAdapter(
            emptyList(),
            onListClick = { list ->
                // Abrir detalle de la lista
                val intent = Intent(this, ReadingListDetailActivity::class.java)
                intent.putExtra("listId", list.id)
                startActivity(intent)
            },
            onListLongClick = { list ->
                showListOptionsDialog(list)
            }
        )
        binding.rvReadingLists.adapter = adapter

        // Observar listas del usuario
        lifecycleScope.launch {
            viewModel.userReadingLists.collect { lists ->
                adapter.updateLists(lists)
                binding.tvEmpty.visibility = if (lists.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Botón para crear nueva lista
        binding.fabAddList.setOnClickListener {
            showCreateListDialog()
        }

        // Cargar listas del usuario
        val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: 
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid != null) {
            viewModel.loadUserReadingLists(firebaseUid)
        } else {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showCreateListDialog() {
        val dialogBinding = DialogCreateListBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Crear Nueva Lista")
            .setView(dialogBinding.root)
            .setPositiveButton("Crear", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etListName.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                val isPublic = dialogBinding.cbPublic.isChecked

                if (name.isEmpty()) {
                    dialogBinding.etListName.error = "El nombre es obligatorio"
                    return@setOnClickListener
                }

                val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: 
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (firebaseUid == null) {
                    Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newList = ReadingList(
                    userId = firebaseUid,
                    name = name,
                    description = description,
                    isPublic = isPublic
                )

                viewModel.saveReadingList(newList)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showListOptionsDialog(list: ReadingList) {
        val options = arrayOf("Editar", "Eliminar", if (list.isPublic) "Hacer privada" else "Hacer pública")
        
        AlertDialog.Builder(this)
            .setTitle(list.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Editar
                        showEditListDialog(list)
                    }
                    1 -> {
                        // Eliminar
                        showDeleteConfirmationDialog(list)
                    }
                    2 -> {
                        // Cambiar visibilidad
                        val updatedList = list.copy(isPublic = !list.isPublic)
                        viewModel.saveReadingList(updatedList)
                    }
                }
            }
            .show()
    }

    private fun showEditListDialog(list: ReadingList) {
        val dialogBinding = DialogCreateListBinding.inflate(layoutInflater)
        dialogBinding.etListName.setText(list.name)
        dialogBinding.etDescription.setText(list.description)
        dialogBinding.cbPublic.isChecked = list.isPublic

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Lista")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etListName.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                val isPublic = dialogBinding.cbPublic.isChecked

                if (name.isEmpty()) {
                    dialogBinding.etListName.error = "El nombre es obligatorio"
                    return@setOnClickListener
                }

                val updatedList = list.copy(
                    name = name,
                    description = description,
                    isPublic = isPublic
                )

                viewModel.saveReadingList(updatedList)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(list: ReadingList) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Lista")
            .setMessage("¿Estás seguro de que quieres eliminar la lista \"${list.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: 
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (firebaseUid != null) {
                    viewModel.deleteReadingList(list.id, firebaseUid)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar listas al volver a la actividad
        val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: 
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid != null) {
            viewModel.loadUserReadingLists(firebaseUid)
        }
    }
}

