# Alldebrid CLI

This CLI tool is intended to provide CLI access to Alldebrid.com.
ATM only the torrent functionalty is build in.
This project and the jar can also be used as a library for java/groovy projects.
A precompiled jar is available at the release page and can [be downloaded with this link](https://github.com/BloQster/alldebrid-cli/releases/download/1.0.0/blq_alldebrid.jar). If you want to build the jar by yourself, you can use the provided gradle wrapper and the shadowJar task.

##Compile
`./gradlew shadowJar` You can find the compiled jar under /build/libs/blq_alldebrid.jar

##BLQ Alldebrid CLI Usage
`java -jar blq_alldebrid.jar [commands]*`

If several commands are provided, they are executed in the order of the command list. The commands can be used multiple times to add multiple links, files or folders. Descriptions starting with a * must be provided in order to let the commands work properly.

###Commands
####Common
- `-help|-h` Shows the helptext and aborts the program.
- `-username|-u username` 			*Sets the username for the login to Alldebrid.
- `-password|-p password` 			*Sets the password for the login to Alldebrid.

#### Adding torrents
- `-magnet|-m magnetLink` Adds a magnetlink to the Alldebrid account.
- `-magnetFile|-mF file` Adds a text file of magnet links to the Alldebrid account. One magnet link per line.
- `-torrent|-t torrentFile` Adds a torrent file (absoltue or relative path) to the Alldebrid account.
- `-torrentFolder|-tF folder` Adds a folder of torrents (recursive) to the Alldebrid account.

#### Show information
- `-list|-l` Lists all Torrents.
- `-listIncomplete|-lI` Lists all incomplete Torrents.
- `-listComplete|-lC` Lists all completed Torrents.

#### Get links
- `-dlList|-dl folderPath` Creates one text file with download links per completed torrent. The text files will be named after the filename provided by Alldebrid. Existing text files will be overwritten. The folder must exist to let the cli work properly.

#### Remove
- `-remove|-rm` Removes all completed downloads.
- `-removeIncomplete|-rmC` Removes all incompleted downloads.
- `-removeAll|-rmA` Removes all downloads.
- `-removeFiles|-rF` Removes all files that were used to import torrents or magnet links. Happens only if no exception occurs.
