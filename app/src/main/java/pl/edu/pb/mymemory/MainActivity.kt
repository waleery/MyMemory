package pl.edu.pb.mymemory

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.models.MemoryGame

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }



    //will be set in onCreate method
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter

    //choosing game mode
    private var boardSize: BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //display different daialog
        when (item.itemId){
            //if id of button == mi_refresh then create Alert and setupBoard(
            R.id.mi_refresh -> {
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?!", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
                return true
            }

            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        //creating new view to pick game mode
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)

        //all buttons with game options
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        //whitch one is selected
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            //Set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    //mehod to create Alert about restart game
    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok") {_, _ ->
                positiveClickListener.onClick(null)
            }.show()

    }

    private fun setupBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Easy: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Easy: 6 x 3"
                tvNumPairs.text = "Easy: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Easy: 6 x 4"
                tvNumPairs.text = "Easy: 0 / 12"
            }
        }

        //setting default textcolor
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))

        //making property MemoryGame
        memoryGame = MemoryGame(boardSize)


        //Adapter provide a binding for the data set to the views of the RecyclerView | adapt each piece of data into a view
        //MainActivity is context, how many elements is in our grid, list of pictures
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                Log.i(TAG, "Card clicked $position")
                updateGameWithFlip(position)
            }
        } )
        rvBoard.adapter = adapter

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvBoard.setHasFixedSize(true)

        //LayoutManager measures and positions item views
        //MainActivity is context, number of columns
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    //updating the memory game with attempted flip at this position
    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (memoryGame.haveWonGame()){
            //Alert the user of an invalid move
            Snackbar.make(clRoot, "You have already won!",  Snackbar.LENGTH_SHORT).show()

            return
        }
        if (memoryGame.isCardFaceUp(position)){
            //Alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid move!",  Snackbar.LENGTH_SHORT).show()
            return
        }

        //Flipping card - return boolean
        if (memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")

            //changing textColor
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full),
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"


            if(memoryGame.haveWonGame())
            {
                Snackbar.make(clRoot, "You won!",  Snackbar.LENGTH_LONG).show()
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        //notifying MemoryBoardAdapt about data change
        adapter.notifyDataSetChanged()
    }
}