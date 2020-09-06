package it.softfork

import java.text.SimpleDateFormat

import org.knowm.xchange.dto.trade.UserTrade

/**
  *
  */
object Utils {
  val datetimeWithoutTimezoneFormat = new SimpleDateFormat("yyyy-M-dd hh:mm:ss")

  def userTradeToRow(exchangeId: String, trade: UserTrade) = {
    val timestampString = datetimeWithoutTimezoneFormat.format(trade.getTimestamp)
    List(exchangeId, trade.getType, trade.getCurrencyPair, trade.getOriginalAmount, trade.getPrice, timestampString)
  }
}
