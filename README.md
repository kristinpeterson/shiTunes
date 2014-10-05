# shiTunes: a desktop music player

##Contributing
1. Add (aka stage) your changes to the local git repository index

    ```git add -A```

2. Commit your changes to the repository (akin to 'saving') with a commit message

    ```git commit -m "this is the message which will be applied to this commit"```

3. Push your changes to the remote git repository (hosted on Bitbucket)

    ```git push```

4. To Pull changes from the Bitbucket remote repository and sync with your local git repository (ie. to get changes
made by other contributors)

    ```git pull```

These commands can all be accomplished via the [Intellij GUI](http://www.jetbrains.com/idea/webhelp/using-git-integration.html) 
(or [Sourcetree](http://www.sourcetreeapp.com/))

For a quick tutorial on git: [Try Git](https://try.github.io/levels/1/challenges/1)

##Dependencies
* [mp3agic v0.8.2](https://github.com/mpatric/mp3agic)
* [BasicPlayer 3.0](http://www.javazoom.net/jlgui/api.html)
* derby.jar
* derbytools.jar

BasicPlayer 3.0 Dependencies:

* commons-logging-api.jar
* jl1.0.1.jar
* jogg-0.0.7.jar
* jorbis-0.0.15.jar
* jspeex0.9.7.jar
* tritonus_share.jar
* vorbisspi1.0.2.jar
* mp3spi1.9.5.jar 

Note: mp3spi1.9.4.jar was included w/ the BasicPlayer 3.0 package, but results in an error when playing .mp3 files. Per
[StackOverflow](http://stackoverflow.com/a/12806411/2237166) article, using updated 
[mp3spi1.9.5.jar](http://www.javazoom.net/mp3spi/sources.html)


##Developed by:
* Melanie Kwon
* Michael Perez
* Kristin Peterson