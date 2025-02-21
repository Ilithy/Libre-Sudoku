package com.kaajjo.libresudoku.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaajjo.libresudoku.core.Cell
import com.kaajjo.libresudoku.core.qqwing.GameDifficulty
import com.kaajjo.libresudoku.core.qqwing.GameType
import com.kaajjo.libresudoku.core.qqwing.QQWingController
import com.kaajjo.libresudoku.core.utils.SudokuParser
import com.kaajjo.libresudoku.data.database.model.SudokuBoard
import com.kaajjo.libresudoku.domain.repository.BoardRepository
import com.kaajjo.libresudoku.domain.repository.SavedGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val boardRepository: BoardRepository,
    private val savedGameRepository: SavedGameRepository
) : ViewModel() {

    val lastSavedGame = savedGameRepository.getLast()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var insertedBoardUid = -1L

    private val difficulties = listOf(
        GameDifficulty.Easy,
        GameDifficulty.Moderate,
        GameDifficulty.Hard,
        GameDifficulty.Challenge,
    )
    var selectedDifficulty by mutableStateOf(difficulties.first())

    private val types = listOf(
        GameType.Default9x9,
        GameType.Default6x6,
        GameType.Default12x12
    )
    var selectedType by mutableStateOf(types.first())

    var isGenerating by mutableStateOf(false)
    var isSolving by mutableStateOf(false)
    var readyToPlay by mutableStateOf(false)

    private var puzzle = List(selectedType.size) { row -> List(selectedType.size) { col -> Cell(row, col, 0) } }
    private var solvedPuzzle = List(selectedType.size) { row -> List(selectedType.size) { col -> Cell(row, col, 0) } }


    fun startGame() {
        isSolving = false
        isGenerating = false

        val gameTypeToGenerate = selectedType
        val gameDifficultyToGenerate = selectedDifficulty
        val size = gameTypeToGenerate.size

        puzzle = List(size) { row -> List(size) { col -> Cell(row, col, 0) } }
        solvedPuzzle = List(size) { row -> List(size) { col -> Cell(row, col, 0) } }

        viewModelScope.launch(Dispatchers.Default) {
            val qqWingController = QQWingController()

            // generating
            isGenerating = true
            val generated = qqWingController.generate(gameTypeToGenerate, gameDifficultyToGenerate)
            isGenerating = false

            isSolving = true
            val solved = qqWingController.solve(generated, gameTypeToGenerate)
            isSolving = false

            if (!qqWingController.isImpossible && qqWingController.solutionCount == 1) {
                for (i in 0 until size) {
                    for (j in 0 until size) {
                        puzzle[i][j].value = generated[i * size + j]
                        solvedPuzzle[i][j].value = solved[i * size + j]
                    }
                }

                withContext(Dispatchers.IO) {
                    val sudokuParser = SudokuParser()
                    insertedBoardUid = boardRepository.insert(
                        SudokuBoard(
                            uid = 0,
                            initialBoard = sudokuParser.boardToString(puzzle),
                            solvedBoard = sudokuParser.boardToString(solvedPuzzle),
                            difficulty = selectedDifficulty,
                            type = selectedType
                        )
                    )
                }

                readyToPlay = true
            }
        }
    }

    fun changeDifficulty(diff: Int) {
        val indexToSet = difficulties.indexOf(selectedDifficulty) + diff
        if (indexToSet >= 0 && indexToSet < difficulties.count()) {
            selectedDifficulty = difficulties[indexToSet]
        }
    }

    fun changeType(diff: Int) {
        val indexToSet = types.indexOf(selectedType) + diff
        if (indexToSet >= 0 && indexToSet < types.count()) {
            selectedType = types[indexToSet]
        }
    }

    fun giveUpLastGame() {
        viewModelScope.launch(Dispatchers.IO) {
            lastSavedGame.value?.let {
                if (!it.completed) {
                    savedGameRepository.update(
                        it.copy(
                            completed = true,
                            canContinue = true
                        )
                    )
                }
            }
        }
    }
}