package it.softfork

import java.io.File
import java.text.SimpleDateFormat

import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.coinbasepro.CoinbaseProExchange
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProTradeHistoryParams
import org.knowm.xchange.dto.marketdata.Trade

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.jdk.CollectionConverters._

object CoinbasePro extends StrictLogging {

  def download(apiKey: String, apiSecret: String, apiPassphrase: String) = Future {
    val exSpec = new CoinbaseProExchange().getDefaultExchangeSpecification()
    exSpec.setApiKey(apiKey)
    exSpec.setSecretKey(apiSecret)
    exSpec.setExchangeSpecificParametersItem("passphrase", apiPassphrase)

    val exchange = ExchangeFactory.INSTANCE.createExchange(exSpec)
    val tradeService = exchange.getTradeService
    val currencyPairs = exchange.getExchangeSymbols().asScala.toSeq
    logger.info(s"Found currency pairs: $currencyPairs")
    val f = new File("test.csv")
    if (f.exists()) {
      f.delete()
    }
    val writer = CSVWriter.open(f)
    writer.writeRow(List("Type", "Pair", "Amount", "Price", "Time"))
    currencyPairs.zipWithIndex.foreach {
      case (pair, i) =>
        if (i % 8 == 0) {
          logger.debug("Sleep for 0.5 seconds due to API rate limiting")
          Thread.sleep(500)
        }
        val params = new CoinbaseProTradeHistoryParams()
        params.setCurrencyPair(pair)
        val trades = tradeService.getTradeHistory(params).getTrades
        val tradeCount = trades.size()
        if (tradeCount > 0) {
          logger.info(s"Found ${tradeCount} trades for $pair")
          trades.forEach { t: Trade =>
            logger.info(s"Trade: $t")
            val datetimeWithoutTimezoneFormat = new SimpleDateFormat("yyyy-M-dd hh:mm:ss")
            val timestampString = datetimeWithoutTimezoneFormat.format(t.getTimestamp)
            writer.writeRow(
              List(t.getType, t.getCurrencyPair, t.getOriginalAmount, t.getPrice, timestampString)
            )
          }
        }
    }
    writer.close()
  }
}
