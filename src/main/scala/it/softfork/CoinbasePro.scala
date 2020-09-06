package it.softfork

import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import it.softfork.Utils.userTradeToRow
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.coinbasepro.CoinbaseProExchange
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProTradeHistoryParams

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.jdk.CollectionConverters._

object CoinbasePro extends StrictLogging {

  def exportCSV(writer: CSVWriter, apiKey: String, apiSecret: String, apiPassphrase: String) = Future {
    val exSpec = new CoinbaseProExchange().getDefaultExchangeSpecification()
    exSpec.setApiKey(apiKey)
    exSpec.setSecretKey(apiSecret)
    exSpec.setExchangeSpecificParametersItem("passphrase", apiPassphrase)

    val exchange = ExchangeFactory.INSTANCE.createExchange(exSpec)
    val tradeService = exchange.getTradeService
    val currencyPairs = exchange.getExchangeSymbols().asScala.toSeq
    logger.info(s"Found currency pairs: $currencyPairs")
    currencyPairs.zipWithIndex.foreach {
      case (pair, i) =>
        if (i % 8 == 0) {
          logger.trace("Sleep for 0.5 seconds due to API rate limiting")
          Thread.sleep(500)
        }
        val params = new CoinbaseProTradeHistoryParams()
        params.setCurrencyPair(pair)
        val trades = tradeService.getTradeHistory(params).getUserTrades.asScala.toSeq
        val tradeCount = trades.length
        if (tradeCount > 0) {
          logger.info(s"Found ${tradeCount} trades for $pair")
          trades.foreach(trade => writer.writeRow(userTradeToRow("coinbasepro", trade)))
        }
    }
  }
}
