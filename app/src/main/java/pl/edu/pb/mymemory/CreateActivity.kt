package pl.edu.pb.mymemory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.utils.EXTRA_BOARD_SIZE
import pl.edu.pb.mymemory.utils.isPermissionGranted
import pl.edu.pb.mymemory.utils.requestPermission
import kotlin.math.log

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTOS_CODE = 12
        private const val READ_EXTERNAL_PHOTOS_CODE = 248
        private const val READ_PHOTOS_PERMISSIONS = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var adapter: ImagePickerAdapter
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

        //CreateActivity is context, choosen images, size of board, instance of the interface
        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener{

            //when user click gray square to select custom image
            override fun onPlaceholderClicked() {
                //checking that we have permissions to read external storage
                if (isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSIONS)){
                    launchIntentForPhotos()
                //asking for permissions
                } else {
                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSIONS, READ_EXTERNAL_PHOTOS_CODE)
                }
            }

        })
        rvImagePicker.adapter = adapter

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvImagePicker.setHasFixedSize(true)

        //LayoutManager measures and positions item views
        //CreateActivity is context, number of columns
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS_CODE){
            if( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()

            //inform user
            } else {
                Toast.makeText(this, "In order to create a custom game, you need to provide access to your photos", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            //finish activity
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //if user choose pics
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTOS_CODE || resultCode != Activity.RESULT_OK || data == null){
            Log.w(TAG, "Did not get data back from the launched activity, user likely canceled flow")
            return
        }
        val selectedUri = data.data    //one image
        val clipData = data.clipData   // multiple images
        if (clipData != null) {
            Log.i(TAG, "clipData num images ${clipData.itemCount}: $ clipData")
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (chosenImageUris.size < numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }
            }
        } else if (selectedUri != null) {
            Log.i(TAG, "data: $selectedUri")
            chosenImageUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun shouldEnableSaveButton(): Boolean {
        //check if we should enable save button
        return true
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)

        //we only care about image files
        intent.type = "image/*"

        //allow to select multiple images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        startActivityForResult(Intent.createChooser(intent,  "Choose pics"), PICK_PHOTOS_CODE)
    }
}