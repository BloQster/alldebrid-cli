package com.bloqster.blqloader.plugins.alldebrid

import com.bloqster.blqloader.plugins.alldebrid.Alldebrid

/**
 * Created by micha on 30.11.2016.
 */
class AlldebridTest extends GroovyTestCase {

    Alldebrid alldebrid
    private final static String torrentFile = "torrents/bb.torrent"
    private final static String magnetLink = "magnet:?xt=urn:btih:f1d6b924bfec5ff90aced606fee4f82c1555c576&dn=production"

    def setup() {
        String username = System.getenv("ALLDEBRID_TEST_ACC")
        String password = System.getenv("AÖÖDEBRID_TEST_PW")
        alldebrid = new Alldebrid(username, password)
    }

    void testLogin() {
        setup()
        alldebrid.login()
        assert true
    }

    void testWrongLogin() {
        def wrongAlldebrid = new Alldebrid("wrong", "username")
        try {
            wrongAlldebrid.login()
            assert false
        } catch (Exception e) {
            assert true
        }
    }

    void testGetAllTorrents() {
        setup()
        def torrents = alldebrid.getAllTorrents()
        assert torrents.size() > 0
    }

    void testAddMagnetLink(){
        setup()
        def torrents = alldebrid.getAllTorrents()
        def bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        if(bunnyTorrent != null){
            alldebrid.delete(bunnyTorrent)
            sleep(100)
            torrents = alldebrid.getAllTorrents()
            bunnyTorrent = torrents.find{
                return it.filename == "production"
            }
            assert bunnyTorrent == null
        }
        alldebrid.addMagnetLink(magnetLink)
        sleep(100)
        torrents = alldebrid.getAllTorrents()
        bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        assert bunnyTorrent != null
    }

    void testAddWrongMagnetLink(){
        setup()
        int torrentSizeBefore = alldebrid.getAllTorrents().size()
        String wrongMagnetLink = "NOT A MAGNET LINK"
        alldebrid.addMagnetLink(wrongMagnetLink)
        sleep(100)
        int torrentSizeAfter = alldebrid.getAllTorrents().size()
        assert torrentSizeBefore == torrentSizeAfter
    }

    void testAddTorrentFile(){
        setup()
        ClassLoader classLoader = getClass().getClassLoader()
        File torrentFile = new File(classLoader.getResource(torrentFile).getFile())
        def torrents = alldebrid.getAllTorrents()
        def bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        if(bunnyTorrent != null){
            alldebrid.delete(bunnyTorrent)
            sleep(100)
            torrents = alldebrid.getAllTorrents()
            bunnyTorrent = torrents.find{
                return it.filename == "production"
            }
            assert bunnyTorrent == null
        }
        alldebrid.addTorrent(torrentFile)
        sleep(100)
        torrents = alldebrid.getAllTorrents()
        bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        assert bunnyTorrent != null
    }

//    void testAddWrongTorrentFile(){
//        setup()
//        ClassLoader classLoader = getClass().getClassLoader()
//        File torrentFile = new File(classLoader.getResource("notTorrent.torrent").getFile())
//        alldebrid.addTorrent(torrentFile)
//    }

    void testAddNotExistingFile(){
        File notExistingFile = new File("NOT_EXISTING")
        assert !notExistingFile.exists()
        setup()
        try{
            alldebrid.addTorrent(notExistingFile)
            assert false
        } catch(FileNotFoundException e){
            assert true
        }

    }

    void testDeleteTorrent(){
        setup()
        ClassLoader classLoader = getClass().getClassLoader()
        File torrentFile = new File(classLoader.getResource("bb.torrent").getFile())
        def torrents = alldebrid.getAllTorrents()
        def bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        if(bunnyTorrent == null){
            alldebrid.addTorrent(torrentFile)
            sleep(100)
            torrents = alldebrid.getAllTorrents()
            bunnyTorrent = torrents.find{
                return it.filename == "production"
            }
            assert bunnyTorrent != null
        }
        alldebrid.delete(bunnyTorrent)
        sleep(100)
        torrents = alldebrid.getAllTorrents()
        bunnyTorrent = torrents.find{
            return it.filename == "production"
        }
        assert bunnyTorrent == null
    }

    void testTable(){
        setup()
        def torrents = alldebrid.getAllTorrents()
        torrents.sort()
        println AlldebridTorrent.tableHeader
        torrents.each {
            println it.toTextTableString()
        }
    }
}
