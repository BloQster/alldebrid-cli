package com.bloqster.blqloader.plugins.alldebrid

import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Created by micha on 30.11.2016.
 */
final class AlldebridTorrent implements Comparable {
    final Long id
    final int server
    final String filename
    final TorrentStatus status
    final long downloaded
    final long fileSize
    final int seeder
    final long speed
    final Date addedDate
    final ArrayList<String> links
    final String removeLink
    final def jsonObject

    private final static String tableFormat = "%-10s%-6s%-52s%-14s%-11s%-13s%-13s%-9s%-13s%-18s"
    public static
    final String tableHeader = String.format(tableFormat, "id", "srv", "filename", "status", "progress", "downloaded", "file size", "seeder", "speed", "added date")
    private final static DecimalFormat decimalProgressFormat = new DecimalFormat("000.00 %")
    private final static DecimalFormat decimalSizeFormat = new DecimalFormat("##0.00")
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy hh:mm", Locale.GERMANY)

    AlldebridTorrent(Long id, int server, String filename, TorrentStatus status, long downloaded, long fileSize, int seeder, long speed, Date addedDate, ArrayList<String> links) {
        this.id = id
        this.server = server
        this.filename = filename
        this.status = status
        this.downloaded = downloaded
        this.fileSize = fileSize
        this.seeder = seeder
        this.speed = speed
        this.addedDate = addedDate
        this.links = links
        this.removeLink = removeLink
        this.jsonObject = jsonObject
    }

    public double finishedPercent() {
        return downloaded / fileSize
    }

    public String toTextTableString() {
        return String.format(tableFormat, id, server, filename.take(50), status, decimalProgressFormat.format(finishedPercent()), toHumanSizeString(downloaded), toHumanSizeString(fileSize), seeder, getHumanSpeedString(), dateFormat.format(addedDate))
    }

    public String getHumanSpeedString() {
        return "${toHumanSizeString(speed)}/s"
    }

    public static String toHumanSizeString(long size) {
        long kbSize = 1024
        long mbSize = kbSize * 1024
        long gbSize = mbSize * 1024
        long tbSize = gbSize * 1024
        long pbSize = tbSize * 1024
        if (size > pbSize) {
            return "${decimalSizeFormat.format(size / pbSize)} PB"
        } else if (size > tbSize) {
            return "${decimalSizeFormat.format(size / tbSize)} TB"
        } else if (size > gbSize) {
            return "${decimalSizeFormat.format(size / gbSize)} GB"
        } else if (size > mbSize) {
            return "${decimalSizeFormat.format(size / mbSize)} MB"
        } else if (size > kbSize) {
            return "${decimalSizeFormat.format(size / kbSize)} KB"
        } else {
            return "${size} B"
        }
    }

    @Override
    int compareTo(Object o) {
        if (!(o instanceof AlldebridTorrent))
            return 0
        AlldebridTorrent other = (AlldebridTorrent) o
        if (this.status == other.status) {
            return this.addedDate.compareTo(other.addedDate)
        } else {
            return this.status == TorrentStatus.DOWNLOADING ? -1 : 1
        }
    }

    @Override
    public String toString() {
        return "AlldebridTorrent{" +
                "id=" + id +
                ", server=" + server +
                ", filename='" + filename + '\'' +
                ", status=" + status +
                ", downloaded=" + downloaded +
                ", fileSize=" + fileSize +
                ", seeder=" + seeder +
                ", speed=" + speed +
                ", addedDate=" + addedDate +
                ", links=" + links +
                ", removeLink='" + removeLink + '\'' +
                '}';
    }
}
