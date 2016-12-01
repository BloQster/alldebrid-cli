package com.bloqster.blqloader.plugins.alldebrid

/**
 * Created by micha on 01.12.2016.
 */
class CLI_Args {
    String username = null
    String password = null
    ArrayList<String> magnetLinks = new ArrayList<>()
    ArrayList<File> torrentFiles = new ArrayList<>()

    ArrayList<File> magnetLinkFiles = new ArrayList<>()
    ArrayList<File> torrentFolders = new ArrayList<>()


    File dlListFolder = null
    boolean list = false
    boolean listComplete = false
    boolean listIncomplete = false
    boolean remove = false
    boolean removeIncomplete = false
    boolean removeAll = false
    boolean removeAddedFiles = false

    CLI_Args(String username, String password, ArrayList<String> magnetLinks, ArrayList<File> torrentFiles, ArrayList<File> magnetLinkFiles, ArrayList<File> torrentFolders, File dlListFolder, boolean list, boolean listComplete, boolean listIncomplete, boolean remove, boolean removeIncomplete, boolean removeAll, boolean removeAddedFiles) {
        this.username = username
        this.password = password
        this.magnetLinks = magnetLinks
        this.torrentFiles = torrentFiles
        this.magnetLinkFiles = magnetLinkFiles
        this.torrentFolders = torrentFolders
        this.dlListFolder = dlListFolder
        this.list = list
        this.listComplete = listComplete
        this.listIncomplete = listIncomplete
        this.remove = remove
        this.removeIncomplete = removeIncomplete
        this.removeAll = removeAll
        this.removeAddedFiles = removeAddedFiles
    }
}
