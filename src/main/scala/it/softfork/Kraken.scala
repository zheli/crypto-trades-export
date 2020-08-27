package it.softfork

import java.text.SimpleDateFormat

import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.kraken.KrakenExchange
import org.rogach.scallop.Subcommand

import scala.jdk.CollectionConverters._

object Kraken extends StrictLogging {

  val krakenSubcommand = new Subcommand("kraken") {
    banner("""Download trading history from Kraken
             |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true, short = 'k')
    val apiSecret = opt[String](descr = "API secret", required = true, short = 's')
  }

  def exportCSV(writer: CSVWriter, apiKey: String, apiSecert: String) = {
    val exSpec = new KrakenExchange().getDefaultExchangeSpecification
    exSpec.setApiKey(apiKey)
    exSpec.setSecretKey(apiSecert)
    val exchange = ExchangeFactory.INSTANCE.createExchange(exSpec)
    val tradeService = exchange.getTradeService
    val currencyPairs = exchange.getExchangeSymbols.asScala.toSeq
    logger.info(s"Found ${currencyPairs.length} currency pairs")

    val datetimeWithoutTimezoneFormat = new SimpleDateFormat("yyyy-M-dd hh:mm:ss")
    val trades = tradeService.getTradeHistory(tradeService.createTradeHistoryParams()).getUserTrades.asScala.toSeq
    logger.info(s"Found ${trades.length} trades")
    trades.foreach { t =>
      val timestampString = datetimeWithoutTimezoneFormat.format(t.getTimestamp)
      writer.writeRow(
        List("kraken", t.getType, t.getCurrencyPair, t.getOriginalAmount, t.getPrice, timestampString)
      )
    }
    trades
  }
}
