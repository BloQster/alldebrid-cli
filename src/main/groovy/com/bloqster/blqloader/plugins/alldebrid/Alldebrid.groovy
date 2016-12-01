package com.bloqster.blqloader.plugins.alldebrid

import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

class Alldebrid {
    private static final String loginURL = "https://alldebrid.com/register/"
    private static final String torrentURL = "https://alldebrid.com/api/torrent.php?json=true"
    public static final String removeURL = "https://alldebrid.com/torrent/?action=remove&id="
    public static final String uploadTorrentURL = "https://upload.alldebrid.com/uploadtorrent.php"


    public final String username
    public final String password

    private CloseableHttpClient httpClient
    private HttpClientContext httpClientContext
    private RequestConfig requestConfig

    private boolean loggedIn = false


    Alldebrid(String username, String password) {
        this.username = username
        this.password = password
        httpClient = HttpClients.createDefault()
        httpClientContext = HttpClientContext.create()
        requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build()
    }

    boolean getLoggedIn() {
        return loggedIn
    }

    private String getUid() {
        if (!loggedIn)
            login()
        BasicCookieStore cookieStore = httpClientContext.getCookieStore()
        BasicClientCookie uidCookie = cookieStore.getCookies().find { BasicClientCookie bsc ->
            return bsc.name == "uid" && (bsc.domain == "alldebrid.com" || bsc.domain == ".alldebrid.com")
        }

        return uidCookie.getValue()
    }

    void addMagnetLink(String magnetLink) {
        if (!loggedIn)
            login()
        String uid = getUid()
        def uploadTorrentRequest = new HttpPost(uploadTorrentURL)
        uploadTorrentRequest.setConfig(requestConfig)
        MultipartEntityBuilder meb = MultipartEntityBuilder.create()
        meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        meb.addTextBody("uid", uid)
        meb.addTextBody("domain", "https://alldebrid.com/torrent/")
        meb.addTextBody("magnet", magnetLink)
        meb.addTextBody("splitfile", "0")
        meb.addTextBody("submit", "Convert this torrent")
        HttpEntity postEntity = meb.build()
        uploadTorrentRequest.setEntity(postEntity)
        CloseableHttpResponse response = httpClient.execute(uploadTorrentRequest, httpClientContext)
        response.close()
        if (response.getStatusLine().statusCode != 302) {
            throw new Exception("Adding the magnetlink \"${magnetLink}\" is not possible: " + response.getStatusLine())
        }
    }

    void addTorrent(File torrent) {
        if (!torrent.exists())
            throw new FileNotFoundException("File does not exist: " + torrent.absolutePath)
        if (!loggedIn)
            login()
        String uid = getUid()
        def uploadTorrentRequest = new HttpPost(uploadTorrentURL)
        uploadTorrentRequest.setConfig(requestConfig)
        MultipartEntityBuilder meb = MultipartEntityBuilder.create()
        meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        FileBody fb = new FileBody(torrent, "application/x-bittorrent", "utf-8")
        meb.addPart("uploadedfile", fb)
        meb.addTextBody("uid", uid)
        meb.addTextBody("domain", "https://alldebrid.com/torrent/")
        meb.addTextBody("magnet", "")
        meb.addTextBody("splitfile", "0")
        meb.addTextBody("submit", "Convert this torrent")
        HttpEntity postEntity = meb.build()
        uploadTorrentRequest.setEntity(postEntity)
        CloseableHttpResponse response = httpClient.execute(uploadTorrentRequest, httpClientContext)
        response.close()
        if (response.getStatusLine().statusCode != 302) {
            throw new Exception("Torrent upload not possible with \"${torrent.absolutePath}\": " + response.getStatusLine())
        }
    }

