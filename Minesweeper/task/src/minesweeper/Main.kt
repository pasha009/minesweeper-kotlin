package minesweeper

import java.util.Scanner
import kotlin.math.max
import kotlin.math.min

class MineSweeper(private val rows: Int, private val cols: Int, private val numMines: Int) {
    data class Cell(var type: Int, var state: CellState)
    private var field = arrayOf<Array<Cell> >()
    private var extraMarked = 0
    private var minesFound = 0
    private var cellsExplored = 0
    private var validFieldSet = false

    enum class CellState {
        EXPLORED,
        MARKED,
        DEFAULT
    }

    init {
        for (r in 1..rows) {
            field += Array(cols) {Cell(0, CellState.DEFAULT)}
        }
        resetField()
    }

    private fun resetField() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                field[r][c].type = 0
            }
        }

        val mines = (0 until rows * cols).shuffled().take(numMines)
        for (pos in mines) {
            val r = pos / rows
            val c = pos % cols
            field[r][c].type = -1
        }



        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (field[r][c].type == 0) {
                    var nmines = 0
                    val nl = max(c - 1, 0)
                    val nr = min(c + 1, cols - 1)
                    val nu = max(r - 1, 0)
                    val nd = min(r + 1, rows - 1)
                    for (tr in nu..nd) {
                        for (tc in nl..nr) {
                            if (field[tr][tc].type == -1) nmines++
                        }
                    }
                    if (nmines != 0){
                        field[r][c].type = nmines
                    }
                }
            }
        }

        if (extraMarked != 0 || minesFound != 0) {
            extraMarked = 0
            minesFound = 0
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val type = field[r][c].type
                    val state = field[r][c].state
                    if (state == CellState.MARKED) {
                        if (type == -1) minesFound++
                        else extraMarked++
                    }
                }
            }
        }
    }

    fun printField() {
        println()
        // column numbers
        print(" |")
        (1..cols).forEach(::print)
        println("|")
        println("-|${"-".repeat(cols)}|")

        // field with row number
        for (r in 0 until rows) {
            print("${r + 1}|")
            for (c in 0 until cols) {
                when (field[r][c].state) {
                    CellState.DEFAULT -> print('.')
                    CellState.MARKED -> print('*')
                    else -> {
                        when (val k = field[r][c].type) {
                            0 -> print('/')
                            -1 -> print("X")
                            else -> print(k)
                        }
                    }
                }
            }
            println("|")
        }

        // last row
        println("-|${"-".repeat(cols)}|")
    }


    fun finished(): Boolean {
//        println("$minesFound $numMines $extraMarked $cellsExplored")
        val winByMarkingAllMines = minesFound == numMines && extraMarked == 0
        val winByExploringAllCells = cellsExplored == rows * cols - numMines
        return winByExploringAllCells || winByMarkingAllMines
    }

    fun markCell(x: Int, y: Int) {
        val state = field[y - 1][x - 1].state
        if(state == CellState.EXPLORED) return
        val type = field[y - 1][x - 1].type
        if (state == CellState.MARKED) {
            if (type == -1) minesFound--
            else extraMarked--
            field[y - 1][x - 1].state = CellState.DEFAULT
        } else {
            if (type == -1) minesFound++
            else extraMarked++
            field[y - 1][x - 1].state = CellState.MARKED
        }
    }

    fun exploreCell(x: Int, y: Int): Boolean {
        val state = field[y - 1][x - 1].state
        if(state == CellState.EXPLORED) return true
        while (!validFieldSet) {
            val cell = field[y - 1][x - 1].type
            if (cell != -1) validFieldSet = true
            else resetField()
        }
        val type = field[y - 1][x - 1].type
        val marked = state == CellState.MARKED
        field[y - 1][x - 1].state = CellState.EXPLORED

        if (marked && type != -1) extraMarked--
        if (type == -1) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (field[r][c].type == -1) {
                        field[r][c].state = CellState.EXPLORED
                    }
                }
            }
            return false
        }
        cellsExplored++
        if (type == 0) {
            val nl = max(x - 2, 0)
            val nr = min(x, cols - 1)
            val nu = max(y - 2, 0)
            val nd = min(y, rows - 1)
            for (tr in nu..nd) {
                for (tc in nl..nr) {
                    if (field[tr][tc].state != CellState.EXPLORED) {
                        exploreCell(tc + 1, tr + 1)
                    }
                }
            }
        }
        return true
    }

}

fun main() {
    val rows = 9
    val cols = 9
    val scanner = Scanner(System.`in`)

    print("How many mines do you want on the field? ")
    val numMines = scanner.nextInt()
    val mineSweeper = MineSweeper(rows, cols, numMines)

    loop@ while (!mineSweeper.finished()) {
        mineSweeper.printField()
        print("Set/unset mines marks or claim a cell as free: ")
        val xGuess = scanner.nextInt()
        val yGuess = scanner.nextInt()
        when (scanner.next()) {
            "mine" -> mineSweeper.markCell(xGuess, yGuess)
            "free" -> {
                if(!mineSweeper.exploreCell(xGuess, yGuess)) {
                    println("You stepped on a mine and failed!")
                    mineSweeper.printField()
                    break@loop
                }
            }
        }
    }

    if (mineSweeper.finished()) {
        mineSweeper.printField()
        println("Congratulations! You found all mines!")
    }

}
