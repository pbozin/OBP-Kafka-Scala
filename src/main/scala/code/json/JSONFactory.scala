package code.json

/**
  * Created by markom on 12/12/16.
  */

case class BankJSON(id: String,
                    short: String,
                    name: String,
                    logo: String,
                    url: String)

case class AccountJSON(id: String,
                       bank: String,
                       label: String,
                       number: String,
                       `type`: String,
                       currency: String,
                       amount: String,
                       IBAN: String)

case class TransactionId(id: String)