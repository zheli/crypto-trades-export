# crypto-trades-export
Export your trade history from different crypto exchanges to csv files.

## Standalone executable
### Requirement
The standalone executable requires Java 8 or later to run. That is, a command like java -version should print a version >= 8.

### Download
You can download all the releases in the [release](https://github.com/zheli/crypto-trades-export/releases) page.

### Build standalone yourself
#### Linux/MacOS
```
curl -fLo coursier https://git.io/coursier-cli && 
chmod +x coursier && 
./coursier bootstrap -r 'bintray:zzzzzz/crypto-trades-export' it.softfork::crypto-trades-export:0.0.2 --standalone -o crypto-trades-export
```
    
#### Windows
```
> bitsadmin /transfer downloadCoursierCli https://git.io/coursier-cli "%cd%\coursier"
> bitsadmin /transfer downloadCoursierBat https://git.io/coursier-bat "%cd%\coursier.bat"
> coursier bootstrap -r 'bintray:zzzzzz/crypto-trades-export' it.softfork::crypto-trades-export:0.0.2 --standalone -o crypto-trades-export
```

## Roadmap
- [x] Export Coinbase Pro trading history
- [x] Export Binance trading history
- [x] Export Kraken trading history
- [x] Make it possible to run as standalone