    ArrayList<AlldebridTorrent> getAllTorrents() {
        if (!loggedIn)
            login()

        def torrentGetRequest = new HttpGet(torrentURL)
        torrentGetRequest.setConfig(requestConfig)
        CloseableHttpResponse response = httpClient.execute(torrentGetRequest, httpClientContext)

        if (response.getStatusLine().statusCode != 200) {
            response.close()
            throw new Exception("GET Torrentlist not possible: " + response.getStatusLine())
        }

        HttpEntity entity = response.getEntity()
        def jsonString = entity.getContent().getText("utf-8")
        def json = new JsonSlurper().parse(jsonString.toCharArray())
        EntityUtils.consume(entity)
        response.close()

        ArrayList<AlldebridTorrent> allTorrents = new ArrayList<>()
        json.each {
            try {
                long id = it[1].toString().toLong()
                int server = it[2].toString().toInteger()
                String filename = it[3].toString()[31..-8]
                TorrentStatus status = (it[4].toString() == "finished") ? TorrentStatus.FINISHED : TorrentStatus.DOWNLOADING
                long downloaded = stringToByte(it[5].toString())
                long fileSize = stringToByte(it[6].toString())
                int seeder = it[7].toString().toInteger()
                long speed = stringToByte(it[8])
                Date addedDate = Date.parse("dd/MM/yyyy HH:mm", it[9].toString())
                ArrayList<String> links = (ArrayList<String>) ((status == TorrentStatus.DOWNLOADING) ? [] : it[10].toString()[10..-98].split(",;,"))

                def alT = new AlldebridTorrent(id, server, filename, status, downloaded, fileSize, seeder, speed, addedDate, links)
                allTorrents.add(alT)
            } catch (Exception e) {
                e.printStackTrace()
                System.err.println("Error for JSON-Object: " + it.toString())
            }
        }

        return allTorrents
    }

    void delete(AlldebridTorrent alldebridTorrent) {
        if (!loggedIn)
            login()
        def deleteGetRequest = new HttpGet(removeURL + alldebridTorrent.id)
        deleteGetRequest.setConfig(requestConfig)
        CloseableHttpResponse response = httpClient.execute(deleteGetRequest, httpClientContext)
        EntityUtils.consume(response.getEntity())
        response.close()
        int sc = response.getStatusLine().statusCode
        if (!((sc == 200 && sc != 302) || (sc != 200 && sc == 302))) {
            throw new Exception("delete not possible for id \"${alldebridTorrent.id}\": " + response.getStatusLine())
        }
    }


    private void login() {
        def loginPostRequest = new HttpPost(loginURL)

        loginPostRequest.setConfig(requestConfig)
        List<BasicNameValuePair> nvps = new ArrayList<>()
        nvps.add(new BasicNameValuePair("action", "login"))
        nvps.add(new BasicNameValuePair("returnpage", ""))
        nvps.add(new BasicNameValuePair("login_login", username))
        nvps.add(new BasicNameValuePair("login_password", password))
        loginPostRequest.setEntity(new UrlEncodedFormEntity(nvps))

        CloseableHttpResponse response = httpClient.execute(loginPostRequest, httpClientContext)
        EntityUtils.consume(response.getEntity())
        response.close()
        if (response.getStatusLine().statusCode == 302) {
            loggedIn = true
        } else {
            throw new Exception("Login not possible (maybe wrong username/password): " + response.getStatusLine())
        }

    }


    private static stringToByte(String sizeString) {
        if(sizeString == "0") return 0
        def splittedSizeString = sizeString.split(" ")
        long size = (long) (splittedSizeString[0].toDouble() * 1024)
        switch (splittedSizeString[1]) {
            case "GB/s":
            case "GB":
                return (long) (splittedSizeString[0].toDouble() * 1024) * 1024 * 1024
                break
            case "MB/s":
            case "MB":
                return (long) (splittedSizeString[0].toDouble() * 1024) * 1024
                break
            case "KB/s":
            case "KB":
                return (long) (splittedSizeString[0].toDouble() * 1024)
                break
            case "B/s":
            case "Bytes/s":
            case "Bytes":
                return splittedSizeString[0].toLong()
            default:
                throw new Exception("NOT HANDLED SIZE: " + splittedSizeString[1])
        }
        return size
    }
}
