package com.example.assignmentplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.assignmentplanner.data.AssignmentEntity
import com.example.assignmentplanner.data.ModuleEntity
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.data.SubtaskEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {

    val allModules: Flow<List<ModuleEntity>> = repository.allModules
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeFilter = MutableStateFlow("All")
    val activeFilter: StateFlow<String> = _activeFilter
    
    @ExperimentalCoroutinesApi
    val filteredAssignments: Flow<List<AssignmentEntity>> = combine(
        repository.allAssignments,
        _searchQuery,
        _activeFilter
    ) { assignments, query, filter ->
        assignments.filter { assignment ->
            val matchesQuery = assignment.title.contains(query, ignoreCase = true) ||
                               assignment.moduleCode.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Pending" -> assignment.status != "COMPLETED"
                "Completed" -> assignment.status == "COMPLETED"
                "Due Soon" -> {
                    // Quick check if within next 48 hours for demo purposes
                    if (assignment.status == "COMPLETED") false
                    else {
                        try {
                            val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            val due = format.parse(assignment.dueDate)
                            if (due != null) {
                                val diff = due.time - System.currentTimeMillis()
                                val hours = diff / (1000 * 60 * 60)
                                hours in 0..48
                            } else false
                        } catch(e: Exception) { false }
                    }
                }
                else -> true // "All"
            }
            matchesQuery && matchesFilter
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        _activeFilter.value = filter
    }

    fun getAssignmentsForModule(moduleCode: String): Flow<List<AssignmentEntity>> {
        return repository.getAssignmentsForModule(moduleCode)
    }
    
    fun getAssignmentByIdFlow(id: Long): Flow<AssignmentEntity?> {
        return repository.getAssignmentByIdFlow(id)
    }

    suspend fun getAssignmentById(id: Long): AssignmentEntity? {
        return repository.getAssignmentById(id)
    }

    fun saveAssignment(assignment: AssignmentEntity, subtasks: List<SubtaskEntity>, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            if (assignment.id == 0L) {
                val newId = repository.insertAssignment(assignment)
                val newSubtasks = subtasks.map { it.copy(assignmentId = newId) }
                repository.insertSubtasks(newSubtasks)
                onSaved(newId)
            } else {
                repository.updateAssignment(assignment)
                // for subtasks, it's slightly trickier on update, but we can handle it
                // let's just insert new ones (ID=0) and update existing
                subtasks.forEach {
                    if (it.id == 0L) {
                        repository.insertSubtask(it.copy(assignmentId = assignment.id))
                    } else {
                        repository.updateSubtask(it)
                    }
                }
                onSaved(assignment.id)
            }
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun getSubtasksForAssignment(assignmentId: Long): Flow<List<SubtaskEntity>> {
        return repository.getSubtasksForAssignment(assignmentId)
    }

    fun updateSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.updateSubtask(subtask)
        }
    }

    fun insertSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.insertSubtask(subtask)
        }
    }
}

class PlannerViewModelFactory(private val repository: PlannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
