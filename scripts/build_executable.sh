#!/bin/sh
VERSION="0.0.2"

./coursier bootstrap -r 'bintray:zzzzzz/crypto-trades-export' it.softfork::crypto-trades-export:$VERSION --standalone -o ../builds/crypto-trades-export-$VERSION --bat=true