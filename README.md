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

Wed Dec  4 13:21:49 PST 2019
Added an internal file to keep trak of write-protected.  Not
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
