package pl.edu.pb.mymemory

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.utils.EXTRA_BOARD_SIZE

class CreateActivity : AppCompatActivity() {

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button

    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1

    //uniform resource identifier
    private val chosenImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        //dispaly back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //get send back from previous Activity
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()

        // ? - only call this atribute if action bar is not null
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"

        //CreateActivity is context, choosen images, size of board
        rvImagePicker.adapter = ImagePickerAdapter(this, chosenImageUris, boardSize)

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvImagePicker.setHasFixedSize(true)

        //LayoutManager measures and positions item views
        //CreateActivity is context, number of columns
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            //finish activity
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}