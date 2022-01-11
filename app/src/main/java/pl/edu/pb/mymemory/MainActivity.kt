package pl.edu.pb.mymemory

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.models.MemoryGame
import pl.edu.pb.mymemory.models.UserImageList
import pl.edu.pb.mymemory.utils.EXTRA_BOARD_SIZE
import pl.edu.pb.mymemory.utils.EXTRA_GAME_NAME

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 17300 //it could be random nuber
    }

    //will be set in onCreate method
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private val db = Firebase.firestore

    //is only set when user is playing own custom game
    private var gameName: String? = null
    private var customGameImages: List<String>? = null

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
            //if user select option to change size
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            //if user select option to CustomGame
            R.id.mi_custom -> {
                showCreationDialog()
            }
            //if user select option to download game
            R.id.mi_download -> {
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            //checking if we have recived gameName
            if(customGameName == null) {
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null) {
                Log.e(TAG, "Invalid custom game data from FireStore")
                Snackbar.make(clRoot, "Sorrym we couldn't find any such game, '$customGameName'", Snackbar.LENGTH_LONG).show()
                 return@addOnSuccessListener
            }
            //if successful
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            gameName = customGameName
            customGameImages = userImageList.images
            setupBoard()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Exception when retriving game", exception)
        }
    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board, null)
        showAlertDialog("Fetch memory game", boardDownloadView, View.OnClickListener {
            //grab the value th text of the game name that users wants to download
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }

    private fun showCreationDialog() {
        //creating new view to pick game mode
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)

        //all buttons with game options
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            //Set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            //Navigate to a new activity
            val intent = Intent(this, CreateActivity::class.java)
            //to send additional data
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        //creating new view to pick game mode
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)

        //all buttons with game options
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        //witch one is selected
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
            gameName = null
            customGameImages = null
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
        //display custom gamename
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
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
        memoryGame = MemoryGame(boardSize, customGameImages)


        //Adapter provide a binding for the data set to the views of the RecyclerView | adapt each piece of data into a view
        //MainActivity is context, how many elements is in our grid, list of pictures, instance
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{

            //when user click square
            override fun onCardClicked(position: Int) {
                Log.i(TAG, "Card clicked $position")
                updateGameWithFlip(position)
            }
        } )
        rvBoard.adapter = adapter

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvBoard.setHasFixedSize(true)

        //LayoutManager measures and positions item views
        //MainActivity is context, second argument is number of columns
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