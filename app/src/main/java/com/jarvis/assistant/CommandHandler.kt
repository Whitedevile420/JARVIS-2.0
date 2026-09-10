package com.jarvis.assistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import java.util.Locale

object CommandHandler {

    private val CALL_WORDS = arrayOf(
        "call", "कॉल", "phone", "फोन", "phone karo", "फोन करो",
        "phone lagao", "फोन लगाओ", "call karo", "कॉल करो",
        "call kar do", "कॉल कर दो", "call laga", "कॉल लगा",
        "phone laga", "फोन लगा"
    )

    private val DIAL_WORDS = arrayOf(
        "dial", "डायल", "number dial", "नंबर डायल"
    )

    private val END_CALL_WORDS = arrayOf(
        "end call", "hang up", "call end", "disconnect call",
        "call kaato", "call katao", "call kato",
        "कॉल काटो", "कॉल काट दो", "कॉल बंद करो",
        "फोन काटो", "फोन काट दो", "फोन बंद करो"
    )

    private val MESSAGE_WORDS = arrayOf(
        "message", "sms", "text", "मैसेज", "संदेश",
        "मैसेज भेजो", "मैसेज करो", "message bhejo", "message karo"
    )

    private val MESSAGE_SEPARATOR_WORDS = arrayOf(
        "saying", "say", "keh do", "kehna", "bolo", "bol do",
        "likho", "likh do", "कि", "कहो", "कह दो",
        "बोलो", "बोल दो", "लिखो", "लिख दो"
    )

    private val SAVE_CONTACT_WORDS = arrayOf(
        "save contact", "add contact", "contact save", "contact banao",
        "new contact", "naya contact", "नया contact", "नया कॉन्टैक्ट",
        "कॉन्टैक्ट सेव", "कॉन्टैक्ट बनाओ", "contact जोड़ो", "contact jodo"
    )

    private val DELETE_CONTACT_WORDS = arrayOf(
        "delete contact", "remove contact", "contact delete", "contact hatao",
        "delete number", "number delete", "कॉन्टैक्ट हटाओ",
        "कॉन्टैक्ट डिलीट", "नंबर हटाओ"
    )

    private val SEARCH_CONTACT_WORDS = arrayOf(
        "find contact", "search contact", "contact number", "number of",
        "find number", "search number", "contact khojo", "number khojo",
        "कॉन्टैक्ट खोजो", "नंबर खोजो", "कॉन्टैक्ट नंबर"
    )

    private val CONTACT_WORDS = arrayOf(
        "contact", "contacts", "कॉन्टैक्ट", "कॉन्टेक्ट"
    )

    private val NUMBER_WORDS = arrayOf(
        "number", "नंबर", "phone number", "फोन नंबर"
    )

    private val CAMERA_WORDS = arrayOf(
        "camera", "कैमरा", "open camera", "कैमरा खोलो"
    )

    private val PHOTO_WORDS = arrayOf(
        "photo", "picture", "pic", "take photo", "click photo",
        "photo lo", "photo lena", "फोटो", "तस्वीर", "फोटो लो",
        "फोटो लेना", "फोटो खींचो", "फोटो खींचना", "फोटो क्लिक करो"
    )

    private val SELFIE_WORDS = arrayOf(
        "selfie", "take selfie", "click selfie",
        "सेल्फी", "सेल्फी लो", "सेल्फी लेना"
    )

    private val YOUTUBE_WORDS = arrayOf("youtube", "यूट्यूब")

    private val WHATSAPP_WORDS = arrayOf(
        "whatsapp", "व्हाट्सएप", "व्हाट्सप्प", "व्हाट्स ऐप"
    )

    private val CHROME_WORDS = arrayOf("chrome", "क्रोम")

    private val GOOGLE_WORDS = arrayOf("google", "गूगल")

    private val SEARCH_WORDS = arrayOf(
        "search", "सर्च", "खोजो", "खोज", "ढूंढो", "ढूंढ"
    )

    private val LIST_APP_WORDS = arrayOf(
        "list apps", "show apps", "installed apps", "apps dikhao", "app dikhao",
        "installed application", "मेरे ऐप दिखाओ", "इंस्टॉल ऐप दिखाओ", "ऐप दिखाओ"
    )

    private val OPEN_WORDS = arrayOf(
        "open", "launch", "start", "खोलो", "खोल", "खोलिए", "चलाओ"
    )

    private val CLOSE_WORDS = arrayOf(
        "close", "force close", "stop app", "band", "bandh",
        "बंद", "बंद करो", "ऐप बंद करो"
    )

