# crypto-trades-export
Export your trade history from different crypto exchanges to csv files.

## Download
You can download all the releases in the [release](https://github.com/zheli/crypto-trades-export/releases) page.

## Usage
The basic command argument structure is like this:
```
./crypto-trades-export [exchange] --key [API key] --secret [API secret] --output
```

For example, to download all BTC/USDT trades from Binance to `my-trade-2020.csv` file, just execute:
```
crypto-trades-export binance \
  --key MY_BINANCE_API_KEY \
  --secret MY_BINANCE_API_SECRET \
  --pair BTCUSDT
  --output my-trades-2020.csv
```

## Roadmap
- [x] Make it possible to run as standalone
- [x] Export Binance trading history
- [x] Build releases automatically
- [ ] Export Coinbase Pro trading history
- [ ] Export Kraken trading history
- [ ] Support Bittrex
