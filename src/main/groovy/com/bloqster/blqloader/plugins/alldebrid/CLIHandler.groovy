package com.bloqster.blqloader.plugins.alldebrid

import groovy.io.FileType
import groovyjarjarcommonscli.MissingArgumentException

class CLIHandler {
    private String[] args
    private CLI_Args cliArgs
    private HashSet<File> filesToDelete
    private Alldebrid alldebrid
    CLIHandler(String[] args) {
        this.args = args
        this.filesToDelete = new ArrayList<>()
        this.alldebrid = null
        this.cliArgs = handleCLIArgs()
    }

    private CLI_Args handleCLIArgs() {
        int argCounter = 0
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
        while (argCounter < args.size()) {
            switch (args[argCounter]) {
                case "-magnet":
                case "-m":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-magnet|-m requires a magnetlink.")
                    }
                    String magnetLink = args[argCounter + 1]
                    magnetLinks.add(magnetLink)
                    argCounter += 2
                    break
                case "-magnetFile":
                case "-mF":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-magnetFile|-mF requires a filepath.")
                    }
                    String filePath = args[argCounter + 1]
                    File magnetFile = new File(filePath)
                    if (!magnetFile.exists()) {
                        throw new IllegalArgumentException("-magnetFile|-mF requires an existing file. File: $filePath")
                    }
                    magnetLinkFiles.add(magnetFile)
                    argCounter += 2
                    break
                case "-torrent":
                case "-t":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-torrent|-t requires a filepath.")
                    }
                    String filePath = args[argCounter + 1]
                    File torrentFile = new File(filePath)
                    if (!torrentFile.exists()) {
                        throw new IllegalArgumentException("-torrent|-t requires an existing file. File: $filePath")
                    }
                    torrentFiles.add(torrentFile)
                    argCounter += 2
                    break
                case "-torrentFolder":
                case "-tF":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-torrentFolder|-tF requires a folder path.")
                    }
                    String folderPath = args[argCounter + 1]
                    File folder = new File(folderPath)
                    if (!folder.isDirectory()) {
                        throw new IllegalArgumentException(args[argCounter + 1] + " is not an existing directory.")
                    }
                    torrentFolders.add(folder)
                    argCounter += 2
                    break;
                case "-list":
                case "-l":
                    list = true
                    argCounter++
                    break
                case "-listIncomplete":
                case "-lI":
                    listIncomplete = true
                    argCounter++
                    break
                case "-listComplete":
                case "-lC":
                    listComplete = true
                    argCounter++
                    break
                case "-dlList":
                case "-dl":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-dlList|-dl requires a folder path.")
                    }

                    dlListFolder = new File(args[argCounter + 1])
                    if (!dlListFolder.isDirectory()) {
                        throw new IllegalArgumentException(args[argCounter + 1] + " is not an existing directory.")
                    }

                    argCounter += 2
                    break
                case "-remove":
                case "-rm":
                    remove = true
                    argCounter++
                    break
                case "-removeIncomplete":
                case "-rmC":
                    removeIncomplete = true
                    argCounter++
                    break
                case "-removeAll":
                case "-rmA":
                    removeAll = true
                    argCounter++
                    break
                case "-removeFiles":
                case "-rF":
                    removeAddedFiles = true
                    argCounter++
                    break;
                case "-username":
                case "-u":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-username|-u requires an username.")
                    }
                    username = args[argCounter + 1]
                    argCounter += 2
                    break
                case "-password":
                case "-p":
                    if (args.size() <= (argCounter + 1)) {
                        throw new MissingArgumentException("-password|-p requires a password.")
                    }
                    password = args[argCounter + 1]
                    argCounter += 2
                    break
                case "-help":
                case "-h":
                    printHelp()
                    return
                default:
                    printHelp()
                    println()
                    throw new IllegalArgumentException("${args[argCounter]} is not a possible option.")
            }
        }
        CLI_Args tmpArgs = new CLI_Args(username, password, magnetLinks, torrentFiles, magnetLinkFiles, torrentFolders, dlListFolder, list, listComplete, listIncomplete, remove, removeIncomplete, removeAll, removeAddedFiles)
        return tmpArgs
    }

    private void addCollectionOfMagnetLinkFiles(Collection<File> magnetLinkFiles){
        println "Adding ${magnetLinkFiles.size()} magnet link file${magnetLinkFiles.size() == 1 ? "" : "s"} to the account: $cliArgs.username \n"
        magnetLinkFiles.eachWithIndex { File magnetLinkFile, int idx ->
            addFileWithMagnetLinks(magnetLinkFile, magnetLinkFiles.size(), idx)
        }
        println ""
    }
    private void addFileWithMagnetLinks(File magnetLinkFile, int collectionSize, int idx){
        filesToDelete.add(magnetLinkFile)
        println "(${idx + 1}|${collectionSize}) Adding magnetlink file: ${magnetLinkFile.name}"
        def lines = magnetLinkFile.readLines("utf-8").findAll { it != "" }
        println "Adding ${lines.size()} magnet link${lines.size() == 1 ? "" : "s"} to the account: $cliArgs.username"
        lines.eachWithIndex { String magnetLink, int linkIdx ->
            println "(${linkIdx + 1}|${lines.size()}) Adding: $magnetLink"
            alldebrid.addMagnetLink(magnetLink)
        }
        println ""
    }
    private void addCollectionOfMagnetLinks(Collection<String> magnetLinks){
        println "Adding ${magnetLinks.size()} magnet link${magnetLinks.size() == 1 ? "" : "s"} to the account: $cliArgs.username"
        cliArgs.magnetLinks.eachWithIndex { String magnetLink, int idx ->
            executeAddMagnetLinkWithIndex(magnetLink, magnetLinks.size(), idx)
        }
        println ""
    }
    private void executeAddMagnetLinkWithIndex(String magnetLink, int collectionSize, int idx){
        println "(${idx + 1}|${collectionSize}) Adding: $magnetLink"
        alldebrid.addMagnetLink(magnetLink)
    }

    private void addCollectionOfTorrentFolders(Collection<File> torrentFolders){
        println "Adding ${cliArgs.torrentFolders.size()} torrent folder${cliArgs.torrentFolders.size() == 1 ? "" : "s"} to the account: $cliArgs.username\n"
        torrentFolders.eachWithIndex { File torrentFolder, int idx ->
            ArrayList<File> torrents = new ArrayList<>()
            torrentFolder.eachFileRecurse(FileType.FILES){
                if(it.name.endsWith(".torrent")){
                    torrents.add(it)
                }
            }
            println "(${idx + 1}|${torrentFolders.size()}) Adding torrent folder: ${torrentFolder.name}"
            addCollectionOfTorrentFiles(torrents)
        }
        println ""
    }

    private void addCollectionOfTorrentFiles(Collection<File> torrentFiles){
        println "Adding ${torrentFiles.size()} torrent file${torrentFiles.size() == 1 ? "" : "s"} to the account: $cliArgs.username"
        torrentFiles.eachWithIndex { File torrentFile, int idx ->
            executeAddTorrentFile(torrentFile, torrentFiles.size(), idx)
        }
        println ""
    }
    private void executeAddTorrentFile(File torrent, int collectionSize, int idx){
        filesToDelete.add(torrent)
        println "(${idx + 1}|${collectionSize}) Adding: ${torrent.name}"
        alldebrid.addTorrent(torrent)
    }

    private void listTorrents(Collection<AlldebridTorrent> torrents){
        println "Listing ${torrents.size()} torrent${(torrents.size() == 1) ? "" : "s"}:"
        println AlldebridTorrent.tableHeader
        torrents.each {
            println it.toTextTableString()
        }
        println ""
    }

    private void listAllTorrents(){
        println "Filter: ALL"
        def torrents = alldebrid.getAllTorrents()
        listTorrents(torrents)
    }
    private void listFilteredTorrents(TorrentStatus filterStatus){
        println "Filter: $filterStatus"
        def torrents = alldebrid.getAllTorrents().findAll{
            return it.status == filterStatus
        }
        listTorrents(torrents)
    }
    private void createDLListFolder(){
        def torrents = alldebrid.getAllTorrents().findAll {
            it.status == TorrentStatus.FINISHED
        }
        println "Creating ${torrents.size()} torrent download file${(torrents.size() == 1) ? "" : "s"}:"
        torrents.eachWithIndex { AlldebridTorrent torrent, int idx ->
            File newDLFile = new File(cliArgs.dlListFolder.getAbsolutePath() + "/" + torrent.filename)
            if (newDLFile.exists()) {
                println "${idx + 1}|${torrents.size()} Overwrite ${torrent.filename}"
            } else {
                println "${idx + 1}|${torrents.size()} Create ${torrent.filename}"
            }

            newDLFile.withWriter("utf-8") { BufferedWriter bw ->
                torrent.links.eachWithIndex { String link, int idxTorrent ->
                    if ((idxTorrent + 1) == torrent.links.size()) {
                        bw.write(link)
                        bw.flush()
                    } else {
                        bw.writeLine(link)
                    }
                }
            }
        }
        println ""
    }
    private void removeTorrents(Collection<AlldebridTorrent> torrents){
        println "Deleting ${torrents.size()} torrent${(torrents.size() == 1) ? "" : "s"}:"
        torrents.eachWithIndex { AlldebridTorrent torrent, int idx ->
            println "${idx + 1}|${torrents.size()} Delete ${torrent.filename}"
            alldebrid.delete(torrent)
        }
        println ""
    }

    private void removeAll(){
        def torrents = alldebrid.getAllTorrents().findAll {
            return it.status == TorrentStatus.DOWNLOADING
        }
        println "Filter: ALL"
        removeTorrents(torrents)
    }

    private void removeFilteredTorrents(TorrentStatus status){
        def torrents = alldebrid.getAllTorrents().findAll {
            return it.status == status
        }
        println "Filter: $status"
        removeTorrents(torrents)
    }

    private void deleteAddedFiles(){
        println "Deleting ${filesToDelete.size()} added file${(filesToDelete.size() == 1) ? "" : "s"}:"
        filesToDelete.eachWithIndex{ File file, int idx ->
            print "${idx + 1}|${filesToDelete.size()} Delete ${file.name} - "
            println "${file.delete()}"
        }
        println ""
    }

    void executeCLIRequest() {
        if(args.size() == 0){
            printHelp()
        } else {
            if (cliArgs.username == null || cliArgs.password == null)
                throw new IllegalArgumentException("Username or Password not provided!")

            alldebrid = new Alldebrid(cliArgs.username, cliArgs.password)
            if (cliArgs.magnetLinks.size() > 0) {
                addCollectionOfMagnetLinks(cliArgs.magnetLinks)
            }

            if (cliArgs.magnetLinkFiles.size() > 0) {
                addCollectionOfMagnetLinkFiles(cliArgs.magnetLinkFiles)
            }

            if (cliArgs.torrentFiles.size() > 0) {
                addCollectionOfTorrentFiles(cliArgs.torrentFiles)
            }

            if(cliArgs.torrentFolders.size() > 0){
                addCollectionOfTorrentFolders(cliArgs.torrentFolders)
            }

            if (cliArgs.list) {
                listAllTorrents()
            }

            if (cliArgs.listIncomplete) {
                listFilteredTorrents(TorrentStatus.DOWNLOADING)
            }

            if (cliArgs.listComplete) {
               listFilteredTorrents(TorrentStatus.FINISHED)
            }

            if (cliArgs.dlListFolder != null) {
                createDLListFolder()
            }

            if (cliArgs.remove) {
                removeFilteredTorrents(TorrentStatus.FINISHED)
            }

            if (cliArgs.removeIncomplete) {
                removeFilteredTorrents(TorrentStatus.DOWNLOADING)
            }

            if (cliArgs.removeAll) {
               removeAll()
            }

            if(cliArgs.removeAddedFiles){
                deleteAddedFiles()
            }
        }
    }

    private static void printHelp() {
        println Thread.currentThread().getContextClassLoader().getResourceAsStream("files/help.txt").getText()
    }
}
