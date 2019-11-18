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