    private val WIFI_WORDS = arrayOf(
        "wifi", "wi-fi", "वाईफाई", "वाई-फाई", "वाई फाई"
    )

    private val BLUETOOTH_WORDS = arrayOf("bluetooth", "ब्लूटूथ")

    private val FILLER_WORDS = arrayOf(
        "please", "the", "a", "an", "to", "of", "for", "me", "my",
        "mujhe", "mujhko", "mujhse", "ko", "se", "ka", "ki", "ke",
        "mein", "me", "par", "karo", "kar", "kijiye", "dijiye",
        "kro", "kr", "lagao", "lagana", "karna", "do", "de",
        "करो", "कर", "कीजिए", "दीजिए", "मुझे", "मुझको", "को", "से",
        "का", "की", "के", "में", "पर", "लगाओ", "लगाना", "करना",
        "दो", "दे", "कर दो", "करदो"
    )

    private data class AppEntry(
        val label: String,
        val packageName: String
    )

    private var appCache = ArrayList<AppEntry>()
    private var appCacheTime = 0L
    private const val APP_CACHE_TIME = 30000L

    fun process(
        context: Context,
        rawCommand: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        val command = rawCommand.trim()

        if (command.isEmpty()) {
            speak("I did not hear anything.")
            return
        }

        when {

            containsAny(command, LIST_APP_WORDS) -> {
                listInstalledApps(context, speak)
            }

            containsAny(command, END_CALL_WORDS) -> {
                speak("Ending an active phone call requires JARVIS to be the default phone dialer.")
            }

            containsAny(command, SELFIE_WORDS) || containsAny(command, PHOTO_WORDS) -> {
                takePhoto(context, command, speak, requestPermission)
            }

            containsAny(command, CAMERA_WORDS) -> {
                openCamera(context, speak, requestPermission)
            }

            containsAny(command, SAVE_CONTACT_WORDS) -> {
                saveContact(context, command, speak)
            }

            containsAny(command, DELETE_CONTACT_WORDS) -> {
                deleteContact(context, command, speak, requestPermission)
            }

            containsAny(command, SEARCH_CONTACT_WORDS) ||
                    (containsAny(command, CONTACT_WORDS) && containsAny(command, SEARCH_WORDS)) -> {
                searchContact(context, command, speak, requestPermission)
            }

            containsAny(command, MESSAGE_WORDS) -> {
                sendMessage(context, command, speak, requestPermission)
            }

            containsAny(command, YOUTUBE_WORDS) -> {

                val query = extractQuery(
                    command,
                    YOUTUBE_WORDS + OPEN_WORDS + SEARCH_WORDS
                )

                if (query.isEmpty()) {
                    openApp(context, "com.google.android.youtube", "YouTube", speak)
                } else {
                    searchYoutube(context, query, speak)
                }
            }

            containsAny(command, WHATSAPP_WORDS) -> {
                openApp(context, "com.whatsapp", "WhatsApp", speak)
            }

            containsAny(command, WIFI_WORDS) -> {
                openWifiSettings(context, speak)
            }

            containsAny(command, BLUETOOTH_WORDS) -> {
                openBluetoothSettings(context, speak)
            }

            containsAny(command, DIAL_WORDS) -> {

                val number = extractRealPhoneNumber(command)

                if (number.isEmpty()) {
                    speak("Please tell me a valid phone number.")
                } else {
                    dialNumber(context, number, speak)
                }
            }

            containsAny(command, CALL_WORDS) -> {
                callFromVoiceCommand(context, command, speak, requestPermission)
            }

            containsAny(command, CHROME_WORDS) ||
                    containsAny(command, GOOGLE_WORDS) ||
                    containsAny(command, SEARCH_WORDS) -> {

                val query = extractQuery(
                    command,
                    CHROME_WORDS + GOOGLE_WORDS + OPEN_WORDS + SEARCH_WORDS
                )

                if (query.isEmpty()) {
                    openApp(context, "com.android.chrome", "Chrome", speak)
                } else {
                    searchChrome(context, query, speak)
                }
            }

            containsAny(command, OPEN_WORDS) -> {

                val name = extractQuery(command, OPEN_WORDS + FILLER_WORDS)

                findAndLaunchApp(context, name, speak)
            }

            containsAny(command, CLOSE_WORDS) -> {
                speak("Android does not allow a normal application to force close another application.")
            }

            else -> {
                speak("Sorry, I did not understand that command.")
            }
        }
    }

