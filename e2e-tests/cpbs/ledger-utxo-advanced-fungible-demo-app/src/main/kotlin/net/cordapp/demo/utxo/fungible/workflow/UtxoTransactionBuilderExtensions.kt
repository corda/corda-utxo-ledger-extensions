package net.cordapp.demo.utxo.fungible.workflow

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.contract.TokenContract
import java.time.Instant

@Suspendable
internal fun UtxoTransactionBuilder.addMintToken(token: Token, notary: MemberX500Name) = this
    .addOutputState(token)
    .addCommand(TokenContract.Mint())
    .addSignatories(token.issuer)
    .setNotary(notary)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))

@Suspendable
internal fun UtxoTransactionBuilder.addMoveTokens(oldTokens: List<StateAndRef<Token>>, newTokens: List<Token>) = this
    .addInputStates(oldTokens.map { it.ref })
    .addOutputStates(newTokens)
    .addCommand(TokenContract.Move())
    .addSignatories(newTokens.map { it.owner })
    .setNotary(oldTokens.map { it.state.notaryName }.distinct().single())
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))

@Suspendable
internal fun UtxoTransactionBuilder.addBurnTokens(oldTokens: List<StateAndRef<Token>>, changeToken: Token?) = this
    .addInputStates(oldTokens.map { it.ref })
    .apply { changeToken?.let { addOutputState(it) } }
    .addCommand(TokenContract.Burn())
    .addSignatories(oldTokens.map { it.state.contractState.owner })
    .addSignatories(oldTokens.map { it.state.contractState.issuer })
    .setNotary(oldTokens.map { it.state.notaryName }.distinct().single())
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))
