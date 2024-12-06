package com.android.unio.model.association

import android.content.Context
import android.util.Log
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CreateAssociationTools {

    private fun parseLineToAssociation(line: String): Association? {
        try {
            // Here is the format in of a csv line :
            // uid,url,name,fullName,category,description,followersCount,image,principalEmailAddress
            val parts = line.split(",")

            if (parts.size < 9) return null // skip incomplete lines

            val uid = parts[0]
            val url = parts[1]
            val name = parts[2]
            val fullName = parts[3]
            val category = AssociationCategory.valueOf(parts[4])
            val description = parts[5]
            val followersCount = parts[6].toIntOrNull() ?: 0
            val image = parts[7]
            val principalEmailAddress = parts[8]

            return Association(
                uid = uid,
                url = url,
                name = name,
                fullName = fullName,
                category = category,
                description = description,
                followersCount = followersCount,
                members = emptyList(), //does not need it for the creation
                roles = emptyList(), //does not need it for the creation
                image = image,
                events = Event.emptyFirestoreReferenceList(),
                principalEmailAddress = principalEmailAddress
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parsing line: $line", e)
            return null
        }
    }

    private fun processAndSaveAssociations(
        inputStream: InputStream,
        saveAssociation: (Association, InputStream?) -> Unit,
        context: Context
    ) {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        try {
            bufferedReader.useLines { lines ->
                lines.forEach { line ->
                    val association = parseLineToAssociation(line)
                    association?.let {


                        val resourceId = context.resources.getIdentifier(it.image, "raw", context.packageName)

                        if (resourceId != 0) {
                            val imageStream = try {
                                context.resources.openRawResource(resourceId)
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Error opening resource: ${it.image}", e)
                                null
                            }
                            saveAssociation(it, imageStream)
                        } else {
                            Log.e("MainActivity", "Resource not found: ${it.image}")
                        }


                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error processing file", e)
        }
    }

    // IN ORDER TO MAKE IT WORK, CALL THIS INTO MAINACTIVITY UnioApp function :
    /*
    val context = LocalContext.current

    LaunchedEffect(Unit) {

    withContext(Dispatchers.IO) {
      delay(30000)

      val inputStream = context.resources.openRawResource(R.raw.associations)
      processAndSaveAssociations(inputStream, { association, imageStream ->
        associationViewModel.saveAssociation(
          association,
          imageStream = imageStream,
          onSuccess = { Log.i("UnioApp", "Saved association: ${association.name}") },
          onFailure = { exception ->
            Log.e("UnioApp", "Failed to save association", exception)
          }
        )
      }, context)
    }
  }*/
}