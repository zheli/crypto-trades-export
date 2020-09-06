package it.softfork

import org.rogach.scallop._

class CommandConfig(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("Crypto-trades-export 0.0.1 (c) 2020 Zhe Li <linuxcity.jn@gmail.com>")
  banner("""Usage: crypto-trades-export [OPTION]... 
           |crypto-trades-export will download your trade data from any exchange and export them as a single CSV file.
           |For example, to download all trades from Kraken to kraken-trades-2020.csv file, execute:
           |crypto-trades-export --output kraken-trades-2020.csv kraken --api-key [your api key] --api-secret [your api secret]
           |Options:
           |""".stripMargin)
  footer("\nFor all other tricks, consult the documentation!")

  val output = opt[String](descr = "Output CSV filename.", short = 'o')

  val coinbasepro = new Subcommand("coinbasepro") {
    banner("""Download trading history from Coinbase Pro
          |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true)
    val apiSecret = opt[String](descr = "API secret", required = true)
    val apiPassphrase = opt[String](descr = "API passphrase", required = true)
  }
  addSubcommand(coinbasepro)

  val binance = new Subcommand("binance") {
    banner("""Download trading history from Binance
             |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true, short = 'k')
    val apiSecret = opt[String](descr = "API secret", required = true, short = 's')
  }
  addSubcommand(binance)

  val kraken = Kraken.krakenSubcommand
  addSubcommand(kraken)

  val bittrex = Bittrex.subcommand
  addSubcommand(bittrex)

  verify()
}