    private fun callFromVoiceCommand(
        context: Context,
        command: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        val directNumber = extractRealPhoneNumber(command)

        if (directNumber.length >= 7) {
            makeCall(context, "the number", directNumber, speak, requestPermission)
            return
        }

        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_CONTACTS)
            return
        }

        val contacts = findMatchingContacts(context, command)

        if (contacts.isEmpty()) {

            val requestedName = extractContactName(command)

            if (requestedName.isEmpty()) {
                speak("Please tell me the contact name.")
            } else {
                speak("I could not find $requestedName in your contacts.")
            }

            return
        }

        val bestContact = contacts[0]

        makeCall(context, bestContact.first, bestContact.second, speak, requestPermission)
    }

    private fun findMatchingContacts(
        context: Context,
        spokenCommand: String
    ): ArrayList<Pair<String, String>> {

        val result = ArrayList<Pair<String, String>>()

        val commandName = extractContactName(spokenCommand)

        if (commandName.isEmpty()) {
            return result
        }

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        ) ?: return result

        val exactMatches = ArrayList<Pair<String, String>>()
        val partialMatches = ArrayList<Pair<String, String>>()

        cursor.use {

            while (it.moveToNext()) {

                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                if (nameIndex < 0 || numberIndex < 0) {
                    continue
                }

                val contactName = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                if (contactName.equals(commandName, ignoreCase = true)) {
                    addUniqueContact(exactMatches, contactName, number)
                    continue
                }

                if (contactName.contains(commandName, ignoreCase = true)) {
                    addUniqueContact(partialMatches, contactName, number)
                    continue
                }

                if (commandName.contains(contactName, ignoreCase = true)) {
                    addUniqueContact(partialMatches, contactName, number)
                }
            }
        }

        for (contact in exactMatches) {
            result.add(contact)
        }

        for (contact in partialMatches) {

            val exists = result.any {
                it.first.equals(contact.first, ignoreCase = true) && it.second == contact.second
            }

            if (!exists) {
                result.add(contact)
            }
        }

        return result
    }

    private fun addUniqueContact(
        list: ArrayList<Pair<String, String>>,
        name: String,
        number: String
    ) {

        val exists = list.any {
            it.first.equals(name, ignoreCase = true) && it.second == number
        }

        if (!exists) {
            list.add(Pair(name, number))
        }
    }

    private fun extractContactName(command: String): String {

        var text = command.trim()

        for (word in CALL_WORDS) {
            text = text.replace(word, " ", ignoreCase = true)
        }

        for (word in DIAL_WORDS) {
            text = text.replace(word, " ", ignoreCase = true)
        }

        for (word in FILLER_WORDS) {
            text = text.replace(word, " ", ignoreCase = true)
        }

        for (word in CONTACT_WORDS) {
            text = text.replace(word, " ", ignoreCase = true)
        }

        text = text.replace(Regex("[,\\.\\?!:;#()\\[\\]{}]"), " ")

        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }

        val nameTokens = tokens.filter { !isOnlyDigits(it) }

        return nameTokens.joinToString(" ").trim()
    }

    private fun makeCall(
        context: Context,
        name: String,
        number: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.CALL_PHONE)
            return
        }

        val cleanNumber = cleanPhoneNumber(number)

        if (cleanNumber.isEmpty()) {
            speak("The contact does not have a valid phone number.")
            return
        }

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Calling $name.")
        } catch (e: Exception) {
            speak("I could not start the call.")
        }
    }

    private fun extractRealPhoneNumber(command: String): String {

        val builder = StringBuilder()

        for (char in command) {
            if (char.isDigit()) {
                builder.append(char)
            }
        }

        val digits = builder.toString()

        if (digits.length < 7) {
            return ""
        }

        return digits
    }

    private fun cleanPhoneNumber(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }

    private fun isOnlyDigits(text: String): Boolean {

        if (text.isEmpty()) {
            return false
        }

        for (char in text) {
            if (!char.isDigit()) {
                return false
            }
        }

        return true
    }

    private fun dialNumber(
        context: Context,
        number: String,
        speak: (String) -> Unit
    ) {

        val cleaned = cleanPhoneNumber(number)

        if (cleaned.length < 7) {
            speak("That does not look like a valid phone number.")
            return
        }

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Opening the dialer.")
        } catch (e: Exception) {
            speak("I could not open the dialer.")
        }
    }

    private fun searchContact(
        context: Context,
        command: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_CONTACTS)
            return
        }

        val name = stripWords(
            command,
            SEARCH_CONTACT_WORDS + CONTACT_WORDS + SEARCH_WORDS + FILLER_WORDS
        )

        if (name.isEmpty()) {
            speak("Which contact should I search for?")
            return
        }

        val number = findPhoneNumber(context, name)

        if (number == null) {
            speak("I could not find a contact named $name.")
        } else {
            speak("$name's number is $number")
        }
    }

    private fun findPhoneNumber(context: Context, name: String): String? {

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
            arrayOf("%$name%"),
            null
        ) ?: return null

        var result: String? = null

        cursor.use {

            if (it.moveToFirst()) {

                val index = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                if (index >= 0) {
                    result = it.getString(index)
                }
            }
        }

        return result
    }

    private fun saveContact(
        context: Context,
        command: String,
        speak: (String) -> Unit
    ) {

        val numberIndex = firstIndexOfAny(command, NUMBER_WORDS)

        if (numberIndex < 0) {
            speak("Please say the contact name and number.")
            return
        }

        val namePart = command.substring(0, numberIndex)
        val numberPart = command.substring(numberIndex)

        val name = stripWords(namePart, SAVE_CONTACT_WORDS + CONTACT_WORDS + FILLER_WORDS)

        val number = stripWords(numberPart, NUMBER_WORDS + FILLER_WORDS).replace(" ", "")

        if (name.isEmpty() || number.isEmpty()) {
            speak("I need both a name and a number.")
            return
        }

        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)

        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Opening contacts to save $name.")
        } catch (e: Exception) {
            speak("I could not open the contacts app.")
        }
    }

    private fun deleteContact(
        context: Context,
        command: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        if (context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_CONTACTS)
            return
        }

        val name = stripWords(command, DELETE_CONTACT_WORDS + CONTACT_WORDS + FILLER_WORDS)

        if (name.isEmpty()) {
            speak("Which contact should I delete?")
            return
        }

        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
            arrayOf("%$name%"),
            null
        )

        var contactId: String? = null

        cursor?.use {

            if (it.moveToFirst()) {

                val index = it.getColumnIndex(ContactsContract.Contacts._ID)

                if (index >= 0) {
                    contactId = it.getString(index)
                }
            }
        }

        if (contactId == null) {
            speak("I could not find a contact named $name.")
            return
        }

        try {

            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId)

            context.contentResolver.delete(uri, null, null)

            speak("Contact $name has been deleted.")

        } catch (e: Exception) {
            speak("I could not delete that contact.")
        }
    }

    private fun sendMessage(
        context: Context,
        command: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        val separatorIndex = firstIndexOfAny(command, MESSAGE_SEPARATOR_WORDS)

        if (separatorIndex < 0) {
            speak("Please tell me what the message should say.")
            return
        }

        val namePart = command.substring(0, separatorIndex)
        val bodyPart = command.substring(separatorIndex)

        val name = stripWords(namePart, MESSAGE_WORDS + FILLER_WORDS)

        var body = bodyPart.trim()

        for (separator in MESSAGE_SEPARATOR_WORDS) {
            if (body.startsWith(separator, ignoreCase = true)) {
                body = body.substring(separator.length).trim()
                break
            }
        }

        if (name.isEmpty() || body.isEmpty()) {
            speak("I need both a contact name and a message.")
            return
        }

        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_CONTACTS)
            return
        }

        val number = findPhoneNumber(context, name)

        if (number == null) {
            speak("I could not find a contact named $name.")
            return
        }

        if (context.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.SEND_SMS)
            return
        }

        try {

            val smsManager = SmsManager.getDefault()

            smsManager.sendTextMessage(number, null, body, null, null)

            speak("Message sent to $name.")

        } catch (e: Exception) {
            speak("I could not send the message.")
        }
    }

    private fun takePhoto(
        context: Context,
        command: String,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.CAMERA)
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {

            context.startActivity(intent)

            if (containsAny(command, SELFIE_WORDS)) {
                speak("Opening the camera for a selfie.")
            } else {
                speak("Opening the camera.")
            }

        } catch (e: Exception) {
            speak("I could not open the camera.")
        }
    }

    private fun openCamera(
        context: Context,
        speak: (String) -> Unit,
        requestPermission: (String) -> Unit
    ) {

        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.CAMERA)
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Opening camera.")
        } catch (e: Exception) {
            speak("I could not open the camera.")
        }
    }

    private fun searchYoutube(
        context: Context,
        query: String,
        speak: (String) -> Unit
    ) {

        val uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Searching YouTube for $query.")
        } catch (e: Exception) {
            speak("I could not open YouTube.")
        }
    }

    private fun searchChrome(
        context: Context,
        query: String,
        speak: (String) -> Unit
    ) {

        val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))

        val chromeIntent = Intent(Intent.ACTION_VIEW, uri)
        chromeIntent.setPackage("com.android.chrome")
        chromeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {

            context.startActivity(chromeIntent)
            speak("Searching Google for $query.")

        } catch (e: Exception) {

            val fallback = Intent(Intent.ACTION_VIEW, uri)
            fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                context.startActivity(fallback)
                speak("Searching for $query.")
            } catch (e2: Exception) {
                speak("I could not open the browser.")
            }
        }
    }

    private fun openWifiSettings(
        context: Context,
        speak: (String) -> Unit
    ) {

        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Opening WiFi settings.")
        } catch (e: Exception) {
            speak("I could not open WiFi settings.")
        }
    }

    private fun openBluetoothSettings(
        context: Context,
        speak: (String) -> Unit
    ) {

        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
            speak("Opening Bluetooth settings.")
        } catch (e: Exception) {
            speak("I could not open Bluetooth settings.")
        }
    }


    private fun listInstalledApps(context: Context, speak: (String) -> Unit) {
        val apps = getInstalledLauncherApps(context)
            .sortedBy { it.label.toLowerCase(Locale.getDefault()) }
        if (apps.isEmpty()) {
            speak("I could not find launchable apps.")
            return
        }
        val names = apps.take(12).joinToString(", ") { it.label }
        val extra = if (apps.size > 12) " and ${apps.size - 12} more" else ""
        speak("I found ${apps.size} launchable apps: $names$extra.")
    }

    private fun findAndLaunchApp(
        context: Context,
        name: String,
        speak: (String) -> Unit
    ) {

        if (name.isEmpty()) {
            speak("Which app should I open?")
            return
        }

        val apps = getInstalledLauncherApps(context)

        val requested = compactAppName(name)

        var partialMatch: AppEntry? = null

        for (app in apps) {

            val appName = compactAppName(app.label)

            if (appName.equals(requested, ignoreCase = true)) {
                launchPackage(context, app, speak)
                return
            }
        }

        for (app in apps) {

            val appName = compactAppName(app.label)

            if (appName.contains(requested, ignoreCase = true) ||
                requested.contains(appName, ignoreCase = true)) {
                partialMatch = app
                break
            }
        }

        if (partialMatch != null) {
            launchPackage(context, partialMatch, speak)
            return
        }

        speak("I could not find an app named $name.")
    }

    private fun getInstalledLauncherApps(context: Context): ArrayList<AppEntry> {

        val now = System.currentTimeMillis()

        if (appCache.isNotEmpty() && now - appCacheTime < APP_CACHE_TIME) {
            return appCache
        }

        val result = ArrayList<AppEntry>()

        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = pm.queryIntentActivities(intent, 0)

        for (info in activities) {

            val label = info.loadLabel(pm).toString().trim()
            val packageName = info.activityInfo.packageName

            if (label.isNotEmpty()) {
                result.add(AppEntry(label, packageName))
            }
        }

        appCache = result
        appCacheTime = now

        return result
    }

    private fun launchPackage(
        context: Context,
        app: AppEntry,
        speak: (String) -> Unit
    ) {

        val pm = context.packageManager

        val launchIntent = pm.getLaunchIntentForPackage(app.packageName)

        if (launchIntent == null) {
            speak("${app.label} cannot be opened.")
            return
        }

        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(launchIntent)
            speak("Opening ${app.label}.")
        } catch (e: Exception) {
            speak("I could not open ${app.label}.")
        }
    }

    private fun openApp(
        context: Context,
        packageName: String,
        label: String,
        speak: (String) -> Unit
    ) {

        val pm = context.packageManager

        val launchIntent = pm.getLaunchIntentForPackage(packageName)

        if (launchIntent == null) {
            speak("$label is not installed.")
            return
        }

        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(launchIntent)
            speak("Opening $label.")
        } catch (e: Exception) {
            speak("I could not open $label.")
        }
    }

    private fun compactAppName(text: String): String {
        return text
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace(".", "")
            .trim()
    }

    private fun stripWords(
        command: String,
        wordsToStrip: Array<String>
    ): String {

        val tokens = command.split(Regex("\\s+")).filter { it.isNotBlank() }

        val kept = tokens.filter { token ->
            wordsToStrip.none { word -> word.equals(token, ignoreCase = true) }
        }

        return kept.joinToString(" ").trim()
    }

    private fun extractQuery(
        command: String,
        wordsToStrip: Array<String>
    ): String {
        return stripWords(command, wordsToStrip + FILLER_WORDS)
    }

    private fun containsAny(command: String, words: Array<String>): Boolean {

        for (word in words) {
            if (command.contains(word, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    private fun firstIndexOfAny(text: String, words: Array<String>): Int {

        var best = -1

        for (word in words) {

            val index = text.indexOf(word, ignoreCase = true)

            if (index >= 0 && (best < 0 || index < best)) {
                best = index
            }
        }

        return best
    }
}