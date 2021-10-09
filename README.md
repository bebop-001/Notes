# Notes
Android Kotlin simple text notepad using Storage Access Framework

App is based on StorageDemo source code discussed in 
the Kotlin edition of 
[Android Studio 3.5 Essentials](https://www.ebookfrenzy.com/ebookpages/kotlin_android_studio_35_ebook.html).
I've used several of this series and they've all been a good value.

At some point this will be a functional simple text notepad.
I will be adding code from various other projects as I go
along.  It's written in Kotlin and I'm using it to learn
Kotlin and the GooglePlay process for android apps.  One of
the goals is to have an app that will work from API-19
through whatever is current (API-29 right now)

*NOTE* app/build.gradle executes calls to git and to PERL.
I can't see how to make this work under Windows so it
currently builds only under Linux.

Mon Dec 23 16:42:39 PST 2019
We now open a file passed by clicking on a .txt file from the file
manager or intent for starting mime plain/text file.  It's a little
clumsy in that files checked out this way are read-only as indicated by
an emoji red-x in the lock field and "Save File" being disabled.  Files
opened and edited this way can be saved using "Save As" and can even
over-write the file that was originally opened.  This has to do with
the fact that the files are opened differently.  Files opened through the
Storage Access Framework -- using the Storage Access File Browser --
can be re-written.  Files opened via a generic
intent from another app (eg file manager) can not be written using
the same URI as they were opened with.

Fri Dec 13 12:10:02 PST 2019
Investigations into large file performance:<br>
Since the main interface to the file is an EditText, I was concerned
about how the app would work with large files.  What I found was:<br/>
* editing a test file of about 500k crashes the app.  The crash happens
while loading the EditText, however reading the file into a string works ok.
    * Read for API-19 device: Size:550011 bytes, time:1.18200 seconds
    * Read for API-26 device: Size:550011 bytes, time:0.09500 seconds
    * for both devices trying to load this file into the EditText
    generated an android.os.TransactionTooLargeException

However both devices can load a 275011 byte file ok so I'm not going to
worry any further about this.  To load a very big
file I think the best approach would be to read the file into a
byte array and write a custom scrolled EditText that used Windowed
interface so the EditText didn't need to deal with the very large
strings.  However -- that's more work than I want to do on this
at the moment.

Sun Dec  8 15:00:36 PST 2019
Added a menu item to allow sharing the current file.

Sat Dec  7 18:17:34 PST 2019
Added a dialog to allow user to abort overwrite of changes.

Fri Dec  6 17:47:53 PST 2019
User-selected font size tracks with file.

Fri Dec  6 11:35:15 PST 2019
added code to allow edit font size changes.  Not persistent yet but
works.

Wed Dec  4 13:21:49 PST 2019
Added an internal file to keep track of write-protected.  Not
real file-system write protect but better than nothing.

Modified title in action bar to show file is modified and readonly/read-write.

Mon Dec  2 11:34:57 PST 2019
moved file open/save stuff from main activity to editWindow 
fragment and split menu items so MainActivity takes care
of non-edit stuff (theme, build info, etc) and editWindow
frag takes care of file related stuff.

I want to be able to have private files and be able to really
write protect.  To do that it looks like I need to do a file
picker that works off of getFilesDir path -- which is accessed
using the File class.

Sun Dec  1 13:09:45 PST 2019
Added some debug to the edit text window so I could see what the textwatcher
is doing.  Added a "WriteProtect" item.  It doesn't actually write protect
the file -- it seems you can't do that using the StorageAccessFramework
which will be the only way to access files in Android 10+.  It's only a flag
that prevents the editor from overwriting the file.  You can still alter the
contents of the edit-text window which means you can still "SaveAs" a different
file.  Unfortunately it also means you can "SaveAs" over the file since the
operating system doesn't give a damn about what the app wants.

I also tried a work-around that lets me see the actual file location but 
that's disabled as well.

Mon Nov 25 12:08:19 PST 2019
Implemented Dark/Light theme.

Sun Nov 24 11:02:26 PST 2019
Added a 'save as' option and modified 'save'.  Save
now just saves the current file without bring up the picker
and 'save as' brings up the picker like 'save' used to.

Sat Nov 23 12:24:13 PST 2019
Fixed a crash bug under New File.  Fixed Save File so it
correctly updates the displayed file name.

Mon Nov 18 11:29:32 PST 2019<br>
I think the basic functionality is all in place.  Added
file properties menu item and build info menu item.
All file operations now happen from the overflow menu.
Added an icon to the action bar to make app recognizable
and modified so the action bar title now displays the name of
the file being edited.
Still haven't tested on Android 10 but works on API 19 through
26.
Bumped rev to version 2.0.0.

Fri Nov 15 17:13:56 PST 2019<br>
removed test code that was creating desktop shortcut each time app started.
Changed icon and color scheme.

Wed Oct 23 08:20:58 PDT 2019<br>
Initial commit to git hub.  Current code creates a shortcut
on the user's home screen if they want one.

Wed Nov 13 12:31:27 PST 2019<br>
Lots of changes.   Basic functionality is there I think.

At this point, the UI is funky but it's working well enough I've 
started to use it.

It uses Google's Storage Access Framework (SAF) and should work
through Android 10.
