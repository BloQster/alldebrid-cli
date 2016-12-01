package com.bloqster.blqloader.plugins.alldebrid

import groovy.io.FileType
import groovyjarjarcommonscli.MissingArgumentException

class Application {
    static void main(String[] args) {
        CLIHandler cliHandler = new CLIHandler(args)
        cliHandler.executeCLIRequest()
    }
}
